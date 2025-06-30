package co.xenastudios.economy.commands;

import co.xenastudios.economy.EconomyPlugin;
import co.xenastudios.economy.utilities.MsgUtility;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles the /withdraw <amount> command for creating banknotes.
 */
public class WithdrawCommand {

	/**
	 * Creates the /withdraw <amount> command node for registration.
	 *
	 * @param plugin The main plugin instance.
	 * @return The constructed command node for registration.
	 */
	public static LiteralCommandNode<CommandSourceStack> createCommand(
			@NotNull final EconomyPlugin plugin
	) {
		// Build the base /withdraw command
		LiteralArgumentBuilder<CommandSourceStack> withdrawCommand =
				Commands.literal("withdraw")
						// Only allow players to use this command
						.requires(sender -> sender.getSender() instanceof Player)
						.then(
								Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
										.executes(ctx -> {
											CommandSender sender = ctx.getSource().getSender();

											// Ensure the sender is a player
											if (!(sender instanceof Player player)) {
												String playerOnlyMsg = plugin.getConfig().getString(
														"messages.error.player-only",
														"<red>Only a player can execute this command!</red>"
												);
												MsgUtility.send(sender, playerOnlyMsg);
												return Command.SINGLE_SUCCESS;
											}

											double amount = DoubleArgumentType.getDouble(ctx, "amount");
											String formattedAmount = plugin.getEconomyHandler().format(amount);

											// Check if the player has enough balance
											if (!plugin.getEconomyHandler().has(player, amount)) {
												String notEnoughMsg = plugin.getConfig().getString(
														"messages.error.not-enough",
														"<red>You do not have enough money!</red>"
												);
												MsgUtility.send(player, notEnoughMsg);
												return Command.SINGLE_SUCCESS;
											}

											// Withdraw the amount from the player's balance
											plugin.getEconomyHandler().withdrawPlayer(player, amount);

											// Generate a unique UUID for the banknote
											UUID noteUUID = UUID.randomUUID();

											// Store the banknote in the data file using DataManager
											plugin.getDataManager().saveBanknote(noteUUID, amount);

											// --- Create the banknote item using config ---
											FileConfiguration config = plugin.getConfig();
											String title = config.getString(
													"banknotes.item.title",
													"<!i><green><b>Banknote</b></green>"
											);
											List<String> loreList = config.getStringList("banknotes.item.lore");
											if (loreList.isEmpty()) {
												loreList = List.of(
														"<!i><dark_gray>Economy Item</dark_gray>",
														"",
														"<!i><green>Information:</green>",
														"<!i><gray>»</gray> <white>Amount:</white> <green><amount></green>",
														"",
														"<!i><green>Right-Click to Redeem</green>"
												);
											}

											// Parse title and lore with MiniMessage and <amount> placeholder
											MiniMessage mm = MiniMessage.miniMessage();
											Component displayName = mm.deserialize(
													title,
													Placeholder.unparsed("amount", formattedAmount)
											);
											List<Component> lore = loreList.stream()
													.map(line -> mm.deserialize(
															line,
															Placeholder.unparsed("amount", formattedAmount)
													))
													.collect(Collectors.toList());

											// Create the banknote item
											ItemStack note = new ItemStack(Material.PAPER);
											ItemMeta meta = note.getItemMeta();
											meta.displayName(displayName);
											meta.lore(lore);
											meta.getPersistentDataContainer().set(
													new NamespacedKey(plugin, "banknote_uuid"),
													PersistentDataType.STRING,
													noteUUID.toString()
											);
											note.setItemMeta(meta);

											// Give the banknote to the player
											player.getInventory().addItem(note);

											// Send confirmation message
											String withdrawnMsg = config.getString(
													"commands.withdraw.messages.withdrawn",
													"<green>Withdrawn <amount> as a banknote!</green>"
											);
											MsgUtility.send(
													player,
													withdrawnMsg,
													Placeholder.unparsed("amount", formattedAmount)
											);

											return Command.SINGLE_SUCCESS;
										})
						);

		// Build and return the complete command node
		return withdrawCommand.build();
	}
}