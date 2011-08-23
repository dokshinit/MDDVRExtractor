/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mctool;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import pincodenew.CryptoNK;
import pincodenew.CryptoTP;

/**
 *
 * @author lex
 */
public final class JTrack {

    public String track;
    public String card;
    public int type;
    public int code;
    public int number;
    /////////////////////////////////////////////////////////////
    JTextField textTrack;
    JTextField textCard;
    JTextField textType;
    JTextField textCode;
    JButton buttonClear;
    JCheckBox checkActive;

    public JTrack(Container owner, int n) {
        track = null;
        card = null;
        type = 0;
        code = -1;
        number = n;

        add(owner);
        refresh();
    }

    private void add(Container owner) {

        // Панель дорожки.
        JPanel p = new JPanel(new MigLayout());
        //p.setBackground(colorPanel);

        p.add(JC.createLabel("<html><b>Дорожка №" + number + "</b></html>", new Color(0x000060)));
        //p.add(createLabel("Данные", colorTextFg));
        p.add(textTrack = JC.createText(30), "growx");
        textTrack.setEditable(false);
        //textTrack.setBackground(new Color(0x808080));
        //textTrack.setForeground(new Color(0xFFFFFF));

        p.add(buttonClear = new JButton("Очистить"), "spany 2, growy, wrap");
        //buttonClear.setToolTipText("Очистка дорожки, пустая дорожка не записывается!");

        p.add(checkActive = new JCheckBox("Вкл", true));
        checkActive.setToolTipText("При выключении данные не обновляются!");

        p.add(JC.createLabel("Номер", JC.colorTextFg), "span 1, split 6");
        p.add(textCard = JC.createText(10), "growx");
        textCard.setEditable(false);

        p.add(JC.createLabel("Тип", JC.colorTextFg));
        p.add(textType = JC.createText(6), "growx");
        textType.setEditable(false);

        p.add(JC.createLabel("Код", JC.colorTextFg));
        p.add(textCode = JC.createText(6), "growx");
        textCode.setEditable(false);

        owner.add(p, number < 3 ? "span, growx, wrap" : "span, growx");

        buttonClear.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                set(null);
            }
        });

    }

    public void refresh() {
        Color color;
        if (track != null) {
            textTrack.setText(track);
            textTrack.setBackground(JC.colorValue);
            color = JC.colorWrongValue;
        } else {
            textTrack.setText("< нет >");
            textTrack.setBackground(JC.colorNoTrack);
            color = JC.colorNoTrack;
        }
        if (card != null) {
            textCard.setText(card);
            textCard.setBackground(JC.colorValue);
        } else {
            textCard.setText("< нет >");
            textCard.setBackground(color);
        }
        if (type > 0) {
            textType.setText(type == 1 ? "НК" : "Т+");
            textType.setBackground(JC.colorValue);
        } else {
            textType.setText("< нет >");
            textType.setBackground(color);
        }
        if (code >= 0) {
            textCode.setText(String.format("%04d", code));
            textCode.setBackground(JC.colorValue);
        } else {
            textCode.setText(code == -1 ? "< нет >" : String.format("%d", code));
            textCode.setBackground(color);
        }
    }

    public void set(String track) {
        this.track = null;
        card = null;
        type = 0;
        code = -1;
        if (track != null && checkActive.isSelected() == true) {
            this.track = track;
            CryptoNK cNK = new CryptoNK();
            CryptoTP cTP = new CryptoTP();
            char[] tr = track.toCharArray();
            if (cTP.verifyTrack(tr) == 0) {
                type = 2;
                code = cTP.decodeTrack(tr);
                card = cTP.getNumber(tr);
            } else if (cNK.verifyTrack(tr) == 0) {
                type = 1;
                code = cNK.decodeTrack(tr);
                card = cNK.getNumber(tr);
            }
        }
        refresh();
    }

    public void set(String vnumber, String vcode, int vtype) {
        char[] tr;
        if (vtype == 1) {
            CryptoNK cNK = new CryptoNK();
            tr = cNK.encodeTrack(vnumber, vcode);
            if (tr != null) {
                set(String.valueOf(tr));
                return;
            }
        } else if (vtype == 2) {
            CryptoTP cTP = new CryptoTP();
            tr = cTP.encodeTrack(vnumber, vcode);
            if (tr != null) {
                set(String.valueOf(tr));
                return;
            }
        }
        set(null);
    }
}
