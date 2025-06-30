package co.xenastudios.economy;

import co.xenastudios.economy.utilities.MsgUtility;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

/**
 * Listens for banknote redemption by players.
 * When a player right-clicks a valid banknote, it is redeemed and removed.
 */
public class BanknoteListener implements Listener {
	private final EconomyPlugin plugin;

	/**
	 * Constructs a new BanknoteListener.
	 *
	 * @param plugin The main plugin instance.
	 */
	public BanknoteListener(EconomyPlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Handles player interaction with banknotes.
	 * Redeems the banknote if valid and not already redeemed.
	 *
	 * @param event The player interact event.
	 */
	@EventHandler
	public void onPlayerUse(PlayerInteractEvent event) {
		// Only handle main hand interactions
		if (event.getHand() != EquipmentSlot.HAND) return;

		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		if (item == null || item.getType() != Material.PAPER) return;

		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;

		NamespacedKey key = new NamespacedKey(plugin, "banknote_uuid");
		if (!meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) return;

		String uuidString = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
		if (uuidString == null) return;

		UUID noteUUID;
		try {
			noteUUID = UUID.fromString(uuidString);
		} catch (IllegalArgumentException e) {
			return;
		}

		// Get the DataManager directly from the plugin
		DataManager dataManager = plugin.getDataManager();

		// Check if the banknote exists and get the amount
		Double amount = dataManager.getBanknote(noteUUID);
		if (amount == null) {
			String invalidMsg = plugin.getConfig().getString(
					"banknotes.messages.error.invalid",
					"<red>This banknote is invalid or already redeemed!</red>"
			);
			MsgUtility.send(player, invalidMsg);
			return;
		}

		// Redeem: add money, remove banknote from file, remove item from hand
		plugin.getEconomyHandler().depositPlayer(player, amount);
		dataManager.removeBanknote(noteUUID);

		// Remove one item from hand
		item.setAmount(item.getAmount() - 1);

		// Send configurable redemption message with MiniMessage and <amount> placeholder
		String redeemedMsg = plugin.getConfig().getString(
				"banknotes.messages.redeemed",
				"<green>Redeemed <amount> into your account!</green>"
		);
		MsgUtility.send(
				player,
				redeemedMsg,
				Placeholder.unparsed("amount", plugin.getEconomyHandler().format(amount))
		);

		event.setCancelled(true);
	}
}