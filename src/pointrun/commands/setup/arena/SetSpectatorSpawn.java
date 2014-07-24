package pointrun.commands.setup.arena;

import org.bukkit.entity.Player;

import pointrun.PointRun;
import pointrun.arena.Arena;
import pointrun.commands.setup.CommandHandlerInterface;

public class SetSpectatorSpawn implements CommandHandlerInterface {

	private PointRun plugin;
	public SetSpectatorSpawn(PointRun plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean handleCommand(Player player, String[] args) {
		Arena arena = plugin.amanager.getArenaByName(args[0]);
		if (arena != null) {
			if (arena.getStatusManager().isArenaEnabled()) {
				player.sendMessage("Disable arena first");
				return true;
			}
			if (arena.getStructureManager().setSpectatorsSpawn(player.getLocation())) {
				player.sendMessage("Spectator spawn set");
			} else {
				player.sendMessage("Spectator spawn should be in arena bounds");
			}
		} else {
			player.sendMessage("Arena does not exist");
		}
		return true;
	}

	@Override
	public int getMinArgsLength() {
		return 1;
	}

}
