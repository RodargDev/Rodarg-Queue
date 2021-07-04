package io.rodarg.queue;

import io.rodarg.queue.models.ServerQueue;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

import static org.bukkit.Bukkit.getLogger;

public class ConfigFormatter {

    private final FileConfiguration config;
    private ServerQueue serverQueue;

    public ConfigFormatter(FileConfiguration config, ServerQueue serverQueue) {
        this.config = config;
        this.serverQueue = serverQueue;
    }

    public String formatConfigText(Player player, String messageLabel) {
        return formatString(player, config.getString(messageLabel), -1);
    }

    public String formatConfigText(Player player, String messageLabel, int position) {
        return formatString(player, config.getString(messageLabel), position);
    }

    private String formatString(Player player, String configString, int position) {
        String formattedString = configString;

        if (formattedString.contains("{USERNAME}")) {
            formattedString = formattedString.replace("{USERNAME}", player.getDisplayName());
        }

        if (position >= 0 && formattedString.contains("{USER_POSITION}")) {
            formattedString = formattedString.replace("{USER_POSITION}", Integer.toString(position));
        } else {
            if (formattedString.contains("{USER_POSITION}")) {
                getLogger().warning("WARNING: {USER_POSITION} cannot be used for this message");
            }
        }

        if (formattedString.contains("{NORMAL_QUEUE_SIZE}")) {
            formattedString = formattedString.replace("{NORMAL_QUEUE_SIZE}", Integer.toString(serverQueue.getMainQueueSize()));
        }

        if (formattedString.contains("{PRIORITY_QUEUE_SIZE}")) {
            formattedString = formattedString.replace("{PRIORITY_QUEUE_SIZE}", Integer.toString(serverQueue.getPriorityQueueSize()));
        }

        return formattedString;
    }

}
