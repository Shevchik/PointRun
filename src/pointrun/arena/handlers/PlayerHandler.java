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

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import pointrun.arena.Arena;
import pointrun.arena.structure.StructureManager.TeleportDestination;
import pointrun.bars.Bars;
import pointrun.messages.Messages;
import pointrun.scoreboards.Scoreboards;

public class PlayerHandler {

	private Arena arena;

	public PlayerHandler(Arena arena) {
		this.arena = arena;
	}

	// check if player can join the arena
	public boolean checkJoin(Player player) {
		if (arena.getStructureManager().getWorld() == null) {
			Messages.sendMessage(player, Messages.arenawolrdna);
			return false;
		}
		if (!arena.getStatusManager().isArenaEnabled()) {
			Messages.sendMessage(player, Messages.arenadisabled);
			return false;
		}
		if (arena.getStatusManager().isArenaRunning()) {
			Messages.sendMessage(player, Messages.arenarunning);
			return false;
		}
		if (arena.getStatusManager().isArenaRegenerating()) {
			Messages.sendMessage(player, Messages.arenaregenerating);
			return false;
		}
		if (player.isInsideVehicle()) {
			Messages.sendMessage(player, Messages.arenavehicle);
			return false;
		}
		if (arena.getPlayersManager().getPlayersCount() == arena.getStructureManager().getMaxPlayers()) {
			Messages.sendMessage(player, Messages.limitreached);
			return false;
		}
		return true;
	}

