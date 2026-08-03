package test.impl;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.editor.LayoutEditor;
import test.TestBase;

public class LayoutEditorTest extends TestBase {

    void main() {
        this.run();
    }

    @Override
    protected void init(final Rivet rivet) {
        rivet.root().addChild(new LayoutEditor());
    }

}
