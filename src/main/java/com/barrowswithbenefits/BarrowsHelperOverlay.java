/*
 * Barrows door tracking/highlighting logic adapted from
 * Barrows-Door-Highlighter by Jordan Hans (2022), BSD-2-Clause.
 * See THIRD_PARTY_NOTICES.txt included with this project.
 */
package com.barrowswithbenefits;

import java.awt.Font;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import net.runelite.api.Perspective;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.ObjectComposition;
import net.runelite.api.WallObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import net.runelite.client.ui.overlay.OverlayUtil;

final class BarrowsHelperOverlay extends Overlay
{
    private final Client client;
    private final BarrowsWithBenefitsPlugin plugin;
    private final BarrowsWithBenefitsConfig config;
    private final ModelOutlineRenderer modelOutlineRenderer;

    @Inject
    BarrowsHelperOverlay(Client client, BarrowsWithBenefitsPlugin plugin, BarrowsWithBenefitsConfig config, ModelOutlineRenderer modelOutlineRenderer)
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
        if ((config.highlightTunnelDoors() || config.showShortestRouteLine())
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

    private static final int CENTRE_ROOM = 4;

    /**
     * Barrows tunnels form a 3x3 room grid. Rather than comparing a door's raw
     * coordinate with the chest, build the graph from the doors which are
     * actually usable in this run and BFS back from the centre room.
     */
    private void renderUsableDoors(Graphics2D graphics)
    {
        if (client.getLocalPlayer() == null || !plugin.isInTunnelMaze())
        {
            return;
        }

        WorldPoint player = client.getLocalPlayer().getWorldLocation();
        if (player == null)
        {
            return;
        }

        /*
         * The old implementation tried to infer a 3x3 room graph from door
         * coordinates. That is too brittle for Barrows because each corridor has
         * multiple physical door leaves/ends. Instead pathfind on RuneLite's live
         * collision map. WorldArea.canTravelInDirection uses the same collision
         * flags the client uses for movement, so the blue route cannot cut through
         * walls or across the middle of rooms.
         */
        List<WorldPoint> path = findShortestWalkablePath(player);
        if (path.isEmpty())
        {
            // Door highlighting must never disappear just because the optional
            // shortest-path solver cannot resolve a route on this tick.
            if (config.highlightTunnelDoors())
            {
                renderAllUsableDoors(graphics);
            }
            return;
        }

        if (config.showShortestRouteLine())
        {
            renderPathTiles(graphics, path);
        }

        if (config.highlightTunnelDoors())
        {
            if (config.shortestRouteDoorsOnly())
            {
                renderPathDoors(graphics, path);
            }
            else
            {
                renderAllUsableDoors(graphics);
            }
        }
    }

    private List<WorldPoint> findShortestWalkablePath(WorldPoint start)
    {
        net.runelite.api.WorldView worldView = client.getTopLevelWorldView();
        if (worldView == null || worldView.getCollisionMaps() == null)
        {
            return java.util.Collections.emptyList();
        }

        /*
         * Build explicit bidirectional edges through each currently usable Barrows
         * wall door. A closed wall-door is represented as blocked in the collision
         * map even when its "Open" action makes it traversable, which is why the
         * previous pure-collision BFS found no route.
         */
        Map<WorldPoint, Set<WorldPoint>> doorCrossings = new HashMap<>();
        for (WallObject door : plugin.getBarrowsDoors())
        {
            if (door == null || door.getWorldLocation() == null || !isUsableDoor(door))
            {
                continue;
            }

            WorldPoint p = door.getWorldLocation();
            int dx = 0;
            int dy = 0;
            switch (door.getOrientationA())
            {
                case 1:
                    dx = -1;
                    break;
                case 4:
                    dx = 1;
                    break;
                case 8:
                    dy = -1;
                    break;
                default:
                    dy = 1;
                    break;
            }

            WorldPoint q = new WorldPoint(p.getX() + dx, p.getY() + dy, p.getPlane());
            doorCrossings.computeIfAbsent(p, k -> new HashSet<>()).add(q);
            doorCrossings.computeIfAbsent(q, k -> new HashSet<>()).add(p);
        }

        Queue<WorldPoint> queue = new ArrayDeque<>();
        Map<WorldPoint, WorldPoint> previous = new HashMap<>();
        Set<WorldPoint> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);

        // Cardinal movement produces a clean RuneScape-style route and prevents
        // corner-cutting/diagonal blue tiles.
        final int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        WorldPoint goal = null;
        int examined = 0;
        final int MAX_TILES = 12000;

        while (!queue.isEmpty() && examined++ < MAX_TILES)
        {
            WorldPoint current = queue.remove();
            if (isCentreRoomTile(current))
            {
                goal = current;
                break;
            }

            WorldArea area = new WorldArea(current, 1, 1);
            for (int[] d : dirs)
            {
                WorldPoint next = new WorldPoint(
                    current.getX() + d[0],
                    current.getY() + d[1],
                    current.getPlane());

                boolean normalMove = area.canTravelInDirection(worldView, d[0], d[1]);
                boolean usableDoorCrossing = doorCrossings
                    .getOrDefault(current, java.util.Collections.emptySet())
                    .contains(next);

                if ((!normalMove && !usableDoorCrossing) || visited.contains(next))
                {
                    continue;
                }

                // LocalPoint being present is a safer scene-boundary test than a
                // hard-coded region id and also works across instanced WorldViews.
                if (LocalPoint.fromWorld(worldView, next) == null)
                {
                    continue;
                }

                visited.add(next);
                previous.put(next, current);
                queue.add(next);
            }
        }

        if (goal == null)
        {
            return java.util.Collections.emptyList();
        }

        List<WorldPoint> path = new ArrayList<>();
        for (WorldPoint p = goal; p != null; p = previous.get(p))
        {
            path.add(p);
            if (p.equals(start))
            {
                break;
            }
        }
        java.util.Collections.reverse(path);
        return path;
    }

