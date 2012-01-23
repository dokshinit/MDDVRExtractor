/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import net.miginfocom.swing.MigLayout;

/**
 * Табулятор с заголовком из кнопок с состоянием. (штатный уж очень мне не
 * нравился!)
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class GUITabPane extends JPanel implements ActionListener {

    /**
     * Панель закладок.
     */
    private JPanel panelBar;
    /**
     * Список всех закладок.
     */
    private ArrayList<Item> items;
    /**
     * Индекс текущей выбранной закладки (в массиве items, если -1 - нет
     * закладок).
     */
    private int index;

    /**
     * Конструктор.
     */
    public GUITabPane() {
        items = new ArrayList<Item>();
        index = -1;
        setLayout(new BorderLayout());
        panelBar = new JPanel(new MigLayout("ins 5 5 3 5, gap 2"));
        panelBar.setBorder(new TitleBorder(new Color(0x404080)));
        add(panelBar, BorderLayout.NORTH);
    }

    /**
     * Добавление закладки.
     *
     * @param title Отображаемое название.
     * @param comp Содержимое.
     */
    public void addTab(String title, JComponent comp) {
        Item i = new Item(items.size(), title, comp);
        items.add(i);

        if (index == -1) {
            index = 0;
            add(comp, BorderLayout.CENTER);
            i.button.setSelected(true);
        }
        panelBar.add(i.button);
    }

    /**
     * Установка названия для вкладки.
     *
     * @param index Номер вкладки (0..n-1).
     * @param title Название вкладки.
     */
    public void setTitleAt(int index, String title) {
        items.get(index).button.setText(title);
    }

    /**
     * Возвращает панель с кнопками-закладками.
     *
     * @return Панель кнопок-закладок.
     */
    public JPanel getBarPanel() {
        return panelBar;
    }

    /**
     * Включение/выключение закладки (доступна/недоступна).
     *
     * @param comp Содержимое закладки.
     * @param state Устанавливаемый режим доступности.
     */
    public void setEnableAt(Component comp, boolean state) {
        for (Item i : items) {
            if (i.comp == comp) {
                i.button.setEnabled(state);
                break;
            }
        }
    }

    /**
     * Обработка нажатий на закладки.
     *
     * @param e Событие.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JToggleButton) {
            JToggleButton b = (JToggleButton) e.getSource();
            for (Item i2 : items) {
                if (i2.button != b) {
                    continue;
                }
                Item i1 = items.get(index);
                // Выключаем старую кнопку.
                i1.button.setSelected(false);
                remove(i1.comp);
                // Включаем новую кнопку.
                i2.button.setSelected(true);
                add(i2.comp, BorderLayout.CENTER);
                index = i2.index;
                // Перерисовываем канву.
                validate();
                repaint();
                break;
            }
        }
    }

    /**
     * Класс - закладка.
     */
    public class Item {

        /**
         * Индекс закладки в массиве закладок.
         */
        private int index;
        /**
         * Содержимое закладки.
         */
        private Component comp;
        /**
         * Кнопка-селектор закладки.
         */
        private JToggleButton button;

        /**
         * Конструктор.
         *
         * @param index Индекс.
         * @param title Отображаемое название.
         * @param comp Содержимое.
         */
        public Item(int index, String title, Component comp) {
            button = GUI.createToggleButton(title);
            this.index = index;
            this.comp = comp;
            button.addActionListener(GUITabPane.this);
        }

        /**
         * Возвращает индекс закладки.
         *
         * @return Индекс.
         */
        public int getIndex() {
            return index;
        }

        /**
         * Возвращает содерживмое закладки.
         *
         * @return Компонент.
         */
        public Component getComp() {
            return comp;
        }

        /**
         * Возвращает кнопку закладки.
         *
         * @return Кнопка закладки.
         */
        public JToggleButton getButton() {
            return button;
        }
    }
}
