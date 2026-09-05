package net.lenni0451.rivet.backend.awt.shape;

import org.intellij.lang.annotations.MagicConstant;

import java.awt.geom.Path2D;

public class RoundedRect extends Path2D.Float {

    private static final float KAPPA = (float) (4 * (Math.sqrt(2) - 1) / 3);

    public RoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr) {
        this.add(x, y, width, height, rtl, rbl, rbr, rtr);
    }

    public RoundedRect(@MagicConstant(flags = {Path2D.WIND_EVEN_ODD, Path2D.WIND_NON_ZERO}) final int rule, final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr) {
        super(rule);
        this.add(x, y, width, height, rtl, rbl, rbr, rtr);
    }

    public void add(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr) {
        this.moveTo(x + rtl, y);
        this.lineTo(x + width - rtr, y);
        if (rtr > 0) {
            float cTr = rtr * KAPPA;
            this.curveTo(x + width - rtr + cTr, y, x + width, y + rtr - cTr, x + width, y + rtr);
        }
        this.lineTo(x + width, y + height - rbr);
        if (rbr > 0) {
            float cBr = rbr * KAPPA;
            this.curveTo(x + width, y + height - rbr + cBr, x + width - rbr + cBr, y + height, x + width - rbr, y + height);
        }
        this.lineTo(x + rbl, y + height);
        if (rbl > 0) {
            float cBl = rbl * KAPPA;
            this.curveTo(x + rbl - cBl, y + height, x, y + height - rbl + cBl, x, y + height - rbl);
        }
        this.lineTo(x, y + rtl);
        if (rtl > 0) {
            float cTl = rtl * KAPPA;
            this.curveTo(x, y + rtl - cTl, x + rtl - cTl, y, x + rtl, y);
        }
        this.closePath();
    }

}
