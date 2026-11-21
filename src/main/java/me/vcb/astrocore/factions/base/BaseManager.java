package me.vcb.astrocore.factions.base;

import me.vcb.astrocore.AstroCore;
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


public class BaseManager implements Listener {

    public static final int MAX_SIZE   = 40;
    public static final int MAX_HEIGHT = 10;
    private static final int BATCH     = 60;

    private static final String MENU_COLOR  = "§a§lBase §8» §7Color";
    private static final String MENU_HEIGHT = "§a§lBase §8» §7Altura";

    private static BaseManager instance;

    private final ItemStack wand;
    private final Map<UUID, DyeColor> pendingColor = new HashMap<>();
    private final Map<UUID, Session> sessions = new HashMap<>();

    public BaseManager() {
        instance = this;
        this.wand = new ItemBuilder(Material.IRON_AXE)
            .setName(Color.translate("&a&lBase Wand"))
            .setLore(List.of(
                Color.translate("&7Click &aizquierdo &7= esquina 1"),
                Color.translate("&7Click &aderecho &7= esquina 2"),
                Color.translate("&7Luego escribí &aconfirmar &7en el chat")
            )).addFakeGlow().build();
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    public static BaseManager getInstance() { return instance; }

    private static final class Session {
        DyeColor color;
        int height;
        Location posOne, posTwo;
        boolean awaitingConfirm;
    }

    public void openMenu(Player player) {
        if (me.vcb.astrocore.perks.PerksMenu.isDisabled(me.vcb.astrocore.perks.PerksMenu.Perk.BASE_TOKEN)) {
            player.sendMessage(Color.translate("&cEste comando está deshabilitado durante este mapa."));
            return;
        }
        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);
        if (faction == null) { player.sendMessage(Color.translate("&cNo estás en una facción.")); return; }
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
        if (ud == null || ud.getBaseTokens() <= 0) {
            player.sendMessage(Color.translate("&cNecesitás un &fToken de Base&c. Comprá uno en &f/perks&c."));
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

        // Heights 2..10 centred-ish across the middle row.
        int slot = 9 + (9 - (MAX_HEIGHT - 1)) / 2; // start on row 2
        for (int h = 2; h <= MAX_HEIGHT; h++) {
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
            int height = Math.max(2, Math.min(MAX_HEIGHT, clicked.getAmount()));
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

        if (!player.getInventory().contains(this.wand)) {
            player.getInventory().addItem(this.wand.clone());
        }
        clearPillars(player);

        player.sendMessage(Color.translate("&a&lBase &7» &fColor: &e" + capitalize(color.name())
            + " &7| Altura: &e" + height));
        player.sendMessage(Color.translate("&7Click &aizq &7= esquina 1, click &ader &7= esquina 2."));
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
            if (sx > MAX_SIZE || sz > MAX_SIZE) {
                player.sendMessage(Color.translate("&cLa selección es " + sx + "x" + sz
                    + ". Máximo " + MAX_SIZE + "x" + MAX_SIZE + "."));
                return;
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
            if (msg.equals("cancelar")) {
                Tasks.sync(() -> cancel(player));
                player.sendMessage(Color.translate("&cBase cancelada."));
                return;
            }
            Tasks.sync(() -> confirmBuild(player));
        }
    }

    private void confirmBuild(Player player) {
        Session session = this.sessions.get(player.getUniqueId());
        if (session == null || session.posOne == null || session.posTwo == null) return;

        Location a = session.posOne, b = session.posTwo;
        if (a.getWorld() == null || !a.getWorld().equals(b.getWorld())) {
            player.sendMessage(Color.translate("&cLas esquinas deben estar en el mismo mundo."));
            return;
        }

        me.vcb.astrocore.factions.type.PlayerFaction faction =
            FactionsManager.getInstance().getPlayerFaction(player);
        if (faction == null) { cancel(player); return; }

        World world = a.getWorld();
        int minX = Math.min(a.getBlockX(), b.getBlockX());
        int maxX = Math.max(a.getBlockX(), b.getBlockX());
        int minZ = Math.min(a.getBlockZ(), b.getBlockZ());
        int maxZ = Math.max(a.getBlockZ(), b.getBlockZ());
        if ((maxX - minX + 1) > MAX_SIZE || (maxZ - minZ + 1) > MAX_SIZE) {
            player.sendMessage(Color.translate("&cMáximo " + MAX_SIZE + "x" + MAX_SIZE + "."));
            return;
        }

        // Whole region must be inside the faction's own claim (strict).
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                me.vcb.astrocore.factions.claim.Claim c =
                    me.vcb.astrocore.factions.claim.ClaimManager.getInstance()
                        .getClaimAt(new Location(world, x, 64, z));
                if (c == null || c.getOwner() == null || !faction.getId().equals(c.getOwner().getId())) {
                    player.sendMessage(Color.translate("&cToda la base debe estar dentro de tu claim "
                        + "(&f" + x + ", " + z + " &cestá fuera)."));
                    return;
                }
            }
        }

        // Consume a base token (re-check in case it changed during selection).
        me.vcb.astrocore.userdata.Userdata ud =
            AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null || ud.getBaseTokens() <= 0) {
            player.sendMessage(Color.translate("&cYa no tenés Tokens de Base. Comprá en &f/perks&c."));
            cancel(player);
            return;
        }

