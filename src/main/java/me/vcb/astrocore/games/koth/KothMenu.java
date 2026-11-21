package me.vcb.astrocore.games.koth;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** /koth — menú con los KOTH disponibles y su estado. */
public class KothMenu extends BaseCommand implements org.bukkit.event.Listener {

    private static final String TITLE = "§6§lKOTHs";

    public KothMenu() {
        super("koth", java.util.List.of("koths"));
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    @org.bukkit.event.EventHandler
    public void onClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (event.getView().getTitle().equals(Color.translate(TITLE))) event.setCancelled(true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Color.translate("&cSolo jugadores."));
            return;
        }
        open(player);
    }

    private void open(Player player) {
        KothManager km = AstroCore.getInstance().getKothManager();
        List<KothData> koths = km.getKoths();

        int rows = Math.max(3, Math.min(6, (koths.size() + 8) / 9 + 2));
        Inventory inv = Bukkit.createInventory(null, rows * 9, Color.translate(TITLE));
        ItemStack bg = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName("§r").build();
        ItemStack accent = new ItemBuilder(Material.ORANGE_STAINED_GLASS_PANE).setName("§r").build();
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, bg);
        for (int i = 0; i < 9; i++) inv.setItem(i, accent);
        inv.setItem(4, me.vcb.astrocore.utils.Heads.skull(me.vcb.astrocore.utils.Heads.BEACON,
            "&6&lKOTHs", java.util.List.of(
                Color.translate("&7Estado de los King of the Hill."),
                Color.translate("&aVerde &7= activo  &8• &cRojo &7= inactivo"))));

        int slot = 10;
        for (KothData koth : koths) {
            RunningKoth running = km.getRunningKoth(koth.getName());
            boolean active = running != null;

            List<String> lore = new ArrayList<>();
            lore.add(Color.translate("&8──────────────"));
            if (active) {
                String capper = running.getCapzone().getCapperName();
                lore.add(Color.translate("&aEstado: &a&lACTIVO"));
                lore.add(Color.translate("&7Tiempo restante: &f" + running.getCapzone().getTimeLeft()));
                lore.add(Color.translate("&7Capturando: &f" + (capper == null || capper.isEmpty() ? "Nadie" : capper)));
            } else {
                lore.add(Color.translate("&cEstado: &c&lInactivo"));
                lore.add(Color.translate("&7Captura: &f" + koth.getCaptime() + "s"));
                lore.add(Color.translate("&7Puntos: &f" + koth.getFactionPoints()));
            }
            if (koth.getCuboid() != null && koth.getCuboid().getCenter() != null) {
                var c = koth.getCuboid().getCenter();
                lore.add(Color.translate("&7Ubicación: &f" + c.getBlockX() + ", " + c.getBlockZ()));
            }
            lore.add(Color.translate("&8──────────────"));

            inv.setItem(slot, new ItemBuilder(active ? Material.BEACON : Material.GOLD_BLOCK)
                .setName(Color.translate((active ? "&a&l" : "&6&l") + koth.getName()))
                .setLore(lore).build());

            slot++;
            if (slot % 9 == 8) slot += 2;
            if (slot >= inv.getSize() - 1) break;
        }

        if (koths.isEmpty()) {
            inv.setItem(inv.getSize() / 2, new ItemBuilder(Material.BARRIER)
                .setName(Color.translate("&cNo hay KOTHs configurados.")).build());
        }

        player.openInventory(inv);
    }
}
