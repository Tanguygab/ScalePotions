package io.github.tanguygab.scalepotions;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SPCommand implements CommandExecutor, TabExecutor {

    private final ScalePotions plugin;

    protected SPCommand(ScalePotions plugin) {
        this.plugin = plugin;
    }

    private void msg(CommandSender sender, String message) {
        sender.sendMessage(plugin.color(message));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String arg = args.length == 0 ? "" : args[0];
        switch (arg) {
            case "give" -> {
                if (args.length < 2) {
                    msg(sender,"&cYou have to specify a potion name!");
                    return true;
                }
                String name = args[1];
                if (!plugin.potions.containsKey(name)) {
                    msg(sender,"&cThis potion doesn't exist!");
                    return true;
                }

                ScalePotion potion = plugin.potions.get(name);

                int amount = 1;
                if (args.length > 3) {
                    try {amount = Integer.parseInt(args[3]);} catch (Exception ignored) {}
                }
                Material type = switch (args.length > 4 ? args[4] : "potion") {
                    case "lingering","lingering_potion" -> Material.LINGERING_POTION;
                    case "splash","splash_potion" -> Material.SPLASH_POTION;
                    default -> Material.POTION;
                };
                ItemStack item = potion.getPotion(amount, type);

                Player player = null;
                if (args.length > 2) {
                    player = plugin.getServer().getPlayer(args[2]);
                    if (player == null) {
                        msg(sender,"&cThis player isn't online!");
                        return true;
                    }
                }
                if (player == null) {
                    if (sender instanceof Player p)
                        player = p;
                    else {
                        msg(sender,"&cYou have to specify a player!");
                        return true;
                    }
                }

                player.getInventory().addItem(item);
            }
            case "reload" -> {
                plugin.onDisable();
                plugin.onEnable();
                msg(sender,"&aPlugin reloaded!");
            }
            default -> msg(sender,"&e[ScalePotions] "+plugin.getDescription().getVersion()+"\n" +
                    "&8- &f/scalepotions give <potion> [player] [amount] [type]\n" +
                    "&8- &f/scalepotions reload");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) return List.of("give","reload");
        if (!args[0].equalsIgnoreCase("give")) return null;
        return switch (args.length) {
            case 2 -> plugin.potions.keySet().stream().toList();
            case 4 -> List.of("1","16","32","48","64");
            case 5 -> List.of("normal","splash","lingering");
            default -> null;
        };
    }
}
