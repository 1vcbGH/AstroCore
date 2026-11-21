package me.vcb.astrocore.customenchants;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CustomEnchantCommand extends BaseCommand implements Listener {

    private static final String TITLE       = "§5§lCustom Enchants";
    private static final String PERM_APPLY  = "astrocore.ce.apply";

    // Slot layout for 16 enchants in a 54-slot GUI (3 centered rows)
    private static final int[] SLOTS = {
        10, 11, 12, 13, 14, 15, 16,   // row 1 (7 slots)
        19, 20, 21, 22, 23, 24, 25,   // row 2 (7 slots)
        28, 29                         // row 3 (2 slots) — total 16
    };

    public CustomEnchantCommand() {
        super("ce", Arrays.asList("customenchants"), true);
        Bukkit.getPluginManager().registerEvents(this,
            me.vcb.astrocore.AstroCore.getInstance());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        // /ce apply <enchant> [level]
        if (args.length >= 2 && args[0].equalsIgnoreCase("apply")) {
            if (!player.hasPermission(PERM_APPLY)) {
                player.sendMessage("§cNo tenés permiso."); return;
            }
            handleApply(player, args);
            return;
        }

        // /ce remove <enchant>
        if (args.length >= 2 && args[0].equalsIgnoreCase("remove")) {
            if (!player.hasPermission(PERM_APPLY)) {
                player.sendMessage("§cNo tenés permiso."); return;
            }
            handleRemove(player, args[1]);
            return;
        }

        // /ce  →  open GUI
        openGui(player);
    }

    // ── GUI ──────────────────────────────────────────────────────────────────

    private void openGui(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        // Fill border with gray glass
        ItemStack border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("§r").build();
        for (int i = 0; i < 54; i++) inv.setItem(i, border);

        CustomEnchant[] enchants = CustomEnchant.values();
        ItemStack held = player.getInventory().getItemInMainHand();
        Map<CustomEnchant, Integer> heldEnchants = CustomEnchantManager.getInstance().getEnchants(held);

        for (int i = 0; i < enchants.length && i < SLOTS.length; i++) {
            CustomEnchant ce = enchants[i];
            int currentLvl = heldEnchants.getOrDefault(ce, 0);

            List<String> lore = new ArrayList<>();
            lore.add(ce.getDescription());
            lore.add("§7Niveles máximos: §e" + ce.getMaxLevel());
            lore.add("§7Aplica a: §b" + shortSlotList(ce));
            lore.add("");
            if (currentLvl > 0) {
                lore.add("§a✔ En ítem en mano: §eNv. " + currentLvl);
            }
            if (player.hasPermission(PERM_APPLY)) {
                lore.add("");
                lore.add("§a▶ Click §7— aplicar al ítem en mano");
                lore.add("§c▶ Click derecho §7— remover del ítem");
            }

            ItemBuilder builder = new ItemBuilder(ce.getIcon())
                .setName(ce.getDisplayName() + (currentLvl > 0 ? " §8(Nv. " + currentLvl + ")" : ""))
                .setLore(lore);
            if (currentLvl > 0) builder.addFakeGlow();

            inv.setItem(SLOTS[i], builder.build());
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(TITLE)) return;
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        if (!player.hasPermission(PERM_APPLY)) return;

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        // Find which enchant was clicked
        int slot = e.getSlot();
        CustomEnchant ce = enchantAtSlot(slot);
        if (ce == null) return;

        ItemStack held = player.getInventory().getItemInMainHand();
        CustomEnchantManager cem = CustomEnchantManager.getInstance();

        if (e.isRightClick()) {
            boolean removed = cem.remove(held, ce);
            player.sendMessage(removed
                ? "§7Removiste §5" + ce.getDisplayName() + " §7del ítem."
                : "§cEse ítem no tiene ese enchant.");
        } else {
            int currentLvl = cem.getLevel(held, ce);
            int nextLvl = currentLvl >= ce.getMaxLevel() ? 1 : currentLvl + 1;
            boolean applied = cem.apply(held, ce, nextLvl);
            player.sendMessage(applied
                ? "§7Aplicaste §5" + ce.getDisplayName() + " §7Nv. §e" + nextLvl + " §7al ítem."
                : "§cEse ítem no es compatible con " + ce.getDisplayName() + "§c.");
        }

        // Refresh GUI
        player.closeInventory();
        openGui(player);
    }

    // ── Apply / Remove from command ──────────────────────────────────────────

    private void handleApply(Player player, String[] args) {
        CustomEnchant ce = CustomEnchant.fromId(args[1]);
        if (ce == null) {
            player.sendMessage("§cEnchant desconocido: §f" + args[1]);
            return;
        }
        int level = 1;
        if (args.length >= 3) {
            try { level = Integer.parseInt(args[2]); }
            catch (NumberFormatException ex) { player.sendMessage("§cNivel inválido."); return; }
        }
        ItemStack held = player.getInventory().getItemInMainHand();
        boolean ok = CustomEnchantManager.getInstance().apply(held, ce, level);
        player.sendMessage(ok
            ? "§7Aplicaste §5" + ce.getDisplayName() + " §7Nv. §e" + level
            : "§cCompatibilidad inválida o nivel fuera de rango.");
    }

    private void handleRemove(Player player, String enchantId) {
        CustomEnchant ce = CustomEnchant.fromId(enchantId);
        if (ce == null) { player.sendMessage("§cEnchant desconocido: §f" + enchantId); return; }
        ItemStack held = player.getInventory().getItemInMainHand();
        boolean ok = CustomEnchantManager.getInstance().remove(held, ce);
        player.sendMessage(ok ? "§7Removiste §5" + ce.getDisplayName() + "§7." : "§cNo encontrado en el ítem.");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private CustomEnchant enchantAtSlot(int slot) {
        for (int i = 0; i < SLOTS.length; i++) {
            if (SLOTS[i] == slot) {
                CustomEnchant[] arr = CustomEnchant.values();
                return i < arr.length ? arr[i] : null;
            }
        }
        return null;
    }

    private String shortSlotList(CustomEnchant ce) {
        if (ce.getApplicable().stream().anyMatch(m -> m.name().contains("SWORD"))) return "Espadas";
        if (ce.getApplicable().stream().anyMatch(m -> m.name().contains("HELMET"))) return "Casco";
        if (ce.getApplicable().stream().anyMatch(m -> m.name().contains("CHESTPLATE"))) return "Pecho";
        if (ce.getApplicable().stream().anyMatch(m -> m.name().contains("LEGGINGS"))) return "Piernas";
        if (ce.getApplicable().stream().anyMatch(m -> m.name().contains("BOOTS"))) return "Botas";
        return "Armadura";
    }
}
