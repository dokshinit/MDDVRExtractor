package dvrextract.gui;

import dvrextract.Err;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.MaskFormatter;

/**
 * Общие настройки граф.интерфейса.
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class GUI {

    ////////////////////////////////////////////////////////////////////////////
    // ЦВЕТ
    ////////////////////////////////////////////////////////////////////////////
    // Цвет фона панелей.
    public static Color bgPanel = new Color(0xEEEEEE);
    // Цвет текста панелей.
    public static Color fgPanel = new Color(0x000000);
    // Цвет фона кнопок.
    public static Color bgButton = new Color(0xF8F8F8);
    // Цвет текста кнопок.
    public static Color fgButton = new Color(0x000000);
    // Цвет фона полей ввода при наличии значений.
    public static Color bgEdit = new Color(0xFFFFFF);
    // Цвет текста полей ввода при наличии значений.
    public static Color fgEdit = new Color(0x000000);
    // Цвет тёмных панелей
    public static Color bgPanelDark = new Color(0xE0E0E0);
    // Цвет фона дорожки при отсутствии данных.
    public static Color colorNoTrack = new Color(0xD0D0D0);
    // Цвет фона полей ввода при отсутствии значения или ошибке.
    public static Color colorWrongValue = new Color(0xFFE0E0);
    // Цвет текстовых надписей.
    public static Color colorTextFg = new Color(0x404080);
    // Цвет фона подсказок
    public static Color colorToolTipBg = new Color(0x7070A0);
    // Цвет текста подсказок
    public static Color colorToolTipFg = new Color(0xFFFFFF);
    // Цвет текста подсказок
    public static Color colorToolTipBorder = new Color(0x505080);
    // Цвет фона примечаний.
    public static Color bgNoteLabel = new Color(0xF8F8FF);
    // Цвет текста примечаний.
    public static Color fgNoteLabel = new Color(0x6060F0);
    //
    ////////////////////////////////////////////////////////////////////////////
    // Бордюры
    ////////////////////////////////////////////////////////////////////////////
    // Окантовка текстовых полей.
    public static Border borderTextField = new CompoundBorder(
            new LineBorder(new Color(0xC0C0C0)),
            new EmptyBorder(3, 3, 3, 3));
    // Окантовка подсказок.
    public static Border borderToolTip = new CompoundBorder(
            new LineBorder(colorToolTipBorder),
            new EmptyBorder(2, 2, 2, 2));
    // Окантовка примечаний.
    public static CompoundBorder borderNoteLabel = new CompoundBorder(
            new LineBorder(new Color(0xE0E0F0)),
            new EmptyBorder(1, 1, 1, 1));

    /**
     * Создание конфигурированной текстовой метки.
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
     * @param title Текст.
     * @return Текстовая метка.
     */
    public static JLabel createLabel(String title) {
        JLabel label = new JLabel(title);
        return label;
    }

    /**
     * Создание конфигурированной текстовой метки-примечания.
     * @param title Текст примечания.
     * @return Текстовая метка-примечание.
     */
    public static JLabel createNoteLabel(String title) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><table><td valign='top'><font color=#4040F0>\u2794</font></td><td valign='top'>");
        sb.append(title);
        sb.append("</td></table></html>");
        JLabel label = new JLabel(sb.toString());
        label.setBorder(borderNoteLabel);
        label.setBackground(bgNoteLabel);
        label.setForeground(fgNoteLabel);
        label.setOpaque(true);
        return label;
    }

    /**
     * Создание конфигурированного форматированного поля ввода.
     * @param format Формат для ввода.
     * @return Поле ввода.
     */
    public static JFormattedTextField createFormattedText(MaskFormatter format) {
        JFormattedTextField text = new JFormattedTextField(format);
        text.setBorder(borderTextField);
        text.setBackground(bgEdit);
        text.setForeground(fgEdit);
        return text;
    }

    /**
     * Создание конфигурированного поля ввода даты-время.
     * @return Поле ввода.
     */
    public static JDateTimeField createDTText() {
        JDateTimeField text = new JDateTimeField();
        text.setBorder(borderTextField);
        text.setBackground(bgEdit);
        text.setForeground(fgEdit);
        return text;
    }

    /**
     * Создание конфигурированного поля ввода.
     * @param title Начальный текст.
     * @param size Пердпочтительный размер текста (в символах), для отображения.
     * @return Поле ввода.
     */
    public static JTextField createText(String title, int size) {
        JTextField text = new JTextField(title, size);
        text.setBorder(borderTextField);
        text.setBackground(bgEdit);
        text.setForeground(fgEdit);
        return text;
    }

    /**
     * Создание конфигурированного поля ввода.
     * @param size Пердпочтительный размер текста (в символах), для отображения.
     * @return Поле ввода.
     */
    public static JTextField createText(int size) {
        return createText("", size);
    }

    /**
     * Создание конфигурированного чекбокса.
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
     * @param title Текст на кнопке.
     * @return Кнопка.
     */
    public static JButton createButton(String title) {
        JButton b = new JButton(title);
        b.setForeground(fgButton);
        return b;
    }

    /**
     * Создание конфигурированной кнопки с состоянием.
     * @param title Текст на кнопке.
     * @return Кнопка с состоянием.
     */
    public static JToggleButton createToggleButton(String title) {
        JToggleButton b = new JToggleButton(title);
        b.setForeground(fgButton);
        return b;
    }

    /**
     * Создание конфигурированного комбобокса.
     * @return Комбобокс.
     */
    public static JExtComboBox createCombo() {
        JExtComboBox b = new JExtComboBox();
        return b;
    }

    /**
     * Центрирует компонент относительно экрана.
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
     * выполнения.
     * Если в потоке Swing - выполняется сразу вызовом метода.
     * Если не в потоке Swing - ставится на выполнение в очередь и ожидает завершения.
     * @param obj Задание.
     */
    public static void InSwingWait(Runnable obj) {
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                obj.run();
            } else {
                java.awt.EventQueue.invokeAndWait(obj);
            }
        } catch (Exception ex) {
            Err.log(ex);
        }
    }

    /**
     * Гарантированный запуск задания в потоке Swing без ожидания завершения 
     * выполнения.
     * Если в потоке Swing - выполняется сразу вызовом метода.
     * Если не в потоке Swing - ставится на выполнение в очередь и выходит.
     * @param obj Задание.
     */
    public static void InSwingLater(Runnable obj) {
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                obj.run();
            } else {
                java.awt.EventQueue.invokeLater(obj);
            }
        } catch (Exception ex) {
            Err.log(ex);
        }
    }
}
