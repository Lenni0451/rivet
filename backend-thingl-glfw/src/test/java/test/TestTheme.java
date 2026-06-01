package test;

import lombok.SneakyThrows;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.theme.impl.DefaultDark;
import net.lenni0451.rivet.theme.text.ThemeLoader;

public class TestTheme extends DefaultDark {

    @Override
    @SneakyThrows
    protected void addValues(final Rivet rivet, final Values values) {
        super.addValues(rivet, values);
        ThemeLoader.load(TestTheme.class.getClassLoader().getResourceAsStream("test_theme"), values, ThemeLoader.ExceptionHandler.RETHROW);
    }

}
