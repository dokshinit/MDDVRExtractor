package dvrextract.gui;

import dvrextract.gui.JExtComboBox;
import java.awt.Color;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicBorders.MarginBorder;

/**
 * Общие настройки граф.интерфейса.
 * @author lex
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
    public static Color colorNoTrack = new Color(0xD0D0D0); //
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

    public static JLabel createLabel(String title) {
        JLabel label = new JLabel(title);
        return label;
    }
    
    // Цвет фона примечаний.
    //public static Color bgNoteLabel = new Color(0xFFFFF0);
    public static Color bgNoteLabel = new Color(0xD0C4FF);
    // Цвет текста примечаний.
    //public static Color fgNoteLabel = new Color(0x4060D0);
    public static Color fgNoteLabel = new Color(0x002080);
    // Бордюр примечаний.
    public static CompoundBorder borderNoteLabel = new CompoundBorder(
            new LineBorder(new Color(0x8080FF)),
            new EmptyBorder(3, 3, 3, 3));

    public static JLabel createNoteLabel(String title) {
        JLabel label = new JLabel(title);
        label.setBorder(borderNoteLabel);
        label.setBackground(bgNoteLabel);
        label.setForeground(fgNoteLabel);
        label.setOpaque(true);
        return label;
    }

    public static JTextField createText(String title, int size) {
        JTextField text = new JTextField(title, size);
        text.setBorder(borderTextField);
        text.setBackground(bgEdit);
        text.setForeground(fgEdit);
        return text;
    }

    public static JTextField createText(int size) {
        return createText("", size);
    }

    public static JCheckBox createCheck(String title, boolean state) {
        JCheckBox check = new JCheckBox(title, state);
        //check.setBackground(bgPanel);
        return check;
    }

    public static CompoundBorder borderButton = new CompoundBorder(
            new LineBorder(new Color(0xA0A0B0)),
            new EmptyBorder(4, 12, 4, 12));

    public static JButton createButton(String title) {
        JButton b = new JButton(title);
        //b.setBackground(bgButton);
        b.setForeground(fgButton);
        //b.setBorder(borderButton);
        return b;
    }

    public static JToggleButton createToggleButton(String title) {
        JToggleButton b = new JToggleButton(title);
        //b.setBackground(bgButton);
        b.setForeground(fgButton);
        //b.setBorder(borderButton);
        return b;
    }

    public static JExtComboBox createCombo(boolean isAll) {
        JExtComboBox b = new JExtComboBox();
        configCombo(b);
        return b;
    }
    
    public static void configCombo(JComboBox combo) {
        //combo.setBackground(bgButton);
        //combo.setBorder(new LineBorder(new Color(0x0)));
    }
}
