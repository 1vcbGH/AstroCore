package me.vcb.astrocore.economy;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.ManagerEnabler;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class EconomyManager implements ManagerEnabler {

    public int getBalance(OfflinePlayer offlinePlayer) {
        Userdata data = AstroCore.getInstance().getUserdataManager().getUserdata(offlinePlayer);
        return data != null ? data.getBalance() : 0;
    }

    public void addBalance(OfflinePlayer player, int amount) {
        this.setBalance(player, this.getBalance(player) + amount);
    }

    public void removeBalance(OfflinePlayer player, int amount) {
        this.setBalance(player, this.getBalance(player) - amount);
    }

    public void setBalance(OfflinePlayer player, int amount) {
        Userdata data = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (data != null) data.changeBalance(amount);
    }

    public TransactionResult depositBalance(OfflinePlayer player, int amount) {
        if(amount <= 0) {
            return TransactionResult.INVALID_AMOUNT;
        }

        if(this.getBalance(player) + amount > Config.MAX_BALANCE) {
            return TransactionResult.MAX_BALANCE;
        }

        this.addBalance(player, amount);
        return TransactionResult.SUCCESS;
    }

    public TransactionResult withdrawBalance(OfflinePlayer player, int amount) {
        if(amount <= 0) {
            return TransactionResult.INVALID_AMOUNT;
        }

        if(this.getBalance(player) < amount) {
            return TransactionResult.MIN_BALANCE;
        }

        this.removeBalance(player, amount);
        return TransactionResult.SUCCESS;
    }

    public TransactionResult transferBalance(Player player, OfflinePlayer target, int amount) {
        if(amount <= 0) {
            return TransactionResult.INVALID_AMOUNT;
        }

        if(this.getBalance(player) < amount) {
            return TransactionResult.MIN_BALANCE;
        }

        if(this.getBalance(target) + amount > Config.MAX_BALANCE) {
            return TransactionResult.MAX_BALANCE;
        }

        this.removeBalance(player, amount);
        this.addBalance(target, amount);

        return TransactionResult.SUCCESS;
    }

    public enum TransactionResult {
        SUCCESS, MAX_BALANCE, MIN_BALANCE, INVALID_AMOUNT
    }
}
