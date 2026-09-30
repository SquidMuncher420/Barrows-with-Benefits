package com.betterbarrows;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

/** Draws compact brother initials at the six Barrows mounds on the minimap. */
final class BetterBarrowsMinimapOverlay extends Overlay
{
    private static final int ABOVE_GROUND_REGION_ID = 14131;
    private static final int REGION_BASE_X = 3520;
    private static final int REGION_BASE_Y = 3264;
    private static final Font LETTER_FONT = new Font("Arial", Font.BOLD, 12);

    private final Client client;
    private final BetterBarrowsPlugin plugin;
    private final BetterBarrowsConfig config;

    @Inject
    BetterBarrowsMinimapOverlay(Client client, BetterBarrowsPlugin plugin, BetterBarrowsConfig config)
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
        if (client.getLocalPlayer() == null
            || client.getLocalPlayer().getWorldLocation().getRegionID() != ABOVE_GROUND_REGION_ID)
        {
            return null;
        }

        graphics.setFont(LETTER_FONT);
        FontMetrics fm = graphics.getFontMetrics();

        for (BetterBarrowsBrother brother : BetterBarrowsBrother.values())
        {
            // Use the exact same surface rectangle data as the large mound overlay.
            int centreX = REGION_BASE_X + brother.getSurfaceRegionX()
                + (brother.getSurfaceDigWidth() - 1) / 2;
            int centreY = REGION_BASE_Y + brother.getSurfaceRegionY()
                + (brother.getSurfaceDigHeight() - 1) / 2;

            LocalPoint local = LocalPoint.fromWorld(client, new WorldPoint(centreX, centreY, 0));
            if (local == null)
            {
                continue;
            }

            Point point = Perspective.localToMinimap(client, local);
            if (point == null)
            {
                continue; // RuneLite clips locations outside the usable minimap radius.
            }

            String letter = brother.getDisplayName().substring(0, 1).toUpperCase();
            Color color;
            if (brother == plugin.getTunnelBrother())
            {
                color = config.tunnelHighlightColor();
            }
            else if (plugin.isBrotherKilled(brother))
            {
                color = config.killedBrotherColor();
            }
            else
            {
                color = config.unkilledBrotherColor();
            }

            Point textPoint = new Point(
                point.getX() - fm.stringWidth(letter) / 2,
                point.getY() + fm.getAscent() / 2);
            OverlayUtil.renderTextLocation(graphics, textPoint, letter, color);
        }

        return null;
    }
}
