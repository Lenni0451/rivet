package net.lenni0451.rivet.text;

import lombok.With;
import net.lenni0451.commons.color.Color;

@With
public record TextFormat(Color color, Color outlineColor, boolean bold, boolean italic, boolean underlined, boolean strikethrough) {

    public static final TextFormat DEFAULT = new TextFormat(Color.WHITE, Color.TRANSPARENT, false, false, false, false);

}
