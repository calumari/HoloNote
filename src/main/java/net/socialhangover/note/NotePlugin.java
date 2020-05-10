package net.socialhangover.note;

import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.plugin.ap.Plugin;
import me.lucko.helper.plugin.ap.PluginDependency;
import org.bukkit.configuration.file.YamlConfiguration;

@Plugin(name = "HoloNote", version = "1.0.0", apiVersion = "1.15", depends = {
        @PluginDependency(value = "helper")
})
public class NotePlugin extends ExtendedJavaPlugin {


    private HologramModule hologramModule;

    protected void enable() {

        this.hologramModule = bindModule(new HologramModule());
        bindModule(new DropperModule(this));
    }

    @Override
    protected void disable() {
        this.hologramModule.disable();
    }
}
