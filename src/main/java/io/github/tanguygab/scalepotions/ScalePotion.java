package io.github.tanguygab.scalepotions;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScalePotion {

    public final static NamespacedKey SCALE_POTION_KEY = new NamespacedKey(ScalePotions.getPlugin(ScalePotions.class),"scale-potion");

    private final String name;
    private final String displayName;
    private final List<String> lore;
    private final Color color;
    private final int seconds;
    private final Map<Attribute,Double> attributes = new HashMap<>();

    public ScalePotion(String name, String displayName, List<String> lore, Color color, int seconds, double scale, double stepHeight, double blockReach, double entityReach) {
        this.name = name;
        this.displayName = displayName;
        this.lore = lore;
        this.color = color;
        this.seconds = seconds;
        attributes.put(Attribute.GENERIC_SCALE,scale);
        attributes.put(Attribute.GENERIC_STEP_HEIGHT,stepHeight);
        attributes.put(Attribute.PLAYER_BLOCK_INTERACTION_RANGE,blockReach);
        attributes.put(Attribute.PLAYER_ENTITY_INTERACTION_RANGE,entityReach);
    }

    public ItemStack getPotion(int amount, Material type) {
        ItemStack potion = new ItemStack(type,amount);
        if (potion.getItemMeta() instanceof PotionMeta meta) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            meta.setColor(color);

            meta.getPersistentDataContainer().set(SCALE_POTION_KEY, PersistentDataType.STRING,name);

            potion.setItemMeta(meta);
        }
        return potion;
    }

    public int getSeconds() {
        return seconds;
    }

    public Map<Attribute, Double> getAttributes() {
        return attributes;
    }
}
