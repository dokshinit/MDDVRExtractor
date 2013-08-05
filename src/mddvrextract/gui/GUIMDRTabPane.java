/*
 * Copyright (c) 2011-2013, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import mddvrextract.App;
import mddvrextract.I18n;
import mddvrextract.I18n.Lang;
import mddvrextract.Resources;
import mddvrextract.gui.ExtTabPane.Item;
import net.miginfocom.swing.MigLayout;

/**
 * Стилизованная панель закладок MDR.
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public final class GUIMDRTabPane extends ExtTabPane {

    public GUIMDRTabPane() {
        super(new MigLayout("fill, ins 0, gap 0", "[][][][][]push[center]10[center]20", "[]"), true);
        getBarPanel().setBackground(GUI.c_Base);
    }

    public JLabel addTab(String title, JComponent content) {
        JLabel l = new JLabel("  " + title + "  ");
        l.setForeground(Color.WHITE);
        l.setBackground(GUI.c_BaseDark);
        l.setOpaque(false);
        l.setVerticalAlignment(JLabel.CENTER);
        l.setHorizontalAlignment(JLabel.CENTER);
        Font f = Resources.GUI.fontBold;//l.getFont();
        l.setFont(f.deriveFont(14.0f)); //f.getSize2D()*1.2f));
        Item it = addItem(content, Item.Type.TAB, l, "sg, grow", content);
        if (getSelectedTab() == null) {
            try {
                selectTab(it);
            } catch (Exception ex) {
            }
        }
        return l;
    }

    public JLabel addFlag(Lang id, ImageIcon image) {
        JLabel l = new JLabel(image);
        l.addMouseListener(this);
        addItem(id, Item.Type.COMP, l, "", null);
        return l;
    }

    /**
     * Установка названия для вкладки.
     *
     * @param index Номер вкладки (0..n-1).
     * @param title Название вкладки.
     */
    public void setTitleAt(Object id, String title) {
        Item it = findItemById(id);
        if (it != null) {
            ((JLabel) it.getComponent()).setText("  " + title + "  ");
        }
    }

    /**
     * Включение/выключение компонента.
     *
     * @param content Содержимое закладки.
     * @param state Устанавливаемый режим доступности.
     */
    public void setEnableAt(Component content, boolean state) {
        Item it = findItemByContent(content);
        if (it != null && it.getType() != Item.Type.STATIC) {
            it.getComponent().setEnabled(state);
        }
    }

    @Override
    public void repaintItem(Item item) {
        if (item != null) {
            item.getComponent().setOpaque(item.isSelected());
            item.getComponent().repaint();
        }
    }

    @Override
    public void fireTabSelect(Item oldtab, Item newtab) {
        if (oldtab != null) {
            remove(oldtab.getContent());
        }
        if (newtab != null) {
            add(newtab.getContent(), BorderLayout.CENTER);
        }
        // Перерисовываем канву.
        validate();
        repaint();
    }

    @Override
    public void fireCompSelect(Item item) {
        I18n.init((Lang) item.getId());
        App.gui.updateLocale();
    }
}