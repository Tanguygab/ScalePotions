package io.github.tanguygab.scalepotions;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class ScalePotions extends JavaPlugin {

    public static final Map<Attribute, Double> baseAttributes = new HashMap<>() {{
        put(Attribute.GENERIC_SCALE, 1.0);
        put(Attribute.GENERIC_STEP_HEIGHT, 0.6);
        put(Attribute.PLAYER_BLOCK_INTERACTION_RANGE, 4.5);
        put(Attribute.PLAYER_ENTITY_INTERACTION_RANGE, 3.0);
        put(Attribute.GENERIC_MOVEMENT_SPEED, 0.1);
    }};

    public final Map<String, ScalePotion> potions = new HashMap<>();
    public final Map<String, Integer> scaledPlayers = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        ConfigurationSection config = getConfig().getConfigurationSection("potions");
        if (config == null) return;

        config.getKeys(false).forEach(potion->{
            ConfigurationSection data = getConfig().getConfigurationSection("potions."+potion);
            if (data == null) return;

            String displayName = data.getString("name",potion);
            List<String> lore = data.getStringList("lore");
            String color = data.getString("color","FF0000");
            Color rgb;
            try {
                java.awt.Color c = java.awt.Color.decode("#"+color);
                rgb = Color.fromRGB(c.getRed(),c.getGreen(),c.getBlue());
            } catch (Exception e) {
                getLogger().warning("Invalid color "+color+" for potion "+potion+"! Defaulting to red");
                rgb = Color.RED;
            }

            potions.put(potion,new ScalePotion(potion,
                    color(displayName),
                    lore.stream().map(this::color).toList(),
                    rgb,
                    data.getInt("seconds",60),
                    data.getDouble("scale",baseAttributes.get(Attribute.GENERIC_SCALE)),
                    data.getDouble("step-height",baseAttributes.get(Attribute.GENERIC_STEP_HEIGHT)),
                    data.getDouble("block-reach",baseAttributes.get(Attribute.PLAYER_BLOCK_INTERACTION_RANGE)),
                    data.getDouble("entity-reach",baseAttributes.get(Attribute.PLAYER_ENTITY_INTERACTION_RANGE)),
                    data.getDouble("speed",baseAttributes.get(Attribute.GENERIC_MOVEMENT_SPEED))
            ));
        });

        getCommand("scalepotions").setExecutor(new SPCommand(this));
        getServer().getPluginManager().registerEvents(new PotionListener(this),this);

        ConfigurationSection players = getConfig().getConfigurationSection("scaled-players");
        if (players != null) {
            players.getValues(false).forEach((uuid,seconds)->scaledPlayers.put(uuid, (int) seconds));
            getConfig().set("scaled-players",null);
            saveConfig();
        }

        getServer().getScheduler().scheduleSyncRepeatingTask(this,()->{
            for (String uuid : scaledPlayers.keySet()) {
                Player player = Bukkit.getPlayer(UUID.fromString(uuid));

                if (player == null || !player.isOnline()) return;

                int seconds = scaledPlayers.get(uuid);
                if (seconds <= 1) {
                    scaledPlayers.remove(uuid);
                    setPlayerAttributes(player,null);
                    return;
                }
                scaledPlayers.put(uuid,seconds-1);

            }
        },0,20);
    }

    @Override
    public void onDisable() {
        potions.clear();
        if (!scaledPlayers.isEmpty()) {
            getConfig().set("scaled-players",scaledPlayers);
            saveConfig();
        }
    }

    public String color(String message) {
        return ChatColor.translateAlternateColorCodes('&',message);
    }

    public void setPlayerAttributes(Player player, String name) {
        if (player == null) return;

        Map<Attribute, Double> values = ScalePotions.baseAttributes;
        if (name != null) {
            if (!potions.containsKey(name)) return;
            ScalePotion potion = potions.get(name);
            values = potion.getAttributes();

            int seconds = potion.getSeconds();
            if (seconds >= 0) scaledPlayers.put(player.getUniqueId().toString(),seconds);
        }

        values.forEach((attribute,value)->{
            AttributeInstance att = player.getAttribute(attribute);
            if (att != null) att.setBaseValue(value);
        });
    }

}
