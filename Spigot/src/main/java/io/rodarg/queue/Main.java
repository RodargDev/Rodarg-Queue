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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Main extends JavaPlugin {

    private Queue<Player> queue = new LinkedList<Player>();

    public List<Player> lastRedirectedPlayers = new ArrayList<Player>();

    public int playerLimit = 50;

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

        getLogger().info("Queue size: " + queue.size());

        if (queue.size() >= 1 || Bukkit.getOnlinePlayers().size() > 0) {
            if (lastRedirectedPlayers.size() > 0) {

                boolean CONNECTION_ISSUE = false;
                List<String> lastPlayers = new ArrayList<String>();

                for (Player p:lastRedirectedPlayers) {
                    lastPlayers.add(p.getDisplayName());
                }

                getLogger().info("Last players: " + lastPlayers.toString());

                for (int i = lastRedirectedPlayers.size(); i > 0; i--) {

                    Player lastRedirectedPlayer = lastRedirectedPlayers.get(i-1);

                    if (lastRedirectedPlayer.isOnline()) {
                        lastRedirectedPlayer.sendMessage(ChatColor.BOLD + "§6Trying to connect to the server! Please wait (Server might be offline)");
                        sendPlayerToMainServer(lastRedirectedPlayer);
                        CONNECTION_ISSUE = true;
                    } else {
                        removePlayerFromLastRedirected(lastRedirectedPlayer);
                    }
                }

                if (CONNECTION_ISSUE) {
                    getLogger().warning("Connection issue");
                    return;
                }

            }

            //TO-DO: Find a way to redirect multiple players in one call
            //for (int i = slots; i > 0; i--) {

            for (int i = 1; i > 0; i--) {

                if (queue.size() < 1) {
                    break;
                }

                Player player = queue.remove();
                lastRedirectedPlayers.add(player);

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
        removePlayerFromLastRedirected(player);
    }

    public void removePlayerFromLastRedirected(Player player) {
        lastRedirectedPlayers.removeIf(selectedPlayer -> selectedPlayer.getDisplayName().equalsIgnoreCase(player.getDisplayName()));
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

    public void skipQueue(Player player) {
        if (!lastRedirectedPlayers.contains(player)) {
            lastRedirectedPlayers.add(player);

            player.sendMessage(ChatColor.BOLD + "§6You are being sent to the server!");
            sendPlayerToMainServer(player);
        }
    }

    public int getQueueSize() {
        return queue.size();
    }

}
