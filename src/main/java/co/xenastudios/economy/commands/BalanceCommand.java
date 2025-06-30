package co.xenastudios.economy.commands;

import co.xenastudios.economy.EconomyPlugin;
import co.xenastudios.economy.utilities.MsgUtility;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.Command;
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
 * Handles the /balance [player] command for checking balances.
 */
public class BalanceCommand {

	/**
	 * Creates the /balance [player] command node for registration.
	 *
	 * @param plugin The main plugin instance.
	 * @return The constructed command node for registration.
	 */
	public static LiteralCommandNode<CommandSourceStack> createCommand(@NotNull final EconomyPlugin plugin) {
		// Build the base /balance command
		LiteralArgumentBuilder<CommandSourceStack> balanceCommand =
				Commands.literal("balance");

		// Permissions from config
		String selfPermission = plugin.getConfig().getString("commands.balance.permissions.default", "economy.command.balance");
		String othersPermission = plugin.getConfig().getString("commands.balance.permissions.others", "economy.command.balance.others");

		// Restrict base command to those with self permission
		if (!selfPermission.isEmpty()) {
			balanceCommand.requires(sender -> sender.getSender().hasPermission(selfPermission));
		}

		// /balance (self)
		balanceCommand.executes(ctx -> {
			CommandSender sender = ctx.getSource().getSender();
			if (!(sender instanceof Player player)) {
				String playerOnlyMsg = plugin.getConfig().getString(
						"messages.error.player-only",
						"<red>Only a player can execute this command!</red>"
				);
				MsgUtility.send(sender, playerOnlyMsg);
				return Command.SINGLE_SUCCESS;
			}

			double balance = plugin.getEconomyHandler().getBalance(player);
			String formatted = plugin.getEconomyHandler().format(balance);

			String balanceMsg = plugin.getConfig().getString(
					"commands.balance.messages.balance",
					"<white>Balance: <green><balance></green></white>"
			);
			MsgUtility.send(
					player,
					balanceMsg,
					Placeholder.unparsed("balance", formatted)
			);
			return Command.SINGLE_SUCCESS;
		});

		// /balance <player> (others)
		balanceCommand.then(
				Commands.argument("target", ArgumentTypes.playerProfiles())
						.requires(sender -> sender.getSender().hasPermission(othersPermission))
						.executes(ctx -> {
							CommandSender sender = ctx.getSource().getSender();
							PlayerProfileListResolver profilesResolver = ctx.getArgument("target", PlayerProfileListResolver.class);
							Collection<PlayerProfile> foundProfiles = profilesResolver.resolve(ctx.getSource());

							// Use the first found profile as the target
							Iterator<PlayerProfile> iterator = foundProfiles.iterator();
							if (!iterator.hasNext()) {
								String noPlayerMsg = plugin.getConfig().getString(
										"messages.error.no-player",
										"<red>That player doesn't exist!</red>"
								);
								MsgUtility.send(sender, noPlayerMsg);
								return Command.SINGLE_SUCCESS;
							}

							PlayerProfile profile = iterator.next();
							String targetName = profile.getName();
							if (targetName == null) {
								String noPlayerMsg = plugin.getConfig().getString(
										"messages.error.no-player",
										"<red>That player doesn't exist!</red>"
								);
								MsgUtility.send(sender, noPlayerMsg);
								return Command.SINGLE_SUCCESS;
							}

							OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

							// Check if the player exists (has played before or is online)
							if (!target.isOnline() && !target.hasPlayedBefore()) {
								String noPlayerMsg = plugin.getConfig().getString(
										"messages.error.no-player",
										"<red>That player doesn't exist!</red>"
								);
								MsgUtility.send(sender, noPlayerMsg);
								return Command.SINGLE_SUCCESS;
							}

							double balance = plugin.getEconomyHandler().getBalance(target);
							String formatted = plugin.getEconomyHandler().format(balance);

							String othersBalanceMsg = plugin.getConfig().getString(
									"commands.balance.messages.others-balance",
									"<white><green><player>'s</green> balance: <green><balance></green></white>"
							);
							MsgUtility.send(
									sender,
									othersBalanceMsg,
									Placeholder.unparsed("player", target.getName()),
									Placeholder.unparsed("balance", formatted)
							);
							return Command.SINGLE_SUCCESS;
						})
		);

		// Build and return the complete command node
		return balanceCommand.build();
	}
}