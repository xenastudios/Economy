package co.xenastudios.economy.commands;

import co.xenastudios.economy.EconomyPlugin;
import co.xenastudios.economy.utilities.MsgUtility;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;

/**
 * Handles the /pay <player> <amount> command for sending money to another player.
 */
public class PayCommand {

	/**
	 * Creates the /pay <player> <amount> command node for registration.
	 *
	 * @param plugin The main plugin instance.
	 * @return The constructed command node for registration.
	 */
	public static LiteralCommandNode<CommandSourceStack> createCommand(@NotNull final EconomyPlugin plugin) {
		// Permission from config
		String permission = plugin.getConfig().getString("commands.pay.permission", "economy.command.pay");

		// Build the base /pay command
		LiteralArgumentBuilder<CommandSourceStack> payCommand =
				Commands.literal("pay")
						.requires(sender -> sender.getSender() instanceof Player && sender.getSender().hasPermission(permission))
						.then(
								Commands.argument("target", ArgumentTypes.playerProfiles())
										.then(
												Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
														.executes(ctx -> {
															CommandSender sender = ctx.getSource().getSender();
															if (!(sender instanceof Player player)) {
																String playerOnlyMsg = plugin.getConfig().getString(
																		"messages.error.player-only",
																		"<red>Only a player can execute this command!</red>"
																);
																MsgUtility.send(sender, playerOnlyMsg);
																return Command.SINGLE_SUCCESS;
															}

															// Resolve target player
															PlayerProfileListResolver profilesResolver = ctx.getArgument("target", PlayerProfileListResolver.class);
															Collection<PlayerProfile> foundProfiles = profilesResolver.resolve(ctx.getSource());
															Iterator<PlayerProfile> iterator = foundProfiles.iterator();
															if (!iterator.hasNext()) {
																String noPlayerMsg = plugin.getConfig().getString(
																		"messages.error.no-player",
																		"<red>That player doesn't exist!</red>"
																);
																MsgUtility.send(player, noPlayerMsg);
																return Command.SINGLE_SUCCESS;
															}

															PlayerProfile profile = iterator.next();
															String targetName = profile.getName();
															if (targetName == null) {
																String noPlayerMsg = plugin.getConfig().getString(
																		"messages.error.no-player",
																		"<red>That player doesn't exist!</red>"
																);
																MsgUtility.send(player, noPlayerMsg);
																return Command.SINGLE_SUCCESS;
															}

															OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

															// Prevent paying self
															if (target.getUniqueId().equals(player.getUniqueId())) {
																String selfMsg = plugin.getConfig().getString(
																		"commands.pay.messages.error.self",
																		"<red>You cannot pay yourself!</red>"
																);
																MsgUtility.send(player, selfMsg);
																return Command.SINGLE_SUCCESS;
															}

															// Check if the player exists (has played before or is online)
															if (!target.isOnline() && !target.hasPlayedBefore()) {
																String noPlayerMsg = plugin.getConfig().getString(
																		"messages.error.no-player",
																		"<red>That player doesn't exist!</red>"
																);
																MsgUtility.send(player, noPlayerMsg);
																return Command.SINGLE_SUCCESS;
															}

															double amount = DoubleArgumentType.getDouble(ctx, "amount");

															// Check sender's balance
															if (!plugin.getEconomyHandler().has(player, amount)) {
																String notEnoughMsg = plugin.getConfig().getString(
																		"messages.error.not-enough",
																		"<red>You do not have enough money!</red>"
																);
																MsgUtility.send(player, notEnoughMsg);
																return Command.SINGLE_SUCCESS;
															}

															// Withdraw from sender and deposit to target
															plugin.getEconomyHandler().withdrawPlayer(player, amount);
															plugin.getEconomyHandler().depositPlayer(target, amount);

															String formatted = plugin.getEconomyHandler().format(amount);

															// Notify sender
															String sentMsg = plugin.getConfig().getString(
																	"commands.pay.messages.sent",
																	"<green>Sent <amount> to <player>.</green>"
															);
															MsgUtility.send(
																	player,
																	sentMsg,
																	Placeholder.unparsed("amount", formatted),
																	Placeholder.unparsed("player", target.getName())
															);

															// Notify target if online
															if (target.isOnline() && target.getPlayer() != null) {
																String receivedMsg = plugin.getConfig().getString(
																		"commands.pay.messages.received",
																		"<green>You received <amount> from <player>.</green>"
																);
																MsgUtility.send(
																		target.getPlayer(),
																		receivedMsg,
																		Placeholder.unparsed("amount", formatted),
																		Placeholder.unparsed("player", player.getName())
																);
															}

															return Command.SINGLE_SUCCESS;
														})
										)
						);

		// Build and return the complete command node
		return payCommand.build();
	}
}