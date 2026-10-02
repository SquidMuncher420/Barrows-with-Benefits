package com.barrowswithbenefits;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(BarrowsWithBenefitsConfig.GROUP)
public interface BarrowsWithBenefitsConfig extends Config
{
    String GROUP = "barrows-with-benefits";

    @ConfigSection(
        name = "Brother tiles",
        description = "Labelled tiles for the six Barrows brothers, coloured by kill status.",
        position = 0
    )
    String BROTHER_TILES_SECTION = "brotherTiles";

    @ConfigSection(
        name = "Brother kill order",
        description = "Choose the preferred surface brother order. The tunnel brother is always moved to last.",
        position = 1
    )
    String BROTHER_ORDER_SECTION = "brotherOrder";

    @ConfigSection(
        name = "Tunnel memory & HUD",
        description = "Remember and mark the brother that leads to the tunnel, including the Barrows-list HUD marker.",
        position = 1
    )
    String TUNNEL_MEMORY_SECTION = "tunnelMemory";

    @ConfigSection(
        name = "Tunnel kill tracker",
        description = "Track a custom tunnel kill plan and Barrows reward potential.",
        position = 2
    )
    String TUNNEL_TRACKER_SECTION = "tunnelKillTracker";

    @ConfigSection(
        name = "Tunnel doors & puzzle",
        description = "Highlight usable Barrows tunnel doors and the correct puzzle answer.",
        position = 3
    )
    String TUNNEL_HELPER_SECTION = "tunnelHelper";

    @ConfigSection(
        name = "Chest loot messages",
        description = "Report the value and contents of Barrows chest loot in game chat.",
        position = 4
    )
    String CHEST_LOOT_SECTION = "chestLoot";

    @ConfigItem(
        keyName = "brotherKillOrder",
        name = "Brother order",
        description = "Comma-separated order using: Dharok, Verac, Ahrim, Torag, Karil, Guthan. The tunnel brother is always last.",
        section = BROTHER_ORDER_SECTION,
        position = 0
    )
    default String brotherKillOrder() { return "Dharok, Ahrim, Karil, Guthan, Torag, Verac"; }

    @ConfigItem(
        keyName = "showBrotherOrderHintArrow",
        name = "Show mound hint arrow",
        description = "Show RuneScape's flashing hint arrow on the mound for the next brother in your order.",
        section = BROTHER_ORDER_SECTION,
        position = 1
    )
    default boolean showBrotherOrderHintArrow() { return true; }


    // ---- Brother tiles ----------------------------------------------------

    @ConfigItem(
        keyName = "showCryptBrotherTiles",
        name = "Show crypt brother tiles",
        description = "Display the six labelled sarcophagus tiles inside the underground brother rooms.",
        section = BROTHER_TILES_SECTION,
        position = 0
    )
    default boolean showCryptBrotherTiles() { return true; }

    @ConfigItem(
        keyName = "showSurfaceBrotherTiles",
        name = "Show surface brother tiles",
        description = "Display the six labelled mound tiles on the hill above the crypts.",
        section = BROTHER_TILES_SECTION,
        position = 1
    )
    default boolean showSurfaceBrotherTiles() { return true; }

    @ConfigItem(
        keyName = "showKillStatusColors",
        name = "Colour tiles by kill status",
        description = "Colour each brother's mound/sarcophagus tile red until killed, then green.",
        section = BROTHER_TILES_SECTION,
        position = 2
    )
    default boolean showKillStatusColors() { return true; }

    @Alpha
    @ConfigItem(
        keyName = "unkilledBrotherColor",
        name = "Not-yet-killed color",
        description = "Color before a brother has been killed this run.",
        section = BROTHER_TILES_SECTION,
        position = 3
    )
    default Color unkilledBrotherColor() { return new Color(0xE5, 0x39, 0x35, 0xFF); }

    @Alpha
    @ConfigItem(
        keyName = "killedBrotherColor",
        name = "Killed color",
        description = "Color once a brother has been killed this run.",
        section = BROTHER_TILES_SECTION,
        position = 4
    )
    default Color killedBrotherColor() { return new Color(0x43, 0xA0, 0x47, 0xFF); }

