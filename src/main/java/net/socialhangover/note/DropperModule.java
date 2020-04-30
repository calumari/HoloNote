package net.socialhangover.note;

import com.google.common.collect.ImmutableList;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Jukebox;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

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
    private static final Set<Inventory> preventInteract = new HashSet<>();

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
                        e.setCancelled(true);

                        Jukebox state = (Jukebox) relative.getState();
                        state.eject();
                        state.setPlaying(e.getItem().getType());
                        state.update();

                        Inventory inv = ((Container) block.getState()).getInventory();
                        preventInteract.add(inv); // possible dupe?

                        Schedulers.sync().runLater(() -> {
                            inv.removeItemAnySlot(e.getItem());
                            preventInteract.remove(inv);
                        }, 1);
                    }
                }).bindWith(consumer);

        Events.subscribe(InventoryClickEvent.class)
                .filter(e -> preventInteract.contains(e.getClickedInventory()))
                .handler(e -> e.setCancelled(true))
                .bindWith(consumer);
    }
}
