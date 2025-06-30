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
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;

/**
 * Handles the /economyadmin command for administrative actions on the Economy plugin.
 * Subcommands: reload, set, give, take
 */
public class EconomyAdminCommand {

	/**
	 * Creates the /economyadmin command node for registration.
	 *
	 * @param plugin The main plugin instance.
	 * @return The constructed command node for registration.
	 */
	public static LiteralCommandNode<CommandSourceStack> createCommand(
			@NotNull final EconomyPlugin plugin
	) {
		// Build the base /economyadmin command
		LiteralArgumentBuilder<CommandSourceStack> economyAdminCommand =
				Commands.literal("economyadmin");

		// Retrieve the required permission from the config
		String permission = plugin.getConfig().getString("commands.economyadmin.permission");

		// Restrict command usage to those with the permission, if set
		if (permission != null && !permission.isEmpty()) {
			economyAdminCommand.requires(
					sender -> sender.getSender().hasPermission(permission)
			);
		}

		// /economyadmin (usage message)
		economyAdminCommand.executes(ctx -> {
			CommandSender sender = ctx.getSource().getSender();
			String usageMsg = plugin.getConfig().getString(
					"messages.error.usage",
					"<red>Usage: <usage></red>"
			);
			MsgUtility.send(
					sender,
					usageMsg,
					Placeholder.unparsed("usage", "/economyadmin reload|set|give|take <player> <amount>")
			);
			return Command.SINGLE_SUCCESS;
		});

		// /economyadmin reload: reloads the plugin config
		economyAdminCommand.then(
				Commands.literal("reload")
						.executes(ctx -> {
							CommandSender sender = ctx.getSource().getSender();
							plugin.reloadConfig();
							plugin.getDataManager().reloadPlayerData();
							plugin.getDataManager().reloadBanknoteData();
							String reloadMsg = plugin.getConfig().getString(
									"commands.economyadmin.messages.config-reloaded",
									"<green>Economy's config has been reloaded.</green>"
							);
							MsgUtility.send(sender, reloadMsg);
							return Command.SINGLE_SUCCESS;
						})
		);

		// /economyadmin set <player> <amount>: sets a player's balance
		economyAdminCommand.then(
				Commands.literal("set")
						.then(Commands.argument("target", ArgumentTypes.playerProfiles())
								.then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
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

											double amount = DoubleArgumentType.getDouble(ctx, "amount");
											// Set balance: withdraw all, then deposit the new amount
											plugin.getEconomyHandler().withdrawPlayer(
													target,
													plugin.getEconomyHandler().getBalance(target)
											);
											plugin.getEconomyHandler().depositPlayer(target, amount);

											String formatted = plugin.getEconomyHandler().format(amount);
											String setMsg = plugin.getConfig().getString(
													"commands.economyadmin.messages.set-success",
													"<green>Set <player>'s balance to <balance></green>"
											);
											MsgUtility.send(
													sender,
													setMsg,
													Placeholder.unparsed("player", target.getName()),
													Placeholder.unparsed("balance", formatted)
											);
											return Command.SINGLE_SUCCESS;
										})
								)
						)
		);

		// /economyadmin give <player> <amount>: adds money to a player's balance
		economyAdminCommand.then(
				Commands.literal("give")
						.then(Commands.argument("target", ArgumentTypes.playerProfiles())
								.then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
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

											double amount = DoubleArgumentType.getDouble(ctx, "amount");
											plugin.getEconomyHandler().depositPlayer(target, amount);

											double newBalance = plugin.getEconomyHandler().getBalance(target);
											String formatted = plugin.getEconomyHandler().format(newBalance);
											String giveMsg = plugin.getConfig().getString(
													"commands.economyadmin.messages.give-success",
													"<green>Gave <amount> to <player>. New balance: <balance></green>"
											);
											MsgUtility.send(
													sender,
													giveMsg,
													Placeholder.unparsed("player", target.getName()),
													Placeholder.unparsed("amount", plugin.getEconomyHandler().format(amount)),
													Placeholder.unparsed("balance", formatted)
											);
											return Command.SINGLE_SUCCESS;
										})
								)
						)
		);

		// /economyadmin take <player> <amount>: removes money from a player's balance
		economyAdminCommand.then(
				Commands.literal("take")
						.then(Commands.argument("target", ArgumentTypes.playerProfiles())
								.then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
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

											double amount = DoubleArgumentType.getDouble(ctx, "amount");
											plugin.getEconomyHandler().withdrawPlayer(target, amount);

											double newBalance = plugin.getEconomyHandler().getBalance(target);
											String formatted = plugin.getEconomyHandler().format(newBalance);
											String takeMsg = plugin.getConfig().getString(
													"commands.economyadmin.messages.take-success",
													"<green>Took <amount> from <player>. New balance: <balance></green>"
											);
											MsgUtility.send(
													sender,
													takeMsg,
													Placeholder.unparsed("player", target.getName()),
													Placeholder.unparsed("amount", plugin.getEconomyHandler().format(amount)),
													Placeholder.unparsed("balance", formatted)
											);
											return Command.SINGLE_SUCCESS;
										})
								)
						)
		);

		// Build and return the complete command node
		return economyAdminCommand.build();
	}
}