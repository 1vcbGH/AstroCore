package me.vcb.astrocore.abilities.reflection;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles armor packet manipulation for ability effects (invisibility, decoy).
 * Uses ProtocolLib instead of NMS reflection.
 */
public class AbilitiesReflection_1_21 {

    /**
     * Sends an equipment packet to a recipient, either showing or hiding the target's armor.
     */
    public static void sendEquipmentPacket(Player recipient, Player target, boolean remove) {
        try {
            PacketContainer packet = ProtocolLibrary.getProtocolManager()
                .createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);

            packet.getIntegers().write(0, target.getEntityId());

            List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipment = new ArrayList<>();

            ItemStack[] armor = target.getInventory().getArmorContents();
            ItemStack mainHand = target.getInventory().getItemInHand();

            ItemStack empty = new ItemStack(Material.AIR);

            equipment.add(new Pair<>(EnumWrappers.ItemSlot.MAINHAND, remove ? empty : mainHand));
            equipment.add(new Pair<>(EnumWrappers.ItemSlot.FEET,  remove ? empty : (armor[0] != null ? armor[0] : empty)));
            equipment.add(new Pair<>(EnumWrappers.ItemSlot.LEGS,  remove ? empty : (armor[1] != null ? armor[1] : empty)));
            equipment.add(new Pair<>(EnumWrappers.ItemSlot.CHEST, remove ? empty : (armor[2] != null ? armor[2] : empty)));
            equipment.add(new Pair<>(EnumWrappers.ItemSlot.HEAD,  remove ? empty : (armor[3] != null ? armor[3] : empty)));

            packet.getSlotStackPairLists().write(0, equipment);

            ProtocolLibrary.getProtocolManager().sendServerPacket(recipient, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
