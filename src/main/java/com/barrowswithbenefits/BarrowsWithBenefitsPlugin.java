/*
 * Barrows door ID tracking adapted from Barrows-Door-Highlighter
 * by Jordan Hans (2022), BSD-2-Clause. See THIRD_PARTY_NOTICES.txt.
 */
package com.barrowswithbenefits;

import com.google.inject.Provides;
import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.ChatMessageType;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.WallObject;
import net.runelite.api.WorldView;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.api.Varbits;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import net.runelite.client.util.QuantityFormatter;

import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_A_L;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_A_R;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_B_L;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_B_R;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_C_L;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_C_R;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_D_L;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_D_R;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_E_L;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_E_R;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_F_L;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_F_R;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_G_L;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_G_R;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_H_L;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_H_R;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_I_L;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_I_R;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_J_L;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_J_R;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_K_L;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_K_R;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_L_L;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_L_R;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_M_L;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_M_R;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_N_L;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_N_R;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_O_L;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_O_R;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_P_L;
import static net.runelite.api.gameval.ObjectID.BARROWS_DOOR_P_R;

@PluginDescriptor(
        name = "Barrows with Benefits",
        description = "Labels Barrows mounds and sarcophagi with each brother's name, colours them red/green by kill "
                + "status for the current run, and remembers which one leads to the tunnels if you leave pre-maturely.",
        tags = {"barrows", "sarcophagus", "sarcophagi", "tunnel", "memory", "brothers", "kill", "tracker"}
)
public class BarrowsWithBenefitsPlugin extends Plugin
{
    private static final int ABOVE_GROUND_REGION_ID = 14131;
    private static final int CRYPT_REGION_ID = 14231;


    private static final long PENDING_SARCOPHAGUS_TIMEOUT_NANOS = 5_000_000_000L;
    private static final String TUNNEL_BROTHER_CONFIG_KEY = "rememberedTunnelBrother";
    private static final String EMPTY_BROTHERS_CONFIG_KEY = "emptySarcophagi";
    private static final String KILLED_BROTHERS_CONFIG_KEY = "killedBrothers";

    private static final String HIDDEN_TUNNEL_PROMPT =
            "You've found a hidden tunnel, do you want to enter?";

    private static final String BROTHER_SPAWN_MESSAGE =
            "You don't find anything.";

    // Marker added to Jagex's existing top-left Barrows brother list.
    private static final String TUNNEL_LIST_MARKER = " <col=3399ff>◆</col>";
    private static final String TUNNEL_TRACKER_START = "<br><col=ffffff>Barrows with Benefits tunnel</col>";
    private static final int SKELETON_TARGET = 2;
    private static final int BLOODWORM_TARGET = 1;

    private static final Set<Integer> BARROWS_DOOR_IDS = Set.of(
            BARROWS_DOOR_A_L, BARROWS_DOOR_A_R, BARROWS_DOOR_B_L, BARROWS_DOOR_B_R,
            BARROWS_DOOR_C_L, BARROWS_DOOR_C_R, BARROWS_DOOR_D_L, BARROWS_DOOR_D_R,
            BARROWS_DOOR_E_L, BARROWS_DOOR_E_R, BARROWS_DOOR_F_L, BARROWS_DOOR_F_R,
            BARROWS_DOOR_G_L, BARROWS_DOOR_G_R, BARROWS_DOOR_H_L, BARROWS_DOOR_H_R,
            BARROWS_DOOR_I_L, BARROWS_DOOR_I_R, BARROWS_DOOR_J_L, BARROWS_DOOR_J_R,
            BARROWS_DOOR_K_L, BARROWS_DOOR_K_R, BARROWS_DOOR_L_L, BARROWS_DOOR_L_R,
            BARROWS_DOOR_M_L, BARROWS_DOOR_M_R, BARROWS_DOOR_N_L, BARROWS_DOOR_N_R,
            BARROWS_DOOR_O_L, BARROWS_DOOR_O_R, BARROWS_DOOR_P_L, BARROWS_DOOR_P_R);

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private BarrowsWithBenefitsConfig config;

    @Inject
    private ConfigManager configManager;

    @Inject
    private ItemManager itemManager;

