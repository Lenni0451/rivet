package net.lenni0451.rivet.backend.thingl.render;

import net.lenni0451.rivet.backend.render.deferred.ModifierCommand;

public sealed interface ThinGLModifierCommand extends ModifierCommand.Custom permits ThinGLModifierCommand.Blur {

    record Blur(int strength) implements ThinGLModifierCommand {
    }

}
