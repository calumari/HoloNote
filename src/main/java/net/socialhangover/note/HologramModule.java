package net.socialhangover.note;

import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

public class HologramModule implements TerminableModule {

    private static final List<ArmorStand> armorStands = new ArrayList<>();

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
                    ArmorStand as = this.spawn(loc, note.getId() + " " + note.getTone()
                            .name() + (note.isSharped() ? "#" : ""));
                    armorStands.add(as);
                }, 1L))
                .bindWith(consumer);

        Schedulers.sync().runRepeating(() -> {
            ListIterator<ArmorStand> it = armorStands.listIterator();
            while (it.hasNext()) {
                ArmorStand as = it.next();
                if (as.getTicksLived() > 100) {
                    as.remove();
                    it.remove();
                }
            }
        }, 1L, TimeUnit.MINUTES, 1L, TimeUnit.MINUTES).bindWith(consumer);
    }

    public void disable() {
        for (ArmorStand as : armorStands) {
            as.remove();
        }
    }

    private ArmorStand spawn(Location loc, String line) {
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
