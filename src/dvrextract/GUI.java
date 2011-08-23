package dvrextract;

import java.awt.Color;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Общие настройки граф.интерфейса.
 * @author lex
 */
public class GUI {

    ////////////////////////////////////////////////////////////////////////////
    // ЦВЕТ
    ////////////////////////////////////////////////////////////////////////////
    // Цвет тёмных панелей
    public static Color colorDarkPanel = new Color(0xE0E0E0);
    // Цвет фона дорожки при отсутствии данных.
    public static Color colorNoTrack = new Color(0xD0D0D0); //
    // Цвет фона полей ввода при наличии значений.
    public static Color colorValue = new Color(0xFFFFFF);
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

    ////////////////////////////////////////////////////////////////////////////
    // Бордюры
    ////////////////////////////////////////////////////////////////////////////
    // Бордюр текстовых полей
    public static Border borderTextField = new CompoundBorder(
            new LineBorder(new Color(0xC0C0C0)),
            new EmptyBorder(3, 3, 3, 3));
    // Бордюр подсказок
    public static Border borderToolTip = new CompoundBorder(
            new LineBorder(colorToolTipBorder),
            new EmptyBorder(2, 2, 2, 2));

    public static JLabel createLabel(String title, Color color) {
        JLabel label = new JLabel(title);
        label.setForeground(color);
        return label;
    }

    public static JTextField createText(String title, int size) {
        JTextField text = new JTextField(title, size);
        text.setBorder(borderTextField);
        return text;
    }

    public static JTextField createText(int size) {
        return createText("", size);
    }

    public static JCheckBox createCheck(String title, boolean state) {
        JCheckBox check = new JCheckBox(title, state);
        check.setBackground(colorDarkPanel);
        return check;
    }
}
