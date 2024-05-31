package io.github.tanguygab.scalepotions;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class PotionListener implements Listener {

    private final ScalePotions plugin;
    private final Map<AreaEffectCloud,String> lingeringPotions = new HashMap<>();
    private final BaseComponent crushedMsg = TextComponent.fromLegacy(ChatColor.RED+"You're getting crushed!");

    public PotionListener(ScalePotions plugin) {
        this.plugin = plugin;

        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin,()->{
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                Location loc = player.getLocation();

                for (AreaEffectCloud cloud : lingeringPotions.keySet()) {
                    if (loc.distance(cloud.getLocation()) <= cloud.getRadius()) {
                        plugin.setPlayerAttributes(player, lingeringPotions.get(cloud));
                        break;
                    }
                }

                if (player.isFlying() || (!loc.getBlock().getRelative(BlockFace.DOWN).getType().isAir() && loc.getY() == loc.getBlockY())) continue;

                double size = player.getAttribute(Attribute.GENERIC_SCALE).getBaseValue();

                for (Entity entity : player.getNearbyEntities(size/3,.5,size/3)) {
                    if (!(entity instanceof LivingEntity le)) continue;

                    double distance = loc.getY() - entity.getLocation().getY();
                    double entitySize = le.getAttribute(Attribute.GENERIC_SCALE).getBaseValue();
                    if (distance < 0 || entitySize >= size) continue;

                    le.damage(size/entitySize,player);
                    if (le instanceof Player p) p.spigot().sendMessage(ChatMessageType.ACTION_BAR,crushedMsg);
                }
            }
        },0,10);
    }

    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent e) {
        String potion = checkPotion(e.getItem());
        if (potion != null) plugin.setPlayerAttributes(e.getPlayer(),potion);
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent e) {
        String potion = checkPotion(e.getEntity().getItem());
        if (potion == null) return;
        e.getAffectedEntities().forEach(entity->{
            if (entity instanceof Player player)
                plugin.setPlayerAttributes(player,potion);
        });
    }

    @EventHandler
    public void onLingeringPotionSplash(LingeringPotionSplashEvent e) {
        String potion = checkPotion(e.getEntity().getItem());
        if (potion == null) return;
        AreaEffectCloud cloud = e.getAreaEffectCloud();
        lingeringPotions.put(cloud,potion);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> lingeringPotions.remove(cloud), 600);
    }


    private String checkPotion(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(ScalePotion.SCALE_POTION_KEY, PersistentDataType.STRING);
    }



}
