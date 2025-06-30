package co.xenastudios.economy.utilities;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

/**
 * Utility class for formatting and sending MiniMessage-based messages.
 */
public final class MsgUtility {

	// Private constructor to prevent instantiation
	private MsgUtility() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Formats a string using MiniMessage.
	 *
	 * @param message The MiniMessage string.
	 * @return The formatted Component.
	 */
	public static Component format(String message) {
		if (message == null) {
			return Component.empty();
		}
		return MiniMessage.miniMessage().deserialize(message);
	}

	/**
	 * Formats a string using MiniMessage with placeholders.
	 *
	 * @param message      The MiniMessage string.
	 * @param placeholders The tag resolvers for placeholders.
	 * @return The formatted Component.
	 */
	public static Component format(String message, TagResolver... placeholders) {
		if (message == null) {
			return Component.empty();
		}
		return MiniMessage.miniMessage().deserialize(message, placeholders);
	}

	/**
	 * Sends a formatted message to a CommandSender.
	 *
	 * @param sender  The recipient.
	 * @param message The MiniMessage string.
	 */
	public static void send(CommandSender sender, String message) {
		if (sender != null && message != null) {
			sender.sendMessage(format(message));
		}
	}

	/**
	 * Sends a formatted message with placeholders to a CommandSender.
	 *
	 * @param sender       The recipient.
	 * @param message      The MiniMessage string.
	 * @param placeholders The tag resolvers for placeholders.
	 */
	public static void send(CommandSender sender, String message, TagResolver... placeholders) {
		if (sender != null && message != null) {
			sender.sendMessage(format(message, placeholders));
		}
	}

	/**
	 * Broadcasts a formatted message to the entire server.
	 *
	 * @param server  The server instance.
	 * @param message The MiniMessage string.
	 */
	public static void broadcast(Server server, String message) {
		if (server != null && message != null) {
			server.broadcast(format(message));
		}
	}

	/**
	 * Broadcasts a formatted message with placeholders to the entire server.
	 *
	 * @param server       The server instance.
	 * @param message      The MiniMessage string.
	 * @param placeholders The tag resolvers for placeholders.
	 */
	public static void broadcast(Server server, String message, TagResolver... placeholders) {
		if (server != null && message != null) {
			server.broadcast(format(message, placeholders));
		}
	}
}