package src.data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import static com.fs.starfarer.api.combat.ShipSystemAPI.SystemState.ACTIVE;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;
import src.data.utils.Utils;

public class FreitagCorporation_Manipulator extends BaseShipSystemScript {

    private static Map mag = new HashMap();

    static {
        mag.put(ShipAPI.HullSize.FIGHTER, 0.15f);
        mag.put(ShipAPI.HullSize.FRIGATE, 0.15f);
        mag.put(ShipAPI.HullSize.DESTROYER, 0.15f);
        mag.put(ShipAPI.HullSize.CRUISER, 0.33f);
        mag.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.33f);
    }
    protected Object STATUSKEY1 = new Object();

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
        } else {
            stats.getMaxSpeed().modifyFlat(id, 300f * effectLevel);
            stats.getAcceleration().modifyFlat(id, 300f * effectLevel);
        }
        effectLevel = 1f;

        float mult = (Float) mag.get(ShipAPI.HullSize.CRUISER);
        if (stats.getVariant() != null) {
            mult = (Float) mag.get(stats.getVariant().getHullSize());
        }
        stats.getHullDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
        stats.getArmorDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
        stats.getEmpDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);

        ShipAPI ship = null;
        boolean player = false;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
        }
        if (ship == null) {
            return;
        }
        if (player) {
            ShipSystemAPI system = ship.getSystem();
            if (system != null) {
                float percent = (1f - mult) * effectLevel * 100;
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1,
                        system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
                        (int) Math.round(percent) + "% less damage taken", false);
            }
        }
        if (ship.getSystem().getCooldownRemaining() < 0.2f) {
            return;
        }
        //   if (AIUtils.canUseSystemThisFrame(ship)) {

        List<ShipAPI> targetsToRepulse = AIUtils.getNearbyEnemies(ship, 10);
        boolean containATrueTarget = false;
        for (ShipAPI target : targetsToRepulse) {
            if (!target.isFighter()) {
                
                containATrueTarget = true;
                break;
            }
        }
        if (containATrueTarget) {

            Vector2f dir = new Vector2f();
            Vector2f positionRepulse = MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius(), ship.getFacing());
            
            SimpleEntity entity = new SimpleEntity(positionRepulse);
            for (ShipAPI target2 : targetsToRepulse) {
                Vector2f.sub(target2.getLocation(), positionRepulse, dir);
                CombatUtils.applyForce(target2, dir, 1000 * (1 + ship.getSystem().getChargeActiveDur() - ship.getSystem().getCooldownRemaining()));
            }
            ship.getVelocity().set(0, 0);
            for (int i = 0; i < 5; i++) {
                    Global.getCombatEngine().spawnEmpArcVisual(positionRepulse, null, MathUtils.getRandomPointOnCircumference(positionRepulse, 10), null, 10, Color.cyan, Color.BLUE);
            }

            Utils.CustomRippleDistortion(positionRepulse, new Vector2f(0, 0), 200, 3f, false, 0f, 360f, 0.5f, 0f, 0.5f, 0.5f, 1f, 0f);
            if (ship.getSystem().getState().equals(ACTIVE))//ship.getSystem().setCooldownRemaining(0.1f);
            {
                ship.getSystem().deactivate();
            }
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getArmorDamageTakenMult().unmodify(id);
        stats.getEmpDamageTakenMult().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("increased engine power", false);
        }
        return null;
    }

}
