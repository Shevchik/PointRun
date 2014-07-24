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

package pointrun;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import pointrun.arena.Arena;
import pointrun.bars.Bars;
import pointrun.commands.ConsoleCommands;
import pointrun.commands.GameCommands;
import pointrun.commands.setup.SetupCommandsHandler;
import pointrun.datahandler.ArenasManager;
import pointrun.datahandler.PlayerDataStore;
import pointrun.eventhandler.PlayerLeaveArenaChecker;
import pointrun.eventhandler.PlayerStatusHandler;
import pointrun.eventhandler.RestrictionHandler;
import pointrun.lobby.GlobalLobby;
import pointrun.messages.Messages;
import pointrun.signs.SignHandler;
import pointrun.signs.editor.SignEditor;

public class PointRun extends JavaPlugin {

	private Logger log;

	public PlayerDataStore pdata;
	public ArenasManager amanager;
	public GlobalLobby globallobby;
	public SignEditor signEditor;

	@Override
	public void onEnable() {
		log = getLogger();
		signEditor = new SignEditor(this);
		globallobby = new GlobalLobby(this);
		Messages.loadMessages(this);
		Bars.loadBars(this);
		pdata = new PlayerDataStore();
		amanager = new ArenasManager();
		getCommand("pointrunsetup").setExecutor(new SetupCommandsHandler(this));
		getCommand("pointrun").setExecutor(new GameCommands(this));
		getCommand("pointrunconsole").setExecutor(new ConsoleCommands(this));
		getServer().getPluginManager().registerEvents(new PlayerStatusHandler(this), this);
		getServer().getPluginManager().registerEvents(new RestrictionHandler(this), this);
		getServer().getPluginManager().registerEvents(new PlayerLeaveArenaChecker(this), this);
		getServer().getPluginManager().registerEvents(new SignHandler(this), this);
		// load arenas
		final File arenasfolder = new File(getDataFolder() + File.separator + "arenas");
		arenasfolder.mkdirs();
		final PointRun instance = this;
		getServer().getScheduler().scheduleSyncDelayedTask(
			this,
			new Runnable() {
				@Override
				public void run() {
					// load globallobyy
					globallobby.loadFromConfig();
					// load arenas
					for (String file : arenasfolder.list()) {
						Arena arena = new Arena(file.substring(0, file.length() - 4), instance);
						arena.getStructureManager().loadFromConfig();
						arena.getStatusManager().enableArena();
						amanager.registerArena(arena);
					}
					// load signs
					signEditor.loadConfiguration();
				}
			},
			20
		);
	}

	@Override
	public void onDisable() {
		// save arenas
		for (Arena arena : amanager.getArenas()) {
			arena.getStatusManager().disableArena();
			arena.getStructureManager().saveToConfig();
		}
		// save lobby
		globallobby.saveToConfig();
		globallobby = null;
		// save signs
		signEditor.saveConfiguration();
		signEditor = null;
		// unload other things
		pdata = null;
		amanager = null;
		log = null;
	}

	public void logSevere(String message) {
		log.severe(message);
	}

}
