package src.data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import src.data.shipsystems.scripts.FreitagCorporation_FluxEater;
import src.data.shipsystems.scripts.FreitagCorporation_TemporalShellBuff;

public class FreitagCorporation_TemporalShellBuffAI implements ShipSystemAIScript {

    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;

    private float tracker = 0;
    private float trackermax = 0.2f;
    private List<ShipAPI> targetteds;
    private float[] targettedsCount;
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
            int length=targetteds.size();
            for(int i=0;i<length;i++){
                targettedsCount[i]-=trackermax;
                if(targettedsCount[i]<=0){
                    targetteds.remove(i);
                    for(int j=i+1;j<3;j++)targettedsCount[j-1]=targettedsCount[j];
                    break;
                }
            }
            
            
            ShipAPI target2 = FreitagCorporation_TemporalShellBuff.getAvailableAllyTarget(ship,targetteds);
            if (target2 == null) {
                return;
            }
            if (!system.isOn()) {
                
                if (AIUtils.canUseSystemThisFrame(ship)) {
                    targetteds.add(target2);
                    targettedsCount[targetteds.size()-1]=2*FreitagCorporation_TemporalShellBuff.TEMPORAL_SHELL_TIME;
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
        targetteds=new ArrayList<>();
        targettedsCount=new float[system.getMaxAmmo()];
    }

}
