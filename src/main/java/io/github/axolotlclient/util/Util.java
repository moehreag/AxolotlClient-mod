package io.github.axolotlclient.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.Window;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.ServerAddress;
import net.minecraft.network.listener.ClientQueryPacketListener;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.network.packet.s2c.query.QueryPongS2CPacket;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

public class Util {



	public static String lastgame;
	public static String game;

	/**
	 * Gets the amount of ticks in between start and end, on a 24000 tick system.
	 *
	 * @param start The start of the time you wish to measure
	 * @param end   The end of the time you wish to measure
	 * @return The amount of ticks in between start and end
	 *
	 */
    public static int getTicksBetween(int start, int end) {
        if (end < start) end += 24000;
        return end - start;
    }

    public static void registerCommand(LiteralArgumentBuilder<FabricClientCommandSource> builder){
        ClientCommandManager.DISPATCHER.register(builder);
    }

	public static String getGame(){

		List<String> sidebar = getSidebar();

		if(sidebar.isEmpty()) game = "";
		else if (MinecraftClient.getInstance().getCurrentServerEntry() != null && MinecraftClient.getInstance().getCurrentServerEntry().address.toLowerCase().contains(sidebar.get(0).toLowerCase())){
			if ( sidebar.get(sidebar.size() -1).toLowerCase(Locale.ROOT).contains(MinecraftClient.getInstance().getCurrentServerEntry().address.toLowerCase(Locale.ROOT)) || sidebar.get(sidebar.size()-1).contains("Playtime")){
				game = "In Lobby";
			}  else {
				if (sidebar.get(sidebar.size()-1).contains("--------")){
					game = "Playing Bridge Practice";
				} else {
					game = "Playing "+ sidebar.get(sidebar.size() -1);
				}
			}
		} else {
			game = "Playing "+ sidebar.get(0);
		}

		if(!Objects.equals(lastgame, game) && game.equals("")) game = lastgame;
		else lastgame = game;

		if (game==null){game="";}

		return game;
	}

    public static LiteralText formatFromCodes(String formattedString){
        LiteralText text = new LiteralText("");
        String[] arr = formattedString.split("§");

        List<Formatting> modifiers = new ArrayList<>();
        for (String s:arr){

            Formatting formatting = Formatting.byCode(s.length()>0 ? s.charAt(0) : 0);
            if(formatting != null && formatting.isModifier()){
                modifiers.add(formatting);
                continue;
            }
            LiteralText part = new LiteralText(s.length()>0 ? s.substring(1):"");
            if(formatting != null){
                part.formatted(formatting);

                if(!modifiers.isEmpty()){
                    modifiers.forEach(part::formatted);
                    modifiers.clear();
                }
            }
            text.append(part);
        }

        return text;
    }

    public static double calculateDistance(Vec3d pos1, Vec3d pos2){
        return calculateDistance(pos1.x, pos2.x, pos1.y, pos2.y, pos1.z, pos2.z);
    }

    public static double calculateDistance(double x1, double x2, double y1, double y2, double z1, double z2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
    }

    public static DrawPosition toGlCoords(int x, int y){
        return toGlCoords(new DrawPosition(x, y));
    }

    public static DrawPosition toGlCoords(DrawPosition pos){
        double scale = MinecraftClient.getInstance().getWindow().getScaleFactor();
        return new DrawPosition((int) (pos.x * scale),
            (int) (MinecraftClient.getInstance().getWindow().getFramebufferHeight() - pos.y * scale - scale));
    }

    public static DrawPosition toMCCoords(int x, int y){
        return toMCCoords(new DrawPosition(x, y));
    }

    public static DrawPosition toMCCoords(DrawPosition pos){
        Window window = MinecraftClient.getInstance().getWindow();
        int x = pos.x * window.getWidth() / window.getFramebufferWidth();
        int y = window.getHeight() - pos.y * window.getHeight() / window.getFramebufferHeight() - 1;
        return new DrawPosition(x, y);
    }

	public static void sendChatMessage(String msg) {
		MinecraftClient.getInstance().player.sendChatMessage(msg);
	}

    public static void sendChatMessage(Text msg){
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(msg);
    }

	public static List<String> getSidebar() {
		List<String> lines = new ArrayList<>();
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null) return lines;

		Scoreboard scoreboard = client.world.getScoreboard();
		if (scoreboard == null) return lines;
		ScoreboardObjective sidebar = scoreboard.getObjectiveForSlot(1);
		if (sidebar == null) return lines;

		Collection<ScoreboardPlayerScore> scores = scoreboard.getAllPlayerScores(sidebar);
		List<ScoreboardPlayerScore> list = scores.stream()
			.filter(input -> input != null && input.getPlayerName() != null && !input.getPlayerName().startsWith("#"))
			.collect(Collectors.toList());

		if (list.size() > 15) {
			scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
		} else {
			scores = list;
		}

		for (ScoreboardPlayerScore score : scores) {
			Team team = scoreboard.getPlayerTeam(score.getPlayerName());
			if (team == null) return lines;
			String text = team.getPrefix().getString() + team.getSuffix().getString();
			if (text.trim().length() > 0)
				lines.add(text);
		}

		lines.add(sidebar.getDisplayName().getString());
		Collections.reverse(lines);

		return lines;
	}

	public static void applyScissor(Rectangle scissor){
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		Window window = MinecraftClient.getInstance().getWindow();
		double scale = window.getScaleFactor();
		GL11.glScissor((int) (scissor.x * scale), (int) ((window.getScaledHeight() - scissor.height - scissor.y) * scale), (int) (scissor.width * scale), (int) (scissor.height * scale));
	}

	public static double lerp(double start, double end, double percent) {
		return start + ((end - start) * percent);
	}

}
