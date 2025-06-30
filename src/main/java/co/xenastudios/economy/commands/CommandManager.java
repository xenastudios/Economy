package co.xenastudios.economy.commands;

import co.xenastudios.economy.EconomyPlugin;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Handles registration of all plugin commands based on configuration.
 */
public class CommandManager {

	/**
	 * Registers commands with the server, based on the plugin's configuration.
	 * Only registers commands if their 'enabled' field is true.
	 *
	 * @param plugin The main plugin instance.
	 */
	public static void registerCommands(EconomyPlugin plugin) {
		FileConfiguration config = plugin.getConfig();

		plugin.getLifecycleManager().registerEventHandler(
				LifecycleEvents.COMMANDS,
				commands -> {
					// Register each command if enabled in config (default: false)
					if (config.getBoolean("commands.balance.enabled", false)) {
						commands.registrar().register(
								BalanceCommand.createCommand(plugin),
								"Check your balance.",
								config.getStringList("commands.balance.aliases")
						);
					}
					if (config.getBoolean("commands.economyadmin.enabled", false)) {
						commands.registrar().register(
								EconomyAdminCommand.createCommand(plugin),
								"Manage your Economy plugin.",
								config.getStringList("commands.economyadmin.aliases")
						);
					}
					if (config.getBoolean("commands.pay.enabled", false)) {
						commands.registrar().register(
								PayCommand.createCommand(plugin),
								"Pay someone else.",
								config.getStringList("commands.pay.aliases")
						);
					}
					if (config.getBoolean("commands.withdraw.enabled", false)) {
						commands.registrar().register(
								WithdrawCommand.createCommand(plugin),
								"Withdraw your balance into a banknote.",
								config.getStringList("commands.withdraw.aliases")
						);
					}
				}
		);

		// Log command registration using the plugin's logger
		plugin.getLogger().info("Commands registered successfully.");
	}
}