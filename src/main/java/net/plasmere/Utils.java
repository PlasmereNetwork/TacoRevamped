package net.plasmere;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final HashMap<ServerPlayerEntity, ServerPlayerEntity> msging = new HashMap<>();
    public static HashMap<ServerPlayerEntity, ServerPlayerEntity> getMsging(){
        return msging;
    }
    public static void putMsges(ServerPlayerEntity from, ServerPlayerEntity to){ msging.put(from, to); }
    public static void remMsges(ServerPlayerEntity from){ msging.remove(from); }
    public static boolean inMsges(ServerPlayerEntity from){ return msging.containsKey(from); }

    private static final List<ServerPlayerEntity> sitToggled = new ArrayList<>();
    public static List<ServerPlayerEntity> getSitToggled() { return sitToggled; }
    public static void putSitToggled(ServerPlayerEntity p) { sitToggled.add(p); }
    public static void remSitToggled(ServerPlayerEntity p) { sitToggled.add(p); }
    public static boolean isSitToggled(ServerPlayerEntity p) { return sitToggled.contains(p); }
    public static void toggleSitToggled(ServerPlayerEntity p) {
        if (sitToggled.contains(p)) {
            remSitToggled(p);
        } else {
            putSitToggled(p);
        }
    }

    public static boolean isHuman(ServerCommandSource source) {
        return source.getEntity() instanceof ServerPlayerEntity;
    }

    public static boolean hasPermission(ServerPlayerEntity player, String perm) {
        if (player.hasPermissionLevel(3) || TacoRevamped.getConfiguration().getPermissions()
                .hasPermission(player.getUuidAsString(), "mute")) {
            return true;
        }
        return false;
    }

    public static boolean hasPermissionFromSource(ServerCommandSource scs, String string) {
        try {
            if (scs.getPlayer().hasPermissionLevel(3) || TacoRevamped.getConfiguration().getPermissions()
                    .hasPermission(scs.getPlayer().getUuidAsString(), "mute")) {
                return true;
            }
            return false;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
		return false;
	}

	public static String codedString(String text){
        return translateAlternateColorCodes('&', text).replace("%nl%", "\n").replace("%newline%", "\n");
    }

    public static final char COLOR_CHAR = '\u00A7';

    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        Validate.notNull(textToTranslate, "Cannot translate null text");
        char[] b = textToTranslate.toCharArray();

        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) {
                b[i] = Utils.COLOR_CHAR;
                b[i+1] = Character.toLowerCase(b[i+1]);
            }
        }

        return new String(b);
    }

    public static MutableText newText() {
        return new LiteralText("");
    }

    public static MutableText newText(@Nullable final String... strings) {
        MutableText text = newText();
        for (String string : strings) {
            text.append(string);
        }
        return text;
    }

    @NotNull
    public static String translate(String string) {
        return translateAlternateColorCodes('&', string);
    }

    public static MutableText newText(final String str) {
        return new LiteralText(translate(str));
    }

    public static MutableText codedText(String text){
        LiteralText mt = new LiteralText(codedString(text));

        try {
            Pattern pattern = Pattern.compile("(http|ftp|https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            String foundUrl = "";

            while (matcher.find()) {
                foundUrl = matcher.group(0);

                return makeLinked(text, foundUrl);
            }
        } catch (Exception e) {
            return mt;
        }
        return mt;
    }

    public static MutableText codedCHText(String text){
        LiteralText mt = new LiteralText(codedString(text));

        try {
            Pattern pattern = Pattern.compile("(http|ftp|https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            String foundUrl = "";

            while (matcher.find()) {
                foundUrl = matcher.group(0);

                String hover = "&4&n" + foundUrl;

                return makeCHLinked(text, foundUrl, hover);
            }
        } catch (Exception e) {
            return mt;
        }
        return mt;
    }

    public static MutableText codedCHNamedText(String text, String name){
        LiteralText mt = new LiteralText(codedString(text));

        try {
            Pattern pattern = Pattern.compile("(http|ftp|https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            String foundUrl = "";

            while (matcher.find()) {
                foundUrl = matcher.group(0);

                String hover = "&4&n" + foundUrl;

                text = text.replace(name, "&e" + name + "&r");

                return makeCHLinked(text, foundUrl, hover);
            }
        } catch (Exception e) {
            return mt;
        }
        return mt;
    }

    public static String chattedString(ServerPlayerEntity playerEntity){
        return "&r<" + getDisplayName(playerEntity) + "&r> ";
    }

    public static String getDisplayName(ServerPlayerEntity playerEntity){
//        Team team = playerEntity.getScoreboard().getTeam(Objects.requireNonNull(playerEntity.getScoreboardTeam()).getName());
//        return team.getPrefix().asString() + team.getColor().toString() + playerEntity.getName().asString() + team.getSuffix().asString();
        String dis = Team.decorateName(playerEntity.getScoreboardTeam(), playerEntity.getName()).asString();
        String dis2 = playerEntity.getDisplayName().asString();
        return dis.equals("") ? dis2.equals("") ? playerEntity.getName().asString() : dis2 : dis;
    }

    public static String getDisplayName(Entity entity){
//        Team team = playerEntity.getScoreboard().getTeam(Objects.requireNonNull(playerEntity.getScoreboardTeam()).getName());
//        return team.getPrefix().asString() + team.getColor().toString() + playerEntity.getName().asString() + team.getSuffix().asString();
        String dis = Team.decorateName(entity.getScoreboardTeam(), entity.getName()).asString();
        String dis2 = entity.getDisplayName().asString();
        String dis3 = entity.getName().asString();
        return dis.equals("") ? dis2.equals("") ? dis3.equals("") ? entity.getEntityName() : dis3 : dis2 : dis;
    }

    public static MutableText getPlayerDisplayNameText(ServerPlayerEntity playerEntity){
        MutableText mutableText = Team.decorateName(playerEntity.getScoreboardTeam(), playerEntity.getName());
        return addTellClickEvent(mutableText, playerEntity);
    }

    public static MutableText addTellClickEvent(MutableText component, ServerPlayerEntity playerEntity) {
        String string = playerEntity.getGameProfile().getName();
        return component.styled(style ->
                style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tell " + string + " ")).withHoverEvent(getPlayerHoverEvent(playerEntity)).withInsertion(string)
        );
    }

    public static HoverEvent getPlayerHoverEvent(ServerPlayerEntity playerEntity) {
        return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new HoverEvent.EntityContent(playerEntity.getType(), playerEntity.getUuid(), playerEntity.getName()));
    }

    public static MutableText makeLinked(String text, String url){
        MutableText mt = new LiteralText(text);
        mt.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
        return mt;
    }

    public static MutableText makeCHLinked(String text, String url, String hover){
        MutableText mt = new LiteralText(text);
        mt.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
        mt.styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, codedText(hover))));
        return mt;
    }

    public static String normalize(String[] strings){
        StringBuilder thing = new StringBuilder();
        int i = 1;
        for (String t : strings){
            if (i < strings.length) {
                thing.append(t).append(" ");
            } else {
                thing.append(t);
            }
        }

        return thing.toString();
    }

    public static String normalize(List<String> strings){
        StringBuilder thing = new StringBuilder();
        int i = 1;
        for (String t : strings){
            if (i < strings.size()) {
                thing.append(t).append(" ");
            } else {
                thing.append(t);
            }
        }

        return thing.toString();
    }
}