    @Inject
    private BarrowsOverlay overlay;

    @Inject
    private BarrowsHudOverlay hudOverlay;

    @Inject
    private BarrowsHelperOverlay helperOverlay;

    @Inject
    private BarrowsMinimapOverlay minimapOverlay;


    @Inject
    private OverlayManager overlayManager;

    private BarrowsBrotherLocationData pendingBrother;
    private Map<Integer, Integer> previousInventory = new HashMap<>();
    private boolean barrowsChestLootPending;
    private int chestLootPendingTicks;
    private long pendingBrotherDeadlineNanos;
    private BarrowsBrotherLocationData tunnelBrother;
    private Widget puzzleAnswer;
    private final Set<BarrowsBrotherLocationData> emptyBrothers = EnumSet.noneOf(BarrowsBrotherLocationData.class);
    private final Set<BarrowsBrotherLocationData> killedBrothers = EnumSet.noneOf(BarrowsBrotherLocationData.class);
    private int bloodwormKills;
    private int cryptRatKills;
    private int giantCryptRatKills;
    private int cryptSpiderKills;
    private int giantCryptSpiderKills;
    private int skeletonKills;
    // NPC instances already counted for tunnel kill tracking. Identity semantics
    // guarantee one count per actual NPC even if RuneLite delivers duplicate signals.
    private final Set<NPC> countedTunnelNpcDeaths =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private int lastObservedBrotherKills = -1;
    private final Set<WallObject> barrowsDoors = new HashSet<>();

