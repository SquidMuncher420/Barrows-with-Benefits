package com.betterbarrows;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.Text;

final class BetterBarrowsHudOverlay extends Overlay
{
    private static final Color COMPLETE_GREEN = new Color(0x66, 0xCC, 0x66);
    private static final int ROW_HEIGHT = 16;
    private static final int DIAMOND_RADIUS = 5;

    private final Client client;
    private final BetterBarrowsPlugin plugin;
    private final BetterBarrowsConfig config;

    @Inject
    BetterBarrowsHudOverlay(Client client, BetterBarrowsPlugin plugin, BetterBarrowsConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        Widget brothers = client.getWidget(InterfaceID.BarrowsOverlay.BROTHERS);
        if (brothers == null || brothers.isHidden())
        {
            return null;
        }

        Rectangle bounds = brothers.getBounds();
        if (bounds == null)
        {
            return null;
        }

        drawTunnelBrother(graphics, brothers, bounds);

        if (plugin.isInTunnelMaze())
        {
            drawTunnelTracker(graphics, bounds);
        }

        return null;
    }

    private void drawTunnelBrother(Graphics2D graphics, Widget brothers, Rectangle bounds)
    {
        BetterBarrowsBrother tunnel = plugin.getTunnelBrother();
        if (tunnel == null)
        {
            return;
        }

        Widget row = findBrotherWidget(brothers, tunnel.getDisplayName());
        if (row == null || row.getBounds() == null)
        {
            return;
        }

        Rectangle rowBounds = row.getBounds();
        Color tunnelColor = config.tunnelHighlightColor();

        if (config.showTunnelDiamond())
        {
            // Native green tick remains in its normal column; diamond is deliberately
            // pushed to the right by a configurable amount.
            int x = bounds.x + bounds.width - 7 + config.tunnelDiamondOffset();
            int y = rowBounds.y + rowBounds.height / 2;

            Polygon diamond = new Polygon(
                    new int[] {x, x + DIAMOND_RADIUS, x, x - DIAMOND_RADIUS},
                    new int[] {y - DIAMOND_RADIUS, y, y + DIAMOND_RADIUS, y},
                    4);

            Color old = graphics.getColor();
            graphics.setColor(tunnelColor);
            graphics.fill(diamond);
            graphics.setColor(old);
        }
    }

    private Widget findBrotherWidget(Widget widget, String brotherName)
    {
        if (widget == null)
        {
            return null;
        }

        String widgetText = widget.getText();
        if (widgetText != null && Text.removeTags(widgetText).trim().startsWith(brotherName))
        {
            return widget;
        }

        Widget found = findInChildren(widget.getDynamicChildren(), brotherName);
        if (found != null) return found;
        found = findInChildren(widget.getStaticChildren(), brotherName);
        if (found != null) return found;
        return findInChildren(widget.getNestedChildren(), brotherName);
    }

    private Widget findInChildren(Widget[] children, String brotherName)
    {
        if (children == null) return null;
        for (Widget child : children)
        {
            Widget found = findBrotherWidget(child, brotherName);
            if (found != null) return found;
        }
        return null;
    }

    private void drawTunnelTracker(Graphics2D graphics, Rectangle brotherBounds)
    {
        Font oldFont = graphics.getFont();
        graphics.setFont(FontManager.getRunescapeSmallFont());

        int x = brotherBounds.x;
        int y = brotherBounds.y + brotherBounds.height + 16;
        boolean drewRow = false;

        if (config.showTunnelKillTracker())
        {
            drewRow |= drawTargetRow(graphics, x, y, "Bloodworm", plugin.getBloodwormKills(), config.bloodwormTarget());
            if (config.bloodwormTarget() > 0) y += ROW_HEIGHT;
            drewRow |= drawTargetRow(graphics, x, y, "Crypt rat", plugin.getCryptRatKills(), config.cryptRatTarget());
            if (config.cryptRatTarget() > 0) y += ROW_HEIGHT;
            drewRow |= drawTargetRow(graphics, x, y, "Giant crypt rat", plugin.getGiantCryptRatKills(), config.giantCryptRatTarget());
            if (config.giantCryptRatTarget() > 0) y += ROW_HEIGHT;
            drewRow |= drawTargetRow(graphics, x, y, "Crypt spider", plugin.getCryptSpiderKills(), config.cryptSpiderTarget());
            if (config.cryptSpiderTarget() > 0) y += ROW_HEIGHT;
            drewRow |= drawTargetRow(graphics, x, y, "Giant crypt spider", plugin.getGiantCryptSpiderKills(), config.giantCryptSpiderTarget());
            if (config.giantCryptSpiderTarget() > 0) y += ROW_HEIGHT;
            drewRow |= drawTargetRow(graphics, x, y, "Skeleton", plugin.getSkeletonKills(), config.skeletonTarget());
            if (config.skeletonTarget() > 0) y += ROW_HEIGHT;
        }

        if (config.showRewardPotential())
        {
            if (drewRow) y += 4;
            double percent = plugin.getRewardPotentialPercent();
            Color potentialColor = rewardPotentialColor(percent);
            OverlayUtil.renderTextLocation(
                    graphics,
                    new net.runelite.api.Point(x, y),
                    String.format("Reward Potential: %.1f%%", percent),
                    potentialColor);
        }

        graphics.setFont(oldFont);
    }


    private Color rewardPotentialColor(double percent)
    {
        if (!config.colorRewardPotential()) return Color.WHITE;
        if (percent < 70.0) return config.rewardLowColor();
        if (percent < 80.0) return config.rewardMediumColor();
        if (percent <= 88.0) return config.rewardGoodColor();
        return config.rewardHighColor();
    }

    private boolean drawTargetRow(Graphics2D graphics, int x, int y, String name, int current, int target)
    {
        if (target <= 0) return false;

        String line = name + "    " + current + "/" + target;
        Color color = current >= target ? COMPLETE_GREEN : Color.WHITE;
        OverlayUtil.renderTextLocation(graphics, new net.runelite.api.Point(x, y), line, color);

        if (current >= target)
        {
            FontMetrics metrics = graphics.getFontMetrics();
            OverlayUtil.renderTextLocation(
                    graphics,
                    new net.runelite.api.Point(x + metrics.stringWidth(line) + 5, y),
                    "✓",
                    COMPLETE_GREEN);
        }
        return true;
    }
}