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

package pointrun.arena;

import java.io.File;

import pointrun.PointRun;
import pointrun.arena.handlers.GameHandler;
import pointrun.arena.handlers.PlayerHandler;
import pointrun.arena.status.PlayersManager;
import pointrun.arena.status.StatusManager;
import pointrun.arena.structure.StructureManager;

public class Arena {

	public PointRun plugin;

	public Arena(String name, PointRun plugin) {
		arenaname = name;
		this.plugin = plugin;
		arenagh = new GameHandler(plugin, this);
		arenaph = new PlayerHandler(plugin, this);
		arenafile = new File(plugin.getDataFolder() + File.separator + "arenas" + File.separator + arenaname + ".yml");
	}

	private String arenaname;
	public String getArenaName() {
		return arenaname;
	}

	private File arenafile;
	public File getArenaFile() {
		return arenafile;
	}

	private GameHandler arenagh;
	public GameHandler getGameHandler() {
		return arenagh;
	}

	private PlayerHandler arenaph;
	public PlayerHandler getPlayerHandler() {
		return arenaph;
	}

	private StatusManager statusManager = new StatusManager(this);
	public StatusManager getStatusManager() {
		return statusManager;
	}

	private StructureManager structureManager = new StructureManager(this);
	public StructureManager getStructureManager() {
		return structureManager;
	}

	private PlayersManager playersManager = new PlayersManager();
	public PlayersManager getPlayersManager() {
		return playersManager;
	}

}
