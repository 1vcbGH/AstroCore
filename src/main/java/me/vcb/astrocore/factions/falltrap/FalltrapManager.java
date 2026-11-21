package me.vcb.astrocore.factions.falltrap;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.factions.Faction;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.factions.enums.Role;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.glass.GlassInfo;
import me.vcb.astrocore.glass.GlassManager.GlassType;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Tasks;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Falltrap creator: GUI to pick a tier + wall material, claim-style pillar
 * selection of two corners, chat confirmation, and a gradual void-clearing build
 * lined with the chosen wall blocks. Restricted to the faction's own claim and
 * capped at 40x40.
 */
public class FalltrapManager implements Listener {

    public static final int  MAX_SIZE   = 40;
    // Blocks changed per tick — kept low so the trap visibly digs down gradually.
    private static final int  BATCH      = 220;

    private static final String MENU_MAIN  = "§4§lFalltrap";
    private static final String MENU_TIER2 = "§4§lFalltrap §7» Tier II";
    private static final String MENU_TIER3 = "§4§lFalltrap §7» Tier III";

    private static FalltrapManager instance;

    private final ItemStack wand;
    private final Map<UUID, Session> sessions = new HashMap<>();
    private final java.util.Set<UUID> building = new java.util.HashSet<>();

    public FalltrapManager() {
        instance = this;
        this.wand = new ItemBuilder(Material.GOLDEN_AXE)
            .setName(Color.translate("&6&lFalltrap Wand"))
            .setLore(List.of(
                Color.translate("&7Click &aizquierdo &7= esquina 1"),
                Color.translate("&7Click &aderecho &7= esquina 2"),
                Color.translate("&7Luego escribí &aconfirmar &7en el chat")
            )).addFakeGlow().build();
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    public static FalltrapManager getInstance() { return instance; }

    private static final class Session {
        Material wall;
        Location posOne, posTwo;
        boolean awaitingConfirm;
    }

    // ── Entry ────────────────────────────────────────────────────────────────

    /** Opens the tier-selection menu (called by /f falltrap). */
    public void openMenu(Player player) {
        if (me.vcb.astrocore.perks.PerksMenu.isDisabled(me.vcb.astrocore.perks.PerksMenu.Perk.FALLTRAP_TOKEN)) {
            player.sendMessage(Color.translate("&cEste comando está deshabilitado durante este mapa."));
            return;
        }
        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);
        if (faction == null) {
            player.sendMessage(Color.translate("&cNo estás en una facción."));
            return;
        }
        if (!faction.getMember(player).getRole().isAtLeast(Role.CO_LEADER)) {
            player.sendMessage(Color.translate("&cNecesitás ser Co-Líder o superior."));
            return;
        }
        if (faction.getClaims().isEmpty()) {
            player.sendMessage(Color.translate("&cTu facción no tiene claim. Claimea terreno primero."));
            return;
        }

        me.vcb.astrocore.userdata.Userdata ud =
            AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null || ud.getFalltrapTokens() <= 0) {
            player.sendMessage(Color.translate("&cNecesitás un &fToken de Falltrap&c. Comprá uno en &f/perks&c."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, Color.translate(MENU_MAIN));
        ItemStack border = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName("§r").build();
        for (int i = 0; i < 27; i++) inv.setItem(i, border);

        inv.setItem(11, new ItemBuilder(Material.CRAFTING_TABLE)
            .setName(Color.translate("&a&lFalltrap Default"))
            .setLore(List.of(
                Color.translate("&7Paredes de &fmesas de crafteo &7(estándar)."),
                Color.translate("&7Sin bordes personalizados."),
                Color.translate("&8──────────────"),
                Color.translate("&eClick para usar")
            )).build());

        inv.setItem(13, new ItemBuilder(Material.FURNACE)
            .setName(Color.translate("&6&lFalltrap Tier II"))
            .setLore(List.of(
                Color.translate("&7Elegí entre &fhornos &7y &fmesas de crafteo&7."),
                Color.translate("&8──────────────"),
                Color.translate("&eClick para elegir")
            )).build());

        inv.setItem(15, new ItemBuilder(Material.RED_WOOL)
            .setName(Color.translate("&c&lFalltrap Tier III"))
            .setLore(List.of(
                Color.translate("&7Elegí entre &ftodos los colores de lana&7."),
                Color.translate("&8──────────────"),
                Color.translate("&eClick para elegir")
            )).build());

        player.openInventory(inv);
    }

    private void openTier2(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Color.translate(MENU_TIER2));
        ItemStack border = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName("§r").build();
        for (int i = 0; i < 27; i++) inv.setItem(i, border);

        inv.setItem(12, new ItemBuilder(Material.CRAFTING_TABLE)
            .setName(Color.translate("&6&lMesas de Crafteo"))
            .setLore(List.of(Color.translate("&eClick para usar"))).build());
        inv.setItem(14, new ItemBuilder(Material.FURNACE)
            .setName(Color.translate("&6&lHornos"))
            .setLore(List.of(Color.translate("&eClick para usar"))).build());
        player.openInventory(inv);
    }

