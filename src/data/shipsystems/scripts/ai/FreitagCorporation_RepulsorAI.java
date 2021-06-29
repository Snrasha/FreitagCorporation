package src.data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import java.awt.geom.Line2D;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class FreitagCorporation_RepulsorAI implements ShipSystemAIScript {

    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private float RANGE = 250;

    private float tracker = 0;
    private float trackermax = 0.4f;

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }
        tracker += amount;

        if (tracker > trackermax) {
            tracker -= trackermax;
            if (ship.getFluxTracker().isOverloadedOrVenting()) {
                return;
            }
            if (!system.isOn()) {
                return;
            }
            if (ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.PURSUING)) {
                return;
            }
            if (ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.MAINTAINING_STRIKE_RANGE)) {
                return;
            }
            if (ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.MANEUVER_TARGET)) {
                return;
            }

            if (ship.areAnyEnemiesInRange()) {
                Vector2f positionRepulse = MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius(), ship.getFacing());
                List<ShipAPI> targetsToRepulse = CombatUtils.getShipsWithinRange(positionRepulse, RANGE);

                int nb = 0;
                for (ShipAPI tmp : targetsToRepulse) {
                    if (tmp.getOwner() != ship.getOwner()
                            && !tmp.isHulk() && !tmp.isShuttlePod()
                            && CombatUtils.isVisibleToSide(tmp, ship.getOwner())) {
                        if (tmp.isFighter()) {
                            nb++;
                        } else {
                            nb += 5;
                        }
                    }
                }
                if(nb>=5){
                    ship.useSystem();
                }
            }

            

        }
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
        this.engine = engine;
        this.tracker = 0;
        this.trackermax = 0.2f;
    }

    /**
     * From LazyLib WeaponUtils
     *
     * Checks if a {@link CombatEntityAPI} is within the arc.
     *
     * @param entity The {@link CombatEntityAPI} to check if ship is aimed at.
     *
     * @return {@code true} if in arc and in range, {@code false} otherwise.
     *
     * @since 1.0
     */
    public boolean isWithinArc(CombatEntityAPI entity, float range) {

        // Check if weapon is aimed at any part of the target
        float arc = 5;
        Vector2f target = entity.getLocation();
        Vector2f wep = ship.getLocation();
        Vector2f endArcLeft = MathUtils.getPointOnCircumference(wep, range,
                ship.getFacing() - arc);
        Vector2f endArcRight = MathUtils.getPointOnCircumference(wep, range,
                ship.getFacing() + arc);
        float radSquared = entity.getCollisionRadius() * entity.getCollisionRadius();

        // Check if target is partially in weapon arc
        return (Line2D.ptSegDistSq(
                wep.x,
                wep.y,
                endArcLeft.x,
                endArcLeft.y,
                target.x,
                target.y) <= radSquared)
                || (Line2D.ptSegDistSq(
                        wep.x,
                        wep.y,
                        endArcRight.x,
                        endArcRight.y,
                        target.x,
                        target.y) <= radSquared);
    }

}