        Material wool  = Material.matchMaterial(session.color.name() + "_WOOL");
        Material glass = Material.matchMaterial(session.color.name() + "_STAINED_GLASS");
        if (wool == null || glass == null) { cancel(player); return; }

        ud.setBaseTokens(ud.getBaseTokens() - 1);

        int baseY = Math.min(a.getBlockY(), b.getBlockY());
        int topY  = Math.min(world.getMaxHeight() - 1, baseY + session.height - 1);

        // 2x2 oak-fence-gate door at a corner of the +X wall, facing outward.
        int doorZ1 = Math.min(minZ + 1, maxZ - 1);
        int doorZ2 = Math.min(minZ + 2, maxZ - 1);
        int doorY1 = baseY + 1;
        int doorY2 = Math.min(baseY + 2, topY - 1);

        // Build the placement queue (bottom to top so it visibly rises).
        List<Placement> queue = new ArrayList<>();
        for (int y = baseY; y <= topY; y++) {
            boolean floorOrRoof = (y == baseY || y == topY);
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean perimeter = x == minX || x == maxX || z == minZ || z == maxZ;

                    boolean door = x == maxX && (z == doorZ1 || z == doorZ2)
                        && (y == doorY1 || y == doorY2) && doorZ1 < doorZ2 && doorY2 > doorY1;

                    Material material;
                    org.bukkit.block.BlockFace facing = null;
                    if (door) {
                        material = Material.OAK_FENCE_GATE;
                        facing = org.bukkit.block.BlockFace.EAST; // lies in the +X wall plane
                    } else if (floorOrRoof) {
                        material = wool;                                   // floor + roof
                    } else if (perimeter) {
                        boolean edge = (x == minX || x == maxX) && (z == minZ || z == maxZ);
                        material = edge ? wool : glass;                    // wool edges, glass walls
                    } else {
                        material = Material.AIR;                           // hollow interior
                    }
                    queue.add(new Placement(x, y, z, material, facing));
                }
            }
        }

        clearPillars(player);
        player.getInventory().remove(this.wand);
        this.sessions.remove(player.getUniqueId());

        player.sendMessage(Color.translate("&a&lBase &7» &fConstruyendo (&r"
            + capitalize(session.color.name()) + "&f, altura " + session.height + ")..."));
        new BuildTask(world, queue, player.getUniqueId()).runTaskTimer(AstroCore.getInstance(), 1L, 1L);
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

    // ── Helpers ──────────────────────────────────────────────────────────────

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

    private record Placement(int x, int y, int z, Material material, org.bukkit.block.BlockFace facing) {}

    private final class BuildTask extends BukkitRunnable {
        private final World world;
        private final List<Placement> queue;
        private final UUID owner;
        private int index;

        BuildTask(World world, List<Placement> queue, UUID owner) {
            this.world = world; this.queue = queue; this.owner = owner;
        }

        @Override
        public void run() {
            int ops = 0;
            while (ops < BATCH && this.index < this.queue.size()) {
                Placement p = this.queue.get(this.index++);
                if (p.y() >= this.world.getMinHeight() && p.y() < this.world.getMaxHeight()) {
                    Block block = this.world.getBlockAt(p.x(), p.y(), p.z());
                    if (block.getType() != p.material() || p.facing() != null) {
                        if (p.facing() != null) {
                            org.bukkit.block.data.BlockData bd = p.material().createBlockData();
                            if (bd instanceof org.bukkit.block.data.Directional dir) dir.setFacing(p.facing());
                            block.setBlockData(bd, false);
                        } else {
                            block.setType(p.material(), false);
                        }
                        this.world.playEffect(new Location(this.world, p.x() + 0.5, p.y(), p.z() + 0.5),
                            org.bukkit.Effect.STEP_SOUND, p.material() == Material.AIR
                                ? Material.WHITE_WOOL : p.material());
                    }
                }
                ops++;
            }

            if (this.index >= this.queue.size()) {
                Player p = Bukkit.getPlayer(this.owner);
                if (p != null) {
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
                    p.sendMessage(Color.translate("&a&l✔ &aBase construida."));
                }
                this.cancel();
            }
        }
    }
}
