package pointrun.commands.setup.lobby;

import org.bukkit.entity.Player;

import pointrun.PointRun;
import pointrun.commands.setup.CommandHandlerInterface;

public class DeleteLobby implements CommandHandlerInterface {

	private PointRun plugin;
	public DeleteLobby(PointRun plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean handleCommand(Player player, String[] args) {
		plugin.globallobby.setLobbyLocation(null);
		player.sendMessage("Lobby deleted");
		return true;
	}

	@Override
	public int getMinArgsLength() {
		return 0;
	}

}
