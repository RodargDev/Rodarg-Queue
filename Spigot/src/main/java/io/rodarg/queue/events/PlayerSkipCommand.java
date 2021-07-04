package io.rodarg.queue.events;

import io.rodarg.queue.Main;
import io.rodarg.queue.models.ServerQueue;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerSkipCommand implements CommandExecutor {

    private Main plugin;
    private ServerQueue serverQueue;

    public PlayerSkipCommand(Main plugin, ServerQueue serverQueue) {
        this.plugin = plugin;
        this.serverQueue = serverQueue;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (sender instanceof Player) {
            serverQueue.skipQueue(((Player) sender).getPlayer());
        }
        return true;
    }

}
