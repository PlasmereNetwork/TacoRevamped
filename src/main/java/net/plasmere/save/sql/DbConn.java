package net.plasmere.save.sql;

import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;

import net.minecraft.util.registry.Registry;
import net.minecraft.util.math.BlockPos;
import net.plasmere.save.sql.helper.InsertPSBuilder;
import net.plasmere.save.sql.helper.SelectPSBuilder;
import net.plasmere.utils.LoggedEventType;
import net.plasmere.utils.PlayerUtils;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.UUID;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.plasmere.utils.PrintToChat;

public class DbConn {
    private static Connection con = null;
    public static MinecraftServer server = null;

    private static SelectPSBuilder searchQuery;
    private static InsertPSBuilder writeInteractionsQuery;
    private static SelectPSBuilder readEventsQuery;

    public static void connect(MinecraftServer server) {
        try {
            Class.forName("org.sqlite.JDBC");
            File databaseFile;
            // Database file
            databaseFile = new File(server.getSavePath(WorldSavePath.ROOT).toFile(), "blocklogger.bl");
            con = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getPath().replace('\\', '/'));
            // Check if table exists
            ensureTable("interactions",
                    "(type STRING, x INT NOT NULL, y INT NOT NULL, z INT NOT NULL, dimension STRING NOT NULL, state STRING, player STRING, time STRING, rolledbackat INT DEFAULT -1)");
            System.out.println("[BL] Connected to database");
            onConnection();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }


    private static void ensureTable(String name, String description) {
        // Create table if it doesn't exist
        String sql = "CREATE TABLE IF NOT EXISTS " + name + " " + description + ";";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.execute();

            System.out.println("[BL] prepared table");
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    public static void writeInteractions(BlockPos pos, BlockState state, PlayerEntity player, LoggedEventType type, boolean isPlayer) {
        if (con == null) {
            // Check if database isn't connected
            throw new IllegalStateException("Database connection not initialized");
        }
        //Get date and time
        LocalDateTime dateTime = LocalDateTime.now();
        String time = dateTime.format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm"));
        InsertPSBuilder.InsertRunner runner = writeInteractionsQuery.createRunner();
        try {
            runner.fillParameter("type", type.name());
            runner.fillParameter("pos", pos.getX(), pos.getY(), pos.getZ());
            if(isPlayer){
                runner.fillParameter("dimension", PlayerUtils.getPlayerDimension(player));
            }
            else{
                runner.fillParameter("dimension", "minecraft:overworld");
            }
            runner.fillParameter("state", state.toString().replace("Block{", "").replace("}", ""));
            if(isPlayer){
                runner.fillParameter("player", getPlayerUuid(player));
            }
            else{
                runner.fillParameter("player", (Object)null);
            }
            runner.fillParameter("time", time);

            runner.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void readEvents(BlockPos pos, String dimension, LoggedEventType eventType, ServerPlayerEntity sourcePlayer) {
        // Check if database is connected
        if (con == null) {
            throw new IllegalStateException("Database connection not initialized");
        }
        String message = "Blocklogger data for " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + " in "+ dimension;
        PrintToChat.print(sourcePlayer, message, Formatting.GOLD);

        SelectPSBuilder.SelectRunner runner = readEventsQuery.createRunner();
        try {
            //Read data
            runner.fillParameter("pos", pos.getX(), pos.getY(), pos.getZ());
            runner.fillParameter("dimension", dimension);
            if (eventType != null) {
                runner.fillParameter("type", eventType.name());
            }

            ResultSet rs = runner.execute();
            // Repeat for every entry
            while (rs.next()) {
                //Get the info from the database and return
                PrintToChat.prepareInteractionsPrint(toStringMap(rs,
                        "x", "y", "z", "state", "dimension", "type", "player", "time"), sourcePlayer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getDisplayName(UUID uuid) {
        MinecraftServer server = DbConn.server;
        String name;
        if(uuid==null){
            name = "A block";
        }
        else{
            name = server.getUserCache().getByUuid(uuid).getName();
        }
        return name;
    }

    public static String getPlayerUuid(PlayerEntity player) {
        // return the player's UUID as a String
        return player.getUuidAsString();
    }

    public static UUID getUuid(String uuid) {
        return UUID.fromString(uuid);
    }

    public static void close() {
        // Closes connection to database
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void readAdvanced(ServerCommandSource scs, HashMap<String, Object> propertyMap) throws CommandSyntaxException {
        ServerPlayerEntity sourcePlayer = scs.getPlayer();
        // Check if database is connected
        if (con == null) {
            throw new IllegalStateException("Database connection not initialized");
        }

        SelectPSBuilder.SelectRunner runner = searchQuery.createRunner();
        if (propertyMap.containsKey("action")) {
            runner.fillParameter("action", (String) propertyMap.get("action"));
        }
        if (propertyMap.containsKey("targets")) {
            GameProfileArgumentType.GameProfileArgument targets = (GameProfileArgumentType.GameProfileArgument)propertyMap.get("targets");
            runner.fillParameter("targets", targets.getNames(scs).stream().map(gp -> gp.getId().toString()).toArray());
        }
        if (propertyMap.containsKey("block")) {
            BlockStateArgument block = (BlockStateArgument)propertyMap.get("block");
            runner.fillParameter("block", Registry.BLOCK.getId(block.getBlockState().getBlock()).toString() + "%");
        }
        if (propertyMap.containsKey("range")) {
            int range = (Integer)propertyMap.get("range");
            range *= range;
            BlockPos playerPos = sourcePlayer.getBlockPos();
            int x = playerPos.getX();
            int y = playerPos.getY();
            int z = playerPos.getZ();
            runner.fillParameter("range", x, x, y, y, z, z, range);
        }
        String message = "Blocklogger data for query";
        PrintToChat.print(sourcePlayer, message, Formatting.GOLD);
        try {
            //Read data
            ResultSet rs = runner.execute();
            // Repeat for every entry
            while (rs.next()) {
                //Get the info from the database and return
                PrintToChat.print(scs.getPlayer(), "At "+rs.getInt("x")+" "+rs.getInt("y")+" "+rs.getInt("z"), Formatting.AQUA);
                PrintToChat.prepareInteractionsPrint(toStringMap(rs,
                        "x", "y", "z", "state", "dimension", "type", "player", "time"), sourcePlayer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static HashMap<String, String> toStringMap (ResultSet rs, String... args) throws SQLException {
        HashMap<String, String> result = new HashMap<>();
        for (String arg : args) {
            result.put(arg, rs.getString(arg));
        }
        return result;
    }

	public static void writeContainerTransaction(BlockPos pos, ItemStack stack, PlayerEntity player, LoggedEventType type) {
        String itemName = stack.toString();
        if (con == null) {
            // Check if database isn't connected
            throw new IllegalStateException("Database connection not initialized");
        }
        //Get date
        LocalDateTime dateTime = LocalDateTime.now();
        String time = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        InsertPSBuilder.InsertRunner runner = writeInteractionsQuery.createRunner();
        try {
            runner.fillParameter("type", type.name());
            runner.fillParameter("pos", pos.getX(), pos.getY(), pos.getZ());
            runner.fillParameter("dimension", PlayerUtils.getPlayerDimension(player));
            runner.fillParameter("state", itemName);
            runner.fillParameter("player", getPlayerUuid(player));
            runner.fillParameter("time", time);

            runner.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}


    private static void onConnection () throws SQLException {
        searchQuery = new SelectPSBuilder(con, "SELECT type,x,y,z,dimension,state,player,time,rolledbackat FROM interactions", "ORDER BY time DESC LIMIT 10");
        searchQuery.addPredicate(JDBCType.VARCHAR, "action", "type = ?");
        searchQuery.addPredicate(JDBCType.ARRAY, "targets", "player IN (?)");
        searchQuery.addPredicate(JDBCType.VARCHAR, "block", "state LIKE ?");
        searchQuery.addPredicate(JDBCType.VARCHAR, "range",
                "((x - ?)*(x - ?) + (y - ?)*(y - ?) + (z - ?)*(z - ?)) <= ?");
        searchQuery.prepare();

        readEventsQuery = new SelectPSBuilder(con, "SELECT type,x,y,z,dimension,state,player,time,rolledbackat FROM interactions", "ORDER BY time DESC LIMIT 10");
        readEventsQuery.addPredicate(JDBCType.VARCHAR, "pos","x = ? AND y = ? AND z = ?");
        readEventsQuery.addPredicate(JDBCType.VARCHAR, "dimension","dimension = ?");
        readEventsQuery.addPredicate(JDBCType.VARCHAR, "type","type = ?");
        readEventsQuery.prepare();

        writeInteractionsQuery = new InsertPSBuilder(con, "INSERT INTO interactions");
        writeInteractionsQuery.addFillable("type");
        writeInteractionsQuery.addFillable("pos", "x", "y", "z");
        writeInteractionsQuery.addFillable("dimension");
        writeInteractionsQuery.addFillable("state");
        writeInteractionsQuery.addFillable("player");
        writeInteractionsQuery.addFillable("time");
        writeInteractionsQuery.prepare();
    }
    public static void writeFluidInteraction(BlockPos pos, String state, PlayerEntity player, LoggedEventType type, boolean isPlayer) {
        if (con == null) {
            // Check if database isn't connected
            throw new IllegalStateException("Database connection not initialized");
        }
        //Get date
        LocalDateTime dateTime = LocalDateTime.now();
        String time = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        InsertPSBuilder.InsertRunner runner = writeInteractionsQuery.createRunner();
        try {
            runner.fillParameter("type", type.name());
            runner.fillParameter("pos", pos.getX(), pos.getY(), pos.getZ());
            runner.fillParameter("dimension", PlayerUtils.getPlayerDimension(player));
            runner.fillParameter("state", state);
            if(isPlayer){
                runner.fillParameter("player", getPlayerUuid(player));
            }
            else{
                runner.fillParameter("player", Util.NIL_UUID);
            }
            runner.fillParameter("time", time);

            runner.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void cusQuery(String query, PlayerEntity player){
        try{
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                PrintToChat.print(player, rs.getString("type")+" "+rs.getInt("x")+ " " + rs.getInt("y")+" "+rs.getInt("z")+" "+getDisplayName(getUuid(rs.getString("player")))+" "+rs.getString("time"), Formatting.DARK_AQUA);
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }
}       
