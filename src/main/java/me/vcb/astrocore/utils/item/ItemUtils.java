package me.vcb.astrocore.utils.item;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.utils.StringUtils;
import me.vcb.astrocore.utils.Tasks;
import me.vcb.astrocore.utils.nms.NmsUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;

public class ItemUtils {

    public static final List<int[]> BLOCK_RELATIVES;
    public static final Set<Material> HOE_ITEMS;
    public static final Predicate<ItemStack> NOT_NULL_ITEM_FILTER;

    public static Enchantment FAKE_GLOW;

    private static Map<String, String> ITEMS_BY_NAME;
    private static Map<String, String> ITEMS_BY_MATERIAL;

    public static void updateInventory(Player player) {
        Tasks.sync(player::updateInventory);
    }

    public static void removeOneItem(Player player) {
        if(player.getItemInHand().getAmount() > 1) {
            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
            return;
        }

        player.getInventory().setItemInHand(new ItemStack(Material.AIR));
    }

    // TODO: verify material for 1.21 — MaterialData removed; callers should migrate to Material-based overloads
    public static int getItemAmount(Player player, Material material) {
        int amount = 0;

        for(ItemStack item : player.getInventory().getContents()) {
            if(item == null || item.getType() != material) continue;
            amount += item.getAmount();
        }

        return amount;
    }

