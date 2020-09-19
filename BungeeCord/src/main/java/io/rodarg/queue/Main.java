package io.rodarg.queue;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Main extends Plugin implements Listener {

    @Override
    public void onEnable() {
        this.getProxy().registerChannel("queue:channel");
        this.getProxy().registerChannel("serverinfo:channel");
        this.getProxy().getPluginManager().registerListener(this, this);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {

        ByteArrayInputStream stream = new ByteArrayInputStream(event.getData());
        DataInputStream in = new DataInputStream(stream);

        if (event.getTag().equals("queue:channel")) {
            //Queue commands

            try {

                String action = in.readUTF();

                if (action.equalsIgnoreCase("ConnectToServer")) {
                    String server = in.readUTF();

                    System.out.print("Queue Message: " + action + " " + server);
                    redirectUserToServer((ProxiedPlayer) event.getReceiver(), server);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (event.getTag().equals("serverinfo:channel")) {
            //PlayerCount channel

            try {

                String action = in.readUTF();

                if (action.equalsIgnoreCase("GetPlayerCount")) {
                    //String server = in.readUTF();

                    System.out.print("Playercount requested");

                    int playerCount = ProxyServer.getInstance().getServerInfo("survival").getPlayers().size();

                    sendPlayerCountToServer((Server) event.getSender(), playerCount);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void sendPlayerCountToServer(Server server, int playercount) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeInt(playercount);

        server.sendData("serverinfo:channel", out.toByteArray());

    }

    public void redirectUserToServer(ProxiedPlayer player, String server) {
        ServerInfo target = ProxyServer.getInstance().getServerInfo(server);
        player.connect(target);
    }

}
