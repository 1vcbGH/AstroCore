package me.vcb.astrocore.handlers.block;

import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.Faction;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.factions.type.SystemFaction;
import me.vcb.astrocore.factions.type.WarzoneFaction;
import me.vcb.astrocore.handlers.manager.Handler;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.item.ItemBuilder;
import me.vcb.astrocore.utils.nms.NmsUtils;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CrowbarHandler extends Handler implements Listener {
	
	private final Pattern pattern;
	
	public CrowbarHandler() {
		this.pattern = Pattern.compile("\\d+");
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(!event.hasItem() || !this.isCrowbar(event.getItem())) return;

		Block block = event.getClickedBlock();

		if(block.getType() != Material.SPAWNER && block.getType() != Material.END_PORTAL_FRAME) {
			if(block.getType() == Material.GRASS_BLOCK || block.getType() == Material.DIRT) {
				event.setCancelled(true);
			}

			return;
		}

		event.setCancelled(true);
		Player player = event.getPlayer();

		if(block.getWorld().getEnvironment() == World.Environment.NETHER) {
			player.sendMessage(Language.PREFIX + Language.CROWBAR_DENY_USAGE_NETHER);
			return;
		}

		if(block.getWorld().getEnvironment() == World.Environment.THE_END) {
			player.sendMessage(Language.PREFIX + Language.CROWBAR_DENY_USAGE_END);
			return;
		}

		Faction factionAt = ClaimManager.getInstance().getFactionAt(block);

		if(Config.CROWBAR_DISABLE_IN_WARZONE && factionAt instanceof WarzoneFaction) {
			player.sendMessage(Language.PREFIX + Language.CROWBAR_DENY_USAGE_WARZONE);
			return;
		}

		if(factionAt instanceof SystemFaction) {
			player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_PROTECTION_DENY_INTERACT
				.replace("<faction>", factionAt.getDisplayName(player)));
			return;
		}

		PlayerFaction playerFaction = FactionsManager.getInstance().getPlayerFaction(player);

		if(factionAt instanceof PlayerFaction && factionAt != playerFaction && !((PlayerFaction) factionAt).isRaidable()) {
			player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_PROTECTION_DENY_INTERACT
				.replace("<faction>", factionAt.getDisplayName(player)));
			return;
		}

		switch(block.getType()) {
			case SPAWNER: {
				this.handleSpawnerBreak(player, event.getItem(), block);
				break;
			}
			case END_PORTAL_FRAME: {
				this.handlePortalFrameBreak(player, event.getItem(), block);
				break;
			}
		}
	}

	private void handleSpawnerBreak(Player player, ItemStack item, Block block) {
		int spawnerUses = this.getUses(item, 0);

		if(spawnerUses <= 0) {
			player.sendMessage(Language.PREFIX + Language.CROWBAR_ZERO_USAGES_SPAWNERS);
			return;
		}

		CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();
		EntityType entityType = creatureSpawner.getSpawnedType();

		String name = Config.CROWBAR_SPAWNER_NAME_COLOR + entityType.name() + " Spawner";
		ItemStack spawner = NmsUtils.getInstance().createMobSpawnerItemStack(entityType, name);

		block.setType(Material.AIR);
		block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, Material.SPAWNER);
		block.getWorld().dropItemNaturally(block.getLocation(), spawner);

		int portalUses = this.getUses(item, 1);

		if(--spawnerUses <= 0 && portalUses <= 0) {
			player.setItemInHand(new ItemStack(Material.AIR));
			return;
		}

		this.updateCrowbarMeta(item, spawnerUses, portalUses);
	}

	private void handlePortalFrameBreak(Player player, ItemStack item, Block block) {
		int portalUses = this.getUses(item, 1);

		if(portalUses <= 0) {
			player.sendMessage(Language.PREFIX + Language.CROWBAR_ZERO_USAGES_PORTALS);
			return;
		}

		block.setType(Material.AIR);
		block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, Material.END_PORTAL_FRAME);
		block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.END_PORTAL_FRAME));

		int spawnerUses = this.getUses(item, 0);

		if(--portalUses <= 0 && spawnerUses <= 0) {
			player.setItemInHand(new ItemStack(Material.AIR));
			return;
		}

		this.updateCrowbarMeta(item, spawnerUses, portalUses);
	}
	
	private boolean isCrowbar(ItemStack item) {
		return (item != null) && (item.getType() == Config.CROWBAR_ITEM.getType()) && (item.getItemMeta()
		.hasDisplayName()) && (item.getItemMeta().getDisplayName().equals(Config.CROWBAR_NAME));
	}
	
	private int getUses(ItemStack item, int a) {
		int uses = 0;

		Matcher matcher = pattern.matcher(Color.strip(item.getItemMeta().getLore().get(a)));
		while(matcher.find()) {
			uses = Integer.parseInt(matcher.group());
		}

		return uses;
	}
	
	public ItemStack getNewCrowbar() {
		ItemStack crowbar = Config.CROWBAR_ITEM.clone();
		this.updateCrowbarMeta(crowbar, Config.CROWBAR_SPAWNER_USES, Config.CROWBAR_PORTAL_USES);

		return crowbar;
	}

	private void updateCrowbarMeta(ItemStack item, int spawners, int portals) {
		ItemBuilder builder = new ItemBuilder(item).setName(Config.CROWBAR_NAME);

		builder.setLore(Config.CROWBAR_LORE.stream().map(line -> line
			.replace("<scount>", String.valueOf(spawners))
			.replace("<pcount>", String.valueOf(portals)))
			.collect(Collectors.toList()));

		item.setItemMeta(builder.getItemMeta());
	}
}
