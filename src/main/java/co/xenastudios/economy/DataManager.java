package co.xenastudios.economy;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Handles both player balances and banknotes.
 */
public final class DataManager {
	private final EconomyPlugin plugin;

	// Player data
	private final File playerFile;
	private final YamlConfiguration playerConfig;

	// Banknote data
	private final File banknoteFile;
	private final YamlConfiguration banknoteConfig;

	public DataManager(@NotNull EconomyPlugin plugin) {
		this.plugin = plugin;

		this.playerFile = new File(plugin.getDataFolder(), "playerdata.yml");
		this.playerConfig = YamlConfiguration.loadConfiguration(playerFile);

		this.banknoteFile = new File(plugin.getDataFolder(), "banknotes.yml");
		this.banknoteConfig = YamlConfiguration.loadConfiguration(banknoteFile);
	}

	// --- Player Data ---

	public double getBalance(@NotNull OfflinePlayer player) {
		return playerConfig.getDouble(player.getUniqueId().toString(), 0.0);
	}

	public void setBalance(@NotNull OfflinePlayer player, double amount) {
		playerConfig.set(player.getUniqueId().toString(), amount);
		savePlayerData();
	}

	public void savePlayerData() {
		try {
			playerConfig.save(playerFile);
		} catch (IOException e) {
			plugin.getLogger().log(Level.WARNING, "Failed to save playerdata.yml:", e);
		}
	}

	public void reloadPlayerData() {
		try {
			playerConfig.load(playerFile);
		} catch (IOException | InvalidConfigurationException e) {
			plugin.getLogger().log(Level.WARNING, "Failed to reload playerdata.yml:", e);
		}
	}

	// --- Banknote Data ---

	public void saveBanknote(@NotNull UUID uuid, double amount) {
		banknoteConfig.set(uuid.toString(), amount);
		saveBanknoteData();
	}

	public Double getBanknote(@NotNull UUID uuid) {
		return banknoteConfig.contains(uuid.toString()) ? banknoteConfig.getDouble(uuid.toString()) : null;
	}

	public boolean banknoteExists(@NotNull UUID uuid) {
		return banknoteConfig.contains(uuid.toString());
	}

	public void removeBanknote(@NotNull UUID uuid) {
		banknoteConfig.set(uuid.toString(), null);
		saveBanknoteData();
	}

	public void saveBanknoteData() {
		try {
			banknoteConfig.save(banknoteFile);
		} catch (IOException e) {
			plugin.getLogger().log(Level.WARNING, "Failed to save banknotes.yml:", e);
		}
	}

	public void reloadBanknoteData() {
		try {
			banknoteConfig.load(banknoteFile);
		} catch (IOException | InvalidConfigurationException e) {
			plugin.getLogger().log(Level.WARNING, "Failed to reload banknotes.yml:", e);
		}
	}
}