    private static boolean isCentreRoomTile(WorldPoint p)
    {
        return p.getX() >= 3546 && p.getX() <= 3557
            && p.getY() >= 9689 && p.getY() <= 9700;
    }

    private void renderPathTiles(Graphics2D graphics, List<WorldPoint> path)
    {
        if (path.size() < 2)
        {
            return;
        }

        // Route progress is automatic: BFS starts from the player's current tile
        // every render, so travelled tiles disappear from the line immediately.
        Stroke oldStroke = graphics.getStroke();
        Color oldColor = graphics.getColor();
        Font oldFont = graphics.getFont();
        graphics.setStroke(new BasicStroke(
            config.shortestRouteLineWidth(),
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND));
        graphics.setColor(config.shortestRouteLineColor());

        java.awt.Point previous = null;
        java.awt.Point lastVisible = null;
        int lastVisibleIndex = -1;

        for (int i = 1; i < path.size(); i++)
        {
            LocalPoint lp = LocalPoint.fromWorld(client.getTopLevelWorldView(), path.get(i));
            if (lp == null)
            {
                continue;
            }

            net.runelite.api.Point canvas = Perspective.localToCanvas(client, lp, 0);
            if (canvas == null)
            {
                continue;
            }

            java.awt.Point current = new java.awt.Point(canvas.getX(), canvas.getY());
            if (previous != null)
            {
                graphics.drawLine(previous.x, previous.y, current.x, current.y);
            }
            previous = current;
            lastVisible = current;
            lastVisibleIndex = i;
        }

        // If some of the route remains beyond what can currently be projected,
        // put a compact arrow at the end of the visible line.
        if (config.showChestDirectionIndicator()
            && lastVisible != null
            && lastVisibleIndex >= 0
            && lastVisibleIndex < path.size() - 1)
        {
            WorldPoint from = path.get(lastVisibleIndex);
            WorldPoint to = path.get(Math.min(path.size() - 1, lastVisibleIndex + 1));
            int dx = Integer.compare(to.getX(), from.getX());
            int dy = Integer.compare(to.getY(), from.getY());

            String arrow;
            if (Math.abs(dx) > Math.abs(dy))
            {
                arrow = dx >= 0 ? ">" : "<";
            }
            else
            {
                arrow = dy >= 0 ? "^" : "v";
            }

            graphics.setFont(new Font("SansSerif", Font.BOLD, 16));
            graphics.drawString(arrow, lastVisible.x - 4, lastVisible.y + 5);
        }

        graphics.setStroke(oldStroke);
        graphics.setColor(oldColor);
        graphics.setFont(oldFont);
    }

