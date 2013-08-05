/*
 * Copyright (c) 2011-2013, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextractor.gui;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.Scrollable;

/**
 * Панель с правилами для скроллирования - только по вертикали, по горизонтали
 * ведёт себя как обычно - подгоняет под ширину (!).
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class JVScrolledPanel extends JPanel implements Scrollable {

    /**
     * Конструктор.
     */
    public JVScrolledPanel() {
        super();
    }

    /**
     * Конструктор.
     *
     * @param layout Менеджер раскладки.
     */
    public JVScrolledPanel(LayoutManager layout) {
        super(layout);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return null;
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect,
            int orientation, int direction) {
        return 40;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect,
            int orientation, int direction) {
        return 40;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
