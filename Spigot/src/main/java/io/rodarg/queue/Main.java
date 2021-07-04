package io.rodarg.queue;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.rodarg.queue.events.PlayerSkipCommand;
import io.rodarg.queue.events.PlayerJoin;
import io.rodarg.queue.events.PlayerLeave;
import io.rodarg.queue.events.PlayerMessage;
import io.rodarg.queue.listeners.PluginMessages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Main extends JavaPlugin {

    private ServerQueue serverQueue;

    public ServerQueue getServerQueue() {
        return serverQueue;
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        FileConfiguration config = this.getConfig();

        serverQueue = new ServerQueue(this, config);

        getServer().getMessenger().registerOutgoingPluginChannel(this, "queue:channel");
        getServer().getMessenger().registerOutgoingPluginChannel(this, "serverinfo:channel");

        new PluginMessages(this, serverQueue);
        requestServerPlayerCount(this);
        notifyUserQueuePosition(this);
        teleportToCenter(this);

        getServer().getPluginManager().registerEvents(new PlayerJoin(this, serverQueue), this);
        getServer().getPluginManager().registerEvents(new PlayerLeave(this, serverQueue), this);
        getServer().getPluginManager().registerEvents(new PlayerMessage(this, serverQueue), this);

        this.getCommand("skip").setExecutor(new PlayerSkipCommand(this, serverQueue));

        getLogger().info("Plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin disabled!");
    }

    public void requestServerPlayerCount(Plugin plugin) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("GetPlayerCount");

                Player p = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
                if (p != null) {
                    if (!getServerQueue().getToServerName().isEmpty()) {
                        out.writeUTF(getServerQueue().getToServerName());
                        p.sendPluginMessage(plugin, "serverinfo:channel", out.toByteArray());
                    } else {
                        getLogger().warning("WARNING: 'to-server' value in the config.yml isn't set, the queue won't work without it");
                    }
                }
            }
        }, 0L, 100L);
    }

    public void notifyUserQueuePosition(Plugin plugin) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                int i = 1;

                for (Player player : serverQueue.getMainQueue()) {
                    if (serverQueue.getToServerName().isEmpty()) {
                        player.sendMessage("§4Please contact an server administrator. 'to-server' value in the config.yml isn't set");
                    }

                    player.sendMessage( ChatColor.BOLD + "§6Position in queue: " + i);
                    i++;
                }

                if (serverQueue.isPriorityQueueActive()) {
                    i = 1;

                    for (Player player : serverQueue.getPriorityQueue()) {
                        player.sendMessage( ChatColor.BOLD + "§6Position in priority queue: " + i);
                        i++;
                    }
                }
            }
        }, 0L, 150L);
    }

    public void teleportToCenter(Plugin plugin) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {

                if (Bukkit.getOnlinePlayers().size() > 0) {

                    Location location = new Location(Iterables.getFirst(Bukkit.getOnlinePlayers(), null).getWorld(), 0, 150, 0);

                    for (Player player : Bukkit.getServer().getOnlinePlayers()) {

                        player.teleport(location);

                    }
                }

            }
        }, 0L, 150L);
    }

}
