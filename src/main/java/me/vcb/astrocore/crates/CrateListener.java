package me.vcb.astrocore.crates;

import me.vcb.astrocore.crates.CrateManager.PendingInput;
import me.vcb.astrocore.crates.gui.*;
import me.vcb.astrocore.utils.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class CrateListener implements Listener {

    // ── Block interact ────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK
            && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        CrateManager cm = CrateManager.getInstance();
        Crate crate = cm.getCrateByLocation(block.getLocation());
        if (crate == null) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            CratePreviewGui.open(player, crate);
            return;
        }

        if (cm.isAnimating(player.getUniqueId())) {
            player.sendMessage(Color.translate("§cYa estás abriendo una crate."));
            return;
        }
        if (cm.isOnCooldown(player.getUniqueId())) return;
        cm.setCooldown(player.getUniqueId());

        var hand = player.getInventory().getItemInMainHand();
        if (!CrateKey.isKeyFor(hand, crate)) {
            player.sendMessage(Color.translate("§cNecesitás una llave de §f"
                + crate.getDisplayName() + "§c para abrir esta crate."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        if (hand.getAmount() <= 1) player.getInventory().setItemInMainHand(null);
        else hand.setAmount(hand.getAmount() - 1);

        block.getWorld().playSound(block.getLocation(), crate.getBukkitSound(), 1f, 1f);

        if (crate.getAnimationType() == CrateAnimationType.INSTANT) {
            cm.giveInstantReward(player, crate);
            return;
        }
        new SpinAnimation(player, crate).start();
    }

    // ── Protect crate block ───────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        if (CrateManager.getInstance().getCrateByLocation(event.getBlock().getLocation()) == null) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(Color.translate("§cNo podés romper el bloque de una crate."));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDamage(BlockDamageEvent event) {
        if (CrateManager.getInstance().getCrateByLocation(event.getBlock().getLocation()) == null) return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (CrateManager.getInstance().getCrateByLocation(event.getBlockPlaced().getLocation()) != null)
            event.setCancelled(true);
    }

    // ── Inventory clicks ──────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        CrateManager cm = CrateManager.getInstance();

        // Block during spin
        if (cm.isAnimating(player.getUniqueId())) {
            SpinAnimation anim = cm.getAnimation(player.getUniqueId());
            if (anim != null && event.getInventory().equals(anim.getInventory()))
                event.setCancelled(true);
            return;
        }

        if (!CrateGuiTracker.isInCrateGui(player)) return;

        int slot = event.getRawSlot();
        var type = CrateGuiTracker.getType(player);

        // REWARD_GRID: fully free inventory — only intercept right-clicks on grid slots
        if (type == CrateGuiTracker.GuiType.REWARD_GRID) {
            if (slot >= 0 && slot < CrateRewardGridGui.GRID_SIZE && event.isRightClick()) {
                handleGridRightClick(player, slot, event);
            }
            // Everything else (player inv clicks, left-clicks, shift-clicks): NOT cancelled
            return;
        }

        // All other GUIs: cancel everything
        event.setCancelled(true);
        if (slot >= event.getInventory().getSize()) return; // player inventory area

        switch (type) {
            case EDITOR_LIST     -> handleEditorListClick(player, slot);
            case EDITOR          -> handleEditorClick(player, slot, event);
            case PARTICLE_SELECT -> handleParticleClick(player, slot);
            case SOUND_SELECT    -> handleSoundClick(player, slot);
            case HOLOGRAM_EDITOR -> handleHologramClick(player, slot, event);
            case ANIMATION_SELECT -> handleAnimationClick(player, slot);
            case PREVIEW            -> {
                if (slot == 49) player.closeInventory();
            }
        }
    }

    // ── Drag in reward grid (allow free dragging) ─────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (CrateGuiTracker.getType(player) != CrateGuiTracker.GuiType.REWARD_GRID) return;

        // Allow all drags in the reward grid — no restrictions
    }

    // ── Grid save ─────────────────────────────────────────────────────────────

    /** Scans slots 0-53, syncs to crate.rewards preserving winnable/chance/commands. */
    private void saveGridState(Crate crate, org.bukkit.inventory.Inventory inv) {
        // Remove rewards whose slot now has a different item or is empty
        crate.getRewards().removeIf(r -> {
            ItemStack current = inv.getItem(r.getSlot());
            return current == null || current.getType().isAir();
        });

        for (int slot = 0; slot < CrateRewardGridGui.GRID_SIZE; slot++) { // GRID_SIZE = 54
            ItemStack item = inv.getItem(slot);
            if (item == null || item.getType().isAir()) continue;

            CrateReward existing = crate.getRewardAtSlot(slot);
            ItemStack cleaned = cleanItem(item);
            if (existing == null) {
                crate.getRewards().add(new CrateReward(slot, cleaned));
            } else {
                ItemStack stored = existing.getItem();
                if (stored == null || stored.getType() != cleaned.getType()) {
                    // Different item placed in this slot — reset reward entirely
                    crate.getRewards().remove(existing);
                    crate.getRewards().add(new CrateReward(slot, cleaned));
                } else {
                    existing.setItem(cleaned);
                }
            }
        }

        CrateManager.getInstance().save();
    }

    /** Strip glow override before saving the raw item. */
    private ItemStack cleanItem(ItemStack item) {
        ItemStack clean = item.clone();
        var meta = clean.getItemMeta();
        if (meta == null) return clean;
        meta.setEnchantmentGlintOverride(false);
        if ("§r".equals(meta.getDisplayName())) meta.setDisplayName(null);
        clean.setItemMeta(meta);
        return clean;
    }

    // ── Grid right-click ──────────────────────────────────────────────────────

    private void handleGridRightClick(Player player, int slot, InventoryClickEvent event) {
        // If player has item on cursor they're trying to place — don't intercept
        ItemStack cursor = event.getCursor();
        if (cursor != null && !cursor.getType().isAir()) return;

        ItemStack item = event.getInventory().getItem(slot);
        if (item == null || item.getType().isAir()) return;

        event.setCancelled(true);

        String crateId = CrateGuiTracker.getCrateId(player);
        Crate  crate   = CrateManager.getInstance().getCrate(crateId);
        if (crate == null) return;

        // Get or create reward in memory (no YAML save yet — saved on close)
        CrateReward reward = crate.getRewardAtSlot(slot);
        if (reward == null) {
            reward = new CrateReward(slot, cleanItem(item));
            crate.getRewards().add(reward);
        }

        if (event.isShiftClick()) {
            // Shift+right-click: remove winnable
            reward.setWinnable(false);
            event.getInventory().setItem(slot, reward.buildGridItem());
            player.sendMessage(Color.translate("§cItem quitado de ganables."));
            return;
        }

        if (!reward.isWinnable()) {
            // First right-click: make winnable, show glow, stay in grid
            reward.setWinnable(true);
            event.getInventory().setItem(slot, reward.buildGridItem());
            player.sendMessage(Color.translate("§aGanable activado. Click der de nuevo para setear el peso."));
        } else {
            // Already winnable: ask for chance via chat (closes grid, returns after input)
            promptChat(player, crateId, PendingInput.InputType.SLOT_CHANCE, slot,
                "§eEscribi el peso para este item (actual: §f" + reward.getChance()
                + "§e). Mayor número = más probable:");
        }
    }

    // ── Hologram editor ───────────────────────────────────────────────────────

    private void handleHologramClick(Player player, int slot, InventoryClickEvent event) {
        String crateId = CrateGuiTracker.getCrateId(player);
        Crate  crate   = CrateManager.getInstance().getCrate(crateId);
        if (crate == null) return;

        if (slot == 45) { CrateEditorGui.open(player, crate); return; }

        if (slot == 49) {
            // Add new line
            promptChat(player, crateId, PendingInput.InputType.HOLOGRAM_LINE_ADD, -1,
                "§eEscribi el texto para la nueva línea (usa & para colores):");
            return;
        }

        int lineIdx = CrateHologramGui.getLineIndex(slot);
        if (lineIdx < 0 || lineIdx >= crate.getHologramLines().size()) return;

        if (event.isRightClick()) {
            // Delete line
            crate.getHologramLines().remove(lineIdx);
            CrateManager.getInstance().save();
            CrateManager.getInstance().refreshHologram(crate);
            player.sendMessage(Color.translate("§cLínea " + (lineIdx + 1) + " borrada."));
            CrateHologramGui.open(player, crate);
        } else {
            // Edit line
            promptChat(player, crateId, PendingInput.InputType.HOLOGRAM_LINE_EDIT, lineIdx,
                "§eEscribi el nuevo texto para la línea §f" + (lineIdx + 1) + " §e(usa & para colores):");
        }
    }

    // ── Editor list ───────────────────────────────────────────────────────────

    private void handleEditorListClick(Player player, int slot) {
        var crates = CrateManager.getInstance().getCrates();
        int s = 10;
        for (int i = 0; i < crates.size(); i++) {
            if (s == slot) { CrateEditorGui.open(player, crates.get(i)); return; }
            s++;
            if (s % 9 == 8) s += 2;
        }
    }

    // ── Editor main ───────────────────────────────────────────────────────────

    private void handleEditorClick(Player player, int slot, InventoryClickEvent event) {
        String crateId = CrateGuiTracker.getCrateId(player);
        Crate  crate   = CrateManager.getInstance().getCrate(crateId);
        if (crate == null) return;

        switch (slot) {
            case 10 -> promptChat(player, crateId, PendingInput.InputType.DISPLAY_NAME, -1,
                "§eEscribi el nuevo nombre de la crate (usa & para colores):");
            case 12 -> CrateHologramGui.open(player, crate);
            case 14 -> CrateParticleGui.open(player, crateId);
            case 16 -> CrateSoundGui.open(player, crateId);
            case 20 -> {
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand == null || hand.getType().isAir()) {
                    player.sendMessage(Color.translate("§cTené el item deseado en la mano."));
                } else {
                    crate.setKeyMaterial(hand.getType().name());
                    CrateManager.getInstance().save();
                    player.sendMessage(Color.translate("§aLlave seteada: §f" + hand.getType().name()));
                    CrateEditorGui.open(player, crate);
                }
            }
            case 24 -> promptChat(player, crateId, PendingInput.InputType.KEY_DESCRIPTION, -1,
                "§eEscribi la descripción de la llave. Usa §f%crate% §epara el nombre:");
            case 28 -> CrateRewardGridGui.open(player, crate);
            case 32 -> CrateAnimationGui.open(player, crate);
            case 38 -> {
                if (event.isShiftClick()) {
                    crate.getMilestoneCommands().clear();
                    CrateManager.getInstance().save();
                    player.sendMessage(Color.translate("§cComandos de hito limpiados."));
                    CrateEditorGui.open(player, crate);
                } else if (event.isRightClick()) {
                    promptChat(player, crateId, PendingInput.InputType.MILESTONE_COMMAND_ADD, -1,
                        "§eEscribi un comando de hito. Placeholders: §f%player% %crate% %count%");
                } else {
                    promptChat(player, crateId, PendingInput.InputType.MILESTONE_INTERVAL, -1,
                        "§eEscribi cada cuántas aperturas dar la recompensa (0 = desactivar):");
                }
            }
            case 34 -> {
                CrateManager.getInstance().deleteCrate(crateId);
                player.sendMessage(Color.translate("§cCrate §f" + crateId + " §celiminada."));
                CrateEditorListGui.open(player);
            }
            case 40 -> CrateEditorListGui.open(player);
        }
    }

    // ── Particle / Sound select ───────────────────────────────────────────────

    private void handleParticleClick(Player player, int slot) {
        String crateId = CrateGuiTracker.getCrateId(player);
        Crate  crate   = CrateManager.getInstance().getCrate(crateId);
        if (crate == null) { player.closeInventory(); return; }

        if (CrateParticleGui.isBackSlot(slot)) { CrateEditorGui.open(player, crate); return; }

        Particle p = CrateParticleGui.getParticleForSlot(slot);
        if (p != null) {
            crate.setParticle(p.name());
            CrateManager.getInstance().save();
            player.sendMessage(Color.translate("§aPartícula: §f" + p.name()));
            CrateEditorGui.open(player, crate);
        }
    }

    private void handleSoundClick(Player player, int slot) {
        String crateId = CrateGuiTracker.getCrateId(player);
        Crate  crate   = CrateManager.getInstance().getCrate(crateId);
        if (crate == null) { player.closeInventory(); return; }

        if (CrateSoundGui.isBackSlot(slot)) { CrateEditorGui.open(player, crate); return; }

        Sound s = CrateSoundGui.getSoundForSlot(slot);
        if (s != null) {
            crate.setSound(s.name());
            CrateManager.getInstance().save();
            player.playSound(player.getLocation(), s, 1f, 1f);
            player.sendMessage(Color.translate("§aSonido: §f" + s.name()));
            CrateEditorGui.open(player, crate);
        }
    }

    private void handleAnimationClick(Player player, int slot) {
        String crateId = CrateGuiTracker.getCrateId(player);
        Crate  crate   = CrateManager.getInstance().getCrate(crateId);
        if (crate == null) { player.closeInventory(); return; }

        if (CrateAnimationGui.isBackSlot(slot)) { CrateEditorGui.open(player, crate); return; }

        CrateAnimationType t = CrateAnimationGui.getTypeForSlot(slot);
        if (t != null) {
            crate.setAnimationType(t);
            CrateManager.getInstance().save();
            player.sendMessage(Color.translate("§aAnimación: §f" + t.getDisplayName()));
            CrateAnimationGui.open(player, crate);
        }
    }

    // ── Chat input ────────────────────────────────────────────────────────────

    private void promptChat(Player player, String crateId,
                             PendingInput.InputType type, int extra, String msg) {
        player.closeInventory();
        player.sendMessage(Color.translate(msg));
        player.sendMessage(Color.translate("§7Escribí §ccancel §7para cancelar."));
        CrateManager.getInstance().setPendingInput(player.getUniqueId(),
            new PendingInput(crateId, type, extra));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        CrateManager cm = CrateManager.getInstance();
        if (!cm.hasPendingInput(player.getUniqueId())) return;

        event.setCancelled(true);
        PendingInput input = cm.getPendingInput(player.getUniqueId());
        cm.setPendingInput(player.getUniqueId(), null);

        String msg = event.getMessage().trim();
        if (msg.equalsIgnoreCase("cancel")) {
            player.sendMessage(Color.translate("§cCancelado."));
            reopenGui(player, input);
            return;
        }

        Crate crate = cm.getCrate(input.crateId());
        if (crate == null) return;

        switch (input.type()) {
            case DISPLAY_NAME -> {
                crate.setDisplayName(msg); cm.save(); cm.refreshHologram(crate);
                player.sendMessage(Color.translate("§aNombre actualizado."));
            }
            case HOLOGRAM_LINE_EDIT -> {
                int lineIdx = input.rewardIndex();
                var lines = crate.getHologramLines();
                if (lineIdx >= 0 && lineIdx < lines.size()) {
                    lines.set(lineIdx, msg);
                    cm.save(); cm.refreshHologram(crate);
                    player.sendMessage(Color.translate("§aLínea " + (lineIdx + 1) + " actualizada."));
                }
            }
            case HOLOGRAM_LINE_ADD -> {
                crate.getHologramLines().add(msg);
                cm.save(); cm.refreshHologram(crate);
                player.sendMessage(Color.translate("§aLínea agregada."));
            }
            case KEY_DESCRIPTION -> {
                crate.setKeyDescription(msg); cm.save();
                player.sendMessage(Color.translate("§aDescripción actualizada."));
            }
            case MILESTONE_INTERVAL -> {
                try {
                    crate.setMilestoneInterval(Math.max(0, Integer.parseInt(msg)));
                    cm.save();
                    player.sendMessage(Color.translate("§aIntervalo de hito: §f"
                        + crate.getMilestoneInterval()));
                } catch (Exception e) { player.sendMessage(Color.translate("§cNúmero inválido.")); }
            }
            case MILESTONE_COMMAND_ADD -> {
                String cmd = msg.startsWith("/") ? msg.substring(1) : msg;
                crate.getMilestoneCommands().add(cmd);
                cm.save();
                player.sendMessage(Color.translate("§aComando de hito agregado: §f/" + cmd));
            }
            case SLOT_CHANCE -> {
                CrateReward r = crate.getRewardAtSlot(input.rewardIndex());
                if (r != null) {
                    try {
                        r.setChance(Math.max(1, Integer.parseInt(msg)));
                        player.sendMessage(Color.translate("§aPeso: §f" + r.getChance()));
                    } catch (Exception e) { player.sendMessage(Color.translate("§cNúmero inválido.")); }
                }
            }
            case SLOT_COMMAND -> {
                CrateReward r = crate.getRewardAtSlot(input.rewardIndex());
                if (r != null) {
                    r.getCommands().add(msg);
                    player.sendMessage(Color.translate("§aComando: §f/" + msg));
                }
            }
        }

        reopenGui(player, input);
    }

    private void reopenGui(Player player, PendingInput input) {
        Crate crate = CrateManager.getInstance().getCrate(input.crateId());
        me.vcb.astrocore.utils.Tasks.sync(() -> {
            if (crate == null) { CrateEditorListGui.open(player); return; }
            switch (input.type()) {
                case DISPLAY_NAME, KEY_DESCRIPTION,
                     MILESTONE_INTERVAL, MILESTONE_COMMAND_ADD -> CrateEditorGui.open(player, crate);
                case HOLOGRAM_LINE_EDIT, HOLOGRAM_LINE_ADD -> CrateHologramGui.open(player, crate);
                case SLOT_CHANCE, SLOT_COMMAND -> CrateRewardGridGui.open(player, crate);
            }
        });
    }

    // ── Join ──────────────────────────────────────────────────────────────────

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        me.vcb.astrocore.utils.Tasks.syncLater(
            () -> CrateManager.getInstance().renderForPlayer(event.getPlayer()), 10L);
    }

    // ── Close ─────────────────────────────────────────────────────────────────

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        CrateManager cm = CrateManager.getInstance();

        // Spin skip on Escape
        if (cm.isAnimating(player.getUniqueId())) {
            SpinAnimation anim = cm.getAnimation(player.getUniqueId());
            if (anim != null && !anim.isFinished()
                    && event.getInventory().equals(anim.getInventory())) {
                me.vcb.astrocore.utils.Tasks.sync(anim::skip);
            }
            return;
        }

        // Auto-save reward grid on close
        if (CrateGuiTracker.getType(player) == CrateGuiTracker.GuiType.REWARD_GRID) {
            String crateId = CrateGuiTracker.getCrateId(player);
            Crate  crate   = cm.getCrate(crateId);
            if (crate != null) saveGridState(crate, event.getInventory());
        }

        CrateGuiTracker.clearIfNotTransitioning(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        CrateGuiTracker.clear(event.getPlayer());
        CrateManager.getInstance().setPendingInput(event.getPlayer().getUniqueId(), null);
    }
}
