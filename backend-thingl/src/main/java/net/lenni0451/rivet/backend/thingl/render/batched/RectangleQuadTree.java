package net.lenni0451.rivet.backend.thingl.render.batched;

import org.joml.primitives.Rectanglef;

import java.util.Arrays;

public class RectangleQuadTree {

    private static final int CHILD_COUNT = 4;

    private Node root;

    public void add(final Rectanglef bounds) {
        if (this.root == null) {
            this.createRoot(bounds.minX, bounds.minY, bounds.maxX, bounds.maxY);
        } else {
            this.ensureRootContains(bounds.minX, bounds.minY, bounds.maxX, bounds.maxY);
        }
        this.root.add(bounds, 0);
    }

    public boolean intersects(final Rectanglef bounds) {
        return this.root != null && this.root.intersects(bounds);
    }

    private void createRoot(final float left, final float top, final float right, final float bottom) {
        final float extent = Math.max(1F, Math.max(right - left, bottom - top));
        final float power = Math.scalb(1F, Math.getExponent(extent));
        final float size = power < extent ? power * 2 : power; // Round up to a power of two
        this.root = new Node(left, top, size);
    }

    private void ensureRootContains(final float left, final float top, final float right, final float bottom) {
        while (left < this.root.x || top < this.root.y || right > this.root.x + this.root.size || bottom > this.root.y + this.root.size) {
            final Node previous = this.root;
            final boolean growLeft = left < previous.x;
            final boolean growUp = top < previous.y;
            if (previous.children == null) { // A leaf can grow in place
                if (growLeft) {
                    previous.x -= previous.size;
                }
                if (growUp) {
                    previous.y -= previous.size;
                }
                previous.size *= 2;
            } else { // Preserve existing child regions by wrapping the root
                this.root = new Node(growLeft ? previous.x - previous.size : previous.x, growUp ? previous.y - previous.size : previous.y, previous.size * 2);
                this.root.children = new Node[CHILD_COUNT];
                this.root.children[(growLeft ? 1 : 0) | (growUp ? 2 : 0)] = previous;
            }
        }
    }

    private static final class Node {

        private static final int LEAF_CAPACITY = 12;
        private static final int MAX_DEPTH = 12;

        private float x;
        private float y;
        private float size;
        private Rectanglef[] entries;
        private int entryCount;
        private Node[] children;

        private Node(final float x, final float y, final float size) {
            this.x = x;
            this.y = y;
            this.size = size;
        }

        private void add(final Rectanglef bounds, int depth) {
            Node node = this;
            while (node.children != null || node.shouldSplit(depth)) {
                if (node.children == null) {
                    node.split(depth);
                }
                final int quadrant = node.getContainingQuadrant(bounds);
                if (quadrant < 0) {
                    break;
                }
                node = node.getOrCreateChild(quadrant);
                depth++;
            }
            node.addEntry(bounds);
        }

        private boolean shouldSplit(final int depth) {
            return this.children == null && this.entryCount >= LEAF_CAPACITY && depth < MAX_DEPTH;
        }

        private void split(final int depth) {
            this.children = new Node[CHILD_COUNT];
            for (int i = this.entryCount - 1; i >= 0; i--) {
                final Rectanglef entry = this.entries[i];
                final int quadrant = this.getContainingQuadrant(entry);
                if (quadrant >= 0) {
                    this.getOrCreateChild(quadrant).add(entry, depth + 1);
                    System.arraycopy(this.entries, i + 1, this.entries, i, this.entryCount - i - 1);
                    this.entries[--this.entryCount] = null;
                }
            }
        }

        private void addEntry(final Rectanglef bounds) {
            if (this.entries == null) {
                this.entries = new Rectanglef[LEAF_CAPACITY];
            } else if (this.entryCount == this.entries.length) {
                this.entries = Arrays.copyOf(this.entries, this.entryCount * 2);
            }
            this.entries[this.entryCount++] = bounds;
        }

        private boolean intersects(final Rectanglef bounds) {
            if (bounds.minX >= this.x + this.size || bounds.maxX <= this.x || bounds.minY >= this.y + this.size || bounds.maxY <= this.y) {
                return false; // Skip the entire subtree
            }
            for (int i = 0; i < this.entryCount; i++) {
                if (this.entries[i].intersectsRectangle(bounds)) {
                    return true;
                }
            }
            if (this.children != null) {
                for (Node child : this.children) {
                    if (child != null && child.intersects(bounds)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private Node getOrCreateChild(final int quadrant) {
            if (this.children[quadrant] == null) {
                final float half = this.size / 2;
                this.children[quadrant] = new Node(this.x + ((quadrant & 1) != 0 ? half : 0), this.y + ((quadrant & 2) != 0 ? half : 0), half);
            }
            return this.children[quadrant];
        }

        private int getContainingQuadrant(final Rectanglef bounds) {
            final float midX = this.x + this.size / 2;
            final float midY = this.y + this.size / 2;
            final int column = bounds.maxX <= midX ? 0 : bounds.minX >= midX ? 1 : -1;
            final int row = bounds.maxY <= midY ? 0 : bounds.minY >= midY ? 2 : -1;
            return column < 0 || row < 0 ? -1 : column | row;
        }

    }

}