    private void renderPathDoors(Graphics2D graphics, List<WorldPoint> path)
    {
        if (path.size() < 2)
        {
            return;
        }

        net.runelite.api.WorldView worldView = client.getTopLevelWorldView();
        List<WallObject> routeDoors = new ArrayList<>();

        for (int i = 0; i < path.size() - 1; i++)
        {
            WorldPoint from = path.get(i);
            WorldPoint to = path.get(i + 1);
            int dx = to.getX() - from.getX();
            int dy = to.getY() - from.getY();

            WorldArea area = new WorldArea(from, 1, 1);
            if (area.canTravelInDirection(worldView, dx, dy))
            {
                continue;
            }

            for (WallObject door : plugin.getBarrowsDoors())
            {
                if (door == null || door.getWorldLocation() == null || !isUsableDoor(door))
                {
                    continue;
                }

                WorldPoint p = door.getWorldLocation();
                int ox = 0, oy = 0;
                switch (door.getOrientationA())
                {
                    case 1: ox = -1; break;
                    case 4: ox = 1; break;
                    case 8: oy = -1; break;
                    default: oy = 1; break;
                }
                WorldPoint q = new WorldPoint(p.getX() + ox, p.getY() + oy, p.getPlane());

                if ((from.equals(p) && to.equals(q)) || (from.equals(q) && to.equals(p)))
                {
                    if (!routeDoors.contains(door))
                    {
                        routeDoors.add(door);
                    }
                }
            }
        }

        if (routeDoors.isEmpty())
        {
            return;
        }

        // Add only the adjacent second leaf of each selected doorway.
        List<WallObject> expanded = new ArrayList<>(routeDoors);
        for (WallObject chosen : new ArrayList<>(routeDoors))
        {
            WorldPoint p = chosen.getWorldLocation();
            for (WallObject candidate : plugin.getBarrowsDoors())
            {
                if (candidate == null || candidate.getWorldLocation() == null || !isUsableDoor(candidate))
                {
                    continue;
                }
                WorldPoint q = candidate.getWorldLocation();
                if (q.getPlane() == p.getPlane()
                    && Math.abs(q.getX() - p.getX()) + Math.abs(q.getY() - p.getY()) <= 1
                    && !expanded.contains(candidate))
                {
                    expanded.add(candidate);
                }
            }
        }

        WallObject nextDoor = routeDoors.get(0);
        WorldPoint nextPoint = nextDoor.getWorldLocation();

        Stroke oldStroke = graphics.getStroke();
        Color oldColor = graphics.getColor();

        for (WallObject door : expanded)
        {
            Shape hull = door.getConvexHull();
            if (hull == null)
            {
                continue;
            }

            boolean isNext = config.emphasizeNextDoor()
                && door.getWorldLocation() != null
                && nextPoint != null
                && Math.abs(door.getWorldLocation().getX() - nextPoint.getX())
                 + Math.abs(door.getWorldLocation().getY() - nextPoint.getY()) <= 1;

            Color base = config.tunnelDoorColor();
            Color color = isNext
                ? new Color(base.getRed(), base.getGreen(), base.getBlue(), Math.max(base.getAlpha(), 220))
                : new Color(base.getRed(), base.getGreen(), base.getBlue(), Math.min(base.getAlpha(), 90));

            graphics.setStroke(new BasicStroke(
                isNext ? Math.min(10, config.tunnelDoorWidth() + 2) : config.tunnelDoorWidth()));
            OverlayUtil.renderPolygon(graphics, hull, color);
        }

        graphics.setStroke(oldStroke);
        graphics.setColor(oldColor);
    }

    private void renderAllUsableDoors(Graphics2D graphics)
    {
        Stroke oldStroke = graphics.getStroke();
        Color oldColor = graphics.getColor();
        graphics.setStroke(new BasicStroke(config.tunnelDoorWidth()));
        for (WallObject door : plugin.getBarrowsDoors())
        {
            if (door != null && isUsableDoor(door))
            {
                Shape hull = door.getConvexHull();
                if (hull != null)
                {
                    OverlayUtil.renderPolygon(graphics, hull, config.tunnelDoorColor());
                }
            }
        }
        graphics.setStroke(oldStroke);
        graphics.setColor(oldColor);
    }

    private int[] bfsDistances(Map<Integer, Set<Integer>> graph, int start)
    {
        int[] distances = new int[9];
        Arrays.fill(distances, Integer.MAX_VALUE);
        distances[start] = 0;

        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(start);

        while (!queue.isEmpty())
        {
            int room = queue.remove();
            for (int next : graph.getOrDefault(room, java.util.Collections.emptySet()))
            {
                if (distances[next] == Integer.MAX_VALUE)
                {
                    distances[next] = distances[room] + 1;
                    queue.add(next);
                }
            }
        }
        return distances;
    }

