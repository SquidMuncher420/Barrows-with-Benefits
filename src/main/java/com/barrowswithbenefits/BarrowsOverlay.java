package com.barrowswithbenefits;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.WallObject;
import net.runelite.api.GroundObject;
import net.runelite.api.DecorativeObject;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

/**
 * Draws the six labelled brother tiles both above ground (the mounds you dig into) and
 * below ground (the crypt rooms containing each sarcophagus), colouring each tile red or
 * green depending on whether that brother has been killed yet this run, and outlining
 * whichever brother's room is known to lead down to the crypt maze and chest in blue.
 *
 * The tile colours are always read live from the plugin's current state, so they update
 * immediately whenever a brother dies, whenever the tunnel brother becomes known, and
 * whenever the player walks back into view of a tile (on the surface, going back down, or
 * coming back up) - there is no separate "refresh" step required.
 */
final class BarrowsOverlay extends Overlay
{
    private static final int ABOVE_GROUND_REGION_ID = 14131;
    private static final int CRYPT_REGION_ID = 14231;
    private static final int CRYPT_PLANE = 3;
    private static final Stroke TILE_STROKE = new BasicStroke(2.0f);
    private static final Stroke OBJECT_STROKE = new BasicStroke(3.0f);

    private final Client client;
    private final BarrowsWithBenefitsPlugin plugin;
    private final BarrowsWithBenefitsConfig config;

    @Inject
    BarrowsOverlay(
        Client client,
        BarrowsWithBenefitsPlugin plugin,
        BarrowsWithBenefitsConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        graphics.setFont(new Font("SansSerif", Font.BOLD, 12));
        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        int regionId = client.getLocalPlayer().getWorldLocation().getRegionID();
        BarrowsBrotherLocationData tunnelBrother = plugin.getTunnelBrother();

        if (regionId == ABOVE_GROUND_REGION_ID)
        {
            if (config.showSurfaceBrotherTiles())
            {
                renderSurfaceDigAreas(graphics, regionId, tunnelBrother);
            }
            return null;
        }

        if (regionId != CRYPT_REGION_ID
            || client.getLocalPlayer().getWorldLocation().getPlane() != CRYPT_PLANE)
        {
            return null;
        }

        // Inside the brother crypts we deliberately do NOT draw floor tiles.
        // Instead, labels/status are rendered directly over the sarcophagi.
        if (config.showCryptBrotherTiles())
        {
            renderSarcophagusHulls(graphics);
        }

        // The staircase renderer is kill-gated internally: it draws nothing
        // before/during the fight and only shows the green exit after the kill.
        renderCurrentRoomStaircase(graphics);

        return null;
    }