    @ConfigItem(
        keyName = "showRoomStaircase",
        name = "Highlight room staircase",
        description = "Outline the staircase only while you are inside that brother's crypt room.",
        section = BROTHER_TILES_SECTION,
        position = 5
    )
    default boolean showRoomStaircase() { return false; }

    @Alpha
    @ConfigItem(
        keyName = "roomStaircaseColor",
        name = "Staircase color",
        description = "Outline color and opacity for the staircase in the current brother room.",
        section = BROTHER_TILES_SECTION,
        position = 6
    )
    default Color roomStaircaseColor() { return new Color(0xFF, 0xE6, 0x00, 0xFF); }

    @Range(min = 1, max = 10)
    @ConfigItem(
        keyName = "roomStaircaseWidth",
        name = "Staircase outline width",
        description = "Thickness of the staircase outline.",
        section = BROTHER_TILES_SECTION,
        position = 7
    )
    default int roomStaircaseWidth() { return 3; }

    // ---- Tunnel memory ------------------------------------------------------

    @ConfigItem(
        keyName = "showTunnelSarcophagus",
        name = "Show tunnel markers",
        description = "Mark the brother whose room leads to the crypt maze and chest.",
        section = TUNNEL_MEMORY_SECTION,
        position = 0
    )
    default boolean showTunnelSarcophagus() { return true; }

    @ConfigItem(
        keyName = "showTunnelDiamond",
        name = "Show tunnel diamond",
        description = "Draw a diamond beside the tunnel brother in the Barrows brother list.",
        section = TUNNEL_MEMORY_SECTION,
        position = 1
    )
    default boolean showTunnelDiamond() { return true; }

    @Alpha
    @ConfigItem(
        keyName = "tunnelHighlightColor",
        name = "Tunnel highlight color",
        description = "Color used to mark the tunnel brother.",
        section = TUNNEL_MEMORY_SECTION,
        position = 2
    )
    default Color tunnelHighlightColor() { return new Color(0x29, 0x79, 0xFF, 0xFF); }

    @Range(min = 0, max = 30)
    @ConfigItem(
        keyName = "tunnelDiamondOffset",
        name = "Diamond right offset",
        description = "Move the tunnel diamond further right so it does not cover the native kill tick.",
        section = TUNNEL_MEMORY_SECTION,
        position = 3
    )
    default int tunnelDiamondOffset() { return 18; }

    // ---- Tunnel kill tracker ----------------------------------------------

    @ConfigItem(
        keyName = "showTunnelKillTracker",
        name = "Show tunnel kill tracker",
        description = "Show the configured NPC kill counters while inside the tunnel maze.",
        section = TUNNEL_TRACKER_SECTION,
        position = 0
    )
    default boolean showTunnelKillTracker() { return true; }

    @Range(min = 0, max = 20)
    @ConfigItem(
        keyName = "bloodwormTarget",
        name = "Bloodworm target",
        description = "Target number of Bloodworms. Set to 0 to hide.",
        section = TUNNEL_TRACKER_SECTION,
        position = 3
    )
    default int bloodwormTarget() { return 1; }

    @Range(min = 0, max = 20)
    @ConfigItem(
        keyName = "cryptRatTarget",
        name = "Crypt rat target",
        description = "Target number of Crypt rats. Set to 0 to hide.",
        section = TUNNEL_TRACKER_SECTION,
        position = 4
    )
    default int cryptRatTarget() { return 0; }

    @Range(min = 0, max = 20)
    @ConfigItem(
        keyName = "giantCryptRatTarget",
        name = "Giant crypt rat target",
        description = "Target number of Giant crypt rats. Set to 0 to hide.",
        section = TUNNEL_TRACKER_SECTION,
        position = 5
    )
    default int giantCryptRatTarget() { return 0; }

    @Range(min = 0, max = 20)
    @ConfigItem(
        keyName = "cryptSpiderTarget",
        name = "Crypt spider target",
        description = "Target number of Crypt spiders. Set to 0 to hide.",
        section = TUNNEL_TRACKER_SECTION,
        position = 6
    )
    default int cryptSpiderTarget() { return 0; }

