package me.dags.actionreplay;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableSignData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;

/**
 * @author dags <dags@dags.me>
 */
public class SignFrame extends KeyFrame.TargetAvatar {

    private final BlockSnapshot blockSnapshot;
    private final ImmutableSignData from;
    private final SignData to;

    public SignFrame(Avatar avatar, ChangeSignEvent event) {
        super(avatar);
        this.from = event.getOriginalText();
        this.to = event.getText();
        this.blockSnapshot = BlockSnapshot.builder().from(event.getTargetTile().getLocation()).build();
    }

    @Override
    public void restore() {
        blockSnapshot.getLocation().ifPresent(loc -> loc.offer(to));
    }

    @Override
    public void reset() {
        blockSnapshot.getLocation().ifPresent(loc -> loc.offer(from.asMutable()));
    }
}
