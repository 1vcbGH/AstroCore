package me.vcb.astrocore.selection;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface SelectionCheck {

    boolean check(Selection selection, Player player, Block block);
}