    /**
     * Draw every diggable tile on each surface mound.
     *
     * Dharok: 6x6 (36 tiles)
     * Verac:   5x6 (30 tiles)
     * Ahrim:   6x7 (42 tiles)
     * Guthan:  7x5 (35 tiles)
     * Torag:   5x4 (20 tiles)
     * Karil:   7x7 (49 tiles)
     *
     * The brother name is drawn once at the visual centre of the rectangle.
     */
    private void renderSurfaceDigAreas(
        Graphics2D graphics,
        int regionId,
        BarrowsBrotherLocationData tunnelBrother)
    {
        WorldView worldView = client.getLocalPlayer().getWorldView();
        int plane = worldView.getPlane();

        for (BarrowsBrotherLocationData brother : BarrowsBrotherLocationData.values())
        {
            boolean isTunnel = config.showTunnelSarcophagus() && brother == tunnelBrother;
            boolean killed = plugin.isBrotherKilled(brother);

            Color markerColor;
            if (isTunnel)
            {
                markerColor = config.tunnelHighlightColor();
            }
            else if (config.showKillStatusColors() && killed)
            {
                markerColor = config.killedBrotherColor();
            }
            else
            {
                markerColor = config.unkilledBrotherColor();
            }

            // Extremely faint mound area. Keep the brother name readable, but make
            // the large world-space rectangle only lightly visible.
            Color areaFillColor = new Color(
                markerColor.getRed(),
                markerColor.getGreen(),
                markerColor.getBlue(),
                18);
            Color areaOutlineColor = new Color(
                markerColor.getRed(),
                markerColor.getGreen(),
                markerColor.getBlue(),
                38);

            // Merge every projected diggable tile into ONE Area. This removes the
            // individual tile grid and leaves a single large mound shape/border.
            Area moundArea = new Area();

            for (int dx = 0; dx < brother.getSurfaceDigWidth(); dx++)
            {
                for (int dy = 0; dy < brother.getSurfaceDigHeight(); dy++)
                {
                    WorldPoint tilePoint = WorldPoint.fromRegion(
                        regionId,
                        brother.getSurfaceRegionX() + dx,
                        brother.getSurfaceRegionY() + dy,
                        plane);
                    LocalPoint localPoint = LocalPoint.fromWorld(worldView, tilePoint);
                    if (localPoint == null)
                    {
                        continue;
                    }

                    Polygon polygon = Perspective.getCanvasTilePoly(client, localPoint);
                    if (polygon != null)
                    {
                        moundArea.add(new Area(polygon));
                    }
                }
            }

            Stroke previousStroke = graphics.getStroke();
            Color previousColor = graphics.getColor();
            Font previousFont = graphics.getFont();

            if (!moundArea.isEmpty())
            {
                graphics.setColor(areaFillColor);
                graphics.fill(moundArea);

                graphics.setStroke(TILE_STROKE);
                graphics.setColor(areaOutlineColor);
                graphics.draw(moundArea);
            }

            // Draw the brother name once in the centre of the whole diggable area.
            double centerX = brother.getSurfaceRegionX() + (brother.getSurfaceDigWidth() - 1) / 2.0;
            double centerY = brother.getSurfaceRegionY() + (brother.getSurfaceDigHeight() - 1) / 2.0;
            int labelRegionX = (int) Math.round(centerX);
            int labelRegionY = (int) Math.round(centerY);

            WorldPoint labelPoint = WorldPoint.fromRegion(
                regionId, labelRegionX, labelRegionY, plane);
            LocalPoint labelLocal = LocalPoint.fromWorld(worldView, labelPoint);

            if (labelLocal != null)
            {
                String label = brother.getDisplayName();
                graphics.setFont(FontManager.getRunescapeBoldFont());
                Point textLocation = Perspective.getCanvasTextLocation(
                    client, graphics, labelLocal, label, 0);

                if (textLocation != null)
                {
                    OverlayUtil.renderTextLocation(
                        graphics, textLocation, label, markerColor);
                }
            }

            graphics.setStroke(previousStroke);
            graphics.setColor(previousColor);
            graphics.setFont(previousFont);
        }
    }