    @Provides
    BarrowsWithBenefitsConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(BarrowsWithBenefitsConfig.class);
    }

    @Override
    protected void startUp()
    {
        overlayManager.add(overlay);
        overlayManager.add(hudOverlay);
        overlayManager.add(helperOverlay);
        overlayManager.add(minimapOverlay);

        clientThread.invokeLater(() ->
        {
            loadPersistedMemory();
            snapshotInventory();
        });
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        overlayManager.remove(hudOverlay);
        overlayManager.remove(helperOverlay);
        overlayManager.remove(minimapOverlay);
        clearPendingBrother();
        barrowsDoors.clear();
        puzzleAnswer = null;
        countedTunnelNpcDeaths.clear();
        previousInventory.clear();
        barrowsChestLootPending = false;
        chestLootPendingTicks = 0;
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        String rawOption = Text.removeTags(event.getMenuOption()).trim();
        String rawTarget = Text.removeTags(event.getMenuTarget()).trim();

        // Arm chest valuation before RuneLite receives the resulting inventory update.
        // Barrows chest actions commonly appear as Search/Open on a chest target.
        if (getCurrentRegionId() == CRYPT_REGION_ID
                && ("Search".equalsIgnoreCase(rawOption) || "Open".equalsIgnoreCase(rawOption))
                && rawTarget.toLowerCase().contains("chest"))
        {
            snapshotInventory();
            barrowsChestLootPending = true;
            chestLootPendingTicks = 5;
        }

        if (!isGameObjectAction(event.getMenuAction()))
        {
            return;
        }

        if (getCurrentRegionId() != CRYPT_REGION_ID)
        {
            return;
        }

        String menuOption = rawOption;
        if (!"Search".equalsIgnoreCase(menuOption))
        {
            return;
        }

        BarrowsBrotherLocationData clickedBrother = BarrowsBrotherLocationData.fromSarcophagusObjectId(event.getId());
        if (clickedBrother == null)
        {
            return;
        }

        pendingBrother = clickedBrother;
        pendingBrotherDeadlineNanos = System.nanoTime() + PENDING_SARCOPHAGUS_TIMEOUT_NANOS;
    }


    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        if (pendingBrother == null
                || getCurrentRegionId() != CRYPT_REGION_ID
                || client.getLocalPlayer() == null
                || client.getLocalPlayer().getWorldLocation().getPlane() != 3)
        {
            return;
        }

        String message = Text.removeTags(event.getMessage()).trim();
        if (BROTHER_SPAWN_MESSAGE.equals(message))
        {
            // This message is not authoritative on its own. Keep the pending
            // search alive: a matching NPC spawn means normal brother encounter;
            // hidden-tunnel dialogue or a five-second no-spawn timeout means tunnel.
        }
    }


    @Subscribe
    public void onNpcSpawned(NpcSpawned event)
    {
        if (pendingBrother == null
                || getCurrentRegionId() != CRYPT_REGION_ID
                || client.getLocalPlayer() == null
                || client.getLocalPlayer().getWorldLocation().getPlane() != 3)
        {
            return;
        }

        NPC npc = event.getNpc();
        if (npc != null && pendingBrother.matchesNpcName(npc.getName()))
        {
            markBrotherEmpty(pendingBrother);
            clearPendingBrother();
        }
    }

    @Subscribe
    public void onActorDeath(ActorDeath event)
    {
        Actor actor = event.getActor();
        if (!(actor instanceof NPC))
        {
            return;
        }

        NPC npc = (NPC) actor;
        String npcName = npc.getName();

        // Count recognised tunnel monsters by the dying NPC's own world location,
        // not the player's location at the instant ActorDeath fires. This avoids
        // intermittent misses while the player is moving between tunnel rooms.
        if (isTrackedTunnelNpcName(npcName) && isNpcInBarrowsTunnel(npc))
        {
            if (countedTunnelNpcDeaths.add(npc))
            {
                trackTunnelNpcKill(npcName);
            }
            return;
        }

        // Brother deaths still belong to the six crypt rooms on plane 3.
        if (getCurrentRegionId() != CRYPT_REGION_ID
                || client.getLocalPlayer() == null
                || client.getLocalPlayer().getWorldLocation().getPlane() != 3)
        {
            return;
        }

        for (BarrowsBrotherLocationData brother : BarrowsBrotherLocationData.values())
        {
            if (brother.matchesNpcName(npcName))
            {
                markBrotherKilled(brother);
                return;
            }
        }
    }


    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event)
    {
        if (event.getContainerId() != InventoryID.INV)
        {
            return;
        }

        Map<Integer, Integer> current = inventoryQuantities(event.getItemContainer());

        if (barrowsChestLootPending)
        {
            long totalValue = calculatePositiveInventoryDeltaValue(previousInventory, current);
            if (totalValue > 0)
            {
                client.addChatMessage(
                        ChatMessageType.GAMEMESSAGE,
                        "",
                        "Barrows with Benefits: Total chest value: "
                                + QuantityFormatter.formatNumber(totalValue) + " gp",
                        null);
                barrowsChestLootPending = false;
                chestLootPendingTicks = 0;
            }
        }

        previousInventory = current;
    }

    private void snapshotInventory()
    {
        ItemContainer inventory = client.getItemContainer(InventoryID.INV);
        previousInventory = inventoryQuantities(inventory);
    }

    private Map<Integer, Integer> inventoryQuantities(ItemContainer container)
    {
        Map<Integer, Integer> quantities = new HashMap<>();
        if (container == null)
        {
            return quantities;
        }

        for (Item item : container.getItems())
        {
            if (item != null && item.getId() > 0 && item.getQuantity() > 0)
            {
                quantities.merge(item.getId(), item.getQuantity(), Integer::sum);
            }
        }
        return quantities;
    }

    private long calculatePositiveInventoryDeltaValue(
            Map<Integer, Integer> before,
            Map<Integer, Integer> after)
    {
        long total = 0L;
        for (Map.Entry<Integer, Integer> entry : after.entrySet())
        {
            int itemId = entry.getKey();
            int gained = entry.getValue() - before.getOrDefault(itemId, 0);
            if (gained > 0)
            {
                total += itemManager.getItemPrice(itemId) * (long) gained;
            }
        }
        return total;
    }


    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOADING)
        {
            barrowsDoors.clear();
        }
        else if (event.getGameState() == GameState.LOGGED_IN)
        {
            loadPersistedMemory();
        }
    }

    @Subscribe
    public void onWallObjectSpawned(WallObjectSpawned event)
    {
        WallObject wallObject = event.getWallObject();
        if (wallObject != null && BARROWS_DOOR_IDS.contains(wallObject.getId()))
        {
            barrowsDoors.add(wallObject);
        }
    }

    @Subscribe
    public void onWallObjectDespawned(WallObjectDespawned event)
    {
        barrowsDoors.remove(event.getWallObject());
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event)
    {

        if (event.getGroupId() == InterfaceID.BARROWS_PUZZLE)
        {
            Widget firstPuzzle = client.getWidget(InterfaceID.BarrowsPuzzle._1);
            puzzleAnswer = null;
            if (firstPuzzle != null)
            {
                int answerModel = firstPuzzle.getModelId() - 3;
                int[] choices = {
                    InterfaceID.BarrowsPuzzle.PIC_A,
                    InterfaceID.BarrowsPuzzle.PIC_B,
                    InterfaceID.BarrowsPuzzle.PIC_C
                };
                for (int choice : choices)
                {
                    Widget candidate = client.getWidget(choice);
                    if (candidate != null && candidate.getModelId() == answerModel)
                    {
                        puzzleAnswer = candidate;
                        break;
                    }
                }
            }
            return;
        }

        // The official Barrows Brothers plugin uses this interface to identify
        // the local player's Barrows reward screen. Unlike scene-object changes,
        // this cannot be triggered by another player opening the shared chest.
        if (event.getGroupId() == InterfaceID.BARROWS_REWARD)
        {
            resetMemory();
        }
    }

    @Subscribe
    public void onWidgetClosed(WidgetClosed event)
    {
        if (event.getGroupId() == InterfaceID.BARROWS_PUZZLE)
        {
            puzzleAnswer = null;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (barrowsChestLootPending && --chestLootPendingTicks <= 0)
        {
            barrowsChestLootPending = false;
            chestLootPendingTicks = 0;
            snapshotInventory();
        }

        syncKilledBrothersFromVarbits();

        if (isInBarrowsTunnel() && barrowsDoors.isEmpty())
        {
            rebuildBarrowsDoorsFromScene();
        }

        if (pendingBrother != null && isHiddenTunnelDialogueVisible())
        {
            setTunnelBrother(pendingBrother);
            clearPendingBrother();
        }
        else if (pendingBrother != null
                && System.nanoTime() >= pendingBrotherDeadlineNanos)
        {
            // Fallback: after a real sarcophagus Search, if the matching brother
            // has not spawned within five seconds and no hidden-tunnel dialogue
            // was caught, treat this sarcophagus as the tunnel entrance.
            setTunnelBrother(pendingBrother);
            clearPendingBrother();
        }

        updateBarrowsBrothersWidget();
    }


    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (!BarrowsWithBenefitsConfig.GROUP.equals(event.getGroup()))
        {
            return;
        }

        clientThread.invokeLater(() ->
        {
        });
    }

    Widget getPuzzleAnswer()
    {
        return puzzleAnswer;
    }

    BarrowsBrotherLocationData getTunnelBrother()
    {
        return tunnelBrother;
    }

    boolean isBrotherEmpty(BarrowsBrotherLocationData brother)
    {
        return emptyBrothers.contains(brother);
    }

    boolean isBrotherKilled(BarrowsBrotherLocationData brother)
    {
        return killedBrothers.contains(brother);
    }

    private void setTunnelBrother(BarrowsBrotherLocationData brother)
    {
        if (brother == null)
        {
            return;
        }

        boolean changed = tunnelBrother != brother;
        tunnelBrother = brother;
        if (emptyBrothers.remove(brother))
        {
            persistEmptyBrothers();
        }
        configManager.setRSProfileConfiguration(
                BarrowsWithBenefitsConfig.GROUP,
                TUNNEL_BROTHER_CONFIG_KEY,
                brother.name());
        if (changed)
        {
        }
    }

    private void resetMemory()
    {
        tunnelBrother = null;
        emptyBrothers.clear();
        killedBrothers.clear();
        bloodwormKills = 0;
        cryptRatKills = 0;
        giantCryptRatKills = 0;
        cryptSpiderKills = 0;
        giantCryptSpiderKills = 0;
        skeletonKills = 0;
        countedTunnelNpcDeaths.clear();
        configManager.unsetRSProfileConfiguration(
                BarrowsWithBenefitsConfig.GROUP,
                TUNNEL_BROTHER_CONFIG_KEY);
        configManager.unsetRSProfileConfiguration(
                BarrowsWithBenefitsConfig.GROUP,
                EMPTY_BROTHERS_CONFIG_KEY);
        configManager.unsetRSProfileConfiguration(
                BarrowsWithBenefitsConfig.GROUP,
                KILLED_BROTHERS_CONFIG_KEY);
        configManager.unsetConfiguration(
                BarrowsWithBenefitsConfig.GROUP,
                TUNNEL_BROTHER_CONFIG_KEY);
        clearPendingBrother();
    }

    private void loadPersistedMemory()
    {
        emptyBrothers.clear();
        killedBrothers.clear();

        String persistedKilledBrothers = configManager.getRSProfileConfiguration(
                BarrowsWithBenefitsConfig.GROUP,
                KILLED_BROTHERS_CONFIG_KEY);

        if (persistedKilledBrothers != null && !persistedKilledBrothers.trim().isEmpty())
        {
            for (String value : persistedKilledBrothers.split(","))
            {
                try
                {
                    killedBrothers.add(BarrowsBrotherLocationData.valueOf(value.trim()));
                }
                catch (IllegalArgumentException ignored)
                {
                    // Ignore invalid values and rewrite the cleaned set below.
                }
            }
            persistKilledBrothers();
        }

        String persistedEmptyBrothers = configManager.getRSProfileConfiguration(
                BarrowsWithBenefitsConfig.GROUP,
                EMPTY_BROTHERS_CONFIG_KEY);

        if (persistedEmptyBrothers != null && !persistedEmptyBrothers.trim().isEmpty())
        {
            for (String value : persistedEmptyBrothers.split(","))
            {
                try
                {
                    emptyBrothers.add(BarrowsBrotherLocationData.valueOf(value.trim()));
                }
                catch (IllegalArgumentException ignored)
                {
                    // Ignore invalid values and rewrite the cleaned set below.
                }
            }
            persistEmptyBrothers();
        }

        String persistedBrother = configManager.getRSProfileConfiguration(
                BarrowsWithBenefitsConfig.GROUP,
                TUNNEL_BROTHER_CONFIG_KEY);

        if (persistedBrother == null || persistedBrother.isEmpty())
        {
            tunnelBrother = null;
            return;
        }

        try
        {
            tunnelBrother = BarrowsBrotherLocationData.valueOf(persistedBrother);
            if (emptyBrothers.remove(tunnelBrother))
            {
                persistEmptyBrothers();
            }
        }
        catch (IllegalArgumentException ex)
        {
            tunnelBrother = null;
            configManager.unsetRSProfileConfiguration(
                    BarrowsWithBenefitsConfig.GROUP,
                    TUNNEL_BROTHER_CONFIG_KEY);
        }
    }

    private void markBrotherEmpty(BarrowsBrotherLocationData brother)
    {
        if (brother == null || brother == tunnelBrother || !emptyBrothers.add(brother))
        {
            return;
        }

        persistEmptyBrothers();
    }

    private void markBrotherKilled(BarrowsBrotherLocationData brother)
    {
        if (brother == null || !killedBrothers.add(brother))
        {
            return;
        }

        persistKilledBrothers();
    }

    private void persistKilledBrothers()
    {
        if (killedBrothers.isEmpty())
        {
            configManager.unsetRSProfileConfiguration(
                    BarrowsWithBenefitsConfig.GROUP,
                    KILLED_BROTHERS_CONFIG_KEY);
            return;
        }

        String value = killedBrothers.stream()
                .map(Enum::name)
                .sorted()
                .collect(Collectors.joining(","));

        configManager.setRSProfileConfiguration(
                BarrowsWithBenefitsConfig.GROUP,
                KILLED_BROTHERS_CONFIG_KEY,
                value);
    }

    private void persistEmptyBrothers()
    {
        if (emptyBrothers.isEmpty())
        {
            configManager.unsetRSProfileConfiguration(
                    BarrowsWithBenefitsConfig.GROUP,
                    EMPTY_BROTHERS_CONFIG_KEY);
            return;
        }

        String value = emptyBrothers.stream()
                .map(Enum::name)
                .sorted()
                .collect(Collectors.joining(","));

        configManager.setRSProfileConfiguration(
                BarrowsWithBenefitsConfig.GROUP,
                EMPTY_BROTHERS_CONFIG_KEY,
                value);
    }

    private void clearPendingBrother()
    {
        pendingBrother = null;
        pendingBrotherDeadlineNanos = 0L;
    }

    private int getCurrentRegionId()
    {
        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null)
        {
            return -1;
        }

        return localPlayer.getWorldLocation().getRegionID();
    }

    private static boolean isGameObjectAction(MenuAction menuAction)
    {
        return menuAction == MenuAction.GAME_OBJECT_FIRST_OPTION
                || menuAction == MenuAction.GAME_OBJECT_SECOND_OPTION
                || menuAction == MenuAction.GAME_OBJECT_THIRD_OPTION
                || menuAction == MenuAction.GAME_OBJECT_FOURTH_OPTION
                || menuAction == MenuAction.GAME_OBJECT_FIFTH_OPTION;
    }

    private void updateBarrowsBrothersWidget()
    {
        Widget brothersWidget = client.getWidget(InterfaceID.BarrowsOverlay.BROTHERS);
        if (brothersWidget == null)
        {
            return;
        }

        // Clean up markers/tracker text written by earlier Barrows with Benefits builds.
        cleanBetterBarrowsText(brothersWidget);

        // Apply the tunnel brother colour once per game tick, after the game has
        // updated the native Barrows list. Doing this in the overlay render loop
        // caused the name to flicker between Jagex white and our blue.
    }

    private void cleanBetterBarrowsText(Widget widget)
    {
        if (widget == null)
        {
            return;
        }

        String originalText = widget.getText();
        if (originalText != null && !originalText.isEmpty())
        {
            String cleanText = originalText.replace(TUNNEL_LIST_MARKER, "");
            int trackerIndex = cleanText.indexOf(TUNNEL_TRACKER_START);
            if (trackerIndex >= 0)
            {
                cleanText = cleanText.substring(0, trackerIndex);
            }

            if (!cleanText.equals(originalText))
            {
                widget.setText(cleanText);
            }
        }

        cleanBetterBarrowsChildren(widget.getDynamicChildren());
        cleanBetterBarrowsChildren(widget.getStaticChildren());
        cleanBetterBarrowsChildren(widget.getNestedChildren());
    }

    private void cleanBetterBarrowsChildren(Widget[] children)
    {
        if (children == null)
        {
            return;
        }

        for (Widget child : children)
        {
            cleanBetterBarrowsText(child);
        }
    }

    private boolean isInBarrowsTunnel()
    {
        Player localPlayer = client.getLocalPlayer();
        return localPlayer != null
                && localPlayer.getWorldLocation().getRegionID() == CRYPT_REGION_ID
                && localPlayer.getWorldLocation().getPlane() != 3;
    }

    boolean isTunnelTargetStillNeeded(String npcName)
    {
        if (!isTrackedTunnelNpcName(npcName))
        {
            return false;
        }

        switch (npcName)
        {
            case "Bloodworm":
                return config.bloodwormTarget() > 0 && bloodwormKills < config.bloodwormTarget();
            case "Crypt rat":
                return config.cryptRatTarget() > 0 && cryptRatKills < config.cryptRatTarget();
            case "Giant crypt rat":
                return config.giantCryptRatTarget() > 0 && giantCryptRatKills < config.giantCryptRatTarget();
            case "Crypt spider":
                return config.cryptSpiderTarget() > 0 && cryptSpiderKills < config.cryptSpiderTarget();
            case "Giant crypt spider":
                return config.giantCryptSpiderTarget() > 0 && giantCryptSpiderKills < config.giantCryptSpiderTarget();
            case "Skeleton":
                return config.skeletonTarget() > 0 && skeletonKills < config.skeletonTarget();
            default:
                return false;
        }
    }

    private boolean isTrackedTunnelNpcName(String npcName)
    {
        return npcName != null
                && ("Bloodworm".equalsIgnoreCase(npcName)
                || "Crypt rat".equalsIgnoreCase(npcName)
                || "Giant crypt rat".equalsIgnoreCase(npcName)
                || "Crypt spider".equalsIgnoreCase(npcName)
                || "Giant crypt spider".equalsIgnoreCase(npcName)
                || "Skeleton".equalsIgnoreCase(npcName));
    }

    private boolean isNpcInBarrowsTunnel(NPC npc)
    {
        if (npc == null || npc.getWorldLocation() == null)
        {
            return false;
        }

        return npc.getWorldLocation().getRegionID() == CRYPT_REGION_ID
                && npc.getWorldLocation().getPlane() != 3;
    }

    private void trackTunnelNpcKill(String npcName)
    {
        if (npcName == null)
        {
            return;
        }

        if ("Bloodworm".equalsIgnoreCase(npcName))
        {
            bloodwormKills++;
        }
        else if ("Crypt rat".equalsIgnoreCase(npcName))
        {
            cryptRatKills++;
        }
        else if ("Giant crypt rat".equalsIgnoreCase(npcName))
        {
            giantCryptRatKills++;
        }
        else if ("Crypt spider".equalsIgnoreCase(npcName))
        {
            cryptSpiderKills++;
        }
        else if ("Giant crypt spider".equalsIgnoreCase(npcName))
        {
            giantCryptSpiderKills++;
        }
        else if ("Skeleton".equalsIgnoreCase(npcName))
        {
            skeletonKills++;
        }
    }

    private String buildTunnelTrackerText()
    {
        int rewardPotential = client.getVarbitValue(net.runelite.api.Varbits.BARROWS_REWARD_POTENTIAL);
        double rewardPercent = rewardPotential / 10.0;

        StringBuilder text = new StringBuilder(TUNNEL_TRACKER_START);

        appendTrackerRow(text, "Bloodworm", bloodwormKills, config.bloodwormTarget());
        appendTrackerRow(text, "Crypt rat", cryptRatKills, config.cryptRatTarget());
        appendTrackerRow(text, "Giant crypt rat", giantCryptRatKills, config.giantCryptRatTarget());
        appendTrackerRow(text, "Crypt spider", cryptSpiderKills, config.cryptSpiderTarget());
        appendTrackerRow(text, "Giant crypt spider", giantCryptSpiderKills, config.giantCryptSpiderTarget());
        appendTrackerRow(text, "Skeleton", skeletonKills, config.skeletonTarget());

        if (config.showRewardPotential())
        {
            text.append(String.format("<br><br>Reward Potential: %.1f%%", rewardPercent));
        }

        return text.toString();
    }

    private static void appendTrackerRow(
            StringBuilder text,
            String name,
            int current,
            int target)
    {
        // Targets set to zero are intentionally hidden from the tunnel display.
        if (target <= 0)
        {
            return;
        }

        text.append("<br>")
                .append(name)
                .append("    ")
                .append(current)
                .append("/")
                .append(target)
                .append(completionTick(current, target));
    }

    private static String completionTick(int current, int target)
    {
        return current >= target ? " <col=00ff00>✓</col>" : "";
    }


    private void syncKilledBrothersFromVarbits()
    {
        int currentKills = 0;
        currentKills += client.getVarbitValue(Varbits.BARROWS_KILLED_AHRIM) > 0 ? 1 : 0;
        currentKills += client.getVarbitValue(Varbits.BARROWS_KILLED_DHAROK) > 0 ? 1 : 0;
        currentKills += client.getVarbitValue(Varbits.BARROWS_KILLED_GUTHAN) > 0 ? 1 : 0;
        currentKills += client.getVarbitValue(Varbits.BARROWS_KILLED_KARIL) > 0 ? 1 : 0;
        currentKills += client.getVarbitValue(Varbits.BARROWS_KILLED_TORAG) > 0 ? 1 : 0;
        currentKills += client.getVarbitValue(Varbits.BARROWS_KILLED_VERAC) > 0 ? 1 : 0;

        // The reward widget is the primary reset signal. This is a safety net for
        // a missed widget event or stale persisted state: a completed/active run
        // cannot legitimately go from one-or-more killed brothers back to zero
        // without a new Barrows run beginning.
        if (currentKills == 0
                && ((lastObservedBrotherKills > 0)
                    || (lastObservedBrotherKills == -1 && !killedBrothers.isEmpty())))
        {
            resetMemory();
        }

        syncKilledBrotherFromVarbit(BarrowsBrotherLocationData.AHRIM, Varbits.BARROWS_KILLED_AHRIM);
        syncKilledBrotherFromVarbit(BarrowsBrotherLocationData.DHAROK, Varbits.BARROWS_KILLED_DHAROK);
        syncKilledBrotherFromVarbit(BarrowsBrotherLocationData.GUTHAN, Varbits.BARROWS_KILLED_GUTHAN);
        syncKilledBrotherFromVarbit(BarrowsBrotherLocationData.KARIL, Varbits.BARROWS_KILLED_KARIL);
        syncKilledBrotherFromVarbit(BarrowsBrotherLocationData.TORAG, Varbits.BARROWS_KILLED_TORAG);
        syncKilledBrotherFromVarbit(BarrowsBrotherLocationData.VERAC, Varbits.BARROWS_KILLED_VERAC);
        lastObservedBrotherKills = currentKills;
    }

    private void syncKilledBrotherFromVarbit(BarrowsBrotherLocationData brother, int varbit)
    {
        boolean killed = client.getVarbitValue(varbit) > 0;
        if (killed)
        {
            killedBrothers.add(brother);
        }
        else
        {
            killedBrothers.remove(brother);
        }
    }

    private void rebuildBarrowsDoorsFromScene()
    {
        Player player = client.getLocalPlayer();
        if (player == null)
        {
            return;
        }

        WorldView worldView = player.getWorldView();
        Scene scene = worldView.getScene();
        Tile[][][] tiles = scene.getTiles();
        int plane = worldView.getPlane();
        if (plane < 0 || plane >= tiles.length)
        {
            return;
        }

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
                WallObject wallObject = tile.getWallObject();
                if (wallObject != null && BARROWS_DOOR_IDS.contains(wallObject.getId()))
                {
                    barrowsDoors.add(wallObject);
                }
            }
        }
    }

    Set<WallObject> getBarrowsDoors()
    {
        return barrowsDoors;
    }

    boolean isInTunnelMaze()
    {
        return isInBarrowsTunnel();
    }

    int getBloodwormKills()
    {
        return bloodwormKills;
    }

    int getCryptRatKills()
    {
        return cryptRatKills;
    }

    int getGiantCryptRatKills()
    {
        return giantCryptRatKills;
    }

    int getCryptSpiderKills()
    {
        return cryptSpiderKills;
    }

    int getGiantCryptSpiderKills()
    {
        return giantCryptSpiderKills;
    }

    int getSkeletonKills()
    {
        return skeletonKills;
    }

    double getRewardPotentialPercent()
    {
        return client.getVarbitValue(net.runelite.api.Varbits.BARROWS_REWARD_POTENTIAL) / 10.0;
    }


    private boolean isHiddenTunnelDialogueVisible()
    {
        // Detect the first hidden-tunnel message ("Click here to continue") so
        // the tunnel marker updates immediately, before the later Yes/No screen.
        Widget dialogueText = client.getWidget(InterfaceID.Objectbox.TEXT);
        return dialogueText != null
                && !dialogueText.isHidden()
                && widgetTreeContainsExactText(dialogueText, HIDDEN_TUNNEL_PROMPT);
    }

    private static boolean widgetTreeContainsExactText(Widget root, String expectedText)
    {
        if (widgetHasExactText(root, expectedText))
        {
            return true;
        }

        if (childrenContainExactText(root.getDynamicChildren(), expectedText))
        {
            return true;
        }

        if (childrenContainExactText(root.getStaticChildren(), expectedText))
        {
            return true;
        }

        return childrenContainExactText(root.getNestedChildren(), expectedText);
    }

    private static boolean childrenContainExactText(Widget[] children, String expectedText)
    {
        if (children == null)
        {
            return false;
        }

        for (Widget child : children)
        {
            if (child != null && widgetTreeContainsExactText(child, expectedText))
            {
                return true;
            }
        }

        return false;
    }

    private static boolean widgetHasExactText(Widget widget, String expectedText)
    {
        String text = widget.getText();
        if (text == null)
        {
            return false;
        }

        return expectedText.equals(Text.removeTags(text).trim());
    }
}