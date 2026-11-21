package me.vcb.astrocore.factions.base;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.elevator.ElevatorManager;
import me.vcb.astrocore.factions.FactionsManager;
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
import org.bukkit.block.BlockFace;
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
 * /f skybase — an aerial base 25 blocks up: glass walls + wool floor/roof/edges,
 * a central elevator down to a 4x4x3 ground enclosure (oak-fence-gate door).
 * Minimum footprint 15x15, selectable height (min 5).
 */
public class SkyBaseManager implements Listener {

    public static final int MIN_SIZE   = 15;
    public static final int MAX_SIZE   = 40;
    public static final int MIN_HEIGHT = 5;
    public static final int MAX_HEIGHT = 15;
    private static final int SKY_OFFSET = 25;
    private static final int BATCH = 90;

    private static final String MENU_COLOR  = "§b§lSkyBase §8» §7Color";
    private static final String MENU_HEIGHT = "§b§lSkyBase §8» §7Altura";

    private static SkyBaseManager instance;

    private final ItemStack wand;
    private final Map<UUID, DyeColor> pendingColor = new HashMap<>();
    private final Map<UUID, Session> sessions = new HashMap<>();

    public SkyBaseManager() {
        instance = this;
        this.wand = new ItemBuilder(Material.NETHERITE_AXE)
            .setName(Color.translate("&b&lSkyBase Wand"))
            .setLore(List.of(
                Color.translate("&7Click &aizquierdo &7= esquina 1"),
                Color.translate("&7Click &aderecho &7= esquina 2"),
                Color.translate("&7Luego escribí &aconfirmar &7en el chat")
            )).addFakeGlow().build();
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    public static SkyBaseManager getInstance() { return instance; }

    private static final class Session {
        DyeColor color;
        int height;
        Location posOne, posTwo;
        boolean awaitingConfirm;
    }

    // ── Menus ──────────────────────────────────────────────────────────────

    public void openMenu(Player player) {
        if (me.vcb.astrocore.perks.PerksMenu.isDisabled(me.vcb.astrocore.perks.PerksMenu.Perk.SKYBASE_TOKEN)) {
            player.sendMessage(Color.translate("&cEste comando está deshabilitado durante este mapa."));
            return;
        }
        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);
        if (faction == null) { player.sendMessage(Color.translate("&cNo estás en una facción.")); return; }
        if (!faction.getMember(player).getRole().isAtLeast(Role.CO_LEADER)) {
            player.sendMessage(Color.translate("&cNecesitás ser Co-Líder o superior.")); return;
        }
        if (faction.getClaims().isEmpty()) {
            player.sendMessage(Color.translate("&cTu facción no tiene claim. Claimea terreno primero.")); return;
        }
        me.vcb.astrocore.userdata.Userdata ud =
            AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null || ud.getSkyBaseTokens() <= 0) {
            player.sendMessage(Color.translate("&cNecesitás un &fToken de SkyBase&c. Comprá uno en &f/perks&c."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, Color.translate(MENU_COLOR));
        ItemStack filler = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName("§r").build();
        for (int i = 0; i < 27; i++) inv.setItem(i, filler);
        int slot = 0;
        for (DyeColor dye : DyeColor.values()) {
            Material wool = Material.matchMaterial(dye.name() + "_WOOL");
            if (wool == null) continue;
            inv.setItem(slot++, new ItemBuilder(wool)
                .setName(Color.translate("&f" + capitalize(dye.name())))
                .setLore(List.of(Color.translate("&eClick para elegir este color"))).build());
        }
        player.openInventory(inv);
    }

    private void openHeightMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Color.translate(MENU_HEIGHT));
        ItemStack filler = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName("§r").build();
        for (int i = 0; i < 27; i++) inv.setItem(i, filler);
        int slot = 9 + Math.max(0, (9 - (MAX_HEIGHT - MIN_HEIGHT + 1)) / 2);
        for (int h = MIN_HEIGHT; h <= MAX_HEIGHT; h++) {
            inv.setItem(slot++, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                .setAmount(h)
                .setName(Color.translate("&a&lAltura: " + h))
                .setLore(List.of(Color.translate("&eClick para construir con esta altura"))).build());
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();

        if (title.equals(Color.translate(MENU_COLOR))) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.getType().name().endsWith("_WOOL")) return;
            DyeColor dye = colorOf(clicked.getType());
            if (dye == null) return;
            this.pendingColor.put(player.getUniqueId(), dye);
            openHeightMenu(player);
            return;
        }
        if (title.equals(Color.translate(MENU_HEIGHT))) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() != Material.LIME_STAINED_GLASS_PANE) return;
            int height = Math.max(MIN_HEIGHT, Math.min(MAX_HEIGHT, clicked.getAmount()));
            DyeColor dye = this.pendingColor.remove(player.getUniqueId());
            if (dye == null) { player.closeInventory(); return; }
            startSelection(player, dye, height);
        }
    }

    // ── Selection ──────────────────────────────────────────────────────────

    private void startSelection(Player player, DyeColor color, int height) {
        player.closeInventory();
        Session session = new Session();
        session.color = color;
        session.height = height;
        this.sessions.put(player.getUniqueId(), session);
        if (!player.getInventory().contains(this.wand)) player.getInventory().addItem(this.wand.clone());
        clearPillars(player);
        player.sendMessage(Color.translate("&b&lSkyBase &7» &fColor: &e" + capitalize(color.name())
            + " &7| Altura: &e" + height + " &7| &fa +25 de altura"));
        player.sendMessage(Color.translate("&7Click &aizq &7= esquina 1, click &ader &7= esquina 2 &7(mín " + MIN_SIZE + "x" + MIN_SIZE + ")."));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Session session = this.sessions.get(player.getUniqueId());
        if (session == null) return;
        ItemStack hand = event.getItem();
        if (hand == null || !hand.isSimilar(this.wand)) return;

        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
            if (action == Action.LEFT_CLICK_AIR || action == Action.RIGHT_CLICK_AIR) event.setCancelled(true);
            return;
        }
        event.setCancelled(true);

        Location loc = event.getClickedBlock().getLocation();
        boolean left = action == Action.LEFT_CLICK_BLOCK;
        if (left) session.posOne = loc; else session.posTwo = loc;
        session.awaitingConfirm = false;

        player.sendMessage(Color.translate("&aEsquina " + (left ? "1" : "2") + " marcada: &f"
            + loc.getBlockX() + ", " + loc.getBlockZ()));
        redrawPillars(player, session);

        if (session.posOne != null && session.posTwo != null) {
            int sx = Math.abs(session.posOne.getBlockX() - session.posTwo.getBlockX()) + 1;
            int sz = Math.abs(session.posOne.getBlockZ() - session.posTwo.getBlockZ()) + 1;
            if (sx < MIN_SIZE || sz < MIN_SIZE) {
                player.sendMessage(Color.translate("&cMínimo " + MIN_SIZE + "x" + MIN_SIZE + " (es " + sx + "x" + sz + ").")); return;
            }
            if (sx > MAX_SIZE || sz > MAX_SIZE) {
                player.sendMessage(Color.translate("&cMáximo " + MAX_SIZE + "x" + MAX_SIZE + ".")); return;
            }
            session.awaitingConfirm = true;
            player.sendMessage(Color.translate("&eSelección: &f" + sx + "x" + sz
                + "&e. Escribí &aconfirmar &een el chat, o &ccancelar&e."));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Session session = this.sessions.get(player.getUniqueId());
        if (session == null || !session.awaitingConfirm) return;
        String msg = event.getMessage().trim().toLowerCase();
        if (msg.equals("confirmar") || msg.equals("cancelar")) {
            event.setCancelled(true);
            if (msg.equals("cancelar")) { Tasks.sync(() -> cancel(player)); player.sendMessage(Color.translate("&cSkyBase cancelada.")); return; }
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
            player.sendMessage(Color.translate("&cLas esquinas deben estar en el mismo mundo.")); return;
        }
        World world = a.getWorld();
        int minX = Math.min(a.getBlockX(), b.getBlockX()), maxX = Math.max(a.getBlockX(), b.getBlockX());
        int minZ = Math.min(a.getBlockZ(), b.getBlockZ()), maxZ = Math.max(a.getBlockZ(), b.getBlockZ());
        if ((maxX - minX + 1) < MIN_SIZE || (maxZ - minZ + 1) < MIN_SIZE) {
            player.sendMessage(Color.translate("&cMínimo " + MIN_SIZE + "x" + MIN_SIZE + ".")); return;
        }
        if ((maxX - minX + 1) > MAX_SIZE || (maxZ - minZ + 1) > MAX_SIZE) {
            player.sendMessage(Color.translate("&cMáximo " + MAX_SIZE + "x" + MAX_SIZE + ".")); return;
        }

        // Whole footprint must be inside the faction's own claim (strict: must be an
        // actual claim owned by this faction, not warzone/wilderness).
        for (int x = minX; x <= maxX; x++)
            for (int z = minZ; z <= maxZ; z++) {
                me.vcb.astrocore.factions.claim.Claim c =
                    me.vcb.astrocore.factions.claim.ClaimManager.getInstance().getClaimAt(new Location(world, x, 64, z));
                if (c == null || c.getOwner() == null || !faction.getId().equals(c.getOwner().getId())) {
                    player.sendMessage(Color.translate("&cToda la skybase debe estar dentro de tu claim (&f" + x + ", " + z + " &cestá fuera)."));
                    return;
                }
            }

        me.vcb.astrocore.userdata.Userdata ud =
            AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null || ud.getSkyBaseTokens() <= 0) {
            player.sendMessage(Color.translate("&cYa no tenés Tokens de SkyBase. Comprá en &f/perks&c.")); cancel(player); return;
        }

        Material wool  = Material.matchMaterial(session.color.name() + "_WOOL");
        Material glass = Material.matchMaterial(session.color.name() + "_STAINED_GLASS");
        if (wool == null || glass == null) { cancel(player); return; }
        ud.setSkyBaseTokens(ud.getSkyBaseTokens() - 1);

        int baseY = Math.min(a.getBlockY(), b.getBlockY());
        int skyY  = Math.min(world.getMaxHeight() - session.height - 1, baseY + SKY_OFFSET);
        int topY  = skyY + session.height - 1;

        int ex = (minX + maxX) / 2;   // elevator column (footprint centre)
        int ez = (minZ + maxZ) / 2;

        List<Placement> queue = new ArrayList<>();

        // ── Ground enclosure: 5x5, walls 3 tall, door of fence gates on all 4 walls. ──
        int gMinX = ex - 2, gMaxX = ex + 2, gMinZ = ez - 2, gMaxZ = ez + 2;
        // floor
        for (int x = gMinX; x <= gMaxX; x++)
            for (int z = gMinZ; z <= gMaxZ; z++)
                queue.add(new Placement(x, baseY, z, wool, null));
        // walls baseY+1..baseY+3
        for (int dy = 1; dy <= 3; dy++) {
            int y = baseY + dy;
            for (int x = gMinX; x <= gMaxX; x++)
                for (int z = gMinZ; z <= gMaxZ; z++) {
                    boolean perimeter = x == gMinX || x == gMaxX || z == gMinZ || z == gMaxZ;
                    if (!perimeter) { queue.add(new Placement(x, y, z, Material.AIR, null)); continue; }

                    // Gate door: the central 3 columns of each wall, on the lower 2 rows.
                    BlockFace gate = gateFacing(x, z, dy, gMinX, gMaxX, gMinZ, gMaxZ, ex, ez);
                    if (gate != null) queue.add(new Placement(x, y, z, Material.OAK_FENCE_GATE, gate));
                    else queue.add(new Placement(x, y, z, wool, null));
                }
        }
        // roof
        for (int x = gMinX; x <= gMaxX; x++)
            for (int z = gMinZ; z <= gMaxZ; z++)
                queue.add(new Placement(x, baseY + 4, z, wool, null));

        // ── SkyBase box at skyY. ──
        for (int y = skyY; y <= topY; y++) {
            for (int x = minX; x <= maxX; x++)
                for (int z = minZ; z <= maxZ; z++) {
                    boolean perimeter = x == minX || x == maxX || z == minZ || z == maxZ;
                    boolean floorOrRoof = (y == skyY || y == topY);
                    Material material;
                    if (floorOrRoof) material = wool;         // floor + roof = wool
                    else if (perimeter) {
                        boolean edge = (x == minX || x == maxX) && (z == minZ || z == maxZ);
                        material = edge ? wool : glass;       // wool edges, glass walls
                    } else material = Material.AIR;
                    queue.add(new Placement(x, y, z, material, null));
                }
        }

        clearPillars(player);
        player.getInventory().remove(this.wand);
        this.sessions.remove(player.getUniqueId());

        final int gx = ex, gz = ez, groundY = baseY, sky = skyY;
        player.sendMessage(Color.translate("&b&lSkyBase &7» &fConstruyendo a +25 (&r" + capitalize(session.color.name()) + "&f, altura " + session.height + ")..."));
        new BuildTask(world, queue, player.getUniqueId(), () -> {
            // Place + register both elevators (same column) once built.
            ElevatorManager em = ElevatorManager.getInstance();
            if (em != null) {
                em.registerElevator(new Location(world, gx, groundY, gz));
                em.registerElevator(new Location(world, gx, sky, gz));
            }
        }).runTaskTimer(AstroCore.getInstance(), 1L, 1L);
    }

    // ── Pillars / cleanup (same as base) ──────────────────────────────────────

    private void redrawPillars(Player player, Session session) {
        clearPillars(player);
        if (session.posOne != null) sendPillar(player, session.posOne);
        if (session.posTwo != null) sendPillar(player, session.posTwo);
    }

    private void sendPillar(Player player, Location clicked) {
        int top = player.getLocation().getBlockY() + 60;
        for (int i = clicked.getWorld().getMinHeight(); i < top; i++) {
            Location loc = clicked.clone(); loc.setY(i);
            AstroCore.getInstance().getGlassManager().generateGlassVisual(player,
                new GlassInfo(GlassType.CLAIM_SELECTION, loc, i % 4 == 0 ? Material.DIAMOND_BLOCK : Material.LIME_STAINED_GLASS, (byte) 5));
        }
    }

    private void clearPillars(Player player) {
        AstroCore.getInstance().getGlassManager().clearGlassVisuals(player, GlassType.CLAIM_SELECTION);
    }

    public void cancel(Player player) {
        if (this.sessions.remove(player.getUniqueId()) != null) {
            player.getInventory().remove(this.wand);
            clearPillars(player);
        }
        this.pendingColor.remove(player.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.sessions.remove(event.getPlayer().getUniqueId());
        this.pendingColor.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Returns the gate facing if (x,z,dy) is a door slot — the central 3 columns
     * of a wall, on the lower 2 rows (dy 1-2) — or null otherwise.
     */
    private static BlockFace gateFacing(int x, int z, int dy,
                                        int gMinX, int gMaxX, int gMinZ, int gMaxZ, int ex, int ez) {
        if (dy > 2) return null; // top row stays solid; gates only on the lower 2 rows
        boolean centralX = (x >= ex - 1 && x <= ex + 1);
        boolean centralZ = (z >= ez - 1 && z <= ez + 1);
        if (x == gMaxX && centralZ) return BlockFace.EAST;
        if (x == gMinX && centralZ) return BlockFace.WEST;
        if (z == gMaxZ && centralX) return BlockFace.SOUTH;
        if (z == gMinZ && centralX) return BlockFace.NORTH;
        return null;
    }

    private static DyeColor colorOf(Material wool) {
        String n = wool.name();
        if (!n.endsWith("_WOOL")) return null;
        try { return DyeColor.valueOf(n.substring(0, n.length() - "_WOOL".length())); }
        catch (IllegalArgumentException e) { return null; }
    }

    private static String capitalize(String enumName) {
        String s = enumName.toLowerCase().replace('_', ' ');
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private record Placement(int x, int y, int z, Material material, BlockFace facing) {}

    private final class BuildTask extends BukkitRunnable {
        private final World world;
        private final List<Placement> queue;
        private final UUID owner;
        private final Runnable onDone;
        private int index;

        BuildTask(World world, List<Placement> queue, UUID owner, Runnable onDone) {
            this.world = world; this.queue = queue; this.owner = owner; this.onDone = onDone;
        }

        @Override
        public void run() {
            int ops = 0;
            while (ops < BATCH && this.index < this.queue.size()) {
                Placement p = this.queue.get(this.index++);
                ops++;
                try {
                    if (p.y() < this.world.getMinHeight() || p.y() >= this.world.getMaxHeight()) continue;
                    Block block = this.world.getBlockAt(p.x(), p.y(), p.z());
                    if (block.getType() == p.material() && p.facing() == null) continue;

                    if (p.facing() != null) {
                        org.bukkit.block.data.BlockData bd = p.material().createBlockData();
                        if (bd instanceof org.bukkit.block.data.Directional dir) dir.setFacing(p.facing());
                        block.setBlockData(bd, false);
                    } else {
                        block.setType(p.material(), false);
                    }
                    this.world.playEffect(new Location(this.world, p.x() + 0.5, p.y(), p.z() + 0.5),
                        org.bukkit.Effect.STEP_SOUND, p.material() == Material.AIR ? Material.WHITE_WOOL : p.material());
                } catch (Throwable ignored) {
                    // never let one bad block abort the whole build (roof, skybase, etc.)
                }
            }
            if (this.index >= this.queue.size()) {
                if (this.onDone != null) this.onDone.run();
                Player pl = Bukkit.getPlayer(this.owner);
                if (pl != null) {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
                    pl.sendMessage(Color.translate("&a&l✔ &aSkyBase construida. &7Parate en el elevador del centro."));
                }
                this.cancel();
            }
        }
    }
}
