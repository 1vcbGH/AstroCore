package me.vcb.astrocore.crates.gui;

import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;

public class CrateSoundGui {

    private static final Object[][] OPTIONS = {
        { Sound.BLOCK_CHEST_OPEN,                  Material.CHEST,            "&6&lCofre"              },
        { Sound.ENTITY_PLAYER_LEVELUP,             Material.EXPERIENCE_BOTTLE,"&a&lSubir de Nivel"     },
        { Sound.UI_TOAST_CHALLENGE_COMPLETE,       Material.NETHER_STAR,       "&e&lDesafío Completado" },
        { Sound.BLOCK_NOTE_BLOCK_PLING,            Material.NOTE_BLOCK,        "&d&lPling"              },
        { Sound.ENTITY_FIREWORK_ROCKET_BLAST,      Material.FIREWORK_ROCKET,   "&b&lFuegos Artificiales"},
        { Sound.BLOCK_ANVIL_LAND,                  Material.ANVIL,             "&7&lYunque"             },
        { Sound.ENTITY_ENDER_DRAGON_GROWL,         Material.DRAGON_EGG,        "&5&lDragón"             },
        { Sound.ENTITY_LIGHTNING_BOLT_THUNDER,     Material.LIGHTNING_ROD,     "&f&lTrueno"             },
        { Sound.BLOCK_BEACON_ACTIVATE,             Material.BEACON,            "&e&lBeacon"             },
        { Sound.ENTITY_WITHER_SPAWN,               Material.WITHER_SKELETON_SKULL, "&8&lWither"        },
        { Sound.ENTITY_VILLAGER_YES,               Material.EMERALD,           "&a&lVillager Sí"        },
        { Sound.BLOCK_PORTAL_TRIGGER,              Material.OBSIDIAN,          "&9&lPortal"             },
    };

    private static final int[] ITEM_SLOTS = {
        10,11,12,13,14,15,
        19,20,21,22,23,24
    };

    public static void open(Player player, String crateId) {
        Inventory inv = Bukkit.createInventory(null, 36,
            Color.translate("&d&lSeleccioná un Sonido"));

        var border = new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE).setName("&r").build();
        for (int i = 0; i < 36; i++) inv.setItem(i, border);

        for (int i = 0; i < OPTIONS.length && i < ITEM_SLOTS.length; i++) {
            Sound    s    = (Sound)    OPTIONS[i][0];
            Material mat  = (Material) OPTIONS[i][1];
            String   name = (String)   OPTIONS[i][2];

            inv.setItem(ITEM_SLOTS[i], new ItemBuilder(mat)
                .setName(Color.translate(name))
                .setLore(Arrays.asList(
                    Color.translate("&8──────────────────"),
                    Color.translate("&7Sonido: &f" + s.name()),
                    Color.translate("&eClick para seleccionar")
                )).build());
        }

        inv.setItem(31, new ItemBuilder(Material.ARROW)
            .setName(Color.translate("&c&lVolver")).build());

        CrateGuiTracker.markSoundSelect(player, crateId);
        player.openInventory(inv);
    }

    public static Sound getSoundForSlot(int slot) {
        for (int i = 0; i < ITEM_SLOTS.length && i < OPTIONS.length; i++)
            if (ITEM_SLOTS[i] == slot) return (Sound) OPTIONS[i][0];
        return null;
    }

    public static boolean isBackSlot(int slot) { return slot == 31; }
}
