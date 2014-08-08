/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package pointrun.arena.handlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import pointrun.PointRun;
import pointrun.arena.Arena;
import pointrun.arena.structure.Kits;
import pointrun.bars.Bars;
import pointrun.messages.Messages;
import pointrun.scoreboards.Scoreboards;

public class GameHandler {

	private PointRun plugin;
	private Arena arena;

	public GameHandler(PointRun plugin, Arena arena) {
		this.plugin = plugin;
		this.arena = arena;
		count = arena.getStructureManager().getCountdown();
	}

	private HashMap<String, PlayerPoints> playerpoints = new HashMap<String, PlayerPoints>();

	public Integer getPlayerPoints(String name) {
		return playerpoints.get(name).getPoints();
	}

	public void removePlayerPoints(String name) {
		playerpoints.remove(name);
	}

	// arena leave handler
	private int leavetaskid;

	public void startArenaAntiLeaveHandler() {
		leavetaskid = Bukkit.getScheduler().scheduleSyncRepeatingTask(
			plugin,
			new Runnable() {
				@Override
				public void run() {
					for (Player player : arena.getPlayersManager().getPlayersCopy()) {
						if (!arena.getStructureManager().isInArenaBounds(player.getLocation())) {
							arena.getPlayerHandler().leavePlayer(player, Messages.playerlefttoplayer, Messages.playerlefttoothers);
						}
					}
					for (Player player : arena.getPlayersManager().getSpectatorsCopy()) {
						if (!arena.getStructureManager().isInArenaBounds(player.getLocation())) {
							arena.getPlayerHandler().leavePlayer(player, "", "");
						}
					}
				}
			},
			0, 1
		);
	}

	public void stopArenaAntiLeaveHandler() {
		Bukkit.getScheduler().cancelTask(leavetaskid);
	}

	// arena start handler (running status updater)
	int runtaskid;
	int count;

	public void runArenaCountdown() {
		arena.getStatusManager().setStarting(true);
		runtaskid = Bukkit.getScheduler().scheduleSyncRepeatingTask(
			plugin,
			new Runnable() {
				@Override
				public void run() {
					// check if countdown should be stopped for some various reasons
					if (arena.getPlayersManager().getCount() < arena.getStructureManager().getMinPlayers()) {
						for (Player player : arena.getPlayersManager().getPlayers()) {
							Bars.setBar(player, Bars.waiting, arena.getPlayersManager().getCount(), 0, arena.getPlayersManager().getCount() * 100 / arena.getStructureManager().getMinPlayers());
						}
						stopArenaCountdown();
					} else
					// start arena if countdown is 0
					if (count == 0) {
						stopArenaCountdown();
						startArena();
					} else
					// countdown
					{
						String message = Messages.arenacountdown;
						message = message.replace("{COUNTDOWN}", String.valueOf(count));
						for (Player player : arena.getPlayersManager().getPlayers()) {
							Messages.sendMessage(player, message);
							Bars.setBar(player, Bars.starting, 0, count, count * 100 / arena.getStructureManager().getCountdown());
						}
						count--;
					}
				}
			},
			0, 20
		);
	}

	public void stopArenaCountdown() {
		arena.getStatusManager().setStarting(false);
		count = arena.getStructureManager().getCountdown();
		Bukkit.getScheduler().cancelTask(runtaskid);
	}

	// main arena handler
	private int timelimit;
	private int arenahandler;

	Random rnd = new Random();

