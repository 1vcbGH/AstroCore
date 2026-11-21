package me.vcb.astrocore.partner.menu;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.partner.Partner;
import me.vcb.astrocore.partner.PartnerManager;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class PartnerRewardMenu implements Listener {

    private static final int SIZE = 54;
    private static final int ADD_SLOT = 49;
    private static final int CLOSE_SLOT = 53;

    private final Partner partner;
    private final String title;

    public PartnerRewardMenu(Partner partner) {
        this.partner = partner;
        this.title = "§e§lRewards: " + partner.getDisplayName();
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, SIZE, this.title);

        List<String> commands = partner.getRewardCommands();
        for (int i = 0; i < commands.size() && i < 49; i++) {
            String cmd = commands.get(i);
            ItemStack paper = new ItemBuilder(Material.PAPER)
                .setName("§c[Eliminar] §7" + cmd)
                .setLore(Arrays.asList("§7Click para eliminar este comando."))
                .build();
            inv.setItem(i, paper);
        }

        // Add command button
        ItemStack addBtn = new ItemBuilder(Material.LIME_DYE)
            .setName("§a§lAgregar Comando")
            .setLore(Arrays.asList("§7Click y escribe el", "§7comando en el chat."))
            .build();
        inv.setItem(ADD_SLOT, addBtn);

        // Close button
        ItemStack closeBtn = new ItemBuilder(Material.BARRIER)
            .setName("§c§lCerrar")
            .build();
        inv.setItem(CLOSE_SLOT, closeBtn);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!this.title.equals(event.getView().getTitle())) return;

        event.setCancelled(true);

        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == CLOSE_SLOT) {
            player.closeInventory();
            return;
        }

        if (slot == ADD_SLOT) {
            PartnerManager manager = AstroCore.getInstance().getPartnerManager();
            manager.startChatInput(player.getUniqueId(), partner.getNick());
            player.closeInventory();
            player.sendMessage("§eEscribe el comando en el chat (usa %player% como placeholder). Escribe §ccancelar §epara cancelar.");
            return;
        }

        // Delete command
        if (current.getType() == Material.PAPER) {
            List<String> commands = partner.getRewardCommands();
            if (slot >= 0 && slot < commands.size()) {
                String removed = commands.remove(slot);
                AstroCore.getInstance().getPartnerManager().save();
                player.sendMessage("§cComando eliminado: §7" + removed);
                this.open(player);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        if (!this.title.equals(event.getView().getTitle())) return;

        Player player = (Player) event.getPlayer();
        PartnerManager manager = AstroCore.getInstance().getPartnerManager();

        // Only remove pending chat input if they closed without clicking "Add Command"
        // (clicking Add sets the pending input and THEN closes the inventory, so we should NOT remove here)
        // We check if they have pending input for THIS partner; if so, let it stand (they clicked Add).
        // If they have pending input for a different partner or no pending input, nothing to do.
    }
}
