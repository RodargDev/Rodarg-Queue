package io.rodarg.queue.events;

import io.rodarg.queue.Main;
import io.rodarg.queue.ServerQueue;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeave implements Listener {

    private Main plugin;
    private ServerQueue serverQueue;

    public PlayerLeave(Main plugin, ServerQueue serverQueue) {
        this.plugin = plugin;
        this.serverQueue = serverQueue;
    }

    @EventHandler
    public void PlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(null);
        serverQueue.removePlayerFromQueue(player);
    }
}
