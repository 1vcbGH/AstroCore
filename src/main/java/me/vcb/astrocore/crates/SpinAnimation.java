package me.vcb.astrocore.crates;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Crate opening animation in a 27-slot inventory. Each {@link CrateAnimationType}
 * has a visually distinct motion:
 *  - SPIN     : top row (slots 0-8) scrolls sideways, lands at center slot 4.
 *  - ROULETTE : center column (4,13,22) scrolls downward, lands at slot 13.
 *  - REVEAL   : 9 face-down cards (slots 9-17) flip one-by-one to reveal items.
 *  - FLASH    : the whole grid strobes random items + colors, freezes on winner.
 *  - INSTANT  : handled by the caller (no animation).
 */
public class SpinAnimation {

    private static final int[] PHASES = {2, 2, 3, 5, 8, 15};

    private static final int   SPIN_SLOTS  = 9;        // row 1: slots 0-8
    private static final int   CENTER_SLOT = 4;        // SPIN landing
    private static final int[] COLUMN      = {4, 13, 22}; // ROULETTE column
    private static final int[] CARDS       = {9,10,11,12,13,14,15,16,17}; // REVEAL/FLASH row
    private static final int   MID_CARD    = 13;       // REVEAL/FLASH landing
    private static final Random RNG = new Random();

    private final Player          player;
    private final Crate           crate;
    private final CrateReward     winner;
    private final CrateAnimationType type;
    private final Inventory       inv;
    private final List<ItemStack> pool;

    // REVEAL state: which cards already flipped face-up
    private final boolean[] flipped = new boolean[9];

    private int      phase    = 0;
    private int      tickAcc  = 0;
    private int      spins    = 0;
    private final int totalSpins;
    private BukkitTask task;
    private boolean  finished = false;

    public SpinAnimation(Player player, Crate crate) {
        this.player = player;
        this.crate  = crate;
        this.winner = crate.rollReward();
        this.type   = crate.getAnimationType() == null ? CrateAnimationType.SPIN : crate.getAnimationType();
        this.inv    = Bukkit.createInventory(null, 27,
            me.vcb.astrocore.utils.Color.translate(crate.getDisplayName()));
        this.pool   = buildPool(crate);
        this.totalSpins = 36;

        buildStaticSlots();
    }

    // ── Static layout (per type) ────────────────────────────────────────────────

    private void buildStaticSlots() {
        ItemStack border  = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("§r").build();
        ItemStack pointer = new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).setName("§e§l▲ Premio").build();
        ItemStack spinning = new ItemBuilder(Material.BARRIER).setName("§c§lGirando...").build();

