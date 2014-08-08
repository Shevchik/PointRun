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

package pointrun.arena.structure;

import java.util.ArrayList;
import java.util.List;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import pointrun.messages.Messages;

public class Rewards {

	private Object economy = null;

	public Rewards() {
		if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
			RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			if (economyProvider != null) {
				economy = economyProvider.getProvider();
			}
		}
	}

	private List<ItemStack> itemrewards = new ArrayList<ItemStack>();

	public List<ItemStack> getItemRewads() {
		return itemrewards;
	}

	private int moneyreward = 0;

	public int getMoneyReward() {
		return moneyreward;
	}

	private List<String> commands = new ArrayList<String>();

	public List<String> getCommandsToExecute() {
		return commands;
	}

	public void setRewards(ItemStack[] rewards) {
		itemrewards.clear();
		for (ItemStack reward : rewards) {
			if (reward != null) {
				itemrewards.add(reward);
			}
		}
	}

	public void setRewards(int money) {
		moneyreward = money;
	}

	public void addCommandToExecute(String command) {
		commands.add(command);
	}

	public void clearCommandsToExceute() {
		commands.clear();
	}

	public void rewardPlayer(Player player) {
		String rewardmessage = "";
		for (ItemStack reward : itemrewards) {
			if (player.getInventory().firstEmpty() != -1) {
				player.getInventory().addItem(reward);
			} else {
				player.getWorld().dropItemNaturally(player.getLocation(),reward);
			}
			rewardmessage += reward.getAmount() + " x " + reward.getType().toString().replace("_", "").toLowerCase()+ ", ";
		}
		if (moneyreward != 0) {
			rewardMoney(player, moneyreward);
			rewardmessage += ChatColor.GOLD.toString() + moneyreward;
		}
		if (rewardmessage.endsWith(", ")) {
			rewardmessage = rewardmessage.substring(0, rewardmessage.length() - 2);
		}
		rewardmessage = Messages.playerrewardmessage.replace("{REWARD}", rewardmessage);
		if (!rewardmessage.isEmpty()) {
			Messages.sendMessage(player, rewardmessage);
		}
		for (String command : commands) {
			command = command.replace("{playerName}", player.getName());
			command = command.replace("{playerUUID}", player.getUniqueId().toString());
			ServerCommandEvent event = new ServerCommandEvent(Bukkit.getConsoleSender(), command);
			Bukkit.getPluginManager().callEvent(event);
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), event.getCommand());
		}
	}

	private void rewardMoney(Player player, int money) {
		if (economy != null) {
			Economy econ = (Economy) economy;
			econ.depositPlayer(player, money);
		}
	}

	public void saveToConfig(FileConfiguration config) {
		config.set("reward.money", moneyreward);
		config.set("reward.items", itemrewards);
		config.set("reward.commands", commands);
	}

	@SuppressWarnings("unchecked")
	public void loadFromConfig(FileConfiguration config) {
		moneyreward = config.getInt("reward.money", moneyreward);
		Object obj = config.get("reward.items");
		if (obj != null) {
			itemrewards = (List<ItemStack>) obj;
		}
		commands = config.getStringList("reward.commands");
	}

}
