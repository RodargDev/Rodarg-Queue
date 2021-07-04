package io.rodarg.queue.listeners;

import io.rodarg.queue.Main;
import io.rodarg.queue.ServerQueue;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import static org.bukkit.Bukkit.getLogger;

public class PluginMessages implements PluginMessageListener {

    private Main plugin;
    private ServerQueue serverQueue;

    public PluginMessages(Main plugin, ServerQueue serverQueue) {
        this.plugin = plugin;
        this.serverQueue = serverQueue;

        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "serverinfo:channel", this);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (channel.equals("serverinfo:channel")) {

            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            try {

                int playerCount = in.readInt();
                getLogger().info("Players in server: " + playerCount + " - open spots: " + (serverQueue.getPlayerLimit() - playerCount));

                serverQueue.fillPlayerSlots(serverQueue.getPlayerLimit() - playerCount);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
