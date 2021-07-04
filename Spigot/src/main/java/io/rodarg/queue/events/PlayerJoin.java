package io.rodarg.queue.events;

import io.rodarg.queue.Main;
import io.rodarg.queue.models.ServerQueue;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private Main plugin;
    private ServerQueue serverQueue;

    public PlayerJoin(Main plugin, ServerQueue serverQueue) {
        this.plugin = plugin;
        this.serverQueue = serverQueue;
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setGameMode(GameMode.SPECTATOR);

        serverQueue.removePlayerFromLastRedirected(player);

        hidePlayer(player);
        event.setJoinMessage(null);

        boolean priorityQueue = serverQueue.addPlayerToQueue(player);

        if (priorityQueue) {
            player.sendMessage(serverQueue.getConfigFormatter().formatConfigText(player, "message.player.position-priority", serverQueue.getQueueSize(player)));
        } else {
            player.sendMessage(serverQueue.getConfigFormatter().formatConfigText(player, "message.player.position-normal", serverQueue.getQueueSize(player)));
        }
    }

    public void hidePlayer(Player newPlayer) {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.hidePlayer(plugin, newPlayer);
            newPlayer.hidePlayer(plugin, player);
        }
    }

}
