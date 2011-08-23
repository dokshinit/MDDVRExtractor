/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

/**
 * Табулятор с заголовком из кнопок с состоянием.
 * (штатный уж очень мне не нравился!)
 * @author lex
 */
public class TabPane extends JPanel implements ActionListener {

    // Панель закладок.
    private JPanel panelBar;
    // Список всех закладок.
    private ArrayList<Item> items;
    // Индекс текущей выбранной закладки (в массиве items, если -1 - нет закладок).
    private int index;
    
    /**
     * Конструктор.
     */
    public TabPane() {
        items = new ArrayList<Item>();
        index = -1;
        setLayout(new BorderLayout());
        panelBar = new JPanel(new MigLayout("ins 5 5 5 5, gap 2","[]"));
        add(panelBar, BorderLayout.NORTH);
    }

    /**
     * Добавление закладки.
     * @param title Отображаемое название.
     * @param comp Содержимое.
     */
    public void addTab(String title, JComponent comp) {
        Item i = new Item(items.size(), title, comp);
        items.add(i);
        
        if (index == -1) {
            index = 0;
            add(comp, BorderLayout.CENTER);
            i.setSelected(true);
        }
        panelBar.add(i);
    }

    /**
     * Включение/выключение закладки (доступна/недоступна).
     * @param comp Содержимое закладки.
     * @param state Устанавливаемый режим доступности.
     */
    public void setEnable(Component comp, boolean state) {
        for (Item i : items) {
            if (i.comp == comp) {
                i.setEnabled(state);
                break;
            }
        }
    }
    
    /**
     * Обработка нажатий на закладки.
     * @param e Событие.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof Item) {
            Item i1 = items.get(index);
            Item i2 = (Item) e.getSource();

            i1.setSelected(false); // Выключаем старую кнопку.
            i2.setSelected(true); // Включаем новую кнопку.

            remove(i1.comp);
            add(i2.comp, BorderLayout.CENTER);
            index = i2.index;
            
            // Перерисовываем канву.
            validate();
            repaint();
        }
    }

    /**
     * Класс - закладка.
     */
    class Item extends JToggleButton {
        public int index; // Индекс закладки в массиве закладок.
        public Component comp; // Содержимое закладки.

        /**
         * Конструктор.
         * @param index Индекс.
         * @param title Отображаемое название.
         * @param comp Содержимое.
         */
        public Item(int index, String title, Component comp) {
            super(title, false);
            this.index = index;
            this.comp = comp;
            addActionListener(TabPane.this);
        }
    }

    
    /**
     * Для проверки - демо.
     * @param args 
     */
    public static void main(String[] args) {

        JFrame frm = new JFrame();
        frm.setLayout(new BorderLayout());
        frm.setPreferredSize(new Dimension(500,500));
        frm.setPreferredSize(new Dimension(500,500));
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        TabPane tab = new TabPane();

        JPanel ptabSource = new JPanel(new MigLayout());
        ptabSource.setBackground(Color.red);
        ptabSource.add(new JLabel("Источник"));
        ptabSource.add(new JTextField(50), "growx");
        ptabSource.add(new JButton("Выбор"), "wrap");
        JPanel panelSrcInfo = new JPanel(new MigLayout());
        
        panelSrcInfo.add(new JLabel("Тип:"));
        panelSrcInfo.add(new JTextField("не определён"));
        panelSrcInfo.add(new JLabel("Камера:"));
        panelSrcInfo.add(new JComboBox(), "wrap");
        ptabSource.add(panelSrcInfo, "span, grow");
        
        tab.addTab("Process", ptabSource);

        JPanel ptabState = new JPanel(new MigLayout());
        tab.addTab("State", ptabState);
        tab.setEnable(ptabState, false);
                
        JPanel ptabLog = new JPanel(new MigLayout());
        tab.addTab("Log", ptabState);
        
        frm.add(tab, BorderLayout.CENTER);
        frm.pack();
        
        frm.setVisible(true);
    }    
}
