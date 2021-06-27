package src.data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import java.util.List;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import src.data.shipsystems.scripts.FreitagCorporation_FluxEater;

public class FreitagCorporation_FluxEaterAI implements ShipSystemAIScript {

    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;

    private float tracker = 0;
    private float trackermax = 0;

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
            ShipAPI target2 = FreitagCorporation_FluxEater.getAvailableAllyTarget(ship);
            if (target2 == null) {
                return;
            }
            if (!system.isOn()) {
                if (AIUtils.canUseSystemThisFrame(ship)) {
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

}