    private void openTier3(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Color.translate(MENU_TIER3));
        ItemStack border = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName("§r").build();
        for (int i = 0; i < 27; i++) inv.setItem(i, border);

        int slot = 0;
        for (DyeColor dye : DyeColor.values()) {
            Material wool = Material.matchMaterial(dye.name() + "_WOOL");
            if (wool == null) continue;
            inv.setItem(slot++, new ItemBuilder(wool)
                .setName(Color.translate("&f" + capitalize(dye.name())))
                .setLore(List.of(Color.translate("&eClick para usar"))).build());
        }
        player.openInventory(inv);
    }

    // ── Menu clicks ────────────────────────────────────────────────────────────

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();

        if (title.equals(Color.translate(MENU_MAIN))) {
            event.setCancelled(true);
            switch (event.getRawSlot()) {
                case 11 -> startSelection(player, Material.CRAFTING_TABLE);
                case 13 -> openTier2(player);
                case 15 -> openTier3(player);
            }
            return;
        }

        if (title.equals(Color.translate(MENU_TIER2))) {
            event.setCancelled(true);
            if (event.getRawSlot() == 12) startSelection(player, Material.CRAFTING_TABLE);
            else if (event.getRawSlot() == 14) startSelection(player, Material.FURNACE);
            return;
        }

        if (title.equals(Color.translate(MENU_TIER3))) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType().name().endsWith("_WOOL")) {
                startSelection(player, clicked.getType());
            }
        }
    }

    // ── Selection start ──────────────────────────────────────────────────────

    private void startSelection(Player player, Material wall) {
        player.closeInventory();

        Session session = new Session();
        session.wall = wall;
        this.sessions.put(player.getUniqueId(), session);

        if (!player.getInventory().contains(this.wand)) {
            player.getInventory().addItem(this.wand.clone());
        }
        clearPillars(player);

        player.sendMessage(Color.translate("&6&lFalltrap &7» &fPared: &e" + capitalize(wall.name())));
        player.sendMessage(Color.translate("&7Click &aizq &7= esquina 1, click &ader &7= esquina 2 &7(dentro de tu claim)."));
    }

    // ── Wand interaction ──────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Session session = this.sessions.get(player.getUniqueId());
        if (session == null) return;

        ItemStack hand = event.getItem();
        if (hand == null || !hand.isSimilar(this.wand)) return;

        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
            if (action == Action.RIGHT_CLICK_AIR || action == Action.LEFT_CLICK_AIR) event.setCancelled(true);
            return;
        }
        event.setCancelled(true);

        Location loc = event.getClickedBlock().getLocation();
        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);
        if (faction == null) { cancel(player); return; }

        Faction at = ClaimManager.getInstance().getFactionAt(loc);
        if (at == null || !at.equals(faction)) {
            player.sendMessage(Color.translate("&cEsa esquina no está dentro del claim de tu facción."));
            return;
        }

        boolean left = action == Action.LEFT_CLICK_BLOCK;
        if (left) session.posOne = loc; else session.posTwo = loc;
        session.awaitingConfirm = false;

        player.sendMessage(Color.translate("&aEsquina " + (left ? "1" : "2") + " marcada: &f"
            + loc.getBlockX() + ", " + loc.getBlockZ()));

        redrawPillars(player, session);

        if (session.posOne != null && session.posTwo != null) {
            int sx = Math.abs(session.posOne.getBlockX() - session.posTwo.getBlockX()) + 1;
            int sz = Math.abs(session.posOne.getBlockZ() - session.posTwo.getBlockZ()) + 1;
            if (sx > MAX_SIZE || sz > MAX_SIZE) {
                player.sendMessage(Color.translate("&cLa selección es " + sx + "x" + sz
                    + ". Máximo " + MAX_SIZE + "x" + MAX_SIZE + "."));
                return;
            }
            session.awaitingConfirm = true;
            player.sendMessage(Color.translate("&eSelección: &f" + sx + "x" + sz
                + "&e. Escribí &aconfirmar &een el chat para construir, o &ccancelar&e."));
        }
    }

    // ── Chat confirm ────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Session session = this.sessions.get(player.getUniqueId());
        if (session == null || !session.awaitingConfirm) return;

        String msg = event.getMessage().trim().toLowerCase();
        if (msg.equals("confirmar") || msg.equals("cancelar")) {
            event.setCancelled(true);
            if (msg.equals("cancelar")) {
                Tasks.sync(() -> cancel(player));
                player.sendMessage(Color.translate("&cFalltrap cancelado."));
                return;
            }
            Tasks.sync(() -> confirmBuild(player));
        }
    }

    private void confirmBuild(Player player) {
        Session session = this.sessions.get(player.getUniqueId());
        if (session == null || session.posOne == null || session.posTwo == null) return;

        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);
        if (faction == null) { cancel(player); return; }

        Location a = session.posOne, b = session.posTwo;
        if (a.getWorld() == null || !a.getWorld().equals(b.getWorld())) {
            player.sendMessage(Color.translate("&cLas esquinas deben estar en el mismo mundo."));
            return;
        }

        World world = a.getWorld();
        int minX = Math.min(a.getBlockX(), b.getBlockX());
        int maxX = Math.max(a.getBlockX(), b.getBlockX());
        int minZ = Math.min(a.getBlockZ(), b.getBlockZ());
        int maxZ = Math.max(a.getBlockZ(), b.getBlockZ());

        if ((maxX - minX + 1) > MAX_SIZE || (maxZ - minZ + 1) > MAX_SIZE) {
            player.sendMessage(Color.translate("&cMáximo " + MAX_SIZE + "x" + MAX_SIZE + "."));
            return;
        }

        // Whole region must be inside the faction's own claim.
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                Faction at = ClaimManager.getInstance().getFactionAt(new Location(world, x, 64, z));
                if (at == null || !at.equals(faction)) {
                    player.sendMessage(Color.translate("&cToda la falltrap debe estar dentro de tu claim "
                        + "(&f" + x + ", " + z + " &cestá fuera)."));
                    return;
                }
            }
        }

        // Consume a falltrap token (re-check in case it changed during selection).
        me.vcb.astrocore.userdata.Userdata ud =
            AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null || ud.getFalltrapTokens() <= 0) {
            player.sendMessage(Color.translate("&cYa no tenés Tokens de Falltrap. Comprá en &f/perks&c."));
            cancel(player);
            return;
        }
        ud.setFalltrapTokens(ud.getFalltrapTokens() - 1);

        Material wall = session.wall;
        int topY    = Math.max(a.getBlockY(), b.getBlockY());
        int bottomY = world.getMinHeight();       // flat bedrock layer
        // Never build above the selection — only replace existing blocks downward.
        int wallTop = topY;

        // End the selection session.
        clearPillars(player);
        player.getInventory().remove(this.wand);
        this.sessions.remove(player.getUniqueId());
        this.building.add(player.getUniqueId());

        player.sendMessage(Color.translate("&aConstruyendo falltrap &7(pared: &f"
            + capitalize(wall.name()) + "&7)&a..."));
        new BuildTask(player.getUniqueId(), world, minX, maxX, minZ, maxZ, topY, wallTop, bottomY, wall)
            .runTaskTimer(AstroCore.getInstance(), 1L, 1L);
    }

    // ── Pillars ──────────────────────────────────────────────────────────────

    private void redrawPillars(Player player, Session session) {
        clearPillars(player);
        if (session.posOne != null) sendPillar(player, session.posOne);
        if (session.posTwo != null) sendPillar(player, session.posTwo);
    }

    private void sendPillar(Player player, Location clicked) {
        int top = player.getLocation().getBlockY() + 60;
        for (int i = clicked.getWorld().getMinHeight(); i < top; i++) {
            Location loc = clicked.clone();
            loc.setY(i);
            AstroCore.getInstance().getGlassManager().generateGlassVisual(player,
                new GlassInfo(GlassType.CLAIM_SELECTION, loc,
                    i % 4 == 0 ? Material.DIAMOND_BLOCK : Material.LIME_STAINED_GLASS, (byte) 5));
        }
    }

    private void clearPillars(Player player) {
        // Synchronous: clearing then re-drawing in the same tick avoids the race
        // where an async clear wipes pillars that were just drawn.
        AstroCore.getInstance().getGlassManager()
            .clearGlassVisuals(player, GlassType.CLAIM_SELECTION);
    }

    // ── Cleanup ────────────────────────────────────────────────────────────────

    public void cancel(Player player) {
        if (this.sessions.remove(player.getUniqueId()) != null) {
            player.getInventory().remove(this.wand);
            clearPillars(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.sessions.remove(event.getPlayer().getUniqueId());
        this.building.remove(event.getPlayer().getUniqueId());
    }

    private static String capitalize(String enumName) {
        String s = enumName.toLowerCase().replace('_', ' ');
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ── Build task ────────────────────────────────────────────────────────────

    private final class BuildTask extends BukkitRunnable {
        private final UUID owner;
        private final World world;
        private final int minX, maxX, minZ, maxZ, topY, wallTop, bottomY;
        private final Material wall;

        private final long total;
        private long done;
        private int lastReported = -1;
        private int x, z, y;

        BuildTask(UUID owner, World world, int minX, int maxX, int minZ, int maxZ,
                  int topY, int wallTop, int bottomY, Material wall) {
            this.owner = owner;
            this.world = world;
            this.minX = minX; this.maxX = maxX;
            this.minZ = minZ; this.maxZ = maxZ;
            this.topY = topY; this.wallTop = wallTop; this.bottomY = bottomY;
            this.wall = wall;
            // Layer-major: fill/clear a whole Y layer before moving down, so the
            // trap visibly sinks from the top toward the void.
            this.y = wallTop; this.x = minX; this.z = minZ;

            int width = maxX - minX + 1, depth = maxZ - minZ + 1;
            int perimeterCols = (width * depth) - Math.max(0, (width - 2) * (depth - 2));
            int interiorCols  = (width * depth) - perimeterCols;
            this.total = (long) perimeterCols * (wallTop - bottomY + 1)
                + (long) interiorCols * (topY - bottomY + 1);
        }

        @Override
        public void run() {
            int ops = 0;
            Material lastMat = null;
            int lx = 0, ly = 0, lz = 0;

            while (ops < BATCH) {
                if (this.y < this.bottomY) {
                    Player p = Bukkit.getPlayer(this.owner);
                    if (p != null) {
                        p.sendMessage(Color.translate("&a&l✔ &aFalltrap construido."));
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
                    }
                    FalltrapManager.this.building.remove(this.owner);
                    this.cancel();
                    return;
                }

                boolean perimeter = this.x == this.minX || this.x == this.maxX
                    || this.z == this.minZ || this.z == this.maxZ;

                int floorY = this.bottomY + 1; // material floor sits on the flat bedrock

                if (this.y == this.bottomY) {
                    // Flat bedrock floor (always, even over caves).
                    if (set(this.x, this.y, this.z, Material.BEDROCK)) {
                        lastMat = Material.BEDROCK; lx = this.x; ly = this.y; lz = this.z;
                    }
                    ops++; this.done++;
                } else if (this.y == floorY) {
                    // Requested material laid on top of the bedrock.
                    if (set(this.x, this.y, this.z, this.wall)) {
                        lastMat = this.wall; lx = this.x; ly = this.y; lz = this.z;
                    }
                    ops++; this.done++;
                } else if (perimeter) {
                    // Walls: fill solid down to the floor (caves included), but never
                    // above the selection (wallTop == topY), so no towers form.
                    if (set(this.x, this.y, this.z, this.wall)) {
                        lastMat = this.wall; lx = this.x; ly = this.y; lz = this.z;
                    }
                    ops++; this.done++;
                } else if (this.y <= this.topY) {
                    // Interior: hollow.
                    if (set(this.x, this.y, this.z, Material.AIR)) {
                        lastMat = this.wall; lx = this.x; ly = this.y; lz = this.z;
                    }
                    ops++; this.done++;
                }

                // advance across the current layer, then drop to the next
                this.z++;
                if (this.z > this.maxZ) {
                    this.z = this.minZ;
                    this.x++;
                    if (this.x > this.maxX) {
                        this.x = this.minX;
                        this.y--;
                    }
                }
            }

            if (lastMat != null) {
                this.world.playEffect(new Location(this.world, lx + 0.5, ly, lz + 0.5),
                    org.bukkit.Effect.STEP_SOUND, lastMat);
            }
            if (this.total > 0) {
                int pct = (int) Math.min(100, (this.done * 100) / this.total);
                if (pct != this.lastReported && pct % 25 == 0) {
                    this.lastReported = pct;
                    Player p = Bukkit.getPlayer(this.owner);
                    if (p != null) p.sendMessage(Color.translate("&6Falltrap&7: &e" + pct + "%"));
                }
            }
        }

        private boolean set(int x, int y, int z, Material material) {
            Block block = this.world.getBlockAt(x, y, z);
            if (block.getType() == material) return false;
            block.setType(material, false);
            return true;
        }
    }
}
