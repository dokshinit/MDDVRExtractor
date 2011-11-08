package dvrextract.gui;

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
    public static Color bgNoteLabel = new Color(0xF8F8FF);
    //public static Color bgNoteLabel = new Color(0xD0C4FF);
    // Цвет текста примечаний.
    public static Color fgNoteLabel = new Color(0x6060F0);
    //public static Color fgNoteLabel = new Color(0x002080);
    // Бордюр примечаний.
    public static CompoundBorder borderNoteLabel = new CompoundBorder(
            new LineBorder(new Color(0xE0E0F0)),
            new EmptyBorder(1, 1, 1, 1));

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

    // Цвет фона примечаний.
    public static Color bgSectionLabel = new Color(0xD8E0F0);
    //public static Color bgNoteLabel = new Color(0xD0C4FF);
    // Цвет текста примечаний.
    public static Color fgSectionLabel = new Color(0x0030F0);
    //public static Color fgNoteLabel = new Color(0x002080);
    // Бордюр примечаний.
    public static CompoundBorder borderSectionLabel = new CompoundBorder(
            new LineBorder(new Color(0xF0F0F0)),
            new EmptyBorder(5, 5, 5, 5));

    public static JLabel createSectionLabel(String title) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append(title);
        sb.append("</html>");
        JLabel label = new JLabel(sb.toString());
        label.setBorder(borderSectionLabel);
        label.setBackground(bgSectionLabel);
        label.setForeground(fgSectionLabel);
        label.setOpaque(true);
        return label;
    }

    public static JFormattedTextField createFormattedText(MaskFormatter format) {
        JFormattedTextField text = new JFormattedTextField(format);
        text.setBorder(borderTextField);
        text.setBackground(bgEdit);
        text.setForeground(fgEdit);
        return text;
    }

    public static JDateTimeField createDTText() {
        JDateTimeField text = new JDateTimeField();
        text.setBorder(borderTextField);
        text.setBackground(bgEdit);
        text.setForeground(fgEdit);
        return text;
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
    
    public static void centerizeFrame(Component frame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(new Point(
                (screenSize.width - frame.getWidth()) / 2,
                (screenSize.height - frame.getHeight()) / 2));
    }
    
    public static void centerizeFrame(Component frame, Component parent) {
        Point loc = parent.getLocationOnScreen();
        Dimension size = parent.getSize();
        frame.setLocation(new Point(
                loc.x + size.width / 2 - (frame.getWidth()) / 2,
                loc.y + size.height / 2 - (frame.getHeight()) / 2));
    }
    
}
