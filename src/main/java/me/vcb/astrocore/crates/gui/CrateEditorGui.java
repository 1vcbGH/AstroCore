package me.vcb.astrocore.crates.gui;

import me.vcb.astrocore.crates.Crate;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.List;

/**
 * Main editor for a single crate.
 *
 * Layout (45-slot / 5 rows):
 *   Row 1-2: info + options
 *   Slot 10 = Nombre       Slot 12 = Hologram lines
 *   Slot 14 = Partícula    Slot 16 = Sonido
 *   Slot 28 = Premios      Slot 30 = Setear ubicación (info)
 *   Slot 34 = Borrar crate
 *   Slot 40 = Volver
 */
public class CrateEditorGui {

    public static void open(Player player, Crate crate) {
        Inventory inv = Bukkit.createInventory(null, 45,
            Color.translate("§5Editar: §f" + crate.getId()));

        var border = new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE).setName("§r").build();
        for (int i = 0; i < 45; i++) inv.setItem(i, border);

        // Display name
        inv.setItem(10, new ItemBuilder(Material.NAME_TAG)
            .setName(Color.translate("§e§lNombre"))
            .setLore(Arrays.asList(
                Color.translate("§7Actual: §f" + crate.getDisplayName()),
                Color.translate("§8──────────────────"),
                Color.translate("§eClick para cambiar en chat")
            )).build());

        // Hologram lines
        inv.setItem(12, new ItemBuilder(Material.OAK_SIGN)
            .setName(Color.translate("§b§lLíneas del Holograma"))
            .setLore(buildHoloLore(crate))
            .build());

        // Particle
        inv.setItem(14, new ItemBuilder(Material.BLAZE_POWDER)
            .setName(Color.translate("§6§lPartícula"))
            .setLore(Arrays.asList(
                Color.translate("§7Actual: §f" + crate.getParticle()),
                Color.translate("§8──────────────────"),
                Color.translate("§eClick para cambiar en chat"),
                Color.translate("§7Ej: §fHAPPY_VILLAGER, CRIT, FLAME")
            )).build());

        // Sound
        inv.setItem(16, new ItemBuilder(Material.NOTE_BLOCK)
            .setName(Color.translate("§d§lSonido"))
            .setLore(Arrays.asList(
                Color.translate("§7Actual: §f" + crate.getSound()),
                Color.translate("§8──────────────────"),
                Color.translate("§eClick para cambiar en chat"),
                Color.translate("§7Ej: §fBLOCK_CHEST_OPEN, ENTITY_PLAYER_LEVELUP")
            )).build());

        // Key material
        Material keyMat;
        try { keyMat = Material.valueOf(crate.getKeyMaterial().toUpperCase()); }
        catch (Exception e) { keyMat = Material.TRIPWIRE_HOOK; }

        inv.setItem(20, new ItemBuilder(keyMat)
            .setName(Color.translate("§6§lItem de la Llave"))
            .setLore(Arrays.asList(
                Color.translate("§7Actual: §f" + crate.getKeyMaterial()),
                Color.translate("§8──────────────────"),
                Color.translate("§eTené el item en la mano y hacé click")
            )).build());

        // Key description
        inv.setItem(24, new ItemBuilder(Material.WRITABLE_BOOK)
            .setName(Color.translate("§6§lDescripción de la Llave"))
            .setLore(Arrays.asList(
                Color.translate("§7Actual: §f" + crate.getKeyDescription()),
                Color.translate("§8──────────────────"),
                Color.translate("§eClick para cambiar en chat"),
                Color.translate("§7Usa §f%crate% §7para el nombre de la crate.")
            )).build());

        // Rewards
        inv.setItem(28, new ItemBuilder(Material.CHEST)
            .setName(Color.translate("§a§lPremios"))
            .setLore(Arrays.asList(
                Color.translate("§7Total: §f" + crate.getRewards().size()),
                Color.translate("§8──────────────────"),
                Color.translate("§eClick para gestionar premios")
            )).build());

        // Set location info
        inv.setItem(30, new ItemBuilder(
                crate.hasLocation() ? Material.GREEN_DYE : Material.RED_DYE)
            .setName(Color.translate("§c§lUbicación del cofre"))
            .setLore(Arrays.asList(
                crate.hasLocation()
                    ? Color.translate("§7Definida: §a✔ §f" + locStr(crate))
                    : Color.translate("§cNo definida"),
                Color.translate("§8──────────────────"),
                Color.translate("§7Usa §f/crate setchest " + crate.getId()),
                Color.translate("§7mirando el bloque del cofre.")
            )).build());

        // Animation type
        inv.setItem(32, new ItemBuilder(crate.getAnimationType().getIcon())
            .setName(Color.translate("§b§lAnimación de Apertura"))
            .setLore(Arrays.asList(
                Color.translate("§7Actual: §f" + crate.getAnimationType().getDisplayName()),
                Color.translate("§7" + crate.getAnimationType().getDescription()),
                Color.translate("§8──────────────────"),
                Color.translate("§eClick para cambiar")
            )).build());

        // Milestone rewards
        inv.setItem(38, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
            .setName(Color.translate("§d§lRecompensas por Hitos"))
            .setLore(Arrays.asList(
                crate.getMilestoneInterval() > 0
                    ? Color.translate("§7Cada §f" + crate.getMilestoneInterval() + " §7aperturas")
                    : Color.translate("§cDesactivado"),
                Color.translate("§7Comandos: §f" + crate.getMilestoneCommands().size()),
                Color.translate("§8──────────────────"),
                Color.translate("§eClick izq: §fintervalo (chat)"),
                Color.translate("§eClick der: §fagregar comando (chat)"),
                Color.translate("§eShift+click: §flimpiar comandos")
            )).build());

        // Delete
        inv.setItem(34, new ItemBuilder(Material.TNT)
            .setName(Color.translate("§c§lEliminar Crate"))
            .setLore(Arrays.asList(
                Color.translate("§cEsto borrará la crate permanentemente."),
                Color.translate("§eClick para confirmar")
            )).build());

        // Back
        inv.setItem(40, new ItemBuilder(Material.ARROW)
            .setName(Color.translate("§c§lVolver"))
            .build());

        CrateGuiTracker.markEditor(player, crate.getId());
        player.openInventory(inv);
    }

    private static List<String> buildHoloLore(Crate crate) {
        List<String> lore = new java.util.ArrayList<>();
        lore.add(Color.translate("§8──────────────────"));
        for (int i = 0; i < crate.getHologramLines().size(); i++)
            lore.add(Color.translate("§7" + (i+1) + ". §f" + crate.getHologramLines().get(i)));
        lore.add(Color.translate("§8──────────────────"));
        lore.add(Color.translate("§eClick para editar en chat"));
        lore.add(Color.translate("§7Formato: §f<línea> <texto>"));
        lore.add(Color.translate("§7Ej: §f1 &6&lMi Crate"));
        return lore;
    }

    private static String locStr(Crate crate) {
        var l = crate.getChestLocation();
        return l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
    }
}
