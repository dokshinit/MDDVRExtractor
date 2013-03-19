/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract.gui;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Рамка с заголовком для панелей групп.
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class GroupBorder extends RoundBorder {

    private transient String title;
    private final Color[] gradient;
    private final boolean isCenterized;

    /**
     * Конструктор.
     *
     * @param title Текст заголовка.
     * @param isCenterized Флаг необходимости центрирования текста заголовка.
     * @param gradient1 Цвет начала градиента.
     * @param gradient2 Цвет конца градиента.
     */
    public GroupBorder(String title, boolean isCenterized, Color gradient1, Color gradient2) {
        super(10);
        this.title = title;
        this.isCenterized = isCenterized;
        this.gradient = new Color[2];
        this.gradient[0] = gradient1;
        this.gradient[1] = gradient2;
    }

    /**
     * Установка строки заголовка.
     *
     * @param title Текст заголовка.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        Insets borderInsets = super.getBorderInsets(c, insets);
        borderInsets.top = getTitleHeight(c);
        return borderInsets;
    }

    /**
     * Возвращает высоту заголовка для компонента (отталкиваясь от высоты
     * текста!).
     *
     * @param c Компонент.
     * @return Высота заголовка.
     */
    protected int getTitleHeight(Component c) {
        FontMetrics metrics = c.getFontMetrics(c.getFont());
        return (int) (metrics.getHeight() * 1.80);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        int titleHeight = getTitleHeight(c);

        // Отрисовка заголовка с градиентом.
        BufferedImage titleImage = GUI.createTranslucentImage(width, titleHeight);
        GradientPaint grad = new GradientPaint(0, 0,
                gradient[0], 0, titleHeight,
                gradient[1], false);
        Graphics2D g2 = (Graphics2D) titleImage.getGraphics();
        g2.setPaint(grad);
        g2.fillRoundRect(x, y, width, height, 10, 10);
        g2.setColor(GUI.deriveColorHSB(gradient[1], 0, 0, -.2f));
        g2.drawLine(x + 1, titleHeight - 1, width - 2, titleHeight - 1);
        g2.setColor(GUI.deriveColorHSB(gradient[1], 0, -.5f, .5f));
        g2.drawLine(x + 1, titleHeight, width - 2, titleHeight);
        g2.setPaint(new GradientPaint(0, 0, new Color(0.0f, 0.0f, 0.0f, 1.0f),
                width, 0, new Color(0.0f, 0.0f, 0.0f, 0.2f)));
        g2.setComposite(AlphaComposite.DstIn);
        g2.fillRect(x, y, width, titleHeight);
        g2.dispose();
        g.drawImage(titleImage, x, y, c);

        super.paintBorder(c, g, x, y, width, height);

        // Отрисовка текста заголовка.
        g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(c.getForeground());
        Font f = c.getFont().deriveFont(Font.BOLD);
        g2.setFont(f);
        FontMetrics metrics = c.getFontMetrics(f);
        if (isCenterized) {
            Rectangle2D rect = metrics.getStringBounds(title, g);
            g2.drawString(title, x + (c.getWidth() - (int) rect.getWidth()) / 2,
                    y + (titleHeight - metrics.getHeight()) / 2 + metrics.getAscent());
        } else {
            g2.drawString(title, x + 8,
                    y + (titleHeight - metrics.getHeight()) / 2 + metrics.getAscent());
        }
        g2.dispose();

    }
}
