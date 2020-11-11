package io.rodarg.queue.events;

import io.rodarg.queue.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private Main plugin;

    public PlayerJoin(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setGameMode(GameMode.SPECTATOR);

        plugin.removePlayerFromLastRedirected(player);

        hidePlayer(player);
        event.setJoinMessage(null);

        boolean priorityQueue = plugin.addPlayerToQueue(player);

        if (priorityQueue) {
            player.sendMessage( ChatColor.BOLD + "§6Position in priority queue: " + plugin.getQueueSize(player));
        } else {
            player.sendMessage( ChatColor.BOLD + "§6Position in queue: " + plugin.getQueueSize(player));
        }
    }

    public void hidePlayer(Player newPlayer) {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.hidePlayer(plugin, newPlayer);
            newPlayer.hidePlayer(plugin, player);
        }
    }

}
