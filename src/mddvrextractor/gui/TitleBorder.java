/*
 * Copyright (c) 2011-2013, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextractor.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.border.AbstractBorder;

/**
 * Класс окантовки для панелей названий в группах.
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class TitleBorder extends AbstractBorder {

    /**
     * Цвет окантовки.
     */
    protected Color color;

    /**
     * Конструктор.
     *
     * @param c Цвет окантовки.
     */
    public TitleBorder(Color c) {
        color = c;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Color oldColor = g.getColor();
        g.setColor(color);
        g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
        g.setColor(oldColor);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(1, 1, 1, 1);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.top = insets.right = insets.bottom = 1;
        return insets;
    }
}
