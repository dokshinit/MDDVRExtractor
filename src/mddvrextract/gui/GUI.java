/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract.gui;

import mddvrextract.Err;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.MaskFormatter;

/**
 * Общие настройки граф.интерфейса.
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class GUI {

    ////////////////////////////////////////////////////////////////////////////
    // ЦВЕТ
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Цвета панелей групп вкладки Обработка.
     */
    public static final class Process {

        public static final class Group {

            public static Color fgtitle;
            public static Color gradient1, gradient2;
            public static Color fgcontent, bgcontent;
        }
        public static Color bgscroll;
    }

    /**
     * Цвета панелей групп вкладки Справка.
     */
    public static final class About {

        public static final class Group {

            public static Color fgtitle;
            public static Color gradient1, gradient2;
            public static Color fgcontent, bgcontent;
        }
        public static Color bgscroll;
    }

    /**
     * Цвета и окантовки текста примечаний.
     */
    public static final class Note {

        public static Color fg, bg;
        public static Border border;
    }

    /**
     * Цвета панели предпросмотра первого кадра.
     */
    public static final class Preview {

        public static Color fg, bg;
        public static Color border;
    }

    /**
     * Создание производного от HSB цвета из базы и указанных смещений для его
     * компонент.
     *
     * @param base Базовый цвет.
     * @param dH Изменение цвета.
     * @param dS Изменение насыщенности.
     * @param dB Изменение яркости.
     * @return Результирующий цвет.
     */
    public static Color deriveColorHSB(Color base, float dH, float dS, float dB) {
        float hsb[] = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
        hsb[0] += dH;
        hsb[1] += dS;
        hsb[2] += dB;
        return Color.getHSBColor(
                hsb[0] < 0 ? 0 : (hsb[0] > 1 ? 1 : hsb[0]),
                hsb[1] < 0 ? 0 : (hsb[1] > 1 ? 1 : hsb[1]),
                hsb[2] < 0 ? 0 : (hsb[2] > 1 ? 1 : hsb[2]));

    }

    /**
     * Создание производного цвета с новой прозрачностью.
     *
     * @param base Базовый цвет.
     * @param alpha Значение прозрачности.
     * @return Результирующий цвет.
     */
    public static Color deriveColorAlpha(Color base, int alpha) {
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
    }

    /**
     * Создание изображения заданных размеров и с возможностью попиксельной
     * прозрачности.
     *
     * @param width Ширина.
     * @param height Высота.
     * @return Изображение.
     */
    public static BufferedImage createTranslucentImage(int width, int height) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration conf = ge.getDefaultScreenDevice().getDefaultConfiguration();
        return conf.createCompatibleImage(width, height, Transparency.TRANSLUCENT);

    }

    ////////////////////////////////////////////////////////////////////////////
    // Окантовки
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Инициализация цветовых схем и компонентов интерфейса.
     */
    public static void init() {

        Color c = UIManager.getColor("nimbusBase");
        if (c == null) {
            c = new Color(0x33628C);
        }

        float hsb[] = Color.RGBtoHSB(c.getRed(), c.getGreen(),
                c.getBlue(), null);

        Process.Group.gradient1 = Color.getHSBColor(hsb[0] - .013f, .15f, .85f);
        Process.Group.gradient2 = Color.getHSBColor(hsb[0] - .005f, .24f, .80f);
        Process.Group.fgtitle = Color.getHSBColor(hsb[0], .54f, .40f);
        c = UIManager.getColor("Panel.background");
        if (c == null) {
            c = new Color(0xD6D9DF);
        }
        Process.Group.bgcontent = deriveColorHSB(c, 0, 0, .06f);
        Process.bgscroll = deriveColorHSB(c, 0, 0, -.06f);

        About.Group.gradient1 = Color.getHSBColor(hsb[0] - .013f, .15f, .85f);
        About.Group.gradient2 = Color.getHSBColor(hsb[0] - .005f, .24f, .80f);
        About.Group.fgtitle = Color.getHSBColor(hsb[0], .54f, .40f);
        if (c == null) {
            c = new Color(0xF2F2BD);
        }
        c = UIManager.getColor("info");
        About.Group.bgcontent = deriveColorHSB(c, 0, -.18f, .08f);
        About.bgscroll = deriveColorHSB(c, -0.04f, -.15f, -.02f);

        if (c == null) {
            c = new Color(0x2F5CB4);
        }
        c = UIManager.getColor("nimbusInfoBlue");
        Note.fg = deriveColorHSB(c, 0, .2f, 0f);
        if (c == null) {
            c = new Color(0xF2F2BD);
        }
        c = UIManager.getColor("info");
        Note.bg = deriveColorHSB(c, 0, -.15f, .05f);
        Note.border = new CompoundBorder(
                new LineBorder(deriveColorHSB(c, -0.04f, -.15f, -.1f)),
                new EmptyBorder(1, 1, 1, 1));

        if (c == null) {
            c = new Color(0x33628C);
        }
        c = UIManager.getColor("nimbusBase");
        Preview.fg = deriveColorHSB(c, 0, -.3f, .2f);
        Preview.bg = deriveColorHSB(c, 0, -.3f, 0f);
        Preview.border = Color.WHITE;
    }

    /**
     * Создание конфигурированной текстовой метки.
     *
     * @param title Текст.
     * @param color Цвет.
     * @return Текстовая метка.
     */
    public static JLabel createLabel(String title, Color color) {
        JLabel label = new JLabel(title);
        label.setForeground(color);
        return label;
    }

    /**
     * Создание конфигурированной текстовой метки.
     *
     * @param title Текст.
     * @return Текстовая метка.
     */
    public static JLabel createLabel(String title) {
        JLabel label = new JLabel(title);
        return label;
    }

    /**
     * Построение форматированного текста примечания для указанного сообщения.
     *
     * @param title Текст примечания.
     * @return Форматированный текст примечания.
     */
    public static String buildNoteLabelText(String title) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><table><td valign='top'>\u2794</td><td valign='top'>");
        sb.append(title);
        sb.append("</td></table></html>");
        return sb.toString();
    }

    /**
     * Создание конфигурированной текстовой метки-примечания.
     *
     * @param title Текст примечания.
     * @return Текстовая метка-примечание.
     */
    public static JLabel createNoteLabel(String title) {
        JLabel label = new JLabel(buildNoteLabelText(title));
        label.setBorder(Note.border);
        label.setBackground(Note.bg);
        label.setForeground(Note.fg);
        label.setOpaque(true);
        return label;
    }

    /**
     * Создание конфигурированного форматированного поля ввода.
     *
     * @param format Формат для ввода.
     * @return Поле ввода.
     */
    public static JFormattedTextField createFormattedText(MaskFormatter format) {
        JFormattedTextField text = new JFormattedTextField(format);
        return text;
    }

    /**
     * Создание конфигурированного поля ввода даты-время.
     *
     * @return Поле ввода.
     */
    public static JDateTimeField createDTText() {
        JDateTimeField text = new JDateTimeField();
        return text;
    }

    /**
     * Создание конфигурированного поля ввода.
     *
     * @param title Начальный текст.
     * @param size Пердпочтительный размер текста (в символах), для отображения.
     * @return Поле ввода.
     */
    public static JTextField createText(String title, int size) {
        JTextField text = new JTextField(title, size);
        return text;
    }

    /**
     * Создание конфигурированного поля ввода.
     *
     * @param size Пердпочтительный размер текста (в символах), для отображения.
     * @return Поле ввода.
     */
    public static JTextField createText(int size) {
        return createText("", size);
    }

    /**
     * Создание конфигурированного чекбокса.
     *
     * @param title Текст названия.
     * @param state Начальное состояние (true-выбран).
     * @return Чекбокс,
     */
    public static JCheckBox createCheck(String title, boolean state) {
        JCheckBox check = new JCheckBox(title, state);
        return check;
    }

    /**
     * Создание конфигурированной кнопки.
     *
     * @param title Текст на кнопке.
     * @return Кнопка.
     */
    public static JButton createButton(String title) {
        JButton b = new JButton(title);
        return b;
    }

    /**
     * Создание конфигурированной кнопки с состоянием.
     *
     * @param title Текст на кнопке.
     * @return Кнопка с состоянием.
     */
    public static JToggleButton createToggleButton(String title) {
        JToggleButton b = new JToggleButton(title);
        return b;
    }

    /**
     * Создание конфигурированного комбобокса.
     *
     * @return Комбобокс.
     */
    public static JExtComboBox createCombo() {
        JExtComboBox b = new JExtComboBox();
        return b;
    }

    /**
     * Центрирует компонент относительно экрана.
     *
     * @param frame Центрируемый компонент.
     */
    public static void centerizeFrame(Component frame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(new Point(
                (screenSize.width - frame.getWidth()) / 2,
                (screenSize.height - frame.getHeight()) / 2));
    }

    /**
     * Центрирует компонент относительно другого компонента.
     *
     * @param frame Центрируемый компонент.
     * @param parent Компонент относительно которого производится центровка.
     */
    public static void centerizeFrame(Component frame, Component parent) {
        Point loc = parent.getLocationOnScreen();
        Dimension size = parent.getSize();
        frame.setLocation(new Point(
                loc.x + size.width / 2 - (frame.getWidth()) / 2,
                loc.y + size.height / 2 - (frame.getHeight()) / 2));
    }

    /**
     * Гарантированный запуск задания в потоке Swing с ожиданием завершения
     * выполнения. Если в потоке Swing - выполняется сразу вызовом метода. Если
     * не в потоке Swing - ставится на выполнение в очередь и ожидает
     * завершения.
     *
     * @param obj Задание.
     */
    public static void InSwingWait(final Runnable obj) {
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                obj.run();
            } else {
                SwingUtilities.invokeAndWait(obj);
            }
        } catch (Exception ex) {
            Err.log(ex);
        }
    }

    /**
     * Гарантированный запуск задания в потоке Swing без ожидания завершения
     * выполнения. Если в потоке Swing - выполняется сразу вызовом метода. Если
     * не в потоке Swing - ставится на выполнение в очередь и выходит.
     *
     * @param obj Задание.
     */
    public static void InSwingLater(final Runnable obj) {
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                obj.run();
            } else {
                SwingUtilities.invokeLater(obj);
            }
        } catch (Exception ex) {
            Err.log(ex);
        }
    }
}
