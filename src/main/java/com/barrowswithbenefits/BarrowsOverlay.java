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
        BarrowsBrotherLocationData tunnelBrother = plugin.getTunnelBrother();

        Scene scene = worldView.getScene();
        Tile[][][] tiles = scene.getTiles();
        if (plane < 0 || plane >= tiles.length)
        {
            return;
        }

        Stroke previousStroke = graphics.getStroke();
        Color previousColor = graphics.getColor();
        graphics.setStroke(OBJECT_STROKE);

        for (BarrowsBrotherLocationData brother : BarrowsBrotherLocationData.values())
        {
            boolean isTunnel = tunnelBrother == brother;

            WorldPoint markerPoint = WorldPoint.fromRegion(
                CRYPT_REGION_ID,
                brother.getCryptRegionX(),
                brother.getCryptRegionY(),
                plane);
            LocalPoint markerLocalPoint = LocalPoint.fromWorld(worldView, markerPoint);
            if (markerLocalPoint == null)
            {
                continue;
            }

            int minSceneX = Math.max(0, markerLocalPoint.getSceneX() - 4);
            int maxSceneX = Math.min(tiles[plane].length - 1, markerLocalPoint.getSceneX() + 4);
            int minSceneY = Math.max(0, markerLocalPoint.getSceneY() - 4);
            int maxSceneY = Math.min(tiles[plane][0].length - 1, markerLocalPoint.getSceneY() + 4);

            boolean killed = plugin.isBrotherKilled(brother);
            Color hullColor = isTunnel
                ? config.tunnelHighlightColor()
                : (config.showKillStatusColors()
                    ? (killed ? config.killedBrotherColor() : config.unkilledBrotherColor())
                    : config.unkilledBrotherColor());

            for (int sceneX = minSceneX; sceneX <= maxSceneX; sceneX++)
            {
                for (int sceneY = minSceneY; sceneY <= maxSceneY; sceneY++)
                {
                    Tile tile = tiles[plane][sceneX][sceneY];
                    if (tile == null)
                    {
                        continue;
                    }

                    GameObject[] gameObjects = tile.getGameObjects();
                    if (gameObjects == null)
                    {
                        continue;
                    }

                    for (GameObject gameObject : gameObjects)
                    {
                        if (gameObject == null
                            || gameObject.getId() != brother.getSarcophagusObjectId()
                            || !gameObject.getSceneMinLocation().equals(tile.getSceneLocation()))
                        {
                            continue;
                        }

                        Shape hull = gameObject.getConvexHull();
                        if (hull != null)
                        {
                            graphics.setColor(hullColor);
                            graphics.draw(hull);

                            String label = brother.getDisplayName();

                            java.awt.Rectangle bounds = hull.getBounds();
                            Font oldFont = graphics.getFont();
                            graphics.setFont(FontManager.getRunescapeBoldFont());

                            java.awt.FontMetrics metrics = graphics.getFontMetrics();
                            int textX = bounds.x + (bounds.width - metrics.stringWidth(label)) / 2;
                            int textY = bounds.y + (bounds.height + metrics.getAscent()) / 2;

                            OverlayUtil.renderTextLocation(
                                graphics,
                                new Point(textX, textY),
                                label,
                                hullColor);

                            graphics.setFont(oldFont);
                        }
                    }
                }
            }
        }

        graphics.setStroke(previousStroke);
        graphics.setColor(previousColor);
    }
}
