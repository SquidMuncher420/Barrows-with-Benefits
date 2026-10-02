package com.barrowswithbenefits;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

/**
 * Compact replacement for Jagex's Barrows HUD, styled like RuneLite's
 * official Barrows Brothers overlay. Hold Alt to drag it.
 */
final class BarrowsTrackerOverlay extends OverlayPanel
{
    private final BarrowsWithBenefitsPlugin plugin;
    private final BarrowsWithBenefitsConfig config;

    @Inject
    BarrowsTrackerOverlay(BarrowsWithBenefitsPlugin plugin, BarrowsWithBenefitsConfig config)
    {
        super(plugin);
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(PRIORITY_LOW);
        setMovable(true);
        setSnappable(true);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!plugin.isInBarrowsArea())
        {
            return null;
        }

        // Match RuneLite's standard Barrows overlay: one compact left/right row
        // per brother, using the normal OverlayPanel background and padding.
        for (BarrowsBrotherLocationData brother : plugin.getEffectiveBrotherOrder())
        {
            boolean tunnel = brother == plugin.getTunnelBrother();
            boolean killed = plugin.isBrotherKilled(brother);

            Color statusColor = tunnel
                    ? config.tunnelHighlightColor()
                    : (killed ? config.killedBrotherColor() : Color.WHITE);
            String status = tunnel ? "◆" : (killed ? "✓" : "✗");
            Color rightColor = tunnel
                    ? config.tunnelHighlightColor()
                    : (killed ? config.killedBrotherColor() : Color.RED);

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(brother.getDisplayName())
                    .leftColor(statusColor)
                    .right(status)
                    .rightFont(FontManager.getDefaultFont())
                    .rightColor(rightColor)
                    .build());
        }

        // Tunnel kill plan is part of the same panel, directly below the brothers.
        if (plugin.isInTunnelMaze() && config.showTunnelKillTracker())
        {
            addTarget("Bloodworm", plugin.getBloodwormKills(), config.bloodwormTarget());
            addTarget("Crypt rat", plugin.getCryptRatKills(), config.cryptRatTarget());
            addTarget("Giant crypt rat", plugin.getGiantCryptRatKills(), config.giantCryptRatTarget());
            addTarget("Crypt spider", plugin.getCryptSpiderKills(), config.cryptSpiderTarget());
            addTarget("Giant crypt spider", plugin.getGiantCryptSpiderKills(), config.giantCryptSpiderTarget());
            addTarget("Skeleton", plugin.getSkeletonKills(), config.skeletonTarget());
        }

        if (plugin.isInTunnelMaze() && config.showRewardPotential())
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Potential")
                    .leftColor(plugin.areTunnelKillTargetsComplete()
                            ? config.killedBrotherColor()
                            : Color.WHITE)
                    .right(String.format("%.1f%%", plugin.getRewardPotentialPercent()))
                    .rightColor(plugin.areTunnelKillTargetsComplete()
                            ? config.killedBrotherColor()
                            : Color.WHITE)
                    .build());
        }

        return super.render(graphics);
    }

    private void addTarget(String name, int current, int target)
    {
        if (target <= 0)
        {
            return;
        }

        boolean complete = current >= target;
        panelComponent.getChildren().add(LineComponent.builder()
                .left(name)
                .leftColor(complete ? config.killedBrotherColor() : Color.WHITE)
                .right(current + "/" + target + (complete ? " ✓" : ""))
                .rightColor(complete ? config.killedBrotherColor() : Color.WHITE)
                .build());
    }
}
