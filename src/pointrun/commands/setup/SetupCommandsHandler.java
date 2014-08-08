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

package pointrun.commands.setup;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pointrun.PointRun;
import pointrun.commands.setup.arena.AddCommandsRewards;
import pointrun.commands.setup.arena.AddKit;
import pointrun.commands.setup.arena.ClearCommandsRewards;
import pointrun.commands.setup.arena.CreateArena;
import pointrun.commands.setup.arena.DeleteArena;
import pointrun.commands.setup.arena.DeleteKit;
import pointrun.commands.setup.arena.DisableArena;
import pointrun.commands.setup.arena.EnableArena;
import pointrun.commands.setup.arena.FinishArena;
import pointrun.commands.setup.arena.SetArena;
import pointrun.commands.setup.arena.SetCountdown;
import pointrun.commands.setup.arena.SetDamage;
import pointrun.commands.setup.arena.SetGameLevelDestroyDelay;
import pointrun.commands.setup.arena.SetItemsRewards;
import pointrun.commands.setup.arena.SetLoseLevel;
import pointrun.commands.setup.arena.SetMaxPlayers;
import pointrun.commands.setup.arena.SetMinPlayers;
import pointrun.commands.setup.arena.SetMoneyRewards;
import pointrun.commands.setup.arena.SetSpawn;
import pointrun.commands.setup.arena.SetSpectatorSpawn;
import pointrun.commands.setup.arena.SetTeleport;
import pointrun.commands.setup.arena.SetTimeLimit;
import pointrun.commands.setup.arena.SetVotePercent;
import pointrun.commands.setup.lobby.DeleteLobby;
import pointrun.commands.setup.lobby.SetLobby;
import pointrun.commands.setup.reload.ReloadBars;
import pointrun.commands.setup.reload.ReloadMSG;
import pointrun.commands.setup.selection.Clear;
import pointrun.commands.setup.selection.SetP1;
import pointrun.commands.setup.selection.SetP2;
import pointrun.messages.Messages;
import pointrun.selectionget.PlayerSelection;

public class SetupCommandsHandler implements CommandExecutor {

	private PlayerSelection plselection = new PlayerSelection();

	private HashMap<String, CommandHandlerInterface> commandHandlers = new HashMap<String, CommandHandlerInterface>();

	public SetupCommandsHandler(PointRun plugin) {
		commandHandlers.put("setp1", new SetP1(plselection));
		commandHandlers.put("setp2", new SetP2(plselection));
		commandHandlers.put("clear", new Clear(plselection));
		commandHandlers.put("setlobby", new SetLobby(plugin));
		commandHandlers.put("deletelobby", new DeleteLobby(plugin));
		commandHandlers.put("reloadmsg", new ReloadMSG(plugin));
		commandHandlers.put("reloadbars", new ReloadBars(plugin));
		commandHandlers.put("create", new CreateArena(plugin));
		commandHandlers.put("delete", new DeleteArena(plugin));
		commandHandlers.put("setarena", new SetArena(plugin, plselection));
		commandHandlers.put("setgameleveldestroydelay", new SetGameLevelDestroyDelay(plugin));
		commandHandlers.put("setloselevel", new SetLoseLevel(plugin, plselection));
		commandHandlers.put("setspawn", new SetSpawn(plugin));
		commandHandlers.put("setspectate", new SetSpectatorSpawn(plugin));
		commandHandlers.put("setmaxplayers", new SetMaxPlayers(plugin));
		commandHandlers.put("setminplayers", new SetMinPlayers(plugin));
		commandHandlers.put("setvotepercent", new SetVotePercent(plugin));
		commandHandlers.put("setcountdown", new SetCountdown(plugin));
		commandHandlers.put("setitemsrewards", new SetItemsRewards(plugin));
		commandHandlers.put("setmoneyrewards", new SetMoneyRewards(plugin));
		commandHandlers.put("addcommandrewards", new AddCommandsRewards(plugin));
		commandHandlers.put("clearcommandrewards", new ClearCommandsRewards(plugin));
		commandHandlers.put("addkit", new AddKit(plugin));
		commandHandlers.put("deleteKit", new DeleteKit(plugin));
		commandHandlers.put("settimelimit", new SetTimeLimit(plugin));
		commandHandlers.put("setteleport", new SetTeleport(plugin));
		commandHandlers.put("setdamage", new SetDamage(plugin));
		commandHandlers.put("finish", new FinishArena(plugin));
		commandHandlers.put("disable", new DisableArena(plugin));
		commandHandlers.put("enable", new EnableArena(plugin));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Player is expected");
			return true;
		}
		Player player = (Player) sender;
		// check permissions
		if (!player.hasPermission("pointrun.setup")) {
			Messages.sendMessage(player, Messages.nopermission);
			return true;
		}
		// get command
		if (args.length > 0 && commandHandlers.containsKey(args[0])) {
			CommandHandlerInterface commandh = commandHandlers.get(args[0]);
			//check args length
			if (args.length - 1 < commandh.getMinArgsLength()) {
				Messages.sendMessage(player, ChatColor.RED+"Not enough args");
				return false;
			}
			//execute command
			boolean result = commandh.handleCommand(player, Arrays.copyOfRange(args, 1, args.length));
			return result;
		}
		return false;
	}

}
