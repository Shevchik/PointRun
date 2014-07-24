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

package pointrun.commands.setup.arena;

import org.bukkit.entity.Player;

import pointrun.PointRun;
import pointrun.arena.Arena;
import pointrun.commands.setup.CommandHandlerInterface;
import pointrun.selectionget.PlayerCuboidSelection;
import pointrun.selectionget.PlayerSelection;

public class SetLoseLevel implements CommandHandlerInterface {

	private PointRun plugin;
	private PlayerSelection selection;
	public SetLoseLevel(PointRun plugin, PlayerSelection selection) {
		this.plugin = plugin;
		this.selection = selection;
	}

	@Override
	public boolean handleCommand(Player player, String[] args) {
		Arena arena = plugin.amanager.getArenaByName(args[0]);
		if (arena == null) {
			player.sendMessage("Arena does not exist");
			return true;
		}
		if (arena.getStatusManager().isArenaEnabled()) {
			player.sendMessage("Disable arena first");
			return true;
		}
		if (arena.getStructureManager().getWorldName() == null) {
			player.sendMessage("Set arena bounds first");
			return true;
		}
		PlayerCuboidSelection sel = selection.getPlayerSelection(player);
		if (arena.getStructureManager().setLooseLevel(sel.getMinimumLocation(), sel.getMaximumLocation())) {
			player.sendMessage("LoseLevel set");
		} else {
			player.sendMessage("LoseLevel should be in arena bounds");
		}
		return true;
	}

	@Override
	public int getMinArgsLength() {
		return 1;
	}

}