    /**
     * Outlines each brother's crypt sarcophagus with their kill-status colour (red/green),
     * and additionally with the blue tunnel colour if they are the known tunnel brother.
     */
    private void renderSarcophagusHulls(Graphics2D graphics)
    {
        WorldView worldView = client.getLocalPlayer().getWorldView();
        int plane = worldView.getPlane();
        WorldPoint player = client.getLocalPlayer().getWorldLocation();
        BarrowsBrotherLocationData tunnelBrother = plugin.getTunnelBrother();

        // Only mark the actual sarcophagus object in the crypt room the player is
        // currently standing in. Do not paint the floor tile beneath it.
        BarrowsBrotherLocationData currentBrother = nearestCryptBrother(player, plane, 12);
        if (currentBrother == null)
        {
            return;
        }

        boolean killed = plugin.isBrotherKilled(currentBrother);
        boolean isTunnel = currentBrother == tunnelBrother;
        Color markerColor = isTunnel
            ? config.tunnelHighlightColor()
            : (config.showKillStatusColors()
                ? (killed ? config.killedBrotherColor() : config.unkilledBrotherColor())
                : config.unkilledBrotherColor());

        WorldPoint markerPoint = WorldPoint.fromRegion(
            CRYPT_REGION_ID,
            currentBrother.getCryptRegionX(),
            currentBrother.getCryptRegionY(),
            plane);
        LocalPoint markerLocalPoint = LocalPoint.fromWorld(worldView, markerPoint);
        if (markerLocalPoint == null)
        {
            return;
        }

        Tile[][][] tiles = worldView.getScene().getTiles();
        int minX = Math.max(0, markerLocalPoint.getSceneX() - 7);
        int maxX = Math.min(tiles[plane].length - 1, markerLocalPoint.getSceneX() + 7);
        int minY = Math.max(0, markerLocalPoint.getSceneY() - 7);
        int maxY = Math.min(tiles[plane][0].length - 1, markerLocalPoint.getSceneY() + 7);

        Stroke oldStroke = graphics.getStroke();
        Color oldColor = graphics.getColor();
        Font oldFont = graphics.getFont();

        // Keep the sarcophagus object outline deliberately light/subtle while
        // retaining the configured brother/tunnel colour.
        Color outlineColor = new Color(
            markerColor.getRed(),
            markerColor.getGreen(),
            markerColor.getBlue(),
            markerColor.getAlpha());
        graphics.setStroke(new BasicStroke(1.5f));

        boolean rendered = false;
        for (int x = minX; x <= maxX && !rendered; x++)
        {
            for (int y = minY; y <= maxY && !rendered; y++)
            {
                Tile tile = tiles[plane][x][y];
                if (tile == null || tile.getGameObjects() == null)
                {
                    continue;
                }

                for (GameObject object : tile.getGameObjects())
                {
                    if (object == null || !object.getSceneMinLocation().equals(tile.getSceneLocation()))
                    {
                        continue;
                    }

                    ObjectComposition composition = client.getObjectDefinition(object.getId());
                    String name = composition == null ? null : composition.getName();
                    if (name == null || !"Sarcophagus".equalsIgnoreCase(name))
                    {
                        continue;
                    }

                    Shape hull = object.getConvexHull();
                    if (hull != null)
                    {
                        OverlayUtil.renderPolygon(graphics, hull, outlineColor);

                        // Put the brother name on the sarcophagus itself, rather
                        // than on a nearby crypt floor tile.
                        String label = currentBrother.getDisplayName();
                        graphics.setFont(FontManager.getRunescapeBoldFont());
                        Point text = Perspective.getCanvasTextLocation(
                            client,
                            graphics,
                            object.getLocalLocation(),
                            label,
                            0);
                        if (text != null)
                        {
                            OverlayUtil.renderTextLocation(graphics, text, label, markerColor);
                        }

                        rendered = true;
                        break;
                    }
                }
            }
        }

        graphics.setStroke(oldStroke);
        graphics.setColor(oldColor);
        graphics.setFont(oldFont);
    }

