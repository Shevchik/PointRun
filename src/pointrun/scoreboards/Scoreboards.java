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

package pointrun.scoreboards;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import pointrun.arena.handlers.GameHandler.PlayerPoints;

public class Scoreboards {

	public static void registerScoreboard(Player player, String title) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective("PointRun", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(title);
        player.setScoreboard(board);
	}

	public static void updateScoreboard(Player player, HashMap<String, PlayerPoints> map) {
		for (String oldentry : player.getScoreboard().getEntries()) {
			if (!map.containsKey(oldentry)) {
				player.getScoreboard().resetScores(oldentry);
			}
		}
		Objective objective = player.getScoreboard().getObjective(DisplaySlot.SIDEBAR);
		for (Entry<String, PlayerPoints> entry : map.entrySet()) {
			objective.getScore(limitString(entry.getKey())).setScore(entry.getValue().getPoints());
		}
	}

	private static String limitString(String string) {
		return string.substring(0, Math.min(16, string.length()));
	}

	public static void unregisterScoreboard(Player player) {
		player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
	}

}
