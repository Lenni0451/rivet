package net.lenni0451.rivet.layout.flex;

import lombok.With;
import lombok.experimental.WithBy;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.*;
import java.util.function.BiConsumer;

@With
@WithBy
public record FlexLayout(FlexDirection direction, FlexWrap wrap, FlexJustify justifyContent, FlexAlignItems alignItems, FlexAlignContent alignContent, int rowGap, int columnGap) implements Layout {

    public static final FlexLayout DEFAULT = new FlexLayout(FlexDirection.ROW, FlexWrap.NO_WRAP, FlexJustify.START, FlexAlignItems.START, FlexAlignContent.START, 0, 0);

    @Override
    public Size computeIdealSize(final Size constraints, final Collection<Component> components) {
        if (components.isEmpty()) return Size.EMPTY;
        boolean rowAxis = this.isRowAxis();
        List<Component> ordered = this.orderComponents(components);

        Map<Component, Size> idealSizes = new IdentityHashMap<>();
        for (Component component : ordered) {
            idealSizes.put(component, component.computeIdealSize(constraints));
        }

        float mainConstraint = rowAxis ? constraints.width() : constraints.height();
        float mainAvailable = this.wrap.equals(FlexWrap.NO_WRAP) ? Float.MAX_VALUE : mainConstraint;
        List<Line> lines = this.createLines(mainAvailable, ordered, idealSizes);

        float crossGap = rowAxis ? this.rowGap : this.columnGap;
        float totalMain = 0;
        float totalCross = 0;
        for (Line line : lines) {
            totalMain = Math.max(totalMain, line.mainSize);
            if (totalCross > 0) totalCross += crossGap;
            totalCross += line.crossSize;
        }
        return rowAxis ? new Size(totalMain, totalCross) : new Size(totalCross, totalMain);
    }

