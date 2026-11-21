package me.vcb.astrocore.npc;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.utils.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class NpcCommand extends BaseCommand {

    private static final String PERM = "astrocore.npc.admin";

    public NpcCommand() {
        super("npc", Arrays.asList("npcs"), PERM, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) { sendHelp(sender); return; }

        NpcManager nm = NpcManager.getInstance();

        switch (args[0].toLowerCase()) {

            case "create" -> {
                if (args.length < 2) { sender.sendMessage(usage("create <id>")); return; }
                if (!(sender instanceof Player p)) { sender.sendMessage("§cSolo jugadores."); return; }
                String id = args[1].toLowerCase();
                if (nm.getNpc(id) != null) { p.sendMessage(Color.translate("§cYa existe un NPC §f" + id)); return; }
                NpcData data = nm.createNpc(id);
                data.setLocation(p.getLocation());
                nm.save();
                nm.respawn(id);
                p.sendMessage(Color.translate("§aNPC §f" + id + " §acreado en tu posición."));
            }

            case "remove", "delete" -> {
                if (args.length < 2) { sender.sendMessage(usage("remove <id>")); return; }
                String id = args[1].toLowerCase();
                if (nm.deleteNpc(id)) sender.sendMessage(Color.translate("§cNPC §f" + id + " §celiminado."));
                else                  sender.sendMessage(Color.translate("§cNo existe NPC §f" + id));
            }

            case "setname" -> {
                if (args.length < 3) { sender.sendMessage(usage("setname <id> <nombre>")); return; }
                String id   = args[1].toLowerCase();
                NpcData data = nm.getNpc(id);
                if (data == null) { sender.sendMessage(notFound(id)); return; }
                String name = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                data.setDisplayName(name);
                nm.save();
                nm.respawn(id);
                sender.sendMessage(Color.translate("§aNombre: §f" + name));
            }

            case "setskin" -> {
                if (args.length < 3) { sender.sendMessage(usage("setskin <id> <jugador>")); return; }
                String id   = args[1].toLowerCase();
                NpcData data = nm.getNpc(id);
                if (data == null) { sender.sendMessage(notFound(id)); return; }
                String skinPlayer = args[2];
                sender.sendMessage(Color.translate("§eFetcheando skin de §f" + skinPlayer + "§e..."));
                nm.fetchSkinAsync(id, skinPlayer);
            }

            case "setlocation", "setloc", "move" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage("§cSolo jugadores."); return; }
                if (args.length < 2) { sender.sendMessage(usage("setlocation <id>")); return; }
                String id   = args[1].toLowerCase();
                NpcData data = nm.getNpc(id);
                if (data == null) { p.sendMessage(notFound(id)); return; }
                data.setLocation(p.getLocation());
                nm.save();
                nm.respawn(id);
                p.sendMessage(Color.translate("§aUbicación del NPC §f" + id + " §aactualizada."));
            }

            case "sethelmet" -> setEquipment(sender, args, nm, "helmet");
            case "setchest"  -> setEquipment(sender, args, nm, "chest");
            case "setlegs"   -> setEquipment(sender, args, nm, "legs");
            case "setboots"  -> setEquipment(sender, args, nm, "boots");
            case "sethand"   -> setEquipment(sender, args, nm, "hand");
            case "setoffhand"-> setEquipment(sender, args, nm, "offhand");

            case "addcmd" -> {
                if (args.length < 3) { sender.sendMessage(usage("addcmd <id> <comando>")); return; }
                String id   = args[1].toLowerCase();
                NpcData data = nm.getNpc(id);
                if (data == null) { sender.sendMessage(notFound(id)); return; }
                String cmd = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                data.getCommands().add(cmd);
                nm.save();
                sender.sendMessage(Color.translate("§aComando agregado: §f/" + cmd));
            }

            case "clearcmds" -> {
                if (args.length < 2) { sender.sendMessage(usage("clearcmds <id>")); return; }
                String id   = args[1].toLowerCase();
                NpcData data = nm.getNpc(id);
                if (data == null) { sender.sendMessage(notFound(id)); return; }
                data.getCommands().clear();
                nm.save();
                sender.sendMessage(Color.translate("§cComandos del NPC §f" + id + " §climpiados."));
            }

            case "list", "gui", "editor", "edit" -> {
                if (sender instanceof Player p && !(args.length >= 2 && args[1].equalsIgnoreCase("text"))) {
                    NpcEditorGui.getInstance().openList(p);
                    return;
                }
                sender.sendMessage(Color.translate("§6NPCs:"));
                for (NpcData d : nm.getAllNpcs()) {
                    String loc = d.getLocation() != null
                        ? d.getLocation().getWorld().getName() + " "
                          + d.getLocation().getBlockX() + " "
                          + d.getLocation().getBlockY() + " "
                          + d.getLocation().getBlockZ()
                        : "§csin ubicación";
                    sender.sendMessage(Color.translate("  §f" + d.getId()
                        + " §7— " + d.getDisplayName()
                        + " §8(" + loc + ")"));
                }
            }

            default -> sendHelp(sender);
        }
    }

    private void setEquipment(CommandSender sender, String[] args, NpcManager nm, String slot) {
        if (!(sender instanceof Player p)) { sender.sendMessage("§cSolo jugadores."); return; }
        if (args.length < 2) { p.sendMessage(usage(args[0] + " <id>")); return; }
        String id   = args[1].toLowerCase();
        NpcData data = nm.getNpc(id);
        if (data == null) { p.sendMessage(notFound(id)); return; }

        ItemStack hand = p.getInventory().getItemInMainHand();
        boolean clear  = hand == null || hand.getType().isAir();

        switch (slot) {
            case "helmet"  -> data.setHelmet(clear ? null : hand);
            case "chest"   -> data.setChestplate(clear ? null : hand);
            case "legs"    -> data.setLeggings(clear ? null : hand);
            case "boots"   -> data.setBoots(clear ? null : hand);
            case "hand"    -> data.setMainHand(clear ? null : hand);
            case "offhand" -> data.setOffHand(clear ? null : hand);
        }
        nm.save();
        nm.respawn(id);
        p.sendMessage(Color.translate(clear
            ? "§cEquipment limpiado para §f" + slot
            : "§aEquipment seteado en §f" + slot + " §ade §f" + id));
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage(Color.translate("§6NPC Commands:"));
        s.sendMessage(Color.translate("§e/npc create <id> §7— Crea NPC en tu pos"));
        s.sendMessage(Color.translate("§e/npc remove <id> §7— Elimina NPC"));
        s.sendMessage(Color.translate("§e/npc setname <id> <nombre> §7— Nombre con colores (&)"));
        s.sendMessage(Color.translate("§e/npc setskin <id> <jugador> §7— Skin de ese jugador"));
        s.sendMessage(Color.translate("§e/npc setlocation <id> §7— Mueve a tu pos"));
        s.sendMessage(Color.translate("§e/npc sethelmet/setchest/setlegs/setboots/sethand/setoffhand <id> §7— Item en mano"));
        s.sendMessage(Color.translate("§e/npc addcmd <id> <cmd> §7— Agrega comando al click"));
        s.sendMessage(Color.translate("§e/npc clearcmds <id> §7— Limpia comandos"));
        s.sendMessage(Color.translate("§e/npc list §7— Abre el editor GUI (§flist text §7para texto)"));
    }

    private String usage(String sub)  { return Color.translate("§cUso: §f/npc " + sub); }
    private String notFound(String id){ return Color.translate("§cNo existe NPC §f" + id); }
}