    @Range(min = 0, max = 20)
    @ConfigItem(
        keyName = "giantCryptSpiderTarget",
        name = "Giant crypt spider target",
        description = "Target number of Giant crypt spiders. Set to 0 to hide.",
        section = TUNNEL_TRACKER_SECTION,
        position = 7
    )
    default int giantCryptSpiderTarget() { return 0; }

    @Range(min = 0, max = 20)
    @ConfigItem(
        keyName = "skeletonTarget",
        name = "Skeleton target",
        description = "Target number of Skeletons. Set to 0 to hide.",
        section = TUNNEL_TRACKER_SECTION,
        position = 8
    )
    default int skeletonTarget() { return 2; }

    @ConfigItem(
        keyName = "highlightTunnelTargetNpcs",
        name = "Highlight target NPCs",
        description = "Highlight tunnel NPCs that are still needed for your configured kill targets.",
        section = TUNNEL_TRACKER_SECTION,
        position = 1
    )
    default boolean highlightTunnelTargetNpcs() { return true; }

    @Alpha
    @ConfigItem(
        keyName = "tunnelNpcHighlightColor",
        name = "NPC highlight colour",
        description = "Outline colour for tunnel NPCs that are still needed.",
        section = TUNNEL_TRACKER_SECTION,
        position = 9
    )
    default Color tunnelNpcHighlightColor() { return new Color(0xFF, 0xA0, 0x00, 0xFF); }

    @Range(min = 1, max = 8)
    @ConfigItem(
        keyName = "tunnelNpcHighlightWidth",
        name = "NPC outline width",
        description = "Thickness of the target NPC outline.",
        section = TUNNEL_TRACKER_SECTION,
        position = 10
    )
    default int tunnelNpcHighlightWidth() { return 2; }

    @ConfigItem(
        keyName = "showRewardPotential",
        name = "Show reward potential",
        description = "Show the current Barrows reward potential while inside the tunnels.",
        section = TUNNEL_TRACKER_SECTION,
        position = 2
    )
    default boolean showRewardPotential() { return true; }

    // ---- Tunnel doors & puzzle ----------------------------------------------

    @ConfigItem(
        keyName = "highlightTunnelDoors",
        name = "Highlight tunnel doors",
        description = "Highlight currently usable/openable doors while inside the Barrows tunnel maze.",
        section = TUNNEL_HELPER_SECTION,
        position = 0
    )
    default boolean highlightTunnelDoors() { return true; }

    
    @ConfigItem(
        keyName = "shortestRouteDoorsOnly",
        name = "Shortest route only",
        description = "Only highlight tunnel doors which are on a shortest route toward the Barrows chest.",
        section = TUNNEL_HELPER_SECTION,
        position = 1
    )
    default boolean shortestRouteDoorsOnly()
    {
        return true;
    }

@ConfigItem(
        keyName = "showShortestRouteLine",
        name = "Show shortest route line",
        description = "Draw a floor line along the selected shortest route toward the Barrows chest.",
        section = TUNNEL_HELPER_SECTION,
        position = 2
    )
    default boolean showShortestRouteLine() { return false; }

    @Alpha
    @ConfigItem(
        keyName = "shortestRouteLineColor",
        name = "Route line colour",
        description = "Colour and opacity of the shortest-route floor line.",
        section = TUNNEL_HELPER_SECTION,
        position = 3
    )
    default Color shortestRouteLineColor() { return new Color(0x29, 0x79, 0xFF, 0xFF); }

    @Range(min = 1, max = 10)
    @ConfigItem(
        keyName = "shortestRouteLineWidth",
        name = "Route line width",
        description = "Thickness of the shortest-route floor line.",
        section = TUNNEL_HELPER_SECTION,
        position = 4
    )
    default int shortestRouteLineWidth() { return 2; }

    @Alpha
    @ConfigItem(
        keyName = "tunnelDoorColor",
        name = "Door highlight colour",
        description = "Outline colour for usable Barrows tunnel doors.",
        section = TUNNEL_HELPER_SECTION,
        position = 2
    )
    default Color tunnelDoorColor() { return new Color(0x43, 0xA0, 0x47, 0xFF); }

    @Range(min = 1, max = 8)
    @ConfigItem(
        keyName = "tunnelDoorWidth",
        name = "Door outline width",
        description = "Thickness of the tunnel door outline.",
        section = TUNNEL_HELPER_SECTION,
        position = 3
    )
    default int tunnelDoorWidth() { return 3; }

    @ConfigItem(
        keyName = "showChestDirectionIndicator",
        name = "Chest direction indicator",
        description = "Show a small arrow at the end of the visible route pointing toward the remaining path.",
        section = TUNNEL_HELPER_SECTION,
        position = 5
    )
    default boolean showChestDirectionIndicator() { return true; }

    @ConfigItem(
        keyName = "emphasizeNextDoor",
        name = "Emphasize next door",
        description = "Make the next route door brighter and thicker than later route doors.",
        section = TUNNEL_HELPER_SECTION,
        position = 6
    )
    default boolean emphasizeNextDoor() { return true; }

    @ConfigItem(
        keyName = "showPuzzleAnswer",
        name = "Highlight puzzle answer",
        description = "Draw a solid, opaque fill over the correct answer on the Barrows door puzzle.",
        section = TUNNEL_HELPER_SECTION,
        position = 4
    )
    default boolean showPuzzleAnswer() { return true; }

    @Alpha
    @ConfigItem(
        keyName = "puzzleAnswerColor",
        name = "Puzzle answer colour",
        description = "Fill colour for the correct Barrows puzzle answer. Should stay fully opaque so it reads clearly.",
        section = TUNNEL_HELPER_SECTION,
        position = 4
    )
    default Color puzzleAnswerColor() { return Color.GREEN; }

    // ---- Chest loot messages ----------------------------------------------

    @ConfigItem(
        keyName = "showChestValue",
        name = "Show chest value",
        description = "Print the total Grand Exchange value of Barrows chest loot in game chat.",
        section = CHEST_LOOT_SECTION,
        position = 0
    )
    default boolean showChestValue() { return true; }

    @ConfigItem(
        keyName = "showChestHaValue",
        name = "Show chest HA value",
        description = "Print the total high alchemy value of Barrows chest loot in game chat.",
        section = CHEST_LOOT_SECTION,
        position = 1
    )
    default boolean showChestHaValue() { return true; }

    @ConfigItem(
        keyName = "showChestDrops",
        name = "Show chest drops",
        description = "Print each item and quantity received from the Barrows chest in game chat.",
        section = CHEST_LOOT_SECTION,
        position = 2
    )
    default boolean showChestDrops() { return true; }

    
    @ConfigItem(
        keyName = "showChestDropValues",
        name = "Show drop values",
        description = "Show the value next to each chest drop. Disable for item name and quantity only.",
        section = CHEST_LOOT_SECTION,
        position = 3
    )
    default boolean showChestDropValues() { return true; }

    @ConfigItem(
        keyName = "chestDropIncludeFilter",
        name = "Only include items",
        description = "Optional comma-separated item names to show. Leave blank to allow all items.",
        section = CHEST_LOOT_SECTION,
        position = 4
    )
    default String chestDropIncludeFilter() { return ""; }

    @ConfigItem(
        keyName = "chestDropHideFilter",
        name = "Hide items",
        description = "Comma-separated item names to hide from chest drop messages. Hidden items take priority over the include list.",
        section = CHEST_LOOT_SECTION,
        position = 5
    )
    default String chestDropHideFilter() { return ""; }

@Range(min = 0, max = 2147483647)
    @ConfigItem(
        keyName = "highValueThreshold",
        name = "High value threshold",
        description = "Colour the total chest value and individual drops red when their Grand Exchange value meets or exceeds this amount. Set to 0 to disable.",
        section = CHEST_LOOT_SECTION,
        position = 3
    )
    default int highValueThreshold() { return 1_000_000; }
}
