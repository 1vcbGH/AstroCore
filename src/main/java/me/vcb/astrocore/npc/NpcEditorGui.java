package me.vcb.astrocore.npc;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GUI editor for NPCs. Opened via {@code /npc list}.
 *  - List view: one head per NPC (its skin), click to edit.
 *  - Editor view: teleport, move, rename, set skin, add/clear commands, delete.
 * Text inputs (rename / setskin / addcmd) are captured via a chat prompt.
 */
public class NpcEditorGui implements Listener {

    private static NpcEditorGui instance;
    public static NpcEditorGui getInstance() { return instance; }

    private static final String LIST_TITLE   = "§8ɴᴘᴄ §7» §fLista";
    private static final String EDIT_PREFIX  = "§8ɴᴘᴄ §7» §f";
    private static final Material BG = Material.BLACK_STAINED_GLASS_PANE;

    // Editor slots
    private static final int TP_SLOT     = 10;
    private static final int MOVE_SLOT   = 11;
    private static final int NAME_SLOT   = 12;
    private static final int SKIN_SLOT   = 13;
    private static final int ADDCMD_SLOT = 14;
    private static final int CLEAR_SLOT  = 15;
    private static final int DELETE_SLOT = 16;
    private static final int BACK_SLOT   = 22;

    // Pending chat inputs: player UUID -> "action:npcId"
    private final Map<UUID, String> pending = new ConcurrentHashMap<>();

    public NpcEditorGui() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    // ── List view ──────────────────────────────────────────────────────────────

    public void openList(Player player) {
        List<NpcData> npcs = new ArrayList<>(NpcManager.getInstance().getAllNpcs());
        int rows = Math.max(1, (int) Math.ceil(npcs.size() / 9.0));
        int size = Math.min(54, rows * 9);
        Inventory inv = Bukkit.createInventory(null, size, LIST_TITLE);

        for (int i = 0; i < npcs.size() && i < size; i++) {
            inv.setItem(i, makeNpcHead(npcs.get(i)));
        }
        player.openInventory(inv);
    }

    private ItemStack makeNpcHead(NpcData data) {
        List<String> lore = new ArrayList<>();
        String loc = data.getLocation() != null
            ? data.getLocation().getWorld().getName() + " "
              + data.getLocation().getBlockX() + " "
              + data.getLocation().getBlockY() + " "
              + data.getLocation().getBlockZ()
            : "§csin ubicación";
        lore.add("§7Ubicación: §f" + loc);
        lore.add("§7Comandos: §f" + data.getCommands().size());
        lore.add("");
        lore.add("§a➡ Click para editar");

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (data.getSkinTexture() != null) {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
            profile.setProperty(new ProfileProperty("textures", data.getSkinTexture()));
            meta.setPlayerProfile(profile);
        }
        meta.setDisplayName(Color.translate("§e" + data.getId()));
        meta.setLore(lore);
        head.setItemMeta(meta);
        return head;
    }

    // ── Editor view ─────────────────────────────────────────────────────────────

    public void openEditor(Player player, String id) {
        NpcData data = NpcManager.getInstance().getNpc(id);
        if (data == null) { player.sendMessage(Color.translate("§cNo existe NPC §f" + id)); return; }

        Inventory inv = Bukkit.createInventory(null, 27, EDIT_PREFIX + id);
        fillBg(inv, 27);

        inv.setItem(TP_SLOT,     button(Material.ENDER_PEARL,  "§aTeleport", "§7Ir a la posición del NPC."));
        inv.setItem(MOVE_SLOT,   button(Material.COMPASS,      "§aMover aquí", "§7Trae el NPC a tu posición."));
        inv.setItem(NAME_SLOT,   button(Material.NAME_TAG,     "§aRenombrar", "§7Escribe el nombre en el chat.", "§7Soporta colores con &."));
        inv.setItem(SKIN_SLOT,   button(Material.PLAYER_HEAD,  "§aCambiar skin", "§7Escribe el nombre del jugador en el chat."));
        inv.setItem(ADDCMD_SLOT, button(Material.COMMAND_BLOCK,"§aAgregar comando", "§7Escribe el comando en el chat.", "§7Usa %player% para el jugador."));
        inv.setItem(CLEAR_SLOT,  button(Material.BARRIER,      "§cLimpiar comandos", "§7Borra todos los comandos del NPC."));
        inv.setItem(DELETE_SLOT, button(Material.TNT,          "§4Eliminar NPC", "§7Shift-Click para confirmar."));
        inv.setItem(BACK_SLOT,   button(Material.ARROW,        "§cVolver", "§7Vuelve a la lista de NPCs."));

        player.openInventory(inv);
    }

