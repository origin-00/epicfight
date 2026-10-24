package yesman.epicfight.api.event;

import net.minecraft.util.Util;
import net.minecraft.resources.Identifier;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.entitypatch.EntityDecorations;

import java.util.function.Function;

/// A utility interface to specify an owner of a module
///
/// Its main usage is [EntityEventListener] and [EntityDecorations] to attach or
/// detach each module by equipping state of a [Skill]
///
/// @see EntityEventListener usage of this inherits
/// @see EntityDecorations usage of this inherits
/// @see Skill implementation of this interface
public interface IdentifierProvider {
    Function<Identifier, IdentifierProvider> CONSTANT_IDENTIFIER_PROVIDERS = Util.memoize(identifier -> {
        return new IdentifierProvider() {
            final Identifier constIdentifier = identifier;

            @Override
            public Identifier getId() {
                return this.constIdentifier;
            }
        };
    });

    static IdentifierProvider constant(Identifier constantIdentifier) {
        return CONSTANT_IDENTIFIER_PROVIDERS.apply(constantIdentifier);
    }

    static IdentifierProvider constant(String constantIdentifier) {
        return constant(Identifier.parse(constantIdentifier));
    }

    IdentifierProvider PERMANENT_LISTENER = constant("permanent_subscriber");;

    static IdentifierProvider permanent() {
        return PERMANENT_LISTENER;
    }

    Identifier getId();

    default String getStringId() {
        return this.getId().toString();
    }
}
