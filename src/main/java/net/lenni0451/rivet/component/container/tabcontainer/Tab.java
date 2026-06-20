package net.lenni0451.rivet.component.container.tabcontainer;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.DecoratedContainer;

import java.util.function.Consumer;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class Tab {

    @Getter
    private final Component header;
    @Getter
    private final TabBackground headerBackground;
    @Getter
    private final DecoratedContainer button;
    @Getter
    private final Component content;

    Tab(final Component header, final Consumer<Tab> headerBackground, final Component content) {
        this.header = header;
        this.headerBackground = new TabBackground(() -> headerBackground.accept(this));
        this.button = new DecoratedContainer(this.headerBackground, header);
        this.content = content;
    }

}
