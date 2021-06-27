package src.data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.combat.AIUtils;

public class FreitagCorporation_FluxEater extends BaseShipSystemScript {

    protected static float FLUX_RANGE = 1500f;

    protected static float FLUX_MIN_CONVERTED = 0.7f;
    protected static String FLUX_STAT_ID = "FreitagCorporation_FluxEater_Stat";
    

    public static final Color JITTER_COLOR = new Color(200, 200, 25, 75);
    public static final Color JITTER_UNDER_COLOR = new Color(200, 200, 25, 155);

    public static float getRange(ShipAPI ship) {
        if (ship == null) {
            return FLUX_RANGE;
        }
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(FLUX_RANGE);
    }

    public static ShipAPI getAvailableAllyTarget(ShipAPI ship) {
        ShipAPI target = null;
        float min = 0.0f;
        List<ShipAPI> allies = AIUtils.getNearbyAllies(ship, getRange(ship));
        for (ShipAPI ally : allies) {
            if (ally.isFighter() || !ally.isAlive()) {
                continue;
            }
            if (ally.getFluxTracker().getCurrFlux() > ship.getSystem().getFluxPerUse() * FLUX_MIN_CONVERTED && ally.getFluxTracker().getCurrFlux() / ally.getFluxTracker().getMaxFlux() > min) {
                min = ally.getFluxTracker().getCurrFlux() / ally.getFluxTracker().getMaxFlux();
                target = ally;
            }
        }
        return target;
    }

    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        ShipAPI ship = null;
        boolean player = false;
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
            ShipAPI target = getAvailableAllyTarget(ship);
            if (target != null) {
                Global.getSoundPlayer().playSound("mine_teleport", 1f, 1f, ship.getLocation(), ship.getVelocity());
             //   float fluxRemoved = target.getFluxTracker().getCurrFlux() - ship.getSystem().getFluxPerUse();
                float fluxRemoved= ship.getSystem().getFluxPerUse();
                if (fluxRemoved < 0) {
                    fluxRemoved = 0;
                }
                float time=2f;
             //  Global.getCombatEngine().getCombatUI().addMessage(0, ": "+fluxRemoved);

                // if flux dissipation flat is 5200/s added, the time is 1s. If time is 2, divide per 2 the flux dissi
                fluxRemoved/=time;
                
                Global.getCombatEngine().addPlugin(createJitterPlugin(target, time/2,fluxRemoved));
           //    target.getMutableStats().getFluxDissipation().modifyFlat(FLUX_STAT_ID, fluxRemoved);
                //target.getFluxTracker().setCurrFlux(val);
            }

        } 
    }

    protected EveryFrameCombatPlugin createJitterPlugin(final ShipAPI target, final float timeEffect,final float fluxRemoved) {
        return new BaseEveryFrameCombatPlugin() {
            float elapsed = 0f;
            int state = 0;
            float inc=0;
            
            
            

            @Override
            public void advance(float amount, List<InputEventAPI> events) {
                if (Global.getCombatEngine().isPaused()) {
                    return;
                }
                elapsed += amount;
                inc++;
                if (elapsed > timeEffect) {
                    state++;
                    elapsed -= timeEffect;
                }
               
                target.getFluxTracker().decreaseFlux(fluxRemoved*amount);
                if (state == 2) {
                   //  Global.getCombatEngine().getCombatUI().addMessage(0, inc+":"+fluxRemoved*amount);
                    target.getMutableStats().getFluxDissipation().unmodifyFlat(FLUX_STAT_ID);
                    Global.getCombatEngine().removePlugin(this);
                    return;
                }  
                float jitterLevel;

                if(state==0){
                    jitterLevel = elapsed / timeEffect;
                }
                else {
                    jitterLevel=(timeEffect-elapsed)/ timeEffect;
                    jitterLevel *= jitterLevel;
                }
                float maxRangeBonus = 25f;
                float jitterRangeBonus = jitterLevel * maxRangeBonus;
                
                target.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 11, 0f, 3f + jitterRangeBonus);
                target.setJitter(this, JITTER_COLOR, jitterLevel, 4, 0f, 0 + jitterRangeBonus);

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

        ShipAPI target = getAvailableAllyTarget(ship);
        if (target != null) {
            return "READY";
        } else {
            return "OUT OF TARGET";
        }
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        return getAvailableAllyTarget(ship) != null;
        // return ship.getShipTarget() != null && ship.getShipTarget().isAlly();
    }

}
