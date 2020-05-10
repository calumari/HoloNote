package net.socialhangover.note;

import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.cooldown.CooldownMap;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.utils.Players;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DoubleDoorModule implements TerminableModule {

    private final CooldownMap<UUID> cooldownMap = CooldownMap.create(Cooldown.of(600, TimeUnit.MILLISECONDS));

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        Events.subscribe(PlayerInteractEvent.class, EventPriority.HIGHEST)
                .filter(EventFilters.ignoreCancelled())
                .filter(e -> e.hasBlock() && e.getClickedBlock().getBlockData() instanceof Door)
                .handler(e -> {
                    Player player = e.getPlayer();
                    if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        handle(e.getClickedBlock());
                        return;
                    }

                    if (Players.isVanished(player) || player.getGameMode() == GameMode.CREATIVE || !player.hasPermission("suggestions.door.knock")) {
                        return;
                    }

                    if (e.getAction() == Action.LEFT_CLICK_BLOCK && !cooldownMap.test(player.getUniqueId())) {
                        Block block = e.getClickedBlock();
                        player.getWorld().playSound(block.getLocation(), block.getType() == Material.IRON_DOOR ? Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR : Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, .5f, 0f);
                    }
                }).bindWith(consumer);

        Events.subscribe(BlockRedstoneEvent.class)
                .filter(e -> e.getBlock().getBlockData() instanceof Door)
                .handler(e -> handle(e.getBlock())).bindWith(consumer);
    }

    private void handle(Block block) {
        Door self = (Door) block.getBlockData();
        BlockFace face = getRelativeFace(self);
        if (face == null) {
            return;
        }

        Block relative = block.getRelative(face);
        if (!(relative.getBlockData() instanceof Door)) {
            return;
        }

        Door other = (Door) relative.getBlockData();
        if (other.getFacing() != self.getFacing()) {
            return;
        }

        // delay for 1 tick to prevent door desync. todo? there is probably a better way.
        Schedulers.sync().runLater(() -> {
            other.setOpen(((Door) block.getBlockData()).isOpen());
            relative.setBlockData(other);
        }, 1);
    }

    private BlockFace getRelativeFace(Door door) {
        Door.Hinge hinge = door.getHinge();
        switch (door.getFacing()) {
            case NORTH:
                return hinge == Door.Hinge.RIGHT ? BlockFace.WEST : BlockFace.EAST;
            case EAST:
                return hinge == Door.Hinge.RIGHT ? BlockFace.NORTH : BlockFace.SOUTH;
            case SOUTH:
                return hinge == Door.Hinge.RIGHT ? BlockFace.EAST : BlockFace.WEST;
            case WEST:
                return hinge == Door.Hinge.RIGHT ? BlockFace.SOUTH : BlockFace.NORTH;
        }
        return null;
    }
}
