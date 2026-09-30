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
        name = "Tunnel memory",
        description = "The remembered sarcophagus that leads down to the crypt maze and chest.",
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
        name = "Tunnel HUD",
        description = "Configure the tunnel brother marker and reward-potential display.",
        position = 3
    )
    String TUNNEL_HUD_SECTION = "tunnelHud";

    @ConfigSection(
        name = "Tunnel doors & puzzle",
        description = "Highlight usable Barrows tunnel doors and the correct puzzle answer.",
        position = 4
    )
    String TUNNEL_HELPER_SECTION = "tunnelHelper";

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
        keyName = "showTunnelSarcophagus",
        name = "Show tunnel markers",
        description = "Mark the brother whose room leads to the crypt maze and chest.",
        section = TUNNEL_MEMORY_SECTION,
        position = 0
    )
    default boolean showTunnelSarcophagus() { return true; }

    @Alpha
    @ConfigItem(
        keyName = "tunnelHighlightColor",
        name = "Tunnel highlight color",
        description = "Color used to mark the tunnel brother.",
        section = TUNNEL_MEMORY_SECTION,
        position = 1
    )
    default Color tunnelHighlightColor() { return new Color(0x29, 0x79, 0xFF, 0xFF); }

    @ConfigItem(
        keyName = "showTunnelKillTracker",
        name = "Show tunnel kill tracker",
        description = "Show the configured NPC kill counters while inside the tunnel maze.",
        section = TUNNEL_TRACKER_SECTION,
        position = 0
    )
    default boolean showTunnelKillTracker() { return true; }

    @ConfigItem(
        keyName = "showRewardPotential",
        name = "Show reward potential",
        description = "Show the current Barrows reward potential while inside the tunnels.",
        section = TUNNEL_TRACKER_SECTION,
        position = 10
    )
    default boolean showRewardPotential() { return true; }

    @Range(min = 0, max = 20)
    @ConfigItem(
        keyName = "bloodwormTarget",
        name = "Bloodworm target",
        description = "Target number of Bloodworms. Set to 0 to hide.",
        section = TUNNEL_TRACKER_SECTION,
        position = 1
    )
    default int bloodwormTarget() { return 1; }

    @Range(min = 0, max = 20)
    @ConfigItem(
        keyName = "cryptRatTarget",
        name = "Crypt rat target",
        description = "Target number of Crypt rats. Set to 0 to hide.",
        section = TUNNEL_TRACKER_SECTION,
        position = 2
    )
    default int cryptRatTarget() { return 0; }

    @Range(min = 0, max = 20)
    @ConfigItem(
        keyName = "giantCryptRatTarget",
        name = "Giant crypt rat target",
        description = "Target number of Giant crypt rats. Set to 0 to hide.",
        section = TUNNEL_TRACKER_SECTION,
        position = 3
    )
    default int giantCryptRatTarget() { return 0; }

    @Range(min = 0, max = 20)
    @ConfigItem(
        keyName = "cryptSpiderTarget",
        name = "Crypt spider target",
        description = "Target number of Crypt spiders. Set to 0 to hide.",
        section = TUNNEL_TRACKER_SECTION,
        position = 4
    )
    default int cryptSpiderTarget() { return 0; }

    @Range(min = 0, max = 20)
    @ConfigItem(
        keyName = "giantCryptSpiderTarget",
        name = "Giant crypt spider target",
        description = "Target number of Giant crypt spiders. Set to 0 to hide.",
        section = TUNNEL_TRACKER_SECTION,
        position = 5
    )
    default int giantCryptSpiderTarget() { return 0; }

    @Range(min = 0, max = 20)
    @ConfigItem(
        keyName = "skeletonTarget",
        name = "Skeleton target",
        description = "Target number of Skeletons. Set to 0 to hide.",
        section = TUNNEL_TRACKER_SECTION,
        position = 6
    )
    default int skeletonTarget() { return 2; }

    @ConfigItem(
        keyName = "highlightTunnelTargetNpcs",
        name = "Highlight target NPCs",
        description = "Highlight tunnel NPCs that are still needed for your configured kill targets.",
        section = TUNNEL_TRACKER_SECTION,
        position = 7
    )
    default boolean highlightTunnelTargetNpcs() { return true; }

    @Alpha
    @ConfigItem(
        keyName = "tunnelNpcHighlightColor",
        name = "NPC highlight colour",
        description = "Outline colour for tunnel NPCs that are still needed.",
        section = TUNNEL_TRACKER_SECTION,
        position = 8
    )
    default Color tunnelNpcHighlightColor() { return new Color(0xFF, 0xA0, 0x00, 0xFF); }

    @Range(min = 1, max = 8)
    @ConfigItem(
        keyName = "tunnelNpcHighlightWidth",
        name = "NPC outline width",
        description = "Thickness of the target NPC outline.",
        section = TUNNEL_TRACKER_SECTION,
        position = 9
    )
    default int tunnelNpcHighlightWidth() { return 2; }

    @ConfigItem(
        keyName = "showTunnelDiamond",
        name = "Show blue tunnel diamond",
        description = "Draw a diamond beside the tunnel brother in the Barrows brother list.",
        section = TUNNEL_HUD_SECTION,
        position = 0
    )
    default boolean showTunnelDiamond() { return true; }

    @Range(min = 0, max = 30)
    @ConfigItem(
        keyName = "tunnelDiamondOffset",
        name = "Diamond right offset",
        description = "Move the tunnel diamond further right so it does not cover the native kill tick.",
        section = TUNNEL_HUD_SECTION,
        position = 1
    )
    default int tunnelDiamondOffset() { return 10; }

    @ConfigItem(
        keyName = "colorRewardPotential",
        name = "Colour reward potential",
        description = "Colour the reward-potential text based on the current percentage.",
        section = TUNNEL_HUD_SECTION,
        position = 3
    )
    default boolean colorRewardPotential() { return true; }

    @Alpha
    @ConfigItem(
        keyName = "rewardLowColor",
        name = "Low potential colour",
        description = "Colour used below 70% reward potential.",
        section = TUNNEL_HUD_SECTION,
        position = 4
    )
    default Color rewardLowColor() { return new Color(0xE5, 0x39, 0x35, 0xFF); }

    @Alpha
    @ConfigItem(
        keyName = "rewardMediumColor",
        name = "Medium potential colour",
        description = "Colour used from 70% through 79.9% reward potential.",
        section = TUNNEL_HUD_SECTION,
        position = 5
    )
    default Color rewardMediumColor() { return new Color(0xFF, 0xA0, 0x00, 0xFF); }

    @Alpha
    @ConfigItem(
        keyName = "rewardGoodColor",
        name = "Good potential colour",
        description = "Colour used from 80% through 88% reward potential.",
        section = TUNNEL_HUD_SECTION,
        position = 6
    )
    default Color rewardGoodColor() { return new Color(0x43, 0xA0, 0x47, 0xFF); }

    @Alpha
    @ConfigItem(
        keyName = "rewardHighColor",
        name = "High potential colour",
        description = "Colour used above 88% reward potential.",
        section = TUNNEL_HUD_SECTION,
        position = 7
    )
    default Color rewardHighColor() { return new Color(0xAB, 0x47, 0xBC, 0xFF); }

    @ConfigItem(
        keyName = "highlightTunnelDoors",
        name = "Highlight tunnel doors",
        description = "Highlight currently usable/openable doors while inside the Barrows tunnel maze.",
        section = TUNNEL_HELPER_SECTION,
        position = 0
    )
    default boolean highlightTunnelDoors() { return true; }

    @Alpha
    @ConfigItem(
        keyName = "tunnelDoorColor",
        name = "Door highlight colour",
        description = "Outline colour for usable Barrows tunnel doors.",
        section = TUNNEL_HELPER_SECTION,
        position = 1
    )
    default Color tunnelDoorColor() { return new Color(0x43, 0xA0, 0x47, 0xFF); }

    @Range(min = 1, max = 8)
    @ConfigItem(
        keyName = "tunnelDoorWidth",
        name = "Door outline width",
        description = "Thickness of the tunnel door outline.",
        section = TUNNEL_HELPER_SECTION,
        position = 2
    )
    default int tunnelDoorWidth() { return 3; }

    @ConfigItem(
        keyName = "showPuzzleAnswer",
        name = "Highlight puzzle answer",
        description = "Draw a solid, opaque fill over the correct answer on the Barrows door puzzle.",
        section = TUNNEL_HELPER_SECTION,
        position = 3
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

    @Range(min = 1, max = 8)
    @ConfigItem(
        keyName = "puzzleAnswerWidth",
        name = "Puzzle legacy width",
        description = "Legacy setting retained for existing configurations; the puzzle answer is now a solid fill.",
        section = TUNNEL_HELPER_SECTION,
        position = 5
    )
    default int puzzleAnswerWidth() { return 3; }

}
