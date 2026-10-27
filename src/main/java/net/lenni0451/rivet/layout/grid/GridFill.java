package net.lenni0451.rivet.layout.grid;

public enum GridFill {

    NONE,
    HORIZONTAL,
    VERTICAL,
    BOTH;

    public boolean horizontal() {
        return this == HORIZONTAL || this == BOTH;
    }

    public boolean vertical() {
        return this == VERTICAL || this == BOTH;
    }

}
