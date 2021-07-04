package io.rodarg.queue;

import org.bukkit.entity.Player;

public class QueuePlayer {

    private int queuePosition;
    private boolean hasPriority;

    private Player player;

    public QueuePlayer(Player player, int queuePosition, boolean hasPriority) {
        this.player = player;
        this.queuePosition = queuePosition;
        this.hasPriority = hasPriority;
    }

}