    @Override
    public void layoutComponents(final Size containerSize, final Collection<Component> components, final BiConsumer<Component, Rectangle> setBounds) {
        if (components.isEmpty()) return;
        boolean rowAxis = this.isRowAxis();
        List<Component> ordered = this.orderComponents(components);

        Map<Component, Size> idealSizes = new IdentityHashMap<>();
        for (Component component : ordered) {
            idealSizes.put(component, component.computeIdealSize(containerSize));
        }

        float mainAvailable = rowAxis ? containerSize.width() : containerSize.height();
        float crossAvailable = rowAxis ? containerSize.height() : containerSize.width();
        float mainAvailableForWrap = this.wrap.equals(FlexWrap.NO_WRAP) ? Float.MAX_VALUE : mainAvailable;
        List<Line> lines = this.createLines(mainAvailableForWrap, ordered, idealSizes);
        lines.replaceAll(line -> this.resolveFlexSizing(line, mainAvailable, rowAxis));
        if (this.wrap.equals(FlexWrap.WRAP_REVERSE)) Collections.reverse(lines);

        float crossGap = rowAxis ? this.rowGap : this.columnGap;
        float mainGap = rowAxis ? this.columnGap : this.rowGap;

        float[] lineCrossSizes = new float[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            lineCrossSizes[i] = lines.get(i).crossSize;
        }
        if (lines.size() == 1) {
            lineCrossSizes[0] = crossAvailable;
        } else if (this.alignContent.equals(FlexAlignContent.STRETCH)) {
            float used = sumWithGaps(lineCrossSizes, crossGap);
            float extra = Math.max(0, crossAvailable - used);
            if (extra > 0) {
                float share = extra / lines.size();
                for (int i = 0; i < lineCrossSizes.length; i++) {
                    lineCrossSizes[i] = lineCrossSizes[i] + share;
                }
            }
        }
        float[] crossOffsets = distribute(lineCrossSizes, crossGap, crossAvailable, Distribution.from(this.alignContent));

        boolean reverseMain = this.direction.equals(FlexDirection.ROW_REVERSE) || this.direction.equals(FlexDirection.COLUMN_REVERSE);
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            float lineCrossOffset = crossOffsets[i];
            float lineCrossSize = lineCrossSizes[i];

            List<AlignableComponent> orderedLine = new ArrayList<>(line.components);
            if (reverseMain) Collections.reverse(orderedLine);
            float[] mainSizes = new float[orderedLine.size()];
            for (int j = 0; j < orderedLine.size(); j++) {
                mainSizes[j] = orderedLine.get(j).mainSize;
            }
            float[] mainOffsets = distribute(mainSizes, mainGap, mainAvailable, Distribution.from(this.justifyContent));

            for (int j = 0; j < orderedLine.size(); j++) {
                AlignableComponent alignableComponent = orderedLine.get(j);
                float mainPos = mainOffsets[j];
                float itemMain = alignableComponent.mainSize;
                float itemCross = alignableComponent.crossSize;
                float crossPos;
                switch (this.resolveAlignSelf(alignableComponent.options)) {
                    case END -> crossPos = lineCrossOffset + lineCrossSize - itemCross;
                    case CENTER -> crossPos = lineCrossOffset + (lineCrossSize - itemCross) / 2F;
                    case STRETCH -> {
                        itemCross = rowAxis ? this.heightOf(alignableComponent.component, lineCrossSize) : this.widthOf(alignableComponent.component, lineCrossSize);
                        crossPos = lineCrossOffset;
                    }
                    default -> crossPos = lineCrossOffset;
                }

                Rectangle bounds = rowAxis
                        ? new Rectangle(mainPos, crossPos, itemMain, itemCross)
                        : new Rectangle(crossPos, mainPos, itemCross, itemMain);
                setBounds.accept(alignableComponent.component, bounds);
            }
        }
    }

    private List<Line> createLines(final float mainAvailable, final List<Component> components, final Map<Component, Size> idealSizes) {
        boolean rowAxis = this.isRowAxis();
        float mainGap = rowAxis ? this.columnGap : this.rowGap;
        boolean canWrap = !this.wrap.equals(FlexWrap.NO_WRAP);

        List<Line> lines = new ArrayList<>();
        List<AlignableComponent> current = new ArrayList<>();
        float currentMain = 0;
        float currentCross = 0;
        for (Component component : components) {
            FlexOptions options = resolveOptions(component);
            Size idealSize = idealSizes.get(component);
            float naturalMain = rowAxis ? idealSize.width() : idealSize.height();
            float basisMain = options.basis() > 0 ? options.basis() : naturalMain;
            float itemMain = rowAxis ? this.widthOf(component, basisMain) : this.heightOf(component, basisMain);
            float itemCross = rowAxis ? this.heightOf(component, idealSize) : this.widthOf(component, idealSize);
            float gapIfAdded = current.isEmpty() ? 0 : mainGap;

            if (canWrap && !current.isEmpty() && currentMain + gapIfAdded + itemMain > mainAvailable) {
                lines.add(new Line(current, currentMain, currentCross));
                current = new ArrayList<>();
                currentMain = 0;
                currentCross = 0;
                gapIfAdded = 0;
            }

            current.add(new AlignableComponent(component, options, itemMain, itemCross));
            currentMain += gapIfAdded + itemMain;
            currentCross = Math.max(currentCross, itemCross);
        }
        if (!current.isEmpty()) lines.add(new Line(current, currentMain, currentCross));
        return lines;
    }

    private Line resolveFlexSizing(final Line line, final float mainAvailable, final boolean rowAxis) {
        float mainGap = rowAxis ? this.columnGap : this.rowGap;
        float free = mainAvailable - line.mainSize;
        if (free == 0) return line;

        List<AlignableComponent> components = line.components;
        List<AlignableComponent> resolved = new ArrayList<>(components.size());
        if (free > 0) {
            float totalGrow = 0;
            for (AlignableComponent component : components) {
                totalGrow += Math.max(0, component.options.grow());
            }
            if (totalGrow <= 0) return line;

            for (AlignableComponent component : components) {
                float grow = Math.max(0, component.options.grow());
                float extra = free * (grow / totalGrow);
                float newMain = rowAxis
                        ? this.widthOf(component.component, component.mainSize + extra)
                        : this.heightOf(component.component, component.mainSize + extra);
                resolved.add(new AlignableComponent(component.component, component.options, newMain, component.crossSize));
            }
        } else {
            float totalShrinkWeighted = 0;
            for (AlignableComponent component : components) {
                totalShrinkWeighted += Math.max(0, component.options.shrink()) * component.mainSize;
            }
            if (totalShrinkWeighted <= 0) return line;

            for (AlignableComponent component : components) {
                float weighted = Math.max(0, component.options.shrink()) * component.mainSize;
                float reduction = -free * (weighted / totalShrinkWeighted);
                float newMain = rowAxis
                        ? this.widthOf(component.component, Math.max(0, component.mainSize - reduction))
                        : this.heightOf(component.component, Math.max(0, component.mainSize - reduction));
                resolved.add(new AlignableComponent(component.component, component.options, newMain, component.crossSize));
            }
        }

        float[] mainSizes = new float[resolved.size()];
        for (int i = 0; i < resolved.size(); i++) {
            mainSizes[i] = resolved.get(i).mainSize;
        }
        float newLineMain = sumWithGaps(mainSizes, mainGap);
        return new Line(resolved, newLineMain, line.crossSize);
    }

    private static float[] distribute(final float[] sizes, final float gap, final float available, final Distribution mode) {
        int count = sizes.length;
        float[] offsets = new float[count];
        if (count == 0) return offsets;

        float used = sumWithGaps(sizes, gap);
        float free = Math.max(0, available - used);
        switch (mode) {
            case START -> {
                float pos = 0;
                for (int i = 0; i < count; i++) {
                    offsets[i] = pos;
                    pos += sizes[i] + gap;
                }
            }
            case END -> {
                float pos = free;
                for (int i = 0; i < count; i++) {
                    offsets[i] = pos;
                    pos += sizes[i] + gap;
                }
            }
            case CENTER -> {
                float pos = free / 2F;
                for (int i = 0; i < count; i++) {
                    offsets[i] = pos;
                    pos += sizes[i] + gap;
                }
            }
            case SPACE_BETWEEN -> {
                if (count == 1) {
                    offsets[0] = free / 2F;
                } else {
                    float spacing = gap + free / (count - 1);
                    float pos = 0;
                    for (int i = 0; i < count; i++) {
                        offsets[i] = pos;
                        pos += sizes[i] + spacing;
                    }
                }
            }
            case SPACE_AROUND -> {
                float spacing = gap + free / count;
                float pos = spacing / 2F;
                for (int i = 0; i < count; i++) {
                    offsets[i] = pos;
                    pos += sizes[i] + spacing;
                }
            }
            case SPACE_EVENLY -> {
                float spacing = gap + free / (count + 1);
                float pos = spacing;
                for (int i = 0; i < count; i++) {
                    offsets[i] = pos;
                    pos += sizes[i] + spacing;
                }
            }
        }
        return offsets;
    }

    private boolean isRowAxis() {
        return this.direction.equals(FlexDirection.ROW) || this.direction.equals(FlexDirection.ROW_REVERSE);
    }

    private List<Component> orderComponents(final Collection<Component> components) {
        List<Component> ordered = new ArrayList<>(components);
        ordered.sort(Comparator.comparingInt(component -> resolveOptions(component).order()));
        return ordered;
    }

    private static FlexOptions resolveOptions(final Component component) {
        return component.layoutOptions() instanceof FlexOptions options ? options : FlexOptions.DEFAULT;
    }

    private static float sumWithGaps(final float[] sizes, final float gap) {
        float sum = 0;
        for (int i = 0; i < sizes.length; i++) {
            if (i > 0) sum += gap;
            sum += sizes[i];
        }
        return sum;
    }

    private FlexAlignItems resolveAlignSelf(final FlexOptions options) {
        return switch (options.align()) {
            case AUTO -> this.alignItems;
            case START -> FlexAlignItems.START;
            case END -> FlexAlignItems.END;
            case CENTER -> FlexAlignItems.CENTER;
            case STRETCH -> FlexAlignItems.STRETCH;
        };
    }


    private record Line(List<AlignableComponent> components, float mainSize, float crossSize) {
    }

    private record AlignableComponent(Component component, FlexOptions options, float mainSize, float crossSize) {
    }

    private enum Distribution {
        START, CENTER, END,
        SPACE_BETWEEN, SPACE_AROUND, SPACE_EVENLY;

        static Distribution from(final FlexJustify justify) {
            return switch (justify) {
                case START -> Distribution.START;
                case CENTER -> Distribution.CENTER;
                case END -> Distribution.END;
                case SPACE_BETWEEN -> Distribution.SPACE_BETWEEN;
                case SPACE_AROUND -> Distribution.SPACE_AROUND;
                case SPACE_EVENLY -> Distribution.SPACE_EVENLY;
            };
        }

        static Distribution from(final FlexAlignContent alignContent) {
            return switch (alignContent) {
                case START, STRETCH -> Distribution.START;
                case CENTER -> Distribution.CENTER;
                case END -> Distribution.END;
                case SPACE_BETWEEN -> Distribution.SPACE_BETWEEN;
                case SPACE_AROUND -> Distribution.SPACE_AROUND;
                case SPACE_EVENLY -> Distribution.SPACE_EVENLY;
            };
        }
    }

}
