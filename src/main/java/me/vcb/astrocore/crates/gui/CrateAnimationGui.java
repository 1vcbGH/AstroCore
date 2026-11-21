package me.vcb.astrocore.crates.gui;

import me.vcb.astrocore.crates.Crate;
import me.vcb.astrocore.crates.CrateAnimationType;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Submenu to pick the opening animation for a crate.
 * One icon per {@link CrateAnimationType}; the active one is highlighted.
 *
 * 27-slot layout: animations placed from slot 10 onward; slot 22 = Volver.
 */
public class CrateAnimationGui {

    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16};
    public  static final int   BACK_SLOT = 22;

    public static void open(Player player, Crate crate) {
        Inventory inv = Bukkit.createInventory(null, 27,
            Color.translate("§5Animación: §f" + crate.getId()));

        var border = new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE).setName("§r").build();
        for (int i = 0; i < 27; i++) inv.setItem(i, border);

        CrateAnimationType[] types = CrateAnimationType.values();
        for (int i = 0; i < types.length && i < SLOTS.length; i++) {
            CrateAnimationType t = types[i];
            boolean active = crate.getAnimationType() == t;

            List<String> lore = new ArrayList<>();
            lore.add(Color.translate("§7" + t.getDescription()));
            lore.add(Color.translate("§8──────────────────"));
            lore.add(active
                ? Color.translate("§a✔ Seleccionada")
                : Color.translate("§eClick para seleccionar"));

            ItemBuilder b = new ItemBuilder(t.getIcon())
                .setName(Color.translate((active ? "§a§l" : "§f§l") + t.getDisplayName()))
                .setLore(lore);
            if (active) b.addFakeGlow();
            inv.setItem(SLOTS[i], b.build());
        }

        inv.setItem(BACK_SLOT, new ItemBuilder(Material.ARROW)
            .setName(Color.translate("§c§lVolver")).build());

        CrateGuiTracker.markAnimationSelect(player, crate.getId());
        player.openInventory(inv);
    }

    /** Maps a clicked slot to an animation type, or null. */
    public static CrateAnimationType getTypeForSlot(int slot) {
        CrateAnimationType[] types = CrateAnimationType.values();
        for (int i = 0; i < SLOTS.length && i < types.length; i++)
            if (SLOTS[i] == slot) return types[i];
        return null;
    }

    public static boolean isBackSlot(int slot) { return slot == BACK_SLOT; }
}
