package net.lenni0451.rivet.component.impl.slider;

import net.lenni0451.rivet.utils.FormatUtils;

public record SliderTicks(double majorTickSpacing, double minorTickSpacing, TickLabelProvider labelProvider) {

    public SliderTicks(final double majorTickSpacing, final double minorTickSpacing) {
        this(majorTickSpacing, minorTickSpacing, defaultLabelProvider(majorTickSpacing));
    }

    private static TickLabelProvider defaultLabelProvider(final double majorTickSpacing) {
        String format = FormatUtils.formatDecimalString("%,f", majorTickSpacing);
        return d -> {
            try {
                return String.format(format, d);
            } catch (Throwable t) {
                return Double.toString(d);
            }
        };
    }


    @FunctionalInterface
    public interface TickLabelProvider {
        String getLabel(final double value);
    }

}
