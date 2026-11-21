package me.vcb.astrocore.factions.upgrades;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.enums.Role;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** /f upgrades — buy faction upgrades with faction balance. */
public class FactionUpgradesGui implements Listener {

    private static final String TITLE = "§2§lMejoras de Facción";
    private static final NumberFormat NF = NumberFormat.getInstance(Locale.US);
    private static final int[] SLOTS = {10, 11, 12, 13, 14}; // one per upgrade (5)

    private static FactionUpgradesGui instance;

    public FactionUpgradesGui() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    public static FactionUpgradesGui getInstance() { return instance; }

    public void open(Player player) {
        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);
        if (faction == null) {
            player.sendMessage(Color.translate("&cNo estás en una facción."));
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, Color.translate(TITLE));
        ItemStack border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("§r").build();
        for (int i = 0; i < 27; i++) inv.setItem(i, border);

        FactionUpgrade[] upgrades = FactionUpgrade.values();
        for (int i = 0; i < upgrades.length; i++) {
            FactionUpgrade up = upgrades[i];
            int level = up.getLevel(faction);
            boolean maxed = level >= up.getMaxLevel();

            List<String> lore = new ArrayList<>();
            lore.add(Color.translate(up.getEffect()));
            lore.add(Color.translate("&8──────────────"));
            lore.add(Color.translate("&7Nivel: &f" + level + "&7/&f" + up.getMaxLevel()));
            if (maxed) {
                lore.add(Color.translate("&a✔ Nivel máximo"));
            } else {
                lore.add(Color.translate("&7Precio: &6$" + NF.format(up.priceFor(level))));
                lore.add("");
                lore.add(Color.translate("&eClick para mejorar"));
            }

            inv.setItem(SLOTS[i], new ItemBuilder(up.getIcon())
                .setName(Color.translate(up.getDisplayName()))
                .setLore(lore).build());
        }

        inv.setItem(22, new ItemBuilder(Material.GOLD_INGOT)
            .setName(Color.translate("&6&lBanco de Facción"))
            .setLore(List.of(Color.translate("&7Saldo: &6$" + NF.format(faction.getBalance())))).build());

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(Color.translate(TITLE))) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();
        FactionUpgrade up = null;
        for (int i = 0; i < SLOTS.length; i++) if (SLOTS[i] == slot) up = FactionUpgrade.values()[i];
        if (up == null) return;

        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);
        if (faction == null) return;
        if (!faction.getMember(player).getRole().isAtLeast(Role.CO_LEADER)) {
            player.sendMessage(Color.translate("&cSolo Co-Líder o superior puede mejorar."));
            return;
        }

        int level = up.getLevel(faction);
        if (level >= up.getMaxLevel()) {
            player.sendMessage(Color.translate("&cYa está al nivel máximo."));
            return;
        }
        int price = up.priceFor(level);
        if (faction.getBalance() < price) {
            player.sendMessage(Color.translate("&cLa facción no tiene suficiente dinero (&6$" + NF.format(price) + "&c)."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        faction.removeBalance(price);
        up.setLevel(faction, level + 1);

        faction.sendMessage(Color.translate("&2&lMejoras &7» &e" + player.getName() + " &amejoró &r"
            + up.getDisplayName() + " &aa nivel &f" + (level + 1) + " &7(&6$" + NF.format(price) + "&7)."));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.3f);
        open(player);
    }
}
