package test.impl;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.container.ScrollContainer;
import net.lenni0451.rivet.component.impl.FormattedLabel;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.layout.list.VerticalListLayout;
import net.lenni0451.rivet.text.model.TextOrigin;
import test.TestBase;
import test.TestTheme;

public class TextStylingTest extends TestBase {

    void main() {
        this.run();
    }

    @Override
    protected void init(final Rivet rivet) {
        rivet.theme(new TestTheme());

        Container container = new Container(new VerticalListLayout(8, true));

        this.addSectionHeader(container, "=== Basic Individual Styles ===");
        container.add(new FormattedLabel("Plain Text"));
        container.add(new FormattedLabel("<bold>Bold Text</bold>"));
        container.add(new FormattedLabel("<italic>Italic Text</italic>"));
        container.add(new FormattedLabel("<underlined>Underlined Text</underlined>"));
        container.add(new FormattedLabel("<strikethrough>Strikethrough Text</strikethrough>"));
        container.add(new FormattedLabel("<shadow>Shadow Text</shadow>"));
        container.add(new FormattedLabel("<color=yellow>Color (Yellow) Text</color>"));
        container.add(new FormattedLabel("<color=cyan outline_color=yellow>Cyan Text + Yellow Outline</color>"));
        container.add(new FormattedLabel("<color=black outline_color=white>Black Text + White Outline</color>"));

        this.addSectionHeader(container, "=== Two-Style Combinations ===");
        container.add(new FormattedLabel("<bold italic>Bold + Italic</bold></italic>"));
        container.add(new FormattedLabel("<bold underlined>Bold + Underlined</bold></underlined>"));
        container.add(new FormattedLabel("<bold strikethrough>Bold + Strikethrough</bold></strikethrough>"));
        container.add(new FormattedLabel("<bold shadow>Bold + Shadow</bold></shadow>"));
        container.add(new FormattedLabel("<bold color=green outline_color=yellow>Bold + Outline</bold></outline_color></color>"));
        container.add(new FormattedLabel("<italic underlined>Italic + Underlined</italic></underlined>"));
        container.add(new FormattedLabel("<italic strikethrough>Italic + Strikethrough</italic></strikethrough>"));
        container.add(new FormattedLabel("<italic shadow>Italic + Shadow</italic></shadow>"));
        container.add(new FormattedLabel("<underlined strikethrough>Underlined + Strikethrough</underlined></strikethrough>"));
        container.add(new FormattedLabel("<underlined shadow>Underlined + Shadow</underlined></shadow>"));
        container.add(new FormattedLabel("<strikethrough shadow>Strikethrough + Shadow</strikethrough></shadow>"));
        container.add(new FormattedLabel("<color=orange outline_color=cyan shadow>Shadow + Outline</shadow></outline_color></color>"));

        this.addSectionHeader(container, "=== Multi-Style Combinations ===");
        container.add(new FormattedLabel("<bold italic underlined>Bold + Italic + Underlined</bold></italic></underlined>"));
        container.add(new FormattedLabel("<bold italic shadow>Bold + Italic + Shadow</bold></italic></shadow>"));
        container.add(new FormattedLabel("<bold italic color=cyan outline_color=magenta>Bold + Italic + Outline</bold></italic></outline_color></color>"));
        container.add(new FormattedLabel("<bold underlined strikethrough>Bold + Underlined + Strikethrough</bold></underlined></strikethrough>"));
        container.add(new FormattedLabel("<bold underlined shadow>Bold + Underlined + Shadow</bold></underlined></shadow>"));
        container.add(new FormattedLabel("<bold italic underlined strikethrough>Bold + Italic + Underlined + Strikethrough</bold></italic></underlined></strikethrough>"));
        container.add(new FormattedLabel("<bold italic underlined shadow>Bold + Italic + Underlined + Shadow</bold></italic></underlined></shadow>"));
        container.add(new FormattedLabel("<bold italic shadow color=red outline_color=yellow>Bold + Italic + Shadow + Outline</bold></italic></shadow></outline_color></color>"));
        container.add(new FormattedLabel("<bold italic underlined strikethrough shadow>All Flags: Bold + Italic + Underlined + Strikethrough + Shadow</bold></italic></underlined></strikethrough></shadow>"));
        container.add(new FormattedLabel("<bold italic underlined strikethrough shadow color=yellow outline_color=red>All Flags + Color + Outline</bold></italic></underlined></strikethrough></shadow></outline_color></color>"));

        this.addSectionHeader(container, "=== Multi-Section Mixed In Single Line ===");
        container.add(new FormattedLabel("<color=red bold>Red Bold</color> <color=green italic>Green Italic</color> <color=blue underlined>Blue Underline</color> <color=yellow strikethrough>Yellow Strike</color> <color=orange shadow>Orange Shadow</color> <color=pink outline_color=yellow>Pink Outline</color>"));
        container.add(new FormattedLabel("<bold>Part 1: <color=cyan>Cyan</color> and <color=yellow>Yellow</color></bold> | <italic>Part 2: <shadow>Shadowed</shadow> and <underlined>Underlined</underlined></italic>"));

        this.addSectionHeader(container, "=== Complete 32-Combination Matrix (Flags: B, I, U, S, Sh) ===");
        for (int mask = 0; mask < 32; mask++) {
            boolean bold = (mask & 1) != 0;
            boolean italic = (mask & 2) != 0;
            boolean underlined = (mask & 4) != 0;
            boolean strikethrough = (mask & 8) != 0;
            boolean shadow = (mask & 16) != 0;

            StringBuilder tagBuilder = new StringBuilder("<");
            StringBuilder descBuilder = new StringBuilder("[");

            if (bold) {
                tagBuilder.append("bold ");
                descBuilder.append("B ");
            }
            if (italic) {
                tagBuilder.append("italic ");
                descBuilder.append("I ");
            }
            if (underlined) {
                tagBuilder.append("underlined ");
                descBuilder.append("U ");
            }
            if (strikethrough) {
                tagBuilder.append("strikethrough ");
                descBuilder.append("S ");
            }
            if (shadow) {
                tagBuilder.append("shadow ");
                descBuilder.append("Sh ");
            }

            tagBuilder.append("color=white outline_color=red>");
            String desc = descBuilder.toString().trim() + "]";
            if (desc.equals("[]")) desc = "[Plain]";

            String markup = tagBuilder + desc + " Quick brown fox jumps over the lazy dog";
            container.add(new FormattedLabel(markup));
        }

        rivet.root().add(new ScrollContainer(container, true, true));
    }

    private void addSectionHeader(final Container container, final String title) {
        container.add(new Label(title).horizontalOrigin(TextOrigin.Horizontal.VISUAL_LEFT));
    }

}
