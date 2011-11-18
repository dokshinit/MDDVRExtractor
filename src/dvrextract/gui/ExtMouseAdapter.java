/*
 * Copyright (c) 2011, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.Timer;

/**
 * Класс, обеспечивающий отработку события двойного клика мышкой.
 * Для JTable - срабатывает только если ячейка ридонли (иначе вызывается
 * редактирование)!
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class ExtMouseAdapter extends MouseAdapter implements ActionListener {

    // Таймаут ожидания второго клика в миллисекундах.
    private int doubleClickDelay = 300;
    // Таймер ожидания второго клика.
    private Timer timer;
    private Object owner;
    private int click = 0;
    private int col = -1;
    private int row = -1;

    /**
     * Конструктор.
     */
    public ExtMouseAdapter() {
        owner = null;
        timer = new Timer(doubleClickDelay, this);
        timer.setRepeats(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        timer.stop();
    }

    private void setOwner(Object obj) {
        owner = obj;
        if (owner != null) {
            if (owner instanceof JTable) {
                JTable tab = (JTable) owner;
                row = tab.getSelectedRow();
                col = tab.getSelectedColumn();
            }
        }
    }

    private boolean checkOwner(Object obj) {
        if (owner == obj) {
            if (owner instanceof JTable) {
                JTable tab = (JTable) owner;
                return tab.getSelectedRow() == row && tab.getSelectedColumn() == col;
            }
            return true;
        }
        return false;
    }

    public void show(String s) {
        System.out.print(s);
        if (owner != null) {
            System.out.print(": " + owner.getClass().getName());
            if (owner instanceof JTable) {
                System.out.print(" row=" + row + " col=" + col);
            }
        } else {
            System.out.print(": null");
        }
        System.out.println();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!checkOwner(e.getSource())) {
            timer.stop();
            click = 0;
            setOwner(e.getSource());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (checkOwner(e.getSource())) {
            click++;
            if (click == 2 && timer.isRunning()) {
                //show("double-click!");
                timer.stop();
                click = 0;
                setOwner(null);
                fireDoubleClick(e);
            } else {
                //show("click!");
                click = 1;
                timer.start();
                fireSingleClick(e);
            }
        } else {
            // Что интересно - такое возможно только если перемещать выделенние
            // ячейки в сторону уменьшения номера колонки или строки.
            // Т.к. начало выделения верхний левый угол.
            //show("out-drag-click!");
            timer.stop();
            click = 0;
            setOwner(null);
            fireSingleClick(e);
        }
    }

    /**
     * Обработчик однократного клика.
     * @param e Событие.
     */
    protected void fireSingleClick(MouseEvent e) {
        //System.out.println("-- single click --");
    }

    /**
     * Обработчик двойного клика.
     * @param e Событие.
     */
    protected void fireDoubleClick(MouseEvent e) {
        //System.out.println("-- double click --");
    }
}
