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

package pointrun.signs;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import pointrun.PointRun;
import pointrun.messages.Messages;
import pointrun.signs.type.JoinSign;
import pointrun.signs.type.LeaveSign;
import pointrun.signs.type.SignType;
import pointrun.signs.type.VoteSign;

public class SignHandler implements Listener {

	private HashMap<String, SignType> signs = new HashMap<String, SignType>();

	public SignHandler(PointRun plugin) {
		signs.put("[join]", new JoinSign(plugin));
		signs.put("[leave]", new LeaveSign(plugin));
		signs.put("[vote]", new VoteSign(plugin));
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onTNTRunSignCreate(SignChangeEvent e) {
		Player player = e.getPlayer();
		if (e.getLine(0).equalsIgnoreCase("[PointRun]") || e.getLine(0).equalsIgnoreCase(ChatColor.BLUE+"[PointRun]")) {
			if (!player.hasPermission("tntrun.setup")) {
				Messages.sendMessage(player, Messages.nopermission);
				e.setCancelled(true);
				e.getBlock().breakNaturally();
				return;
			}
			String line = e.getLine(1).toLowerCase();
			if (signs.containsKey(line)) {
				signs.get(line).handleCreation(e);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSignClick(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		if (!(e.getClickedBlock().getState() instanceof Sign)) {
			return;
		}
		Sign sign = (Sign) e.getClickedBlock().getState();
		if (sign.getLine(0).equalsIgnoreCase(ChatColor.BLUE + "[PointRun]")) {
			String line = sign.getLine(1).toLowerCase();
			if (signs.containsKey(line)) {
				signs.get(line).handleClick(e);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onSignDestroy(BlockBreakEvent e) {
		if (!(e.getBlock().getState() instanceof Sign)) {
			return;
		}
		Player player = e.getPlayer();
		Sign sign = (Sign) e.getBlock().getState();
		if (sign.getLine(0).equalsIgnoreCase(ChatColor.BLUE + "[PointRun]")) {
			if (!player.hasPermission("tntrun.setup")) {
				Messages.sendMessage(player, Messages.nopermission);
				e.setCancelled(true);
				return;
			}
			String line = sign.getLine(1).toLowerCase();
			if (signs.containsKey(line)) {
				signs.get(line).handleDestroy(e);
			}
		}
	}

}
