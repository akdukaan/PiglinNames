package org.acornmc.piglinnames;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class EventLogin implements Listener {
    IEssentials iess = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");


    @EventHandler
    public void onLogin(final PlayerLoginEvent event) {
        new Thread(() -> {
            // reset prefix if no longer vip
            Player p = event.getPlayer();
            if (!p.hasPermission("piglinnames.keepprefix")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + p.getName() + " meta removeprefix 1");
                    }
                }.runTask(iess);
            }

            User user = iess.getUserMap().getUser(p.getName());
            String nick = user.getNickname();
            System.out.println("1.1 " + p.getName());
            System.out.println("2.1 " + nick);
            String stripnick = ChatColor.stripColor(nick);
            if (stripnick == null) {
                return;
            }

            // if not vip and nick was colored
            if (!p.hasPermission("piglinnames.keepnickname") && !stripnick.equals(nick)) {
                System.out.println("resetting nickname for player because no longer vip");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "nick " + p.getName() + " off");
                    }
                }.runTask(iess);
                return;
            }

            // if they have a nick with text different than their name
            // and the nick has been their name in the past
            if (!p.getName().equals(stripnick) && oldName(p.getName(), stripnick) && p.hasPermission("piglinnames.drbot")) {
                System.out.println("resetting nickname for player because changed name");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "nick " + p.getName() + " off");
                    }
                }.runTask(iess);
            }
        }).start();

        // removenick if
        // colored nick and not VIP
        // nick.stripcolor is equalignorecase as an old name

    }

    public boolean oldName(String playername, String stripnick) {

        try {
            PreviousPlayerNameEntry[] entries = getPlayerPreviousNames(playername);
            for (PreviousPlayerNameEntry entry : entries) {
                String name = entry.getPlayerName();
                if (name.equalsIgnoreCase(stripnick)) {
                    return true;
                }
            }
        } catch (IOException ioException) {
            System.out.println("Could not get previous names for player");
        }
        return false;
    }

    /**
     * The URL from Mojang API that provides the JSON String in response.
     */
    private static final String LOOKUP_URL = "https://api.mojang.com/user/profiles/%s/names";
    private static final Gson JSON_PARSER = new Gson();


    /**
     * <h1>NOTE: Avoid running this method <i>Synchronously</i> with the main thread! It blocks while attempting to get a response from Mojang servers!</h1>
     * Alternative method accepting an {@link OfflinePlayer} (and therefore {@link Player}) objects as parameter.
     * @param uuid The UUID String to lookup
     * @return Returns an array of {@link PreviousPlayerNameEntry} objects, or null if the response couldn't be interpreted.
     * @throws IOException
     */
    public static PreviousPlayerNameEntry[] getPlayerPreviousNames(String uuid) throws IOException {
        if (uuid == null || uuid.isEmpty())
            return null;
        String response = getRawJsonResponse(new URL(String.format(LOOKUP_URL, uuid)));
        return JSON_PARSER.fromJson(response, PreviousPlayerNameEntry[].class);
    }


    /**
     * This is a helper method used to read the response of Mojang's API webservers.
     * @param u the URL to connect to
     * @return a String with the data read.
     * @throws IOException Inherited by {@link BufferedReader#readLine()}, {@link BufferedReader#close()}, {@link URL}, {@link HttpURLConnection#getInputStream()}
     */
    private static String getRawJsonResponse(URL u) throws IOException {
        HttpURLConnection con = (HttpURLConnection) u.openConnection();
        con.setDoInput(true);
        con.setConnectTimeout(2000);
        con.setReadTimeout(2000);
        con.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String response = in.readLine();
        in.close();
        return response;
    }

    /**
     * This class represents the typical response expected by Mojang servers when requesting the name history of a player.
     */
    public class PreviousPlayerNameEntry {
        private String name;
        @SerializedName("changedToAt")

        public String getPlayerName() {
            return name;
        }

        @Override
        public String toString() {
            return "Name: " + name;
        }
    }
}
