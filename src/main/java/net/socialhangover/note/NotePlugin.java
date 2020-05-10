package net.socialhangover.note;

import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.plugin.ap.Plugin;
import me.lucko.helper.plugin.ap.PluginDependency;
import org.bukkit.configuration.file.YamlConfiguration;

@Plugin(name = "HoloNote", version = "1.0.0", apiVersion = "1.15", depends = {
        @PluginDependency(value = "helper")
})
public class NotePlugin extends ExtendedJavaPlugin {

    private YamlConfiguration config;

    private HologramModule hologramModule;

    protected void enable() {
        this.config = loadConfig("config.yml");

        this.hologramModule = bindModule(new HologramModule());
        bindModule(new DropperModule(this));
        bindModule(new DoubleDoorModule());
    }

    @Override
    protected void disable() {
        this.hologramModule.disable();
    }
}
