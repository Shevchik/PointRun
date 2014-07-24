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

package pointrun.commands.setup.reload;

import org.bukkit.entity.Player;

import pointrun.PointRun;
import pointrun.bars.Bars;
import pointrun.commands.setup.CommandHandlerInterface;

public class ReloadBars implements CommandHandlerInterface {

	private PointRun plugin;
	public ReloadBars(PointRun plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean handleCommand(Player player, String[] args) {
		Bars.loadBars(plugin);
		player.sendMessage("nars reloaded");
		return true;
	}

	@Override
	public int getMinArgsLength() {
		return 0;
	}

}