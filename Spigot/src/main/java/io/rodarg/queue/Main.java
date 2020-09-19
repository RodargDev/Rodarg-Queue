package io.rodarg.queue;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.rodarg.queue.events.PlayerJoin;
import io.rodarg.queue.events.PlayerLeave;
import io.rodarg.queue.events.PlayerMessage;
import io.rodarg.queue.listeners.PluginMessages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedList;
import java.util.Queue;

public class Main extends JavaPlugin {

    private Queue<Player> queue = new LinkedList<Player>();

    public int playerLimit = 50;
    public boolean CONNECTION_ISSUE = false;
    public Player lastRedirectedPlayer = null;

    @Override
    public void onEnable() {
        getServer().getMessenger().registerOutgoingPluginChannel(this, "queue:channel");
        getServer().getMessenger().registerOutgoingPluginChannel(this, "serverinfo:channel");

        new PluginMessages(this);
        requestServerPlayerCount(this);
        notifyUserQueuePosition(this);
        teleportToCenter(this);

        getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);
        getServer().getPluginManager().registerEvents(new PlayerLeave(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMessage(this), this);
        getLogger().info("Plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin disabled!");
    }

    public void fillPlayerSlots(int slots) {

        //add to list if redirected. remove from main queue and put in sent list. if object
        //of player is online from sent list in next iteration. dont execute other loop and wait till everyone is offline
        //if server is empty whole queue should be redirected

        getLogger().info("Queue size: " + queue.size());
        if (lastRedirectedPlayer != null) {
            getLogger().info("Last player: " + lastRedirectedPlayer.getDisplayName());
        }

        if (queue.size() >= 1 || Bukkit.getOnlinePlayers().size() > 0) {

            for (int i = slots; i >= 0; i--) {

                if (queue.size() < 1 && Bukkit.getOnlinePlayers().size() < 1) {
                    break;
                }

                if (lastRedirectedPlayer != null) {
                    if (lastRedirectedPlayer.isOnline() && CONNECTION_ISSUE) {
                        lastRedirectedPlayer.sendMessage(ChatColor.BOLD + "§6Trying to connect to the server!");
                        sendPlayerToMainServer(lastRedirectedPlayer);
                    }

                    if (lastRedirectedPlayer.isOnline()) {
                        CONNECTION_ISSUE = true;
                    } else {
                        CONNECTION_ISSUE = false;
                    }
                }

                if (CONNECTION_ISSUE) {
                    break;
                }

                Player player = queue.remove();
                lastRedirectedPlayer = player;

                player.sendMessage(ChatColor.BOLD + "§6You are being sent to the server!");
                sendPlayerToMainServer(player);

            }

        }
    }

    public void addPlayerToQueue(Player player) {
        //Add player to queue on server join
        queue.add(player);
    }

    public void removePlayerFromQueue(Player player) {
        //Remove player from queue on server leave
        queue.remove(player);
    }

    public void sendPlayerToMainServer(Player player) {
        getLogger().info("Sending " + player.getDisplayName() + " to server");

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ConnectToServer");
        out.writeUTF("survival");

        player.sendPluginMessage(this, "queue:channel", out.toByteArray());
    }

    public void requestServerPlayerCount(Plugin plugin) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("GetPlayerCount");

                Player p = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
                if (p != null) {
                    p.sendPluginMessage(plugin, "serverinfo:channel", out.toByteArray());
                }
            }
        }, 0L, 100L);
    }

    public void notifyUserQueuePosition(Plugin plugin) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                int i = 1;

                for (Player player : queue) {

                    player.sendMessage( ChatColor.BOLD + "§6Position in queue: " + i);

                    i++;
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
