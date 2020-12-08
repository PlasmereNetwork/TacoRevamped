package net.plasmere.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import net.plasmere.TacoRevamped;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

public class UUIDFetcher {
    static public UUID fetch(String username) throws IOException {
        try {
            String JSONString = "";

            HttpURLConnection connection = (HttpURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + username).openConnection();
            InputStream is = connection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null) {
                JSONString = line;
            }

            Object obj = new JsonParser().parse(JSONString);
            JsonObject jo = (JsonObject) obj;

            String id = jo.get("id").getAsString();

            // String uuid = id.substring(0, 7) + "-" + id.substring(8, 11) + "-"
            //        + id.substring(12, 15) + "-" + id.substring(16, 19) + "-"
            //        + id.substring(20);
            String uuid = formatToUUID(id);

            return UUID.fromString(uuid);
            //return UUID.fromString(id);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String formatToUUID(String unformatted){
        StringBuilder formatted = new StringBuilder();
        int i = 1;
        for (Character character : unformatted.toCharArray()){
            if (i == 9 || i == 13 || i == 17 || i == 21){
                formatted.append("-").append(character);
            } else {
                formatted.append(character);
            }
            i++;
        }

        return formatted.toString();
    }

    public static ServerPlayerEntity getServerPlayerEntity(UUID uuid, String name){
        try {
            MinecraftServer server = TacoRevamped.getServer();
            Path path = server.getSavePath(WorldSavePath.PLAYERDATA);
            File uuidFile = new File(path + File.separator + uuid + ".dat");
            return new ServerPlayerEntity(server, server.getOverworld(), new GameProfile(uuid, name));
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static GameProfile getGameProfile(String name){
        MinecraftServer server = TacoRevamped.getServer();
        try {
            ServerPlayerEntity other = Objects.requireNonNull(server).getPlayerManager().getPlayer(UUIDFetcher.fetch(name));

            if (other != null) {
                return other.getGameProfile();
            }

            UUID uuid = fetch(name);

            Path path = server.getSavePath(WorldSavePath.PLAYERDATA);
            File uuidFile = new File(path + File.separator + uuid + ".dat");
            return new GameProfile(uuid, name);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static ServerPlayerEntity getServerPlayerEntity(String name){
        MinecraftServer server = TacoRevamped.getServer();
        try {
            ServerPlayerEntity other = Objects.requireNonNull(server).getPlayerManager().getPlayer(UUIDFetcher.fetch(name));

            if (other != null) {
                return other;
            }

            UUID uuid = fetch(name);

            Path path = server.getSavePath(WorldSavePath.PLAYERDATA);
            File uuidFile = new File(path + File.separator + uuid + ".dat");
            return new ServerPlayerEntity(server, server.getOverworld(), new GameProfile(uuid, name));
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
