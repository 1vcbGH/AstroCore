package me.vcb.astrocore.factions.upgrades;

import me.vcb.astrocore.factions.type.PlayerFaction;
import org.bukkit.Material;

/** Faction upgrades bought with faction balance via /f upgrades. */
public enum FactionUpgrade {

    ALLIES("&a&lAliados Extra", Material.GOLDEN_APPLE, 3,
        "&7+1 al máximo de aliados por nivel"),
    HOME_WARMUP("&b&lHome más Rápido", Material.ENDER_PEARL, 4,
        "&7-1s al warmup de /f home por nivel"),
    BALANCE_CAP("&6&lTope de Dinero", Material.GOLD_BLOCK, 5,
        "&7+1,000,000 al tope de dinero por nivel"),
    MEMBERS("&d&lMiembros Extra", Material.PLAYER_HEAD, 2,
        "&7+1 miembro al máximo (¡muy caro!)"),
    VAULT_SIZE("&e&lPáginas de Vaults", Material.CHEST, 5,
        "&7+1 página a los cofres (/f vault) por nivel");

    private final String displayName;
    private final Material icon;
    private final int maxLevel;
    private final String effect;

    FactionUpgrade(String displayName, Material icon, int maxLevel, String effect) {
        this.displayName = displayName;
        this.icon = icon;
        this.maxLevel = maxLevel;
        this.effect = effect;
    }

    public String getDisplayName() { return displayName; }
    public Material getIcon() { return icon; }
    public int getMaxLevel() { return maxLevel; }
    public String getEffect() { return effect; }

    public int getLevel(PlayerFaction f) {
        return switch (this) {
            case ALLIES       -> f.getUpgradeAllies();
            case HOME_WARMUP  -> f.getUpgradeHomeWarmup();
            case BALANCE_CAP  -> f.getUpgradeBalanceCap();
            case MEMBERS      -> f.getUpgradeMembers();
            case VAULT_SIZE   -> f.getUpgradeVaultSize();
        };
    }

    public void setLevel(PlayerFaction f, int level) {
        switch (this) {
            case ALLIES       -> f.setUpgradeAllies(level);
            case HOME_WARMUP  -> f.setUpgradeHomeWarmup(level);
            case BALANCE_CAP  -> f.setUpgradeBalanceCap(level);
            case MEMBERS      -> f.setUpgradeMembers(level);
            case VAULT_SIZE   -> f.setUpgradeVaultSize(level);
        }
    }

    /** Price (faction balance) to go from currentLevel to currentLevel+1. */
    public int priceFor(int currentLevel) {
        int next = currentLevel + 1;
        return switch (this) {
            case ALLIES       -> 200_000 * next;                // 200k, 400k, 600k
            case HOME_WARMUP  -> 150_000 * next;                // 150k ... 600k
            case BALANCE_CAP  -> 150_000 * next;                // 150k ... 750k
            case MEMBERS      -> next == 1 ? 5_000_000 : 15_000_000; // very expensive
            case VAULT_SIZE   -> 100_000 * next;                // 100k ... 500k
        };
    }
}
