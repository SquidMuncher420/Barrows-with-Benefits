/*
 * Barrows door tracking/highlighting logic adapted from
 * Barrows-Door-Highlighter by Jordan Hans (2022), BSD-2-Clause.
 * See THIRD_PARTY_NOTICES.txt included with this project.
 */
package com.betterbarrows;

import java.awt.Font;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.ObjectComposition;
import net.runelite.api.WallObject;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import net.runelite.client.ui.overlay.OverlayUtil;

final class BetterBarrowsHelperOverlay extends Overlay
{
    private final Client client;
    private final BetterBarrowsPlugin plugin;
    private final BetterBarrowsConfig config;
    private final ModelOutlineRenderer modelOutlineRenderer;

    @Inject
    BetterBarrowsHelperOverlay(Client client, BetterBarrowsPlugin plugin, BetterBarrowsConfig config, ModelOutlineRenderer modelOutlineRenderer)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.modelOutlineRenderer = modelOutlineRenderer;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.UNDER_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 12));
        renderTargetNpcOutlines();
        // Match the known-working Barrows Door Highlighter: tunnel doors are
        // rendered on top-level WorldView plane 0. Do not use WorldPoint plane
        // here; that was preventing valid Barrows doors from rendering.
        if (config.highlightTunnelDoors()
                && client.getTopLevelWorldView() != null
                && client.getTopLevelWorldView().getPlane() == 0)
        {
            renderUsableDoors(graphics);
        }

        if (config.showPuzzleAnswer())
        {
            renderPuzzleAnswer(graphics);
        }

        return null;
    }

    private void renderUsableDoors(Graphics2D graphics)
    {

        Stroke oldStroke = graphics.getStroke();
        Color oldColor = graphics.getColor();
        graphics.setStroke(new BasicStroke(config.tunnelDoorWidth()));

        for (WallObject door : plugin.getBarrowsDoors())
        {
            ObjectComposition composition = client.getObjectDefinition(door.getId());
            if (composition == null || composition.getImpostorIds() == null)
            {
                continue;
            }

            ObjectComposition impostor = composition.getImpostor();
            if (impostor == null)
            {
                continue;
            }

            // Barrows Door Highlighter's key behaviour: the active impostor has
            // actions when this door is unlocked/usable. Only those doors are
            // highlighted so the available route is visible at a glance.
            // Match the known-working Barrows Door Highlighter implementation:
            // the active impostor has a non-empty actions array when unlocked.
            String[] actions = impostor.getActions();
            if (actions == null || actions.length == 0)
            {
                continue;
            }

            Shape hull = door.getConvexHull();
            if (hull != null)
            {
                OverlayUtil.renderPolygon(graphics, hull, config.tunnelDoorColor());
            }
        }

        graphics.setStroke(oldStroke);
        graphics.setColor(oldColor);
    }

    private void renderTargetNpcOutlines()
    {
        if (!config.highlightTunnelTargetNpcs() || !plugin.isInTunnelMaze())
        {
            return;
        }

        for (NPC npc : client.getNpcs())
        {
            if (npc != null && plugin.isTunnelTargetStillNeeded(npc.getName()))
            {
                modelOutlineRenderer.drawOutline(
                        npc,
                        config.tunnelNpcHighlightWidth(),
                        config.tunnelNpcHighlightColor(),
                        2);
            }
        }
    }

    private void renderPuzzleAnswer(Graphics2D graphics)
    {
        Widget answerWidget = plugin.getPuzzleAnswer();
        if (answerWidget == null || answerWidget.isHidden() || answerWidget.getBounds() == null)
        {
            return;
        }

        Rectangle bounds = answerWidget.getBounds();

        // User-requested marker: solid, opaque fill (no outline) over the correct
        // answer, using the configurable puzzle answer colour.
        graphics.setColor(config.puzzleAnswerColor());
        graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}
