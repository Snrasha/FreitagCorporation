package src.data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class FreitagCorporation_TemporalShellBuff extends BaseShipSystemScript {

    public static final float MAX_TIME_MULT = 3f;

    protected static float TEMPORAL_SHELL_RANGE = 1500f;
    protected static String TEMPORAL_SHELL_ID = "FreitagCorporation_TemporalShellBuff_Stat";
    public static float TEMPORAL_SHELL_TIME = 2f;

    public static final Color JITTER_COLOR = new Color(90, 165, 255, 55);
    public static final Color JITTER_UNDER_COLOR = new Color(90, 165, 255, 155);

    public static float getRange(ShipAPI ship) {
        if (ship == null) {
            return TEMPORAL_SHELL_RANGE;
        }
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(TEMPORAL_SHELL_RANGE);
    }

    public static ShipAPI getAvailableAllyTarget(ShipAPI ship) {
        ShipAPI target = null;
        float min = 0.7f;
        List<ShipAPI> allies = AIUtils.getNearbyAllies(ship, getRange(ship));
        for (ShipAPI ally : allies) {
            if (ally.isFighter() || !ally.isAlive()) {
                continue;
            }
            if (ally.getMutableStats().getTimeMult().getFlatStatMod(TEMPORAL_SHELL_ID) != null) {
                continue;
            }

            if (ally.getAIFlags().hasFlag(AIFlags.NEEDS_HELP)
                    || ally.getAIFlags().hasFlag(AIFlags.PURSUING)
                    || ally.getFluxTracker().getCurrFlux() / ally.getFluxTracker().getMaxFlux() > min) {
                min = ally.getFluxTracker().getCurrFlux() / ally.getFluxTracker().getMaxFlux();
                target = ally;
            }
        }
        return target;
    }
    public static ShipAPI getAvailableAllyTarget(ShipAPI ship,List<ShipAPI> ignored) {
        ShipAPI target = null;
        float min = 0.7f;
        List<ShipAPI> allies = AIUtils.getNearbyAllies(ship, getRange(ship));
        for (ShipAPI ally : allies) {
            if (ally.isFighter() || !ally.isAlive()) {
                continue;
            }
            if (ally.getMutableStats().getTimeMult().getFlatStatMod(TEMPORAL_SHELL_ID) != null) {
                continue;
            }
            if(ignored.contains(ally))continue;

            if (ally.getAIFlags().hasFlag(AIFlags.NEEDS_HELP)
                    || ally.getAIFlags().hasFlag(AIFlags.PURSUING)
                    || ally.getFluxTracker().getCurrFlux() / ally.getFluxTracker().getMaxFlux() > min) {
                min = ally.getFluxTracker().getCurrFlux() / ally.getFluxTracker().getMaxFlux();
                target = ally;
            }
        }
        return target;
    }

    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        ShipAPI ship;
        boolean player;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();

        } else {
            return;
        }

        float jitterLevel = effectLevel;
        if (state == ShipSystemStatsScript.State.OUT) {
            jitterLevel *= jitterLevel;
        }
        float maxRangeBonus = 25f;
        float jitterRangeBonus = jitterLevel * maxRangeBonus;
        ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 11, 0f, 3f + jitterRangeBonus);
        ship.setJitter(this, JITTER_COLOR, jitterLevel, 4, 0f, 0 + jitterRangeBonus);

        if (state == ShipSystemStatsScript.State.IN) {
        } else if (effectLevel >= 1) {
            ShipAPI target = null;
            if (player) {
                Vector2f mousepos = ship.getMouseTarget();
                if (mousepos != null) {
                    float dist = Misc.getDistance(ship.getLocation(), mousepos);
                    float max = getRange(ship) + ship.getCollisionRadius();
                    if (dist <= max) {
                        target = getShipAt(mousepos);
                    }
                }
            } else {
                target = getAvailableAllyTarget(ship);
            }
            if (target != null) {
                Global.getSoundPlayer().playSound("mine_teleport", 1f, 1f, ship.getLocation(), ship.getVelocity());
                Global.getCombatEngine().addPlugin(createTemporalShellBuffPlugin(target, TEMPORAL_SHELL_TIME));
            }

        }
    }

    protected EveryFrameCombatPlugin createTemporalShellBuffPlugin(final ShipAPI target, final float timeEffect) {
        return new BaseEveryFrameCombatPlugin() {
            float elapsed = 0f;
            int state = 0;
            float inc = 0;

            @Override
            public void advance(float amount, List<InputEventAPI> events) {
                if (Global.getCombatEngine().isPaused()) {
                    return;
                }
                boolean player = target == Global.getCombatEngine().getPlayerShip();

                float jitterLevel;
                elapsed += amount;
                if (state == 1) {
                    jitterLevel = 1f;
                    if (elapsed > timeEffect) {
                        state++;
                        elapsed -= timeEffect;
                    }
                } else {
                    if (elapsed > 0.25f) {
                        state++;
                        elapsed -= 0.25f;
                    }
                    if (state == 0) {
                        jitterLevel = elapsed / 0.25f;
                    } else {
                        jitterLevel = (0.25f - elapsed) / 0.25f;
                    }
                }
                if (state == 3) {
                    Global.getCombatEngine().getTimeMult().unmodify(TEMPORAL_SHELL_ID);
                    target.getMutableStats().getTimeMult().unmodify(TEMPORAL_SHELL_ID);
                    Global.getCombatEngine().removePlugin(this);
                    return;
                }
                float jitterRangeBonus = 0;
                float maxRangeBonus = 10f;
                if (jitterLevel > 1) {
                    jitterLevel = 1f;
                }
                switch (state) {
                    case 0:
                        jitterRangeBonus = jitterLevel * maxRangeBonus;
                        break;
                    case 1:
                        jitterRangeBonus = maxRangeBonus;
                        break;
                    case 2:
                        jitterRangeBonus = jitterLevel * maxRangeBonus;
                        break;
                    default:
                        break;
                }
                float effectLevel = jitterLevel * jitterLevel;

                jitterLevel = (float) Math.sqrt(jitterLevel);

                target.setJitter(this, JITTER_COLOR, jitterLevel, 3, 0, 0 + jitterRangeBonus);
                target.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 25, 0f, 7f + jitterRangeBonus);

                float shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * effectLevel;
                target.getMutableStats().getTimeMult().modifyMult(TEMPORAL_SHELL_ID, shipTimeMult);
                if (player) {
                    Global.getCombatEngine().getTimeMult().modifyMult(TEMPORAL_SHELL_ID, 1f / shipTimeMult);
                } else {
                    Global.getCombatEngine().getTimeMult().unmodify(TEMPORAL_SHELL_ID);
                }

                target.getEngineController().fadeToOtherColor(this, JITTER_COLOR, new Color(0, 0, 0, 0), effectLevel, 0.5f);
                target.getEngineController().extendFlame(this, -0.25f, -0.25f, -0.25f);
            }
        };
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) {
            return null;
        }
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) {
            return null;
        }
        Vector2f target = ship.getMouseTarget();

        if (target != null) {

            float dist = Misc.getDistance(ship.getLocation(), target);
            float max = getRange(ship) + ship.getCollisionRadius();
            if (dist > max) {
                return "OUT OF RANGE";
            } else {
                ShipAPI shipFound = getShipAt(target);
                if (shipFound != null) {
                    return "READY";
                } else {
                    return "OUT OF TARGET";
                }
            }
        }
        return null;
    }

    private ShipAPI getShipAt(Vector2f loc) {
        float min = Float.MAX_VALUE;
        ShipAPI ship = null;
        for (ShipAPI tmp : Global.getCombatEngine().getShips()) {
            if (tmp.isShuttlePod()) {
                continue;
            }
            if (tmp.isFighter()) {
                continue;
            }

            if (MathUtils.getDistance(tmp.getLocation(), loc) < (min + tmp.getCollisionRadius())) {
                ship = tmp;
                min = MathUtils.getDistance(tmp.getLocation(), loc);
            }
        }
        return ship;
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        Vector2f target = ship.getMouseTarget();

        if (target != null) {

            float dist = Misc.getDistance(ship.getLocation(), target);
            float max = getRange(ship) + ship.getCollisionRadius();
            if (dist > max) {
                return false;
            } else {
                ShipAPI shipFound = getShipAt(target);
                if (shipFound != null) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

}
