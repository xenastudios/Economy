package co.xenastudios.economy;

import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Basic implementation of Vault's Economy API for Economy.
 * Handles player balances using DataManager and playerdata.yml.
 */
public final class BasicEconomy extends AbstractEconomy {
	private final EconomyPlugin plugin;

	public BasicEconomy(@NotNull EconomyPlugin plugin) {
		this.plugin = plugin;
	}

	private @NotNull DataManager getDataManager() {
		return plugin.getDataManager();
	}

	private @NotNull OfflinePlayer getPlayer(@NotNull String name) {
		// Bukkit.getOfflinePlayer(String) is deprecated, but still required for legacy support
		return Bukkit.getOfflinePlayer(name);
	}

	private void setBalance(@NotNull OfflinePlayer player, double balance) {
		getDataManager().setBalance(player, balance);
	}

	@Override
	public boolean isEnabled() {
		return plugin.isEnabled();
	}

	@Override
	public @NotNull String getName() {
		return plugin.getName();
	}

	@Override
	public boolean hasBankSupport() {
		return false;
	}

	@Override
	public int fractionalDigits() {
		return 2;
	}

	@Override
	public @NotNull String format(double amount) {
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
		DecimalFormat format = new DecimalFormat("$#,##0.00", symbols);
		return format.format(amount);
	}

	@Override
	public @NotNull String currencyNamePlural() {
		return "$";
	}

	@Override
	public @NotNull String currencyNameSingular() {
		return "$";
	}

	@Override
	public boolean hasAccount(@NotNull String playerName) {
		return true;
	}

	@Override
	public boolean hasAccount(@NotNull String playerName, @NotNull String worldName) {
		return true;
	}

	@Override
	public double getBalance(@NotNull String playerName) {
		OfflinePlayer player = getPlayer(playerName);
		return getDataManager().getBalance(player);
	}

	@Override
	public double getBalance(@NotNull String playerName, @NotNull String world) {
		return getBalance(playerName);
	}

	@Override
	public boolean has(@NotNull String playerName, double amount) {
		return getBalance(playerName) >= amount;
	}

	@Override
	public boolean has(@NotNull String playerName, @NotNull String worldName, double amount) {
		return has(playerName, amount);
	}

	@Override
	public @NotNull EconomyResponse withdrawPlayer(@NotNull String playerName, double amount) {
		synchronized (getDataManager()) {
			OfflinePlayer player = getPlayer(playerName);
			double balance = getDataManager().getBalance(player);
			if (balance >= amount) {
				double newBalance = balance - amount;
				setBalance(player, newBalance);
				return new EconomyResponse(amount, newBalance, ResponseType.SUCCESS, null);
			}
			return new EconomyResponse(amount, balance, ResponseType.FAILURE, "Not enough money.");
		}
	}

	@Override
	public @NotNull EconomyResponse withdrawPlayer(@NotNull String playerName, @NotNull String worldName, double amount) {
		return withdrawPlayer(playerName, amount);
	}

	@Override
	public @NotNull EconomyResponse depositPlayer(@NotNull String playerName, double amount) {
		synchronized (getDataManager()) {
			OfflinePlayer player = getPlayer(playerName);
			double balance = getDataManager().getBalance(player);
			double newBalance = balance + amount;
			setBalance(player, newBalance);
			return new EconomyResponse(amount, newBalance, ResponseType.SUCCESS, null);
		}
	}

	@Override
	public @NotNull EconomyResponse depositPlayer(@NotNull String playerName, @NotNull String worldName, double amount) {
		return depositPlayer(playerName, amount);
	}

	// Bank methods are not implemented
	@Override
	public @NotNull EconomyResponse createBank(@NotNull String name, @NotNull String player) {
		return new EconomyResponse(0.0D, 0.0D, ResponseType.FAILURE, "Not Implemented!");
	}

	@Override
	public @NotNull EconomyResponse deleteBank(@NotNull String name) {
		return new EconomyResponse(0.0D, 0.0D, ResponseType.FAILURE, "Not Implemented!");
	}

	@Override
	public @NotNull EconomyResponse bankBalance(@NotNull String name) {
		return new EconomyResponse(0.0D, 0.0D, ResponseType.FAILURE, "Not Implemented!");
	}

	@Override
	public @NotNull EconomyResponse bankHas(@NotNull String name, double amount) {
		return new EconomyResponse(0.0D, 0.0D, ResponseType.FAILURE, "Not Implemented!");
	}

	@Override
	public @NotNull EconomyResponse bankWithdraw(@NotNull String name, double amount) {
		return new EconomyResponse(0.0D, 0.0D, ResponseType.FAILURE, "Not Implemented!");
	}

	@Override
	public @NotNull EconomyResponse bankDeposit(@NotNull String name, double amount) {
		return new EconomyResponse(0.0D, 0.0D, ResponseType.FAILURE, "Not Implemented!");
	}

	@Override
	public @NotNull EconomyResponse isBankOwner(@NotNull String name, @NotNull String playerName) {
		return new EconomyResponse(0.0D, 0.0D, ResponseType.FAILURE, "Not Implemented!");
	}

	@Override
	public @NotNull EconomyResponse isBankMember(@NotNull String name, @NotNull String playerName) {
		return new EconomyResponse(0.0D, 0.0D, ResponseType.FAILURE, "Not Implemented!");
	}

	@Override
	public @NotNull List<String> getBanks() {
		return Collections.emptyList();
	}

	@Override
	public boolean createPlayerAccount(@NotNull String playerName) {
		// Always returns true for compatibility
		return true;
	}

	@Override
	public boolean createPlayerAccount(@NotNull String playerName, @NotNull String worldName) {
		// Always returns true for compatibility
		return true;
	}
}