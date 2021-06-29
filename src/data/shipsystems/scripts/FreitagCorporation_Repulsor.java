package src.data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.scripts.util.MagicLensFlare;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import src.data.utils.Utils;

public class FreitagCorporation_Repulsor extends BaseShipSystemScript {

    private static Map mag = new HashMap();

    static {
        mag.put(ShipAPI.HullSize.FIGHTER, 0.15f);
        mag.put(ShipAPI.HullSize.FRIGATE, 0.15f);
        mag.put(ShipAPI.HullSize.DESTROYER, 0.15f);
        mag.put(ShipAPI.HullSize.CRUISER, 0.33f);
        mag.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.33f);
    }

    private static final int RANGE = 500;
    protected Object STATUSKEY1 = new Object();

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship = null;
        boolean player = false;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
        }
        if (ship == null) {
            return;
        }

        if (effectLevel == 1) {

            Vector2f dir = new Vector2f();
            Vector2f positionRepulse = MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius()*0.8f, ship.getFacing());
            List<ShipAPI> targetsToRepulse = CombatUtils.getShipsWithinRange(positionRepulse, RANGE);
            for (ShipAPI tmp : targetsToRepulse) {
                if (tmp.getOwner() != ship.getOwner() && !tmp.isHulk() && !tmp.isShuttlePod()) {
                    Vector2f.sub(tmp.getLocation(), positionRepulse, dir);
                    float power = 200000/tmp.getMass();
                    if(power>20000)power=20000;
                    if(power<100)power=100; //require min 1000 to be visible for a condor.
                    /*if (tmp.isFighter()) {
                        power *= 10;
                    }*/
                    CombatUtils.applyForce(tmp, dir, power);
                }
            }
            MagicLensFlare.createSharpFlare(
                    Global.getCombatEngine(),
                    ship,
                    positionRepulse,
                    5,
                    250,
                    ship.getFacing(),
                    new Color(50, 175, 255),
                    new Color(200, 200, 255)
            );
            
            Utils.CustomRippleDistortion(positionRepulse, new Vector2f(0, 0), 400, 5f, false, 0f, 360f, 0.5f, 0f, 4f, 0.5f, 2f, 0f);
            Utils.CustomBubbleDistortion(positionRepulse, new Vector2f(0, 0),400, 5f, false, 0f, 360f, 0.5f, 0f, 4f, 0.5f, 2f, 0f);

        }
        effectLevel = 1f;

        float mult = (Float) mag.get(ShipAPI.HullSize.CRUISER);
        if (stats.getVariant() != null) {
            mult = (Float) mag.get(stats.getVariant().getHullSize());
        }
        stats.getHullDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
        stats.getArmorDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
        stats.getEmpDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
        if (player) {
            ShipSystemAPI system = ship.getSystem();
            if (system != null) {
                float percent = (1f - mult) * effectLevel * 100;
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1,
                        system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
                        (int) Math.round(percent) + "% less damage taken", false);
            }
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getArmorDamageTakenMult().unmodify(id);
        stats.getEmpDamageTakenMult().unmodify(id);
    }

}
