package me.vcb.astrocore.commands.base;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.item.ItemBuilder;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RenameCommand extends BaseCommand {

    public RenameCommand() {
        super("rename", "astrocore.rename", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(args.length == 0) {
            player.sendMessage(Language.PREFIX + Language.RENAME_USAGE);
            return;
        }

        ItemStack item = player.getItemInHand();

        if(item == null || item.getType() == Material.AIR) {
            player.sendMessage(Language.PREFIX + Language.ITEMS_NOT_HOLDING);
            return;
        }

        String newName = Color.translate(StringUtils.joinArray(args, " ", 1));

        if(newName.length() > Config.RENAME_MAX_LENGTH) {
            player.sendMessage(Language.PREFIX + Language.RENAME_MAX_LENGTH_EXCEEDED
                .replace("<length>", String.valueOf(Config.RENAME_MAX_LENGTH)));
            return;
        }

        boolean newNameValid = true;

        for(int i = 1; i < args.length; i++) {
            if(Config.RENAME_BLACKLISTED_WORDS.contains(args[i].toLowerCase())) {
                newNameValid = false;
                break;
            }
        }

        if(!newNameValid) {
            player.sendMessage(Language.PREFIX + Language.RENAME_BLACKLISTED_WORD);
            return;
        }

        item.setItemMeta(new ItemBuilder(item).setName(newName).getItemMeta());

        player.sendMessage(Language.PREFIX + Language.RENAME_RENAMED.replace("<name>", newName));
    }
}