    /**
     * Outline the staircase in the brother crypt the player is currently inside.
     * Nothing is drawn for the other five rooms. We identify the staircase from
     * its live object definition/action rather than relying on a brittle object ID.
     */
    private void renderCurrentRoomStaircase(Graphics2D graphics)
    {
        if (client.getLocalPlayer() == null)
        {
            return;
        }

        WorldView worldView = client.getLocalPlayer().getWorldView();
        int plane = worldView.getPlane();
        WorldPoint player = client.getLocalPlayer().getWorldLocation();
        BarrowsBrotherLocationData currentBrother = nearestCryptBrother(player, plane, 16);

        // No staircase marker before/during the fight.
        if (currentBrother == null || !plugin.isBrotherKilled(currentBrother))
        {
            return;
        }

        final int staircaseId = getCryptStaircaseObjectId(currentBrother);
        final Color configured = config.roomStaircaseColor();
        final Color green = new Color(0x00, 0xFF, 0x3C, configured.getAlpha());
        final Stroke oldStroke = graphics.getStroke();
        final Color oldColor = graphics.getColor();

        graphics.setStroke(new BasicStroke(
                Math.max(3, config.roomStaircaseWidth()),
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));

        Tile[][][] tiles = worldView.getScene().getTiles();

        // Find the exact staircase object for this brother and draw its MODEL
        // hull. We intentionally do not render a canvas tile or a nearby-object
        // fallback: the green marker must wrap the entire staircase itself.
        for (Tile[] row : tiles[plane])
        {
            if (row == null)
            {
                continue;
            }

            for (Tile tile : row)
            {
                if (tile == null)
                {
                    continue;
                }

                if (tile.getGameObjects() != null)
                {
                    for (GameObject object : tile.getGameObjects())
                    {
                        if (object == null || object.getId() != staircaseId)
                        {
                            continue;
                        }

                        Shape hull = object.getConvexHull();
                        if (hull != null)
                        {
                            OverlayUtil.renderPolygon(graphics, hull, green);
                            graphics.setStroke(oldStroke);
                            graphics.setColor(oldColor);
                            return;
                        }

                        // If RuneLite cannot expose a hull for this model, use
                        // the object's full clickable shape—not its ground tile.
                        Shape clickbox = object.getClickbox();
                        if (clickbox != null)
                        {
                            OverlayUtil.renderPolygon(graphics, clickbox, green);
                            graphics.setStroke(oldStroke);
                            graphics.setColor(oldColor);
                            return;
                        }
                    }
                }

                WallObject wall = tile.getWallObject();
                if (wall != null && wall.getId() == staircaseId)
                {
                    Shape hull = wall.getConvexHull();
                    if (hull == null)
                    {
                        hull = wall.getClickbox();
                    }
                    if (hull != null)
                    {
                        OverlayUtil.renderPolygon(graphics, hull, green);
                        graphics.setStroke(oldStroke);
                        graphics.setColor(oldColor);
                        return;
                    }
                }

                DecorativeObject decorative = tile.getDecorativeObject();
                if (decorative != null && decorative.getId() == staircaseId)
                {
                    Shape hull = decorative.getConvexHull();
                    if (hull == null)
                    {
                        hull = decorative.getClickbox();
                    }
                    if (hull != null)
                    {
                        OverlayUtil.renderPolygon(graphics, hull, green);
                        graphics.setStroke(oldStroke);
                        graphics.setColor(oldColor);
                        return;
                    }
                }

                GroundObject ground = tile.getGroundObject();
                if (ground != null && ground.getId() == staircaseId)
                {
                    Shape hull = ground.getConvexHull();
                    if (hull == null)
                    {
                        hull = ground.getClickbox();
                    }
                    if (hull != null)
                    {
                        OverlayUtil.renderPolygon(graphics, hull, green);
                        graphics.setStroke(oldStroke);
                        graphics.setColor(oldColor);
                        return;
                    }
                }
            }
        }

        graphics.setStroke(oldStroke);
        graphics.setColor(oldColor);
    }

    private static int getCryptStaircaseObjectId(BarrowsBrotherLocationData brother)
    {
        switch (brother)
        {
            case AHRIM:
                return 20667;
            case DHAROK:
                return 20668;
            case GUTHAN:
                return 20669;
            case KARIL:
                return 20670;
            case TORAG:
                return 20671;
            case VERAC:
                return 20672;
            default:
                return -1;
        }
    }

    private static WorldPoint getCryptStairPoint(BarrowsBrotherLocationData brother, int plane)
    {
        switch (brother)
        {
            case AHRIM:
                return new WorldPoint(3557, 9703, plane);
            case DHAROK:
                return new WorldPoint(3556, 9718, plane);
            case GUTHAN:
                return new WorldPoint(3534, 9704, plane);
            case KARIL:
                return new WorldPoint(3546, 9684, plane);
            case TORAG:
                return new WorldPoint(3568, 9683, plane);
            case VERAC:
                return new WorldPoint(3578, 9706, plane);
            default:
                throw new IllegalArgumentException("Unknown Barrows brother: " + brother);
        }
    }

    private BarrowsBrotherLocationData nearestCryptBrother(WorldPoint player, int plane, int maxDistance)
    {
        BarrowsBrotherLocationData nearest = null;
        int best = Integer.MAX_VALUE;
        for (BarrowsBrotherLocationData brother : BarrowsBrotherLocationData.values())
        {
            WorldPoint marker = WorldPoint.fromRegion(
                CRYPT_REGION_ID, brother.getCryptRegionX(), brother.getCryptRegionY(), plane);
            int distance = Math.abs(player.getX() - marker.getX()) + Math.abs(player.getY() - marker.getY());
            if (distance < best)
            {
                best = distance;
                nearest = brother;
            }
        }
        return best <= maxDistance ? nearest : null;
    }

}
