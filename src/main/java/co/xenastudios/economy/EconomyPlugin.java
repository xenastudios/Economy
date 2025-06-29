package co.xenastudios.economy;

import org.bukkit.plugin.java.JavaPlugin;

public class EconomyPlugin extends JavaPlugin {
	@Override
	public void onEnable() {
		this.getLogger().info("Plugin enabled!");
	}

	@Override
	public void onDisable() {
		this.getLogger().info("Plugin disabled!");
	}
}