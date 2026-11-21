package me.vcb.astrocore.crates.gui;

import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.List;

public class CrateParticleGui {

    /** {Particle, Material icon, display name} — only particles that work without extra data */
    private static final Object[][] OPTIONS = {
        { Particle.HAPPY_VILLAGER,   Material.EMERALD,            "&a&lVillager Feliz"      },
        { Particle.CRIT,             Material.DIAMOND_SWORD,      "&f&lCrit"                },
        { Particle.ENCHANTED_HIT,    Material.ENCHANTED_BOOK,     "&5&lCrit Magico"         },
        { Particle.SMALL_FLAME,      Material.BLAZE_POWDER,       "&6&lLlama"               },
        { Particle.HEART,            Material.RED_DYE,            "&c&lCorazon"             },
        { Particle.NOTE,             Material.NOTE_BLOCK,         "&d&lNota Musical"        },
        { Particle.END_ROD,          Material.END_ROD,            "&f&lEnd Rod"             },
        { Particle.REVERSE_PORTAL,   Material.PURPLE_DYE,         "&5&lDragon"              },
        { Particle.TOTEM_OF_UNDYING, Material.TOTEM_OF_UNDYING,   "&e&lTotem"               },
        { Particle.FIREWORK,         Material.FIREWORK_ROCKET,    "&b&lFuegos Artificiales" },
        { Particle.SPIT,             Material.SNOWBALL,           "&b&lNieve"               },
        { Particle.SOUL_FIRE_FLAME,  Material.SOUL_TORCH,         "&3&lAlma"                },
        { Particle.WITCH,            Material.BREWING_STAND,      "&8&lBruja"               },
        { Particle.POOF,             Material.WHITE_WOOL,         "&7&lNube"                },
        { Particle.LAVA,             Material.LAVA_BUCKET,        "&c&lLava"                },
        { Particle.GLOW,             Material.GLOWSTONE_DUST,     "&e&lBrillo"              },
    };

    private static final int[] ITEM_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25
    };

    public static void open(Player player, String crateId) {
        Inventory inv = Bukkit.createInventory(null, 36,
            Color.translate("&6&lSelecciona una Particula"));

        var border = new ItemBuilder(Material.ORANGE_STAINED_GLASS_PANE).setName("&r").build();
        for (int i = 0; i < 36; i++) inv.setItem(i, border);

        for (int i = 0; i < OPTIONS.length && i < ITEM_SLOTS.length; i++) {
            Particle p    = (Particle)  OPTIONS[i][0];
            Material mat  = (Material)  OPTIONS[i][1];
            String   name = (String)    OPTIONS[i][2];

            inv.setItem(ITEM_SLOTS[i], new ItemBuilder(mat)
                .setName(Color.translate(name))
                .setLore(Arrays.asList(
                    Color.translate("&8------------------"),
                    Color.translate("&7ID: &f" + p.name()),
                    Color.translate("&eClick para seleccionar")
                )).build());
        }

        inv.setItem(31, new ItemBuilder(Material.ARROW)
            .setName(Color.translate("&c&lVolver")).build());

        CrateGuiTracker.markParticleSelect(player, crateId);
        player.openInventory(inv);
    }

    public static Particle getParticleForSlot(int slot) {
        for (int i = 0; i < ITEM_SLOTS.length && i < OPTIONS.length; i++)
            if (ITEM_SLOTS[i] == slot) return (Particle) OPTIONS[i][0];
        return null;
    }

    public static boolean isBackSlot(int slot) { return slot == 31; }
}
