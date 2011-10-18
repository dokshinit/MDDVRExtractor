package dvrextract;

import java.awt.Color;
import java.awt.Component;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;

/**
 * Подсвечивание строк таблицы лога.
 * @author Докшин_А_Н
 */
public class LogHighlighter extends AbstractHighlighter {

    Color errorColor;
    Color hideInfoColor;
    Color infoColor;

    public LogHighlighter() {
        this.errorColor = new Color(0xFF0000);
        this.hideInfoColor = new Color(0xA0A0A0);
        this.infoColor = new Color(0x0000FF);
    }

    @Override
    protected Component doHighlight(Component component, ComponentAdapter adapter) {
        Object val = adapter.getValue(1);
        if (val != null) {
            if (val.getClass() == Integer.class) {
                if ((Integer) val < 0) {
                    component.setForeground(errorColor);
                }
                if ((Integer) val == 1) {
                    component.setForeground(hideInfoColor);
                }
                if ((Integer) val > 1) {
                    component.setForeground(infoColor);
                }
            }
        }
        return component;
    }
}