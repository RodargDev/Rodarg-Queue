package io.rodarg.queue.models;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.rodarg.queue.ConfigFormatter;
import io.rodarg.queue.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

import static org.bukkit.Bukkit.getLogger;

public class ServerQueue {

    private Queue<Player> mainQueue = new LinkedList<Player>();
    private Queue<Player> priorityQueue = new LinkedList<Player>();
    private List<Player> lastRedirectedPlayers = new ArrayList<Player>();

    public int playerLimit;
    private final String toServerName;
    private final boolean priorityQueueActive;

    private final Main plugin;
    private ConfigFormatter configFormatter;

    public Queue<Player> getMainQueue() {
        return mainQueue;
    }

    public int getMainQueueSize() {
        return mainQueue.size();
    }

    public Queue<Player> getPriorityQueue() {
        return priorityQueue;
    }

    public int getPriorityQueueSize() {
        return priorityQueue.size();
    }

    public int getPlayerLimit() {
        return playerLimit;
    }

    public String getToServerName() {
        return toServerName;
    }

    public boolean isPriorityQueueActive() {
        return priorityQueueActive;
    }

    public ServerQueue(Main plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.configFormatter = new ConfigFormatter(config, this);

        this.playerLimit = config.getInt("player-limit");
        this.toServerName = config.getString("to-server");
        this.priorityQueueActive = config.getBoolean("priority-queue");
    }

    public void fillPlayerSlots(int slots) {

        getLogger().info("Queue size: " + mainQueue.size());

        if (priorityQueueActive) {
            getLogger().info("Priority queue size: " + priorityQueue.size());
        }

        if (mainQueue.size() >= 1 || priorityQueue.size() >= 1 || Bukkit.getOnlinePlayers().size() > 0) {
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
                        lastRedirectedPlayer.sendMessage(configFormatter.formatConfigText(lastRedirectedPlayer, "message.player.cannot-connect"));
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

                if (mainQueue.size() < 1 && priorityQueue.size() < 1) {
                    break;
                }

                Player player;

                if (priorityQueueActive) {
                    Random random = new Random();

                    int randomValue = random.nextInt(4 - 1 + 1) + 1;

                    if (randomValue > 1 && priorityQueue.size() >= 1 || mainQueue.size() < 1) {
                        player = priorityQueue.remove();
                    } else {
                        player = mainQueue.remove();
                    }
                } else {
                    player = mainQueue.remove();
                }

                lastRedirectedPlayers.add(player);

                player.sendMessage(configFormatter.formatConfigText(player, "message.player.redirect-normal"));
                sendPlayerToServer(player);

            }

        }
    }

    public void sendPlayerToServer(Player player) {
        getLogger().info("Sending " + player.getDisplayName() + " to server");

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ConnectToServer");

        if (!getToServerName().isEmpty()) {
            out.writeUTF(toServerName);
            player.sendPluginMessage(plugin, "queue:channel", out.toByteArray());
        }
    }

    public void skipQueue(Player player) {
        if (!lastRedirectedPlayers.contains(player) && player.hasPermission("queue.skip")) {
            lastRedirectedPlayers.add(player);

            player.sendMessage(configFormatter.formatConfigText(player, "message.player.redirect-skip"));

            getLogger().info(player.getDisplayName() + " skipped the queue");

            sendPlayerToServer(player);
        }
    }

    public boolean addPlayerToQueue(Player player) {
        if (hasPriorityQueue(player)) {
            priorityQueue.add(player);
            player.sendMessage(configFormatter.formatConfigText(player, "message.player.welcome-priority"));
            return true;
        } else {
            mainQueue.add(player);
            player.sendMessage(configFormatter.formatConfigText(player, "message.player.welcome-normal"));
            return false;
        }
    }

    public void removePlayerFromQueue(Player player) {
        if (hasPriorityQueue(player)) {
            priorityQueue.remove(player);
        } else {
            mainQueue.remove(player);
        }

        removePlayerFromLastRedirected(player);
    }

    public void removePlayerFromLastRedirected(Player player) {
        lastRedirectedPlayers.removeIf(selectedPlayer -> selectedPlayer.getDisplayName().equalsIgnoreCase(player.getDisplayName()));
    }

    public ConfigFormatter getConfigFormatter() {
        return configFormatter;
    }

    public int getQueueSize(Player player) {
        if (hasPriorityQueue(player)) {
            return priorityQueue.size();
        } else {
            return mainQueue.size();
        }
    }

    public boolean hasPriorityQueue(Player player) {
        if (player.hasPermission("queue.priority") && priorityQueueActive) {
            return true;
        } else {
            return false;
        }
    }

}
