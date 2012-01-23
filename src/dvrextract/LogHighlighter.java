/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

import java.awt.Color;
import java.awt.Component;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;

/**
 * Подсвечивание строк таблицы лога.
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class LogHighlighter extends AbstractHighlighter {

    /**
     * Цвет текста строки с типом ERROR.
     */
    private Color errorColor;
    /**
     * Цвет текста строки с типом HIDE.
     */
    private Color hideInfoColor;
    /**
     * Цвет текста строки с типом INFO.
     */
    private Color infoColor;

    /**
     * Конструктор.
     */
    public LogHighlighter() {
        errorColor = new Color(0xFF0000);
        hideInfoColor = new Color(0xA0A0A0);
        infoColor = new Color(0x0000FF);
    }

    /**
     * Подсветка компонента.
     *
     * @param component Компонент.
     * @param adapter Адаптер компонента.
     * @return Обработанный компонент.
     */
    @Override
    protected Component doHighlight(Component component, ComponentAdapter adapter) {
        Object val = adapter.getValue(1);
        if (val != null) {
            if (val.getClass() == Integer.class) {
                if ((Integer) val < 0) {
                    component.setForeground(errorColor);
                } else if ((Integer) val == 1) {
                    component.setForeground(hideInfoColor);
                } else if ((Integer) val > 1) {
                    component.setForeground(infoColor);
                }
            }
        }
        return component;
    }
}