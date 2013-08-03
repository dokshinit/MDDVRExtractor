/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 * Клас реализующий альтернативную панель закзадок.
 *
 * Прежде всего для возможности представления закладок любыми элементами
 * (например надписями), с гибким менеджером компоновки и возможностью изменения
 * вида курсора при наведении. Возможно добавление интерактивных компонентов
 * (срабатывающих на нажатие) или статических (без взаимодействия с
 * пользователем).
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class ExtTabPane extends JPanel implements MouseListener {

    /**
     * Панель закладок.
     */
    private JPanel panelBar;
    // Список всех элементов (включая закладки).
    private ArrayList<Item> items;
    // Список элементов-закладок.
    private ArrayList<Item> tabItems;
    //
    private Item selectedTab;
    private boolean isCursorHandle;

    public ExtTabPane() {
        this(null, true);
    }

    public ExtTabPane(MigLayout layout, boolean iscursorhandle) {

        items = new ArrayList<Item>();
        tabItems = new ArrayList<Item>();
        if (layout == null) {
            layout = new MigLayout();
        }
        panelBar = new JPanel(layout);
        setLayout(new BorderLayout());
        add(panelBar, BorderLayout.NORTH);

        selectedTab = null;
        isCursorHandle = iscursorhandle;
    }

    public Item addItem(Object id, Item.Type type, JComponent comp, String cond, JComponent content) {
        Item item = new Item(id, type, comp, cond, content);
        items.add(item);
        if (type == Item.Type.TAB) {
            tabItems.add(item);
        }
        comp.addMouseListener(this);
        if (isCursorHandle && (type == Item.Type.TAB || type == Item.Type.COMP)) {
            comp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        panelBar.add(comp, cond);
        return item;
    }

    /**
     * Возвращает панель заголовка закладок.
     *
     * @return Панель кнопок-закладок.
     */
    public JPanel getBarPanel() {
        return panelBar;
    }

    public Item getSelectedTab() {
        return selectedTab;
    }

    public Item findItemById(Object id) {
        if (items == null) {
            return null;
        }
        for (Item it : items) {
            if (it.getId() == id) {
                return it;
            }
        }
        return null;
    }

    public Item findItemByComponent(Object comp) {
        if (items == null) {
            return null;
        }
        for (Item it : items) {
            if (it.getComponent() == comp) {
                return it;
            }
        }
        return null;
    }

    public Item findItemByContent(Object cont) {
        if (items == null) {
            return null;
        }
        for (Item it : items) {
            if (it.getContent() == cont) {
                return it;
            }
        }
        return null;
    }

    public Item getTabByIndex(int index) {
        return tabItems.get(index);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Item it = findItemByComponent(e.getSource());
        if (it != null) {
            switch (it.getType()) {
                case TAB:
                    try {
                        selectTab(it);
                    } catch (Exception ex) {
                    }
                    break;
                case COMP:
                    fireCompSelect(it);
                    break;
            }
        }
        e.setSource(null); // Зануляем, т.к. на метке с изображением событие генерируется дважды!
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public void selectTab(int id) throws FaultException, VetoException {
        Item it = findItemById(id);
        selectTab(it);
    }

    public void selectTab(Item item) throws FaultException, VetoException {
        if (item == null) {
            throw new FaultException("Item is null!");
        }
        if (item.getType() != Item.Type.TAB) {
            throw new FaultException("Item is not tab!");
        }
        if (item == selectedTab) {
            //throw new FaultException("Item already selected!");
            return;
        }

        // Проверка возможности выбора закладки.
        Item old = selectedTab;
        fireVetoTabSelect(old, item);

        // Обновление текущей выбранной закладки.
        if (selectedTab != null) {
            selectedTab.selected = false;
            repaintItem(selectedTab);
        }
        selectedTab = item;
        // Обновление новой выбранной закладки.
        if (selectedTab != null) {
            selectedTab.selected = true;
            repaintItem(selectedTab);
        }
        // Обработка выбора.
        fireTabSelect(old, item);
    }

    /**
     * Перерисовка элемента при выборе.
     *
     * @param item
     */
    public void repaintItem(Item item) {
    }

    /**
     * Проверка возможности выбора закладки.
     *
     * @param item
     * @throws Exception
     */
    public void fireVetoTabSelect(Item oldtab, Item newtab) throws VetoException {
    }

    /**
     * Обработка выбора закладки.
     *
     * @param item
     */
    public void fireTabSelect(Item oldtab, Item newtab) {
    }

    /**
     * Обработка выбора компонента.
     *
     * @param item
     */
    public void fireCompSelect(Item item) {
    }

    public static class Item {

        public static enum Type {

            TAB, // Закладка.
            COMP, // Активный компонент (не закладка).
            STATIC; // Статический элемент.
        }
        private Object id; // Идентификатор.
        private Type type;
        private boolean selected; // Только для типа TAB.
        private JComponent component; // Компонент-закладка.
        private String conditions;
        private JComponent content; // Компонент-содержимое закладки.

        public Item(Object id, Type type, JComponent component, String conditions, JComponent content) {
            this.id = id;
            this.type = type;
            this.selected = false;
            this.component = component;
            this.conditions = conditions;
            this.content = type == Type.TAB ? content : null;
        }

        public Object getId() {
            return id;
        }

        public Type getType() {
            return type;
        }

        public boolean isSelected() {
            return selected;
        }

        public JComponent getComponent() {
            return component;
        }

        public String getConditions() {
            return conditions;
        }

        public JComponent getContent() {
            return content;
        }
    }
}
