package io.rodarg.queue.events;

import io.rodarg.queue.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

        plugin.removePlayerFromLastRedirected(player);

        hidePlayer(player);
        player.sendMessage(ChatColor.BOLD + "§6You have entered the queue");
        event.setJoinMessage(null);
        plugin.addPlayerToQueue(player);
        player.sendMessage( ChatColor.BOLD + "§6Position in queue: " + plugin.getQueueSize());
    }

    public void hidePlayer(Player newPlayer) {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.hidePlayer(plugin, newPlayer);
            newPlayer.hidePlayer(plugin, player);
        }
    }

}
