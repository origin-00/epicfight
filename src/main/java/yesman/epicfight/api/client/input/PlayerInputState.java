package yesman.epicfight.api.client.input;

import net.minecraft.world.entity.player.Input;
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/// Represents the abstract state of the player input (client-side).
/// Mirrors the vanilla Minecraft [Input] class.
public record PlayerInputState(
        float leftImpulse,
        float forwardImpulse,
        boolean up,
        boolean down,
        boolean left,
        boolean right,
        boolean jumping,
        boolean sneaking
) {
    /// Since 1.21.2 the vanilla input is a [ClientInput]: impulses in its move vector, keys in an
    /// immutable [Input] record.
    public static PlayerInputState fromVanillaInput(ClientInput input) {
        Input keys = input.keyPresses;
        Vec2 moveVector = input.getMoveVector();
        return new PlayerInputState(
                moveVector.x, moveVector.y,
                keys.forward(), keys.backward(),
                keys.left(), keys.right(),
                keys.jump(), keys.shift()
        );
    }

    /// Applies the values from a [PlayerInputState] to a vanilla [Input] instance.
    ///
    /// **Note:** Updating the vanilla [Input] fields has side effects, so this change is **not immutable**.
    ///
    /// @param updated the new input state to apply
    /// @param input   the existing vanilla Input instance to update
    public static ClientInput applyToVanillaInput(@NotNull PlayerInputState updated, @NotNull ClientInput input) {
        input.moveVector = new Vec2(updated.leftImpulse(), updated.forwardImpulse());
        input.keyPresses = new Input(updated.up(), updated.down(), updated.left(), updated.right(), updated.jumping(), updated.sneaking(), input.keyPresses.sprint());
        return input;
    }

    public Vec2 getMoveVector() {
        return new Vec2(this.leftImpulse, this.forwardImpulse);
    }

    public boolean hasForwardImpulse() {
        return this.forwardImpulse > 1.0E-5F;
    }

    public @NotNull PlayerInputState copyWith(
            @Nullable Float leftImpulse,
            @Nullable Float forwardImpulse,
            @Nullable Boolean up,
            @Nullable Boolean down,
            @Nullable Boolean left,
            @Nullable Boolean right,
            @Nullable Boolean jumping,
            @Nullable Boolean sneaking
    ) {
        return new PlayerInputState(
                leftImpulse != null ? leftImpulse : this.leftImpulse,
                forwardImpulse != null ? forwardImpulse : this.forwardImpulse,
                up != null ? up : this.up,
                down != null ? down : this.down,
                left != null ? left : this.left,
                right != null ? right : this.right,
                jumping != null ? jumping : this.jumping,
                sneaking != null ? sneaking : this.sneaking
        );
    }

    public @NotNull PlayerInputState withLeftImpulse(float leftImpulse) {
        return copyWith(leftImpulse, null, null, null, null, null, null, null);
    }

    public @NotNull PlayerInputState withForwardImpulse(float forwardImpulse) {
        return copyWith(null, forwardImpulse, null, null, null, null, null, null);
    }

    public @NotNull PlayerInputState withUp(boolean up) {
        return copyWith(null, null, up, null, null, null, null, null);
    }

    public @NotNull PlayerInputState withDown(boolean down) {
        return copyWith(null, null, null, down, null, null, null, null);
    }

    public @NotNull PlayerInputState withLeft(boolean left) {
        return copyWith(null, null, null, null, left, null, null, null);
    }

    public @NotNull PlayerInputState withRight(boolean right) {
        return copyWith(null, null, null, null, null, right, null, null);
    }

    public @NotNull PlayerInputState withJumping(boolean jumping) {
        return copyWith(null, null, null, null, null, null, jumping, null);
    }

    public @NotNull PlayerInputState withSneaking(boolean sneaking) {
        return copyWith(null, null, null, null, null, null, null, sneaking);
    }
}