    /**
     * Convert a world point into the Barrows 3x3 room grid:
     * 0 1 2
     * 3 4 5
     * 6 7 8
     *
     * The centre room occupies x=3546..3557, y=9689..9700. The same boundary
     * lines divide the surrounding eight tunnel rooms.
     */
    private static WorldPoint roomCentre(int room, int plane)
    {
        // Centres of the nine Barrows tunnel rooms:
        // 0 1 2
        // 3 4 5
        // 6 7 8
        final int[] xs = {3539, 3551, 3564};
        final int[] ys = {9707, 9694, 9682};

        if (room < 0 || room >= 9)
        {
            return null;
        }

        int row = room / 3;
        int col = room % 3;
        return new WorldPoint(xs[col], ys[row], plane);
    }

    private static int roomFor(WorldPoint point)
    {
        // Use nearest known tunnel-room centre. This is stable for doors positioned
        // several tiles inside a corridor, unlike the old hard boundary sampling.
        final int[] xs = {3539, 3551, 3564};
        final int[] ys = {9707, 9694, 9682};
        int bestRoom = 0;
        int bestDistance = Integer.MAX_VALUE;
        for (int row = 0; row < 3; row++)
        {
            for (int col = 0; col < 3; col++)
            {
                int d = Math.abs(point.getX() - xs[col]) + Math.abs(point.getY() - ys[row]);
                if (d < bestDistance)
                {
                    bestDistance = d;
                    bestRoom = row * 3 + col;
                }
            }
        }
        return bestRoom;
    }

    /**
     * Determine which two adjacent grid rooms a door joins by sampling a tile
     * on each side of it. This avoids hard-coding individual door IDs and also
     * groups the two adjacent wall objects which make up one visual doorway.
     */
    private static DoorEdge edgeFor(WallObject door)
    {
        WorldPoint p = door.getWorldLocation();
        if (p == null)
        {
            return null;
        }

        // Find the closest adjacent room pair to this live Barrows door. This
        // handles both physical ends of the short connecting corridors.
        int bestA = -1;
        int bestB = -1;
        int best = Integer.MAX_VALUE;
        for (int a = 0; a < 9; a++)
        {
            for (int b = a + 1; b < 9; b++)
            {
                if (!adjacent(a, b))
                {
                    continue;
                }
                WorldPoint ca = roomCentre(a, p.getPlane());
                WorldPoint cb = roomCentre(b, p.getPlane());
                int midX = (ca.getX() + cb.getX()) / 2;
                int midY = (ca.getY() + cb.getY()) / 2;
                int d = Math.abs(p.getX() - midX) + Math.abs(p.getY() - midY);
                if (d < best)
                {
                    best = d;
                    bestA = a;
                    bestB = b;
                }
            }
        }
        return bestA < 0 ? null : new DoorEdge(door, bestA, bestB);
    }

    private static boolean adjacent(int a, int b)
    {
        int ar = a / 3;
        int ac = a % 3;
        int br = b / 3;
        int bc = b % 3;
        return Math.abs(ar - br) + Math.abs(ac - bc) == 1;
    }

    private static final class Doorway
    {
        private final int a;
        private final int b;
        private final List<WallObject> doors = new ArrayList<>();
        private boolean usable;

        private Doorway(int a, int b)
        {
            this.a = a;
            this.b = b;
        }

        private boolean touches(int room)
        {
            return a == room || b == room;
        }

        private int other(int room)
        {
            if (a == room)
            {
                return b;
            }
            if (b == room)
            {
                return a;
            }
            return -1;
        }
    }

    private static final class DoorEdge
    {
        private final WallObject door;
        private final int a;
        private final int b;

        private DoorEdge(WallObject door, int a, int b)
        {
            this.door = door;
            this.a = a;
            this.b = b;
        }

        private boolean touches(int room)
        {
            return a == room || b == room;
        }

        private int other(int room)
        {
            if (a == room)
            {
                return b;
            }
            if (b == room)
            {
                return a;
            }
            return -1;
        }
    }

    private boolean isUsableDoor(WallObject door)
    {
        ObjectComposition composition = client.getObjectDefinition(door.getId());
        if (composition == null || composition.getImpostorIds() == null)
        {
            return false;
        }

        ObjectComposition impostor = composition.getImpostor();
        if (impostor == null)
        {
            return false;
        }

        String[] actions = impostor.getActions();
        return actions != null && actions.length > 0;
    }

    private void renderTargetNpcOutlines()
    {
        if (!config.highlightTunnelTargetNpcs()
                || !plugin.isInTunnelMaze()
                || plugin.areTunnelKillTargetsComplete())
        {
            // Once every configured tunnel kill target has been reached, remove
            // all NPC outlines for the rest of this run. The plugin's existing
            // chest/run reset clears the kill counters, so they return next run.
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
