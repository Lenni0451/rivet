package net.lenni0451.rivet.component;

public class Label extends Component {

    private String text;

    public String getText() {
        return this.text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    @Override
    protected void computePreferredSize() {

    }

}
