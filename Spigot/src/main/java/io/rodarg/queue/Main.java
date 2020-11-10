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

import java.util.*;

public class Main extends JavaPlugin {

    private Queue<Player> queue = new LinkedList<Player>();
    private Queue<Player> priorityQueue = new LinkedList<Player>();

    public List<Player> lastRedirectedPlayers = new ArrayList<Player>();

    public int playerLimit = 50;
    private String serverName = null;
    private boolean PRIORITY_QUEUE_ACTIVE = false;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        playerLimit = this.getConfig().getInt("player-limit");
        serverName = this.getConfig().getString("to-server");
        PRIORITY_QUEUE_ACTIVE = this.getConfig().getBoolean("priority-queue");

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

        if (PRIORITY_QUEUE_ACTIVE) {
            getLogger().info("Priority queue size: " + priorityQueue.size());
        }

        if (queue.size() >= 1 || priorityQueue.size() >= 1 || Bukkit.getOnlinePlayers().size() > 0) {
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
                        sendPlayerToServer(lastRedirectedPlayer);
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

            // TO-DO: Find a way to redirect multiple players in one call
            // Example:
            //for (int i = slots; i > 0; i--) {
            for (int i = 1; i > 0; i--) {

                if (queue.size() < 1 && priorityQueue.size() < 1) {
                    break;
                }

                Player player;

                if (PRIORITY_QUEUE_ACTIVE) {
                    Random random = new Random();

                    int randomValue = random.nextInt(4 - 1 + 1) + 1;

                    if (randomValue > 1 && priorityQueue.size() >= 1 || queue.size() < 1) {
                        player = priorityQueue.remove();
                    } else {
                        player = queue.remove();
                    }
                } else {
                    player = queue.remove();
                }

                lastRedirectedPlayers.add(player);

                player.sendMessage(ChatColor.BOLD + "§6You are being sent to the server!");
                sendPlayerToServer(player);

            }

        }
    }

    public boolean addPlayerToQueue(Player player) {
        if (hasPriorityQueue(player)) {
            priorityQueue.add(player);
            player.sendMessage(ChatColor.BOLD + "§6You have entered the priority queue");
            return true;
        } else {
            queue.add(player);
            player.sendMessage(ChatColor.BOLD + "§6You have entered the queue");
            return false;
        }
    }

    public void removePlayerFromQueue(Player player) {
        if (hasPriorityQueue(player)) {
            priorityQueue.remove(player);
        } else {
            queue.remove(player);
        }

        removePlayerFromLastRedirected(player);
    }

    public void removePlayerFromLastRedirected(Player player) {
        lastRedirectedPlayers.removeIf(selectedPlayer -> selectedPlayer.getDisplayName().equalsIgnoreCase(player.getDisplayName()));
    }

    public void sendPlayerToServer(Player player) {
        getLogger().info("Sending " + player.getDisplayName() + " to server");

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ConnectToServer");

        if (serverName == null) {
            player.sendMessage("§4Please contact an server administrator. 'to-server' value in the config.yml isn't set");
        } else {
            out.writeUTF(serverName);
            player.sendPluginMessage(this, "queue:channel", out.toByteArray());
        }
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

                if (PRIORITY_QUEUE_ACTIVE) {
                    i = 1;

                    for (Player player : priorityQueue) {
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

    public boolean hasPriorityQueue(Player player) {
        if (player.hasPermission("queue.priority") && PRIORITY_QUEUE_ACTIVE) {
            return true;
        } else {
            return false;
        }
    }

    public void skipQueue(Player player) {
        if (!lastRedirectedPlayers.contains(player)) {
            lastRedirectedPlayers.add(player);

            player.sendMessage(ChatColor.BOLD + "§6You are being sent to the server!");

            sendPlayerToServer(player);
        }
    }

    public int getQueueSize(Player player) {
        if (hasPriorityQueue(player)) {
            return priorityQueue.size();
        } else {
            return queue.size();
        }
    }

}
