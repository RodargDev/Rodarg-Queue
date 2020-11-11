package io.rodarg.queue.events;

import io.rodarg.queue.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerMessage implements Listener {

    private Main plugin;

    public PlayerMessage(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();

        if (event.getMessage().equalsIgnoreCase("skip")) {
            plugin.skipQueue(player);
        }

        event.setCancelled(true);
    }

}
