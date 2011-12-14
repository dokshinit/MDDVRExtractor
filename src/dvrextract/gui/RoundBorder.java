/*
 * Copyright (c) 2011, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.border.Border;

/**
 * Рамка с закруглёнными краями.
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class RoundBorder implements Border {

    /**
     * Радиус скругления углов.
     */
    private int cornerRadius;

    /**
     * Конструктор.
     * @param cornerRadius Радиус скругления углов.
     */
    public RoundBorder(int cornerRadius) {
        this.cornerRadius = cornerRadius;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return getBorderInsets(c, new Insets(0, 0, 0, 0));
    }

    /**
     * Устанавливает и возвращает отступы для данного типа рамки.
     * @param c Компонент.
     * @param insets Корректируемые отступы.
     * @return Скорректированные отступы.
     */
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.top = insets.bottom = cornerRadius / 2;
        insets.left = insets.right = 1;
        return insets;
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color color = GUI.deriveColorHSB(c.getBackground(), 0, 0, -.3f);
        g2.setColor(GUI.deriveColorAlpha(color, 40));
        g2.drawRoundRect(x, y + 2, width - 1, height - 3, cornerRadius, cornerRadius);
        g2.setColor(GUI.deriveColorAlpha(color, 90));
        g2.drawRoundRect(x, y + 1, width - 1, height - 2, cornerRadius, cornerRadius);
        g2.setColor(GUI.deriveColorAlpha(color, 255));
        g2.drawRoundRect(x, y, width - 1, height - 1, cornerRadius, cornerRadius);
        g2.dispose();
    }
}
