package io.rodarg.queue.events;

import io.rodarg.queue.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerCommand implements CommandExecutor {

    private Main plugin;

    public PlayerCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (sender instanceof Player) {
            plugin.skipQueue(((Player) sender).getPlayer());
        }
        return true;
    }

}
