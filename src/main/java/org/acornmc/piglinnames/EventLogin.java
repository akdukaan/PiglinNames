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
            if (!p.hasPermission("piglinnames.keepprefix") && p.hasPermission("piglinnames.previouslynicked")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + p.getName() + " meta removeprefix 1");
                    }
                }.runTask(iess);
            }

            User user = iess.getUserMap().getUser(p.getName());
            String nick = user.getNickname();
            if (nick == null) {
                return;
            }

            String stripNick = ChatColor.stripColor(nick);
            boolean coloredNick = !stripNick.equals(nick);

            if (coloredNick) {
                if (p.hasPermission("piglinnames.keepnickname")) {
                    if (!p.getName().equalsIgnoreCase(stripNick)) {
                        // actually i should only be doing this if they using an old name. I'll do this when i wanna deal with mojangapi
                        System.out.println("resetting nickname for player reason 1");
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "nick " + p.getName() + " off");
                            }
                        }.runTask(iess);
                    }
                    return;
                } else {
                    System.out.println("resetting nickname for player reason 2");
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "nick " + p.getName() + " off");
                        }
                    }.runTask(iess);
                }
            } else {
                if (p.hasPermission("piglinnames.previouslynicked")) {
                    System.out.println("resetting nickname for player reason 3");
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "nick " + p.getName() + " off");
                        }
                    }.runTask(iess);
                }
                // else if they are using an old name, change it (i'll do this when i wanna deal with mojangapi) !
            }
        }).start();

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
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        System.out.println("line 133");
        con.connect();
        System.out.println("line 134");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        System.out.println("line 135");
        String response = in.readLine();
        System.out.println("line 136");
        in.close();
        System.out.println("line 138");
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
