package data.missions.FreitagCorporation_classic;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {

        // Set up the fleets
        
        api.initFleet(FleetSide.PLAYER, "HSS", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true);

        // Set a blurb for each fleet
        api.setFleetTagline(FleetSide.PLAYER, Factions.TRITACHYON);
        api.setFleetTagline(FleetSide.ENEMY, Factions.INDEPENDENT);

        // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Win.");

        // Set up the player's fleet
        api.addToFleet(FleetSide.PLAYER, "FreitagCorporation_Crevette_Standard", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "FreitagCorporation_Ecrevisse_Standard", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "FreitagCorporation_Euphausia_Standard", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "FreitagCorporation_Euphausia_Standard", FleetMemberType.SHIP, true);

        api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, "Taskmaster", false);
        // Set up the map.

        // Set up the map.
        float width = 5000f;
        float height = 10000f;
        api.initMap((float) -width / 2f, (float) width / 2f, (float) -height / 2f, (float) height / 2f);

        api.addPlanet(0, 0, 400f, "barren", 200f, true);
        api.addRingAsteroids(0, 0, 30, 32, 32, 48, 200);
    }

}