        switch (type) {
            case ROULETTE -> {
                for (int i = 0; i < 27; i++) inv.setItem(i, border);
                inv.setItem(3, new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).setName("§e§l▶").build());
                inv.setItem(5, new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).setName("§e§l◀").build());
                inv.setItem(26, spinning);
                for (int s : COLUMN) inv.setItem(s, pool.get(RNG.nextInt(pool.size())));
            }
            case REVEAL -> {
                for (int i = 0; i < 27; i++) inv.setItem(i, border);
                ItemStack faceDown = new ItemBuilder(Material.ORANGE_STAINED_GLASS_PANE).setName("§6§l?").build();
                for (int s : CARDS) inv.setItem(s, faceDown);
                inv.setItem(4, spinning);
            }
            case FLASH -> {
                for (int i = 0; i < 27; i++) inv.setItem(i, border);
                for (int s : CARDS) inv.setItem(s, pool.get(RNG.nextInt(pool.size())));
                inv.setItem(4, spinning);
            }
            default -> { // SPIN
                for (int i = 9; i < 18; i++) inv.setItem(i, border);
                inv.setItem(13, pointer);
                for (int i = 18; i < 27; i++) inv.setItem(i, border);
                inv.setItem(22, spinning);
                for (int i = 0; i < SPIN_SLOTS; i++) inv.setItem(i, pool.get(i % pool.size()));
            }
        }
    }

    // ── Start ─────────────────────────────────────────────────────────────────

    public void start() {
        player.openInventory(inv);
        CrateManager.getInstance().setAnimating(player.getUniqueId(), this);

        task = new BukkitRunnable() {
            @Override
            public void run() {
                tickAcc++;
                int interval = PHASES[Math.min(phase, PHASES.length - 1)];
                if (tickAcc < interval) return;
                tickAcc = 0;

                step();
                spins++;

                if (spins % 9 == 0 && phase < PHASES.length - 1) phase++;

                if (spins >= totalSpins) {
                    finish();
                    cancel();
                }
            }
        }.runTaskTimer(AstroCore.getInstance(), 0L, 1L);
    }

    // ── Animation step (per type) ───────────────────────────────────────────────

    private void step() {
        switch (type) {
            case ROULETTE -> rouletteStep();
            case REVEAL   -> revealStep();
            case FLASH    -> flashStep();
            default       -> spinStep();
        }
        player.updateInventory();
        float pitch = 0.5f + ((float) (PHASES.length - Math.min(phase, PHASES.length - 1)) / PHASES.length) * 1.5f;
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, pitch);
    }

    /** Horizontal scroll across the top row. */
    private void spinStep() {
        for (int i = 0; i < SPIN_SLOTS - 1; i++)
            inv.setItem(i, inv.getItem(i + 1));
        inv.setItem(SPIN_SLOTS - 1, pool.get(spins % pool.size()));
    }

    /** Vertical scroll down the center column. */
    private void rouletteStep() {
        inv.setItem(COLUMN[2], inv.getItem(COLUMN[1]));
        inv.setItem(COLUMN[1], inv.getItem(COLUMN[0]));
        inv.setItem(COLUMN[0], pool.get(spins % pool.size()));
    }

    /** Flip the next face-down card to a random item, one every few spins. */
    private void revealStep() {
        int target = Math.min(CARDS.length, (spins * CARDS.length / totalSpins) + 1);
        int faceUp = 0;
        for (boolean f : flipped) if (f) faceUp++;
        if (faceUp < target) {
            for (int i = 0; i < CARDS.length; i++) {
                if (!flipped[i]) {
                    flipped[i] = true;
                    inv.setItem(CARDS[i], pool.get(RNG.nextInt(pool.size())));
                    player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 0.8f, 1.4f);
                    break;
                }
            }
        }
        // jiggle already-flipped (non-center) cards for life
        for (int i = 0; i < CARDS.length; i++) {
            if (flipped[i] && CARDS[i] != MID_CARD && RNG.nextInt(4) == 0)
                inv.setItem(CARDS[i], pool.get(RNG.nextInt(pool.size())));
        }
    }

    /** Strobe the whole grid: random items in cards + flashing colored borders. */
    private void flashStep() {
        for (int s : CARDS) inv.setItem(s, pool.get(RNG.nextInt(pool.size())));
        Material flashColor = (spins % 2 == 0)
            ? Material.RED_STAINED_GLASS_PANE : Material.YELLOW_STAINED_GLASS_PANE;
        ItemStack flash = new ItemBuilder(flashColor).setName("§r").build();
        for (int i = 0; i < 9; i++)   inv.setItem(i, flash);
        for (int i = 18; i < 27; i++) inv.setItem(i, flash);
    }

    // ── Skip ──────────────────────────────────────────────────────────────────

    public void skip() {
        if (finished) return;
        if (task != null) { task.cancel(); task = null; }
        giveReward();
        cleanup();
    }

    public boolean isFinished() { return finished; }

    // ── Finish ──────────────────────────────────────────────────────────────────

    private void finish() {
        if (finished) return;
        if (!player.isOnline()) { cleanup(); return; }

        int landing = switch (type) {
            case ROULETTE -> COLUMN[1];
            case REVEAL, FLASH -> MID_CARD;
            default -> CENTER_SLOT;
        };

        if (winner != null) {
            inv.setItem(landing, winner.buildSpinItem());
            if (type == CrateAnimationType.REVEAL || type == CrateAnimationType.FLASH) {
                ItemStack pane = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).setName("§r").build();
                for (int s : CARDS) if (s != landing) inv.setItem(s, pane);
            }
        }

        int statusSlot = switch (type) {
            case ROULETTE -> 26;
            case REVEAL, FLASH -> 4;
            default -> 22;
        };
        inv.setItem(statusSlot, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
            .setName("§a§l¡Premio ganado!").build());
        player.updateInventory();

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

        finished = true;
        Bukkit.getScheduler().runTaskLater(AstroCore.getInstance(), () -> {
            if (player.isOnline()) {
                giveReward();
                player.closeInventory();
            }
            cleanup();
        }, 30L);
    }

    private void giveReward() {
        if (winner == null) return;

        ItemStack item = winner.getItem();
        if (item != null) {
            if (player.getInventory().firstEmpty() == -1)
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            else
                player.getInventory().addItem(item);
        }

        for (String cmd : winner.getCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                cmd.replace("%player%", player.getName()));
        }

        CrateManager.getInstance().recordOpenAndCheckMilestone(player, crate);
    }

    private void cleanup() {
        CrateManager.getInstance().setAnimating(player.getUniqueId(), null);
    }

    // ── Pool builder ──────────────────────────────────────────────────────────

    private static List<ItemStack> buildPool(Crate crate) {
        List<ItemStack> pool = new ArrayList<>();
        List<CrateReward> winnable = crate.getRewards().stream()
            .filter(CrateReward::isWinnable).collect(java.util.stream.Collectors.toList());
        if (winnable.isEmpty()) {
            pool.add(new ItemBuilder(Material.BARRIER).setName("§cSin premios").build());
            return pool;
        }
        int total = winnable.stream().mapToInt(CrateReward::getChance).sum();
        for (CrateReward r : winnable) {
            int count = Math.max(1, (int) Math.round(((double) r.getChance() / total) * 20));
            for (int i = 0; i < count; i++) pool.add(r.buildSpinItem());
        }
        java.util.Collections.shuffle(pool);
        if (pool.size() < SPIN_SLOTS) {
            int base = pool.size();
            for (int i = 0; pool.size() < SPIN_SLOTS; i++)
                pool.add(pool.get(i % base).clone());
        }
        return pool;
    }

    public Inventory getInventory() { return inv; }
}
