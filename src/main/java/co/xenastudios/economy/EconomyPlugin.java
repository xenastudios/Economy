package co.xenastudios.economy;

import co.xenastudios.economy.commands.CommandManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Main class for the Economy plugin.
 * Handles plugin lifecycle events, economy service registration, and data management.
 */
public final class EconomyPlugin extends JavaPlugin {
	// Volatile for thread safety in case of async access
	private volatile @Nullable Economy economyHandler;
	private @Nullable DataManager dataManager;

	@Override
	public void onEnable() {
		// Save the default config.yml if it does not exist
		saveDefaultConfig();

		// Initialize the data manager (handles both player and banknote data)
		this.dataManager = new DataManager(this);

		// Register the economy service with the lowest priority
		BasicEconomy basicEconomy = new BasicEconomy(this);
		ServicesManager servicesManager = Bukkit.getServicesManager();
		servicesManager.register(
				Economy.class,
				basicEconomy,
				this,
				ServicePriority.Lowest
		);

		// Register the banknote listener for banknote redemption
		getServer().getPluginManager().registerEvents(new BanknoteListener(this), this);

		// Register all plugin commands
		CommandManager.registerCommands(this);

		this.getLogger().info("Plugin enabled!");
	}

	@Override
	public void onDisable() {
		// Save player and banknote data
		if (this.dataManager != null) {
			this.dataManager.savePlayerData();
			this.dataManager.saveBanknoteData();
		}

		Bukkit.getServicesManager().unregister(Economy.class, getEconomyHandler());
		this.getLogger().info("Plugin disabled!");
	}

	/**
	 * Gets the data manager.
	 *
	 * @return the data manager instance
	 * @throws IllegalStateException if the data manager is not initialized
	 */
	public @NotNull DataManager getDataManager() {
		if (this.dataManager == null) {
			throw new IllegalStateException("DataManager is not initialized.");
		}
		return this.dataManager;
	}

	/**
	 * Gets the economy handler, initializing it if necessary.
	 * Uses Vault's service manager to retrieve the registered provider.
	 *
	 * @return the economy handler
	 */
	public @NotNull Economy getEconomyHandler() {
		// Double-checked locking for thread safety
		Economy handler = this.economyHandler;
		if (handler == null) {
			synchronized (this) {
				handler = this.economyHandler;
				if (handler == null) {
					ServicesManager servicesManager = Bukkit.getServicesManager();
					RegisteredServiceProvider<Economy> registration =
							servicesManager.getRegistration(Economy.class);
					if (registration != null) {
						handler = registration.getProvider();
					}
					if (handler == null) {
						handler = new BasicEconomy(this);
					}
					this.economyHandler = handler;
				}
			}
		}
		return handler;
	}
}