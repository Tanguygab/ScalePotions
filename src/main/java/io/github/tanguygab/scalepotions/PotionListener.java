package io.github.tanguygab.scalepotions;

import org.bukkit.entity.AreaEffectCloud;
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

    public PotionListener(ScalePotions plugin) {
        this.plugin = plugin;

        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin,()->{
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                for (AreaEffectCloud cloud : lingeringPotions.keySet()) {
                    if (player.getLocation().distance(cloud.getLocation()) <= cloud.getRadius()) {
                        plugin.setPlayerAttributes(player, lingeringPotions.get(cloud));
                        break;
                    }
                }
            }
        },0,20);
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
