/*
 * Copyright (c) 2011, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract.gui;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 * Панель содержащая изображение.
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class GUIImagePanel extends JPanel {

    /**
     * Изображение.
     */
    private Image image;
    /**
     * Текстовая метка по центру картинки - выводится когда картинки нет.
     */
    private JLabel label;

    /**
     * Конструктор.
     */
    public GUIImagePanel() {
        this(null, null);
    }

    /**
     * Конструктор.
     * @param image Изображение. 
     */
    public GUIImagePanel(Image image) {
        this(image, null);
    }

    /**
     * Конструктор. 
     * @param title Текстовая ссылка.
     */
    public GUIImagePanel(String title) {
        this(null, title);
    }

    /**
     * Конструктор. 
     * @param image Изображение.
     * @param title Текстовая ссылка.
     */
    public GUIImagePanel(Image image, String title) {
        setLayout(new MigLayout("fill", "[center]", "[center]"));
        add(label = new JLabel());
        setImage(image);
        setLabelText(title);
    }

    /**
     * Устанавливает текст надписи.
     * @param title Текст надписи.
     */
    public final void setLabelText(String title) {
        label.setText(title);
    }

    /**
     * Возвращает граф.элемент текстовой надписи.
     * @return Лейбл текстововй надписи.
     */
    public final JLabel getLabel() {
        return label;
    }

    /**
     * Устанавливает изображение панели.
     * @param image Изображение.
     */
    public final void setImage(Image image) {
        this.image = image;
        label.setVisible(image == null);
        validate();
        repaint();
    }

    /**
     * Возвращает изображение панели.
     * @return Изображение.
     */
    public Image getImage() {
        return image;
    }

    /**
     * Отрисовка компонента.
     * @param g Контекст.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Отрисовываем поверх всей панели (маштабируемое) изображение.
        if (image != null) {
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        }
    }
}