	// spawn player on arena
	@SuppressWarnings("deprecation")
	public void spawnPlayer(final Player player, String msgtoplayer, String msgtoarenaplayers) {
		// teleport player to arena
		arena.plugin.pdata.storePlayerLocation(player);
		player.teleport(arena.getStructureManager().getSpawnPoint());
		// set player visible to everyone
		for (Player aplayer : Bukkit.getOnlinePlayers()) {
			aplayer.showPlayer(player);
		}
		// change player status
		arena.plugin.pdata.storePlayerGameMode(player);
		player.setFlying(false);
		player.setAllowFlight(false);
		arena.plugin.pdata.storePlayerInventory(player);
		arena.plugin.pdata.storePlayerArmor(player);
		arena.plugin.pdata.storePlayerPotionEffects(player);
		arena.plugin.pdata.storePlayerHunger(player);
		// update inventory
		player.updateInventory();
		// add mining fatigue effect so player won't even attempt to break blocks
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, 5));
		// send message to player
		Messages.sendMessage(player, msgtoplayer);
		// send message to other players
		for (Player oplayer : arena.getPlayersManager().getPlayers()) {
			msgtoarenaplayers = msgtoarenaplayers.replace("{PLAYER}", player.getName());
			Messages.sendMessage(oplayer, msgtoarenaplayers);
		}
		// set player on arena data
		arena.getPlayersManager().addPlayer(player);
		// send message about arena player count
		String message = Messages.playerscountinarena;
		message = message.replace("{COUNT}", String.valueOf(arena.getPlayersManager().getPlayersCount()));
		Messages.sendMessage(player, message);
		// modify signs
		arena.plugin.signEditor.modifySigns(arena.getArenaName());
		// modify bars
		if (!arena.getStatusManager().isArenaStarting()) {
			for (Player oplayer : arena.getPlayersManager().getPlayers()) {
				Bars.setBar(oplayer, Bars.waiting, arena.getPlayersManager().getPlayersCount(), 0, arena.getPlayersManager().getPlayersCount() * 100 / arena.getStructureManager().getMinPlayers());
			}
		}
		// check for game start
		if (!arena.getStatusManager().isArenaStarting() && arena.getPlayersManager().getPlayersCount() == arena.getStructureManager().getMinPlayers()) {
			arena.getGameHandler().runArenaCountdown();
		}
	}

	// move to spectators
	public void spectatePlayer(Player player, String msgtoplayer, String msgtoarenaplayers) {
		// remove from players
		arena.getPlayersManager().removePlayer(player);
		// teleport to spectators spawn
		player.teleport(arena.getStructureManager().getSpectatorSpawn());
		// clear inventory
		player.getInventory().clear();
		player.getInventory().setArmorContents(new ItemStack[4]);
		// allow flight
		player.setAllowFlight(true);
		player.setFlying(true);
		// hide from others
		for (Player oplayer : Bukkit.getOnlinePlayers()) {
			oplayer.hidePlayer(player);
		}
		// send message to player
		Messages.sendMessage(player, msgtoplayer);
		// modify signs
		arena.plugin.signEditor.modifySigns(arena.getArenaName());
		// send message to other players and update bars
		for (Player oplayer : arena.getPlayersManager().getAllParticipantsCopy()) {
			msgtoarenaplayers = msgtoarenaplayers.replace("{PLAYER}", player.getName());
			Messages.sendMessage(oplayer, msgtoarenaplayers);
		}
		//add to spectators
		arena.getPlayersManager().addSpectator(player);
	}

	// remove player from arena
	public void leavePlayer(Player player, String msgtoplayer, String msgtoarenaplayers) {
		boolean spectator = arena.getPlayersManager().isSpectator(player);
		// remove player from arena and restore his state
		removePlayerFromArenaAndRestoreState(player, false);
		// should not send messages and other things when player is a spectator
		if (spectator) {
			return;
		}
		// send message to player
		Messages.sendMessage(player, msgtoplayer);
		// modify signs
		arena.plugin.signEditor.modifySigns(arena.getArenaName());
		// send message to other players and update bars
		for (Player oplayer : arena.getPlayersManager().getAllParticipantsCopy()) {
			msgtoarenaplayers = msgtoarenaplayers.replace("{PLAYER}", player.getName());
			Messages.sendMessage(oplayer, msgtoarenaplayers);
			if (!arena.getStatusManager().isArenaStarting() && !arena.getStatusManager().isArenaRunning()) {
				Bars.setBar(oplayer, Bars.waiting, arena.getPlayersManager().getPlayersCount(), 0, arena.getPlayersManager().getPlayersCount() * 100 / arena.getStructureManager().getMinPlayers());
			}
		}
	}

	protected void leaveWinner(Player player, String msgtoplayer) {
		// remove player from arena and restore his state
		removePlayerFromArenaAndRestoreState(player, true);
		// send message to player
		Messages.sendMessage(player, msgtoplayer);
		// modify signs
		arena.plugin.signEditor.modifySigns(arena.getArenaName());
	}

	@SuppressWarnings("deprecation")
	private void removePlayerFromArenaAndRestoreState(Player player, boolean winner) {
		// remove all potion effects
		for (PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}
		// reset spectators
		if (arena.getPlayersManager().isSpectator(player)) {
			arena.getPlayersManager().removeSpecator(player);
			for (Player oplayer : Bukkit.getOnlinePlayers()) {
				oplayer.showPlayer(player);
			}
			player.setAllowFlight(false);
			player.setFlying(false);
		}
		// remove player points
		arena.getGameHandler().removePlayerPoints(player);
		// remove scoreboard
		Scoreboards.unregisterScoreboard(player);
		// remove vote
		votes.remove(player.getName());
		// remove bar
		Bars.removeBar(player);
		// remove player on arena data
		arena.getPlayersManager().removePlayer(player);
		// restore player status
		arena.plugin.pdata.restorePlayerHunger(player);
		arena.plugin.pdata.restorePlayerPotionEffects(player);
		arena.plugin.pdata.restorePlayerArmor(player);
		arena.plugin.pdata.restorePlayerInventory(player);
		// reward player before restoring gamemode if player is winner
		if (winner) {
			arena.getStructureManager().getRewards().rewardPlayer(player);
		}
		arena.plugin.pdata.restorePlayerGameMode(player);
		// restore location or teleport to lobby
		if (arena.getStructureManager().getTeleportDestination() == TeleportDestination.LOBBY && arena.plugin.globallobby.isLobbyLocationWorldAvailable()) {
			player.teleport(arena.plugin.globallobby.getLobbyLocation());
			arena.plugin.pdata.clearPlayerLocation(player);
		} else {
			arena.plugin.pdata.restorePlayerLocation(player);
		}
		// update inventory
		player.updateInventory();
	}

	// vote for game start
	private HashSet<String> votes = new HashSet<String>();

	public boolean vote(Player player) {
		if (!votes.contains(player.getName())) {
			votes.add(player.getName());
			if (!arena.getStatusManager().isArenaStarting() && arena.getPlayersManager().getPlayersCount() > 1 && votes.size() >= arena.getPlayersManager().getPlayersCount() * arena.getStructureManager().getVotePercent()) {
				arena.getGameHandler().runArenaCountdown();
			}
			return true;
		}
		return false;
	}

}
