package me.vcb.astrocore.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.classes.Miner;
import me.vcb.astrocore.classes.manager.PvpClass;
import me.vcb.astrocore.classes.manager.PvpClassType;
import me.vcb.astrocore.commands.manager.BaseCommand;
import org.bukkit.command.CommandSender;

import java.util.Collections;

public class MinerEffectsCommand extends BaseCommand {

    public MinerEffectsCommand() {
        super("minereffects", Collections.singletonList("minereffect"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        PvpClass pvpClass = AstroCore.getInstance().getPvpClassManager()
            .getPvpClassByName(PvpClassType.MINER.getName());

        if(pvpClass instanceof Miner) {
            ((Miner) pvpClass).sendEffectInfo(sender);
        }
    }
}
