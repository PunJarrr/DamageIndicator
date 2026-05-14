package dev.punjarrr.damageIndicator.utils;

import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlacklistedEntities {

    private static void addSafe(List<EntityType> list, String... names) {
        for (String name : names) {
            try {
                list.add(EntityType.valueOf(name));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public static List<EntityType> getBlacklistedEntities() {
        List<EntityType> blacklistedEntities = new ArrayList<>(Config.getBlacklistedEntities());

        blacklistedEntities.addAll(Arrays.asList(
                EntityType.AREA_EFFECT_CLOUD,
                EntityType.ARMOR_STAND,
                EntityType.ARROW,
                EntityType.BLOCK_DISPLAY,
                EntityType.BREEZE_WIND_CHARGE,
                EntityType.CHEST_MINECART,
                EntityType.COMMAND_BLOCK_MINECART,
                EntityType.DRAGON_FIREBALL,
                EntityType.EGG,
                EntityType.END_CRYSTAL,
                EntityType.ENDER_PEARL,
                EntityType.EVOKER_FANGS,
                EntityType.EXPERIENCE_ORB,
                EntityType.EYE_OF_ENDER,
                EntityType.FALLING_BLOCK,
                EntityType.FIREBALL,
                EntityType.FIREWORK_ROCKET,
                EntityType.FISHING_BOBBER,
                EntityType.FURNACE_MINECART,
                EntityType.GLOW_ITEM_FRAME,
                EntityType.HOPPER_MINECART,
                EntityType.INTERACTION,
                EntityType.ITEM,
                EntityType.ITEM_DISPLAY,
                EntityType.ITEM_FRAME,
                EntityType.LEASH_KNOT,
                EntityType.LIGHTNING_BOLT,
                EntityType.LLAMA_SPIT,
                EntityType.MARKER,
                EntityType.MINECART,
                EntityType.OMINOUS_ITEM_SPAWNER,
                EntityType.PAINTING,
                EntityType.SHULKER_BULLET,
                EntityType.SMALL_FIREBALL,
                EntityType.SNOWBALL,
                EntityType.SPAWNER_MINECART,
                EntityType.TEXT_DISPLAY,
                EntityType.TNT,
                EntityType.TNT_MINECART,
                EntityType.TRIDENT,
                EntityType.WIND_CHARGE,
                EntityType.WITHER_SKULL,
                EntityType.OAK_BOAT,        EntityType.OAK_CHEST_BOAT,
                EntityType.SPRUCE_BOAT,     EntityType.SPRUCE_CHEST_BOAT,
                EntityType.BIRCH_BOAT,      EntityType.BIRCH_CHEST_BOAT,
                EntityType.JUNGLE_BOAT,     EntityType.JUNGLE_CHEST_BOAT,
                EntityType.ACACIA_BOAT,     EntityType.ACACIA_CHEST_BOAT,
                EntityType.DARK_OAK_BOAT,   EntityType.DARK_OAK_CHEST_BOAT,
                EntityType.MANGROVE_BOAT,   EntityType.MANGROVE_CHEST_BOAT,
                EntityType.BAMBOO_RAFT,     EntityType.BAMBOO_CHEST_RAFT,
                EntityType.CHERRY_BOAT,     EntityType.CHERRY_CHEST_BOAT
        ));

        // Added after 1.21.1
        addSafe(blacklistedEntities,
                "PALE_OAK_BOAT", "PALE_OAK_CHEST_BOAT", // 1.21.4+
                "MANNEQUIN"                              // 1.21.11+
        );

        return blacklistedEntities;
    }
}
