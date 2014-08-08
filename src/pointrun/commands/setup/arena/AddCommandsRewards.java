package pointrun.commands.setup.arena;

import org.bukkit.entity.Player;

import pointrun.PointRun;
import pointrun.arena.Arena;
import pointrun.commands.setup.CommandHandlerInterface;

public class AddCommandsRewards implements CommandHandlerInterface {

	private PointRun plugin;
	public AddCommandsRewards(PointRun plugin) {
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
			arena.getStructureManager().getRewards().addCommandToExecute(args[1]);
			player.sendMessage("Command to execute on reward added");
		} else {
			player.sendMessage("Arena does not exist");
		}
		return true;
	}

	@Override
	public int getMinArgsLength() {
		return 2;
	}

}
