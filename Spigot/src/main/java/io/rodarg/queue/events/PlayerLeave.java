package io.rodarg.queue.events;

import io.rodarg.queue.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeave implements Listener {

    private Main plugin;

    public PlayerLeave(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void PlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(null);
        plugin.removePlayerFromQueue(player);
    }
}
