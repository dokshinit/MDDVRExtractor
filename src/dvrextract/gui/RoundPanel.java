/*
 * Copyright (c) 2011, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.geom.RoundRectangle2D;
import org.jdesktop.swingx.JXPanel;

/**
 * Панель с закруглёнными краями (только под рамку!).
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class RoundPanel extends JXPanel {

    private transient RoundRectangle2D.Float roundBounds;

    public RoundPanel(LayoutManager layout, int cornerRadius) {
        super(layout);
        this.roundBounds = new RoundRectangle2D.Float(
                0, 0, 0, 0, cornerRadius, cornerRadius);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Dimension size = getSize();
        roundBounds.width = size.width;
        roundBounds.height = size.height;
        g2.setColor(getBackground());
        g2.fill(roundBounds);
        super.paintComponent(g);
    }
}