    // ── Click handling ────────────────────────────────────────────────────────

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();

        if (title.equals(LIST_TITLE)) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() != Material.PLAYER_HEAD || !item.hasItemMeta()) return;
            String id = ChatColor.stripColor(item.getItemMeta().getDisplayName()).trim().toLowerCase();
            openEditor(player, id);
            return;
        }

        if (title.startsWith(EDIT_PREFIX)) {
            event.setCancelled(true);
            String id = title.substring(EDIT_PREFIX.length()).toLowerCase().trim();
            NpcManager nm = NpcManager.getInstance();
            NpcData data = nm.getNpc(id);
            if (data == null) { player.closeInventory(); return; }

            switch (event.getRawSlot()) {
                case TP_SLOT -> {
                    Location loc = data.getLocation();
                    if (loc == null) { player.sendMessage(Color.translate("§cNPC sin ubicación.")); return; }
                    player.closeInventory();
                    player.teleport(loc);
                    player.sendMessage(Color.translate("§aTeleportado al NPC §f" + id));
                }
                case MOVE_SLOT -> {
                    data.setLocation(player.getLocation());
                    nm.save();
                    nm.respawn(id);
                    player.sendMessage(Color.translate("§aNPC §f" + id + " §amovido a tu posición."));
                    openEditor(player, id);
                }
                case NAME_SLOT   -> promptChat(player, "name", id, "§eEscribe el nuevo nombre (con & para colores):");
                case SKIN_SLOT   -> promptChat(player, "skin", id, "§eEscribe el nombre del jugador para la skin:");
                case ADDCMD_SLOT -> promptChat(player, "addcmd", id, "§eEscribe el comando (sin /, usa %player%):");
                case CLEAR_SLOT -> {
                    data.getCommands().clear();
                    nm.save();
                    player.sendMessage(Color.translate("§cComandos limpiados de §f" + id));
                    openEditor(player, id);
                }
                case DELETE_SLOT -> {
                    if (!event.isShiftClick()) {
                        player.sendMessage(Color.translate("§cShift-Click para confirmar la eliminación."));
                        return;
                    }
                    nm.deleteNpc(id);
                    player.closeInventory();
                    player.sendMessage(Color.translate("§cNPC §f" + id + " §celiminado."));
                }
                case BACK_SLOT -> openList(player);
            }
        }
    }

    // ── Chat prompt ─────────────────────────────────────────────────────────────

    private void promptChat(Player player, String action, String id, String message) {
        pending.put(player.getUniqueId(), action + ":" + id);
        player.closeInventory();
        player.sendMessage(Color.translate(message));
        player.sendMessage(Color.translate("§7(escribe §fcancel §7para cancelar)"));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String entry = pending.remove(player.getUniqueId());
        if (entry == null) return;

        event.setCancelled(true);
        String message = event.getMessage();
        String[] parts = entry.split(":", 2);
        String action = parts[0];
        String id     = parts[1];

        Tasks.sync(() -> {
            NpcManager nm = NpcManager.getInstance();
            NpcData data = nm.getNpc(id);
            if (data == null) { player.sendMessage(Color.translate("§cEl NPC ya no existe.")); return; }

            if (message.equalsIgnoreCase("cancel")) {
                player.sendMessage(Color.translate("§7Cancelado."));
                openEditor(player, id);
                return;
            }

            switch (action) {
                case "name" -> {
                    data.setDisplayName(message);
                    nm.save();
                    nm.respawn(id);
                    player.sendMessage(Color.translate("§aNombre actualizado: §f" + message));
                }
                case "skin" -> {
                    player.sendMessage(Color.translate("§eFetcheando skin de §f" + message + "§e..."));
                    nm.fetchSkinAsync(id, message);
                }
                case "addcmd" -> {
                    String cmd = message.startsWith("/") ? message.substring(1) : message;
                    data.getCommands().add(cmd);
                    nm.save();
                    player.sendMessage(Color.translate("§aComando agregado: §f/" + cmd));
                }
            }
            openEditor(player, id);
        });
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private ItemStack button(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Color.translate(name));
        List<String> l = new ArrayList<>();
        for (String s : lore) l.add(Color.translate(s));
        meta.setLore(l);
        item.setItemMeta(meta);
        return item;
    }

    private void fillBg(Inventory inv, int size) {
        ItemStack pane = new ItemStack(BG);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName("§r");
        pane.setItemMeta(meta);
        for (int i = 0; i < size; i++) inv.setItem(i, pane);
    }
}
