package io.rodarg.queue.events;

import io.rodarg.queue.Main;
import io.rodarg.queue.models.ServerQueue;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerMessage implements Listener {

    private Main plugin;
    private ServerQueue serverQueue;

    public PlayerMessage(Main plugin, ServerQueue serverQueue) {
        this.plugin = plugin;
        this.serverQueue = serverQueue;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();

        if (event.getMessage().equalsIgnoreCase("skip")) {
            serverQueue.skipQueue(player);
        }

        event.setCancelled(true);
    }

}