	public void startArena() {
		//set running
		arena.getStatusManager().setRunning(true);
		//send messages
		String message = Messages.arenastarted;
		message = message.replace("{TIMELIMIT}", String.valueOf(arena.getStructureManager().getTimeLimit()));
		for (Player player : arena.getPlayersManager().getPlayers()) {
			Messages.sendMessage(player, message);
		}
		//update signs
		plugin.signEditor.modifySigns(arena.getArenaName());
		//give kits
		Kits kits = arena.getStructureManager().getKits();
		if (kits.getKits().size() > 0) {
			String[] kitnames = kits.getKits().toArray(new String[kits.getKits().size()]);
			for (Player player : arena.getPlayersManager().getPlayers()) {
				kits.giveKit(kitnames[rnd.nextInt(kitnames.length)], player);
			}
		}
		//add points and register scoreboard
		for (Player player : arena.getPlayersManager().getPlayers()) {
			playerpoints.put(player.getName(), new PlayerPoints());
			Scoreboards.registerScoreboard(player, ChatColor.BLUE+"Points");
		}
		timelimit = arena.getStructureManager().getTimeLimit() * 20; // timelimit is in ticks
		//start handler
		arenahandler = Bukkit.getScheduler().scheduleSyncRepeatingTask(
			plugin,
			new Runnable() {
				@Override
				public void run() {
					// finish game if player count is 0
					if (arena.getPlayersManager().getCount() == 0) {
						finishGame();
						return;
					}
					// move everyone to spectators if time is out
					if (timelimit < 0) {
						for (Player player : arena.getPlayersManager().getPlayersCopy()) {
							arena.getPlayerHandler().spectatePlayer(player, Messages.arenatimeout, "");
						}
						return;
					}
					// handle players
					for (Player player : arena.getPlayersManager().getPlayersCopy()) {
						// update bar
						Bars.setBar(player, Bars.playing, arena.getPlayersManager().getCount(), timelimit / 20, timelimit * 5 / arena.getStructureManager().getTimeLimit());
						// update scoreboard
						Scoreboards.updateScoreboard(player, playerpoints);
						// handle player
						handlePlayer(player);
					}
					// update bars and scoreboard for spectators too
					for (Player player : arena.getPlayersManager().getSpectators()) {
						// update bar
						Bars.setBar(player, Bars.playing, arena.getPlayersManager().getCount(), timelimit / 20, timelimit * 5 / arena.getStructureManager().getTimeLimit());
						// update scoreboard
						Scoreboards.updateScoreboard(player, playerpoints);
					}
					// decrease timelimit
					timelimit--;
				}
			},
			0, 1
		);
	}

	public void stopArena() {
		for (Player player : arena.getPlayersManager().getAllParticipantsCopy()) {
			arena.getPlayerHandler().leavePlayer(player, "", "");
		}
		arena.getStatusManager().setRunning(false);
		Bukkit.getScheduler().cancelTask(arenahandler);
		plugin.signEditor.modifySigns(arena.getArenaName());
		if (arena.getStatusManager().isArenaEnabled()) {
			startArenaRegen();
		}
	}

	// player handlers
	public void handlePlayer(final Player player) {
		Location plloc = player.getLocation();
		Location plufloc = plloc.clone().add(0, -1, 0);
		// remove block under player feet
		int points = arena.getStructureManager().getGameZone().destroyBlock(plufloc, arena);
		if (points != -1) {
			playerpoints.get(player.getName()).modifyPoints(points);
			player.playSound(plloc, Sound.ORB_PICKUP, 1, 1);
		}
		// check for lose
		if (arena.getStructureManager().getLoseLevel().isLooseLocation(plloc)) {
			// move to spectators
			arena.getPlayerHandler().spectatePlayer(player, Messages.playerlosttoplayer, Messages.playerlosttoothers);
			return;
		}
	}

	private void finishGame() {
		//calculate winner
		HashSet<Player> allplayers = arena.getPlayersManager().getAllParticipantsCopy();
		if (!allplayers.isEmpty()) {
			Player winner = allplayers.iterator().next();
			int max = playerpoints.get(winner.getName()).getPoints();
			for (Player spectator : allplayers) {
				int spoints = playerpoints.get(spectator.getName()).getPoints();
				if (spoints > max) {
					max = spoints;
					winner = spectator;
				}
			}
			//kick winner
			arena.getPlayerHandler().leaveWinner(winner, Messages.playerwontoplayer);
			broadcastWin(winner);
			//kick other players
			for (Player player : arena.getPlayersManager().getPlayersCopy()) {
				arena.getPlayerHandler().leavePlayer(player, Messages.playerlosttoplayer, "");
			}
		}
		stopArena();
	}

	private void broadcastWin(Player player) {
		String message = Messages.playerwonbroadcast;
		message = message.replace("{PLAYER}", player.getName());
		message = message.replace("{ARENA}", arena.getArenaName());
		Messages.broadcastMessage(message);
	}

	private void startArenaRegen() {
		// set arena is regenerating status
		arena.getStatusManager().setRegenerating(true);
		// modify signs
		plugin.signEditor.modifySigns(arena.getArenaName());
		// schedule gamezone regen
		int delay = arena.getStructureManager().getGameZone().regen(arena.plugin);
		// regen finished
		Bukkit.getScheduler().scheduleSyncDelayedTask(
			arena.plugin,
			new Runnable() {
				@Override
				public void run() {
					// set not regenerating status
					arena.getStatusManager().setRegenerating(false);
					// modify signs
					plugin.signEditor.modifySigns(arena.getArenaName());
				}
			},
			delay
		);
	}

	public static class PlayerPoints {

		private int points = 0;

		public void modifyPoints(int points) {
			this.points += points;
		}

		public int getPoints() {
			return points;
		}

	}

}
