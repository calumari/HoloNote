package net.socialhangover.note;

import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HologramModule implements TerminableModule {

    private static final Map<Block, ArmorStand> holograms = new HashMap<>();

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        Events.subscribe(PlayerInteractEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(e -> e.getAction() == Action.RIGHT_CLICK_BLOCK)
                .filter(e -> e.getClickedBlock().getBlockData() instanceof NoteBlock)
                .handler(e -> Schedulers.sync().runLater(() -> {
                    Block block = e.getClickedBlock();
                    NoteBlock noteBlock = (NoteBlock) block.getBlockData();
                    Note note = noteBlock.getNote();
                    Location loc = block.getLocation().clone().add(0.5D, 1.0D, 0.5D);
                    ArmorStand as = spawn(loc, note.getId() + " " + note.getTone().name() + (note.isSharped() ? "#" : ""));
                    holograms.put(block, as);
                }, 1L))
                .bindWith(consumer);

        Events.subscribe(BlockBreakEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(e -> holograms.containsKey(e.getBlock()))
                .handler(e -> {
                    holograms.remove(e.getBlock()).remove();
                }).bindWith(consumer);

        Schedulers.sync().runRepeating(() -> {
            for (Iterator<Map.Entry<Block, ArmorStand>> it = holograms.entrySet().iterator(); it.hasNext(); ) {
                ArmorStand as = it.next().getValue();
                if (as.getTicksLived() > 100) {
                    as.remove();
                    it.remove();
                }
            }
        }, 1L, TimeUnit.MINUTES, 1L, TimeUnit.MINUTES).bindWith(consumer);
    }

    public void disable() {
        for (ArmorStand as : holograms.values()) {
            as.remove();
        }
        holograms.clear();
    }

    private static ArmorStand spawn(Location loc, String line) {
        ArmorStand as = null;
        for (Entity e : loc.getWorld().getNearbyEntities(loc, 1.0D, 1.0D, 1.0D)) {
            if (e.getType() == EntityType.ARMOR_STAND && loc.equals(e.getLocation())) {
                as = (ArmorStand) e;
                break;
            }
        }
        if (as == null) {
            as = loc.getWorld().spawn(loc, ArmorStand.class);
            as.setSmall(true);
            as.setMarker(true);
            as.setArms(false);
            as.setBasePlate(false);
            as.setGravity(false);
            as.setVisible(false);
            as.setCustomNameVisible(true);
        }
        as.setCustomName(line);
        return as;
    }
}