    public static void removeItem(Inventory inventory, Material material, int amount) {
        ItemStack[] contents = inventory.getContents();

        for(int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];

            if(item == null || material != item.getType()) continue;

            if(amount >= item.getAmount()) {
                amount -= item.getAmount();
                inventory.setItem(i, null);
            } else {
                item.setAmount(item.getAmount() - amount);
                break;
            }
        }
    }

    public static ItemStack parseItem(String value) {
        // In 1.21, numeric material IDs are no longer supported; use material names only.
        String itemData = ITEMS_BY_NAME.get(value);

        if(itemData != null) {
            String[] itemArray = itemData.split(":");

            Material material = Material.getMaterial(itemArray[0]);
            short durability = itemArray.length > 1 ? Short.parseShort(itemArray[1]) : 0;

            if(material != null) {
                // items.yml mapping resolved to a valid material
                if(material == Material.SPAWNER) {
                    EntityType entityType = ItemUtils.getSpawnerType(durability);
                    return NmsUtils.getInstance().createMobSpawnerItemStack(entityType, null);
                }
                return new ItemStack(material, 1);
            }
            // items.yml has a stale/numeric entry — fall through to direct name lookup below
        }

        Material material = Material.getMaterial(value);

        if(material != null) {
            return new ItemStack(material);
        }

        // Support "MATERIAL_NAME:durability" or legacy "ID:data" format.
        if(value.contains(":")) {
            String[] itemArray = value.split(":");
            if(itemArray.length < 2) return null;

            // Try as a modern material name first.
            material = Material.getMaterial(itemArray[0]);

            // Otherwise treat itemArray[0] as a legacy numeric ID (e.g. "353:0" = sugar).
            if(material == null) {
                Integer id = tryParseInt(itemArray[0]);
                if(id == null) {
                    AstroCore.getInstance().log("&c[item] parseItem failed (not a name/id): " + value);
                    return null;
                }

                Integer dataInt = tryParseInt(itemArray[1]);
                byte data = (byte) (dataInt != null ? dataInt : 0);

                // Primary: explicit table for the legacy item IDs used in configs.
                // Guarantees correct resolution regardless of Paper's legacy support.
                Material manual = manualLegacyMaterial(id, data);
                if(manual != null) return new ItemStack(manual, 1);

                Material legacy = legacyMaterialById(id);
                if(legacy != null) {
                    if(legacy == Material.LEGACY_MOB_SPAWNER) {
                        EntityType entityType = ItemUtils.getSpawnerType(data);
                        return NmsUtils.getInstance().createMobSpawnerItemStack(entityType, null);
                    }
                    try {
                        @SuppressWarnings("deprecation")
                        Material modern = Bukkit.getUnsafe().fromLegacy(new MaterialData(legacy, data), true);
                        if(modern != null && modern != Material.AIR) {
                            return new ItemStack(modern, 1);
                        }
                    } catch(Throwable t) {
                        AstroCore.getInstance().log("&c[item] fromLegacy threw for " + value + ": " + t.getMessage());
                    }
                }

                // Fallback: resolve via items.yml friendly name (id:data -> "Sugar" -> SUGAR).
                String friendly = ITEMS_BY_MATERIAL.get(value);
                if(friendly != null) {
                    Material byName = Material.matchMaterial(friendly);
                    if(byName != null) return new ItemStack(byName, 1);
                }

                AstroCore.getInstance().log("&c[item] parseItem could not resolve legacy id: " + value
                        + " (legacy=" + legacy + ", friendly=" + friendly + ")");
                return null;
            }

            if(material == Material.SPAWNER) {
                short durability = Short.parseShort(itemArray[1]);
                EntityType entityType = ItemUtils.getSpawnerType(durability);
                return NmsUtils.getInstance().createMobSpawnerItemStack(entityType, null);
            }

            return new ItemStack(material, 1);
        }

        return null;
    }

    private static Integer tryParseInt(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch(NumberFormatException e) { return null; }
    }

    /** Explicit pre-1.13 numeric ID -> modern Material for the items used in class/kit configs. */
    private static Material manualLegacyMaterial(int id, byte data) {
        switch(id) {
            case 263: return data == 1 ? Material.CHARCOAL : Material.COAL;
            case 265: return Material.IRON_INGOT;
            case 266: return Material.GOLD_INGOT;
            case 280: return Material.STICK;
            case 288: return Material.FEATHER;
            case 295: return Material.WHEAT_SEEDS;
            case 318: return Material.FLINT;
            case 322: return Material.GOLDEN_APPLE;
            case 332: return Material.SNOWBALL;
            case 341: return Material.SLIME_BALL;
            case 348: return Material.GLOWSTONE_DUST;
            case 351: // dye / ink sack — data = colour
                switch(data) {
                    case 0:  return Material.INK_SAC;
                    case 1:  return Material.RED_DYE;
                    case 2:  return Material.GREEN_DYE;
                    case 3:  return Material.COCOA_BEANS;
                    case 4:  return Material.LAPIS_LAZULI;
                    case 15: return Material.BONE_MEAL;
                    default: return Material.GRAY_DYE;
                }
            case 367: return Material.ROTTEN_FLESH;
            case 370: return Material.GHAST_TEAR;
            case 371: return Material.GOLD_NUGGET;
            case 375: return Material.SPIDER_EYE;
            case 376: return Material.FERMENTED_SPIDER_EYE;
            case 377: return Material.BLAZE_POWDER;
            case 378: return Material.MAGMA_CREAM;
            case 379: return Material.BREWING_STAND;
            case 382: return Material.GLISTERING_MELON_SLICE;
            default:  return null;
        }
    }

    private static Map<Integer, Material> LEGACY_BY_ID;

    /** Resolves a legacy (pre-1.13) numeric item/block ID to its LEGACY_* Material. */
    @SuppressWarnings("deprecation")
    private static Material legacyMaterialById(int id) {
        if(LEGACY_BY_ID == null) {
            LEGACY_BY_ID = new HashMap<>();
            for(Material m : Material.values()) {
                if(!m.isLegacy()) continue;
                try {
                    LEGACY_BY_ID.putIfAbsent(m.getId(), m);
                } catch(Throwable ignore) { /* non-legacy id access guarded by isLegacy */ }
            }
        }
        return LEGACY_BY_ID.get(id);
    }

    public static String getItemName(ItemStack item) {
        // In 1.21, numeric type IDs are removed; look up by material name
        String itemName = ITEMS_BY_MATERIAL.get(item.getType().name() + ":0");
        return itemName != null ? itemName : item.getType().name();
    }

    public static String getMaterialName(Material material) {
        String name = ITEMS_BY_MATERIAL.get(material.name() + ":0");
        return name != null ? name : material.name();
    }

    public static boolean hasEnchantment(ItemStack item, Enchantment enchantment) {
        return item != null && item.containsEnchantment(enchantment);
    }

    public static void setupItemStackMaps() {
        ITEMS_BY_NAME = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        ITEMS_BY_MATERIAL = new HashMap<>();

        AstroCore.getInstance().getItemsFile().getKeys(false).forEach(name ->
            ITEMS_BY_NAME.put(name, AstroCore.getInstance().getItemsFile().getString(name)));

        ITEMS_BY_NAME.forEach((name, id) -> ITEMS_BY_MATERIAL.put(id, name));
    }

    public static boolean isHoeItem(Material material) {
        return HOE_ITEMS.contains(material);
    }

    public static boolean isOre(Material material) {
        switch(material) {
            case DIAMOND_ORE:
            case EMERALD_ORE:
            case GOLD_ORE:
            case IRON_ORE:
            case LAPIS_ORE:
            case REDSTONE_ORE:
            case COAL_ORE:
            case NETHER_QUARTZ_ORE:
                return true;
            default:
                return false;
        }
    }

    public static boolean isArmor(Material material) {
        switch(material) {
            case DIAMOND_HELMET:
            case GOLDEN_HELMET:
            case IRON_HELMET:
            case CHAINMAIL_HELMET:
            case LEATHER_HELMET:
            case DIAMOND_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case IRON_CHESTPLATE:
            case CHAINMAIL_CHESTPLATE:
            case LEATHER_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case IRON_LEGGINGS:
            case CHAINMAIL_LEGGINGS:
            case LEATHER_LEGGINGS:
            case DIAMOND_BOOTS:
            case GOLDEN_BOOTS:
            case IRON_BOOTS:
            case CHAINMAIL_BOOTS:
            case LEATHER_BOOTS:
                return true;
            default:
                return false;
        }
    }

    public static boolean isSalvageable(Material material) {
        switch(material) {
            case DIAMOND_HELMET:
            case GOLDEN_HELMET:
            case IRON_HELMET:
            case CHAINMAIL_HELMET:
            case LEATHER_HELMET:
            case DIAMOND_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case IRON_CHESTPLATE:
            case CHAINMAIL_CHESTPLATE:
            case LEATHER_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case IRON_LEGGINGS:
            case CHAINMAIL_LEGGINGS:
            case LEATHER_LEGGINGS:
            case DIAMOND_BOOTS:
            case GOLDEN_BOOTS:
            case IRON_BOOTS:
            case CHAINMAIL_BOOTS:
            case LEATHER_BOOTS:
            case DIAMOND_SWORD:
            case GOLDEN_SWORD:
            case IRON_SWORD:
            case STONE_SWORD:
            case WOODEN_SWORD:
            case DIAMOND_PICKAXE:
            case GOLDEN_PICKAXE:
            case IRON_PICKAXE:
            case STONE_PICKAXE:
            case WOODEN_PICKAXE:
            case DIAMOND_AXE:
            case GOLDEN_AXE:
            case IRON_AXE:
            case STONE_AXE:
            case WOODEN_AXE:
            case DIAMOND_SHOVEL:
            case GOLDEN_SHOVEL:
            case IRON_SHOVEL:
            case STONE_SHOVEL:
            case WOODEN_SHOVEL:
            case DIAMOND_HOE:
            case GOLDEN_HOE:
            case IRON_HOE:
            case STONE_HOE:
            case WOODEN_HOE:
            case BOW:
                return true;
            default:
                return false;
        }
    }

    public static boolean isRepairable(Material material) {
        switch(material) {
            case DIAMOND_HELMET:
            case GOLDEN_HELMET:
            case IRON_HELMET:
            case CHAINMAIL_HELMET:
            case LEATHER_HELMET:
            case DIAMOND_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case IRON_CHESTPLATE:
            case CHAINMAIL_CHESTPLATE:
            case LEATHER_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case IRON_LEGGINGS:
            case CHAINMAIL_LEGGINGS:
            case LEATHER_LEGGINGS:
            case DIAMOND_BOOTS:
            case GOLDEN_BOOTS:
            case IRON_BOOTS:
            case CHAINMAIL_BOOTS:
            case LEATHER_BOOTS:
            case DIAMOND_SWORD:
            case GOLDEN_SWORD:
            case IRON_SWORD:
            case STONE_SWORD:
            case WOODEN_SWORD:
            case DIAMOND_PICKAXE:
            case GOLDEN_PICKAXE:
            case IRON_PICKAXE:
            case STONE_PICKAXE:
            case WOODEN_PICKAXE:
            case DIAMOND_AXE:
            case GOLDEN_AXE:
            case IRON_AXE:
            case STONE_AXE:
            case WOODEN_AXE:
            case DIAMOND_SHOVEL:
            case GOLDEN_SHOVEL:
            case IRON_SHOVEL:
            case STONE_SHOVEL:
            case WOODEN_SHOVEL:
            case DIAMOND_HOE:
            case GOLDEN_HOE:
            case IRON_HOE:
            case STONE_HOE:
            case WOODEN_HOE:
            case FISHING_ROD:
            case BOW:
            case FLINT_AND_STEEL:
                return true;
            default:
                return false;
        }
    }

    public static EntityType getSpawnerType(short durability) {
        switch(durability) {
            case 50: return EntityType.CREEPER;
            case 51: return EntityType.SKELETON;
            case 52: return EntityType.SPIDER;
            case 53: return EntityType.GIANT;
            case 54: return EntityType.ZOMBIE;
            case 55: return EntityType.SLIME;
            case 56: return EntityType.GHAST;
            case 57: return EntityType.ZOMBIFIED_PIGLIN;
            case 58: return EntityType.ENDERMAN;
            case 59: return EntityType.CAVE_SPIDER;
            case 60: return EntityType.SILVERFISH;
            case 61: return EntityType.BLAZE;
            case 62: return EntityType.MAGMA_CUBE;
            case 63: return EntityType.ENDER_DRAGON;
            case 64: return EntityType.WITHER;
            case 65: return EntityType.BAT;
            case 66: return EntityType.WITCH;
            case 91: return EntityType.SHEEP;
            case 92: return EntityType.COW;
            case 93: return EntityType.CHICKEN;
            case 94: return EntityType.SQUID;
            case 95: return EntityType.WOLF;
            case 96: return EntityType.MOOSHROOM;
            case 97: return EntityType.SNOW_GOLEM;
            case 98: return EntityType.OCELOT;
            case 99: return EntityType.IRON_GOLEM;
            case 100: return EntityType.HORSE;
            default: return EntityType.PIG;
        }
    }

    public static int getSpawnerDurability(EntityType type) {
        switch(type) {
            case CREEPER: return 50;
            case SKELETON: return 51;
            case SPIDER: return 52;
            case GIANT: return 53;
            case ZOMBIE: return 54;
            case SLIME: return 55;
            case GHAST: return 56;
            case ZOMBIFIED_PIGLIN: return 57;
            case ENDERMAN: return 58;
            case CAVE_SPIDER: return 59;
            case SILVERFISH: return 60;
            case BLAZE: return 61;
            case MAGMA_CUBE: return 62;
            case ENDER_DRAGON: return 63;
            case WITHER: return 64;
            case BAT: return 65;
            case WITCH: return 66;
            case SHEEP: return 91;
            case COW: return 92;
            case CHICKEN: return 93;
            case SQUID: return 94;
            case WOLF: return 95;
            case MOOSHROOM: return 96;
            case SNOW_GOLEM: return 97;
            case OCELOT: return 98;
            case IRON_GOLEM: return 99;
            case HORSE: return 100;
            default: return 90;
        }
    }

    public static void registerFakeEnchantmentGlow() {
        // Enchantment registration via reflection is not needed in Paper 1.21+
        // FakeGlow is already registered via EnchantmentWrapper constructor
    }

    static {
        BLOCK_RELATIVES = new ArrayList<>();

        for(int x = -1; x <= 1; x++) {
            for(int y = -1; y <= 1; y++) {
                for(int z = -1; z <= 1; z++) {
                    BLOCK_RELATIVES.add(new int[] { x, y, z });
                }
            }
        }

        NOT_NULL_ITEM_FILTER = item -> item != null && item.getType() != Material.AIR;

        FAKE_GLOW = null; // no-op in Paper 1.21; use setEnchantmentGlintOverride instead

        HOE_ITEMS = EnumSet.of(Material.DIAMOND_HOE, Material.GOLDEN_HOE,
            Material.IRON_HOE, Material.WOODEN_HOE, Material.STONE_HOE);
    }
}
