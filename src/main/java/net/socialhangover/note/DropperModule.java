package net.socialhangover.note;

import com.google.common.collect.ImmutableList;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DropperModule implements TerminableModule {

    private static final List<Material> MUSIC_DISCS = ImmutableList.<Material>builder()
            .add(Material.MUSIC_DISC_11)
            .add(Material.MUSIC_DISC_13)
            .add(Material.MUSIC_DISC_BLOCKS)
            .add(Material.MUSIC_DISC_CAT)
            .add(Material.MUSIC_DISC_CHIRP)
            .add(Material.MUSIC_DISC_FAR)
            .add(Material.MUSIC_DISC_MALL)
            .add(Material.MUSIC_DISC_MELLOHI)
            .add(Material.MUSIC_DISC_STAL)
            .add(Material.MUSIC_DISC_STRAD)
            .add(Material.MUSIC_DISC_WAIT)
            .add(Material.MUSIC_DISC_WARD)
            .build();
    private static final Set<Block> delete = new HashSet<>();

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        Events.subscribe(BlockDispenseEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(e -> e.getBlock().getType() == Material.DROPPER)
                .filter(e -> MUSIC_DISCS.contains(e.getItem().getType()))
                .handler(e -> {
                    Block block = e.getBlock();
                    Dispenser dispenser = (Dispenser) block.getBlockData();
                    Block relative = block.getRelative(dispenser.getFacing());

                    if (relative.getType() == Material.JUKEBOX) {
                        Jukebox state = (Jukebox) relative.getState();
                        state.eject();
                        state.setPlaying(e.getItem().getType());
                        state.update();

                        delete.add(relative); // possible dupe?
                    }
                }).bindWith(consumer);


        Events.subscribe(ItemSpawnEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .handler(e -> {
                    Block block = e.getEntity().getLocation().getBlock();
                    if (delete.contains(block)) {
                        e.setCancelled(true);
                        delete.remove(block);
                    }
                }).bindWith(consumer);
    }
}
