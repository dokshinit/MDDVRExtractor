/*
 * Copyright (c) 2011, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

import dvrextract.gui.TitleBorder;
import dvrextract.gui.GUI;
import dvrextract.gui.JVScrolledPanel;
import java.awt.Color;
import java.net.URI;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXHyperlink;

/**
 * Вкладка "О программе".
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public final class GUI_TabAbout extends JPanel {

    /**
     * Окантовка панели названия группы.
     */
    private static final TitleBorder groupTitleBorder = new TitleBorder(new Color(0x404080));
    /**
     * Цвет фона панели группы.
     */
    private static final Color groupBackground = new Color(0xfffef6);
    /**
     * Цвет фона панели названия группы.
     */
    private static final Color groupTitleBackground = new Color(0xBFCFFF);
    /**
     * Цвет фона подложки.
     */
    private static final Color scrollBackground = new Color(0xe8e5d9);
    public static final String dot = "<html><b>*</b></html>";
    /**
     * Скролл для всей закладки (добавляется в панель - единственный компонет в ней).
     */
    private JScrollPane scroll;
    /**
     * Панель скролируемая только вертикально (добавляется в скролл).
     */
    private JVScrolledPanel panel;
    //
    public static String[] x_groups;
    public static String[] x_labels;
    private JLabel[] labelsGr;
    private JLabel[] labels;
    private Link linkJDK, linkFFMpegWin, linkFFMpegLinux;

    /**
     * Конструктор.
     */
    public GUI_TabAbout() {
        labelsGr = new JLabel[x_groups.length];
        for (int i = 0; i < x_groups.length; i++) {
            labelsGr[i] = GUI.createLabel(buildGroupText(x_groups[i]));
        }
        labels = new JLabel[x_labels.length];
        for (int i = 0; i < x_labels.length; i++) {
            labels[i] = GUI.createLabel(x_labels[i]);
        }
        linkJDK = new Link("Oracle Java SE (JRE/JDK)", "http://www.oracle.com/technetwork/java/javase/downloads/index.html");
        linkFFMpegWin = new Link("Linux", "http://ffmpeg.org/download.html");
        linkFFMpegLinux = new Link("Windows", "http://ffmpeg.zeranoe.com/builds/");
    }

    /**
     * Создание метки панели группы.
     * @param title Текст.
     * @return Метка.
     */
    private String buildGroupText(String title) {
        return "<html><font color=#4040BF style='font-size: 16pt; font-weight: bold'>"
                + title + "</font></html>";
    }

    /**
     * Добавляет панель группы и возвращает ссылку на панель контента группы.
     * @param title Название группы.
     * @param bodyConditions Дополнительные параметры геометрии панели контента.
     * @return Ссылка на панель контента группы.
     */
    private JPanel addGroupPanel(int ititle, String bodyConditions) {
        JPanel group = new JPanel(new MigLayout("ins 0", "grow", "[]5[]"));
        group.setBackground(groupBackground);

        JPanel gtitle = new JPanel(new MigLayout("ins 3", "grow"));
        gtitle.add(labelsGr[ititle], "center");
        gtitle.setBorder(groupTitleBorder);
        gtitle.setBackground(groupTitleBackground);

        JPanel gcontent = new JPanel(new MigLayout("ins 0", bodyConditions));
        gcontent.setBackground(groupBackground);

        group.add(gtitle, "growx, wrap");
        group.add(gcontent, "gapleft 15, gapright 15, gapbottom 5, growx");
        panel.add(group, "grow, wrap");
        return gcontent;
    }

    /**
     * Добавляет на панель парный текстовый элемент - заголовок + текст.
     * @param panel Панель.
     * @param head Заголовок.
     * @param text Текст.
     */
    private void addLine(JPanel panel, int ihead, int itext) {
        // Корректируем текст
        labels[ihead].setText("<html>" + x_labels[ihead] + "</html>");
        labels[itext].setText("<html>" + x_labels[itext] + "</html>");
        panel.add(labels[ihead], "top");
        panel.add(labels[itext], "top, wrap");
    }

    private void addLine(JPanel panel, int itext) {
        // Корректируем текст
        labels[itext].setText("<html>" + x_labels[itext] + "</html>");
        panel.add(labels[itext], "spanx, left, top, wrap");
    }

    private void addListLine(JPanel panel, int itext) {
        // Корректируем текст
        labels[itext].setText("<html>" + x_labels[itext] + "</html>");
        panel.add(new JLabel(dot), "top");
        panel.add(labels[itext], "top, wrap");
    }

    private void addNumListLine(JPanel panel, String snum, int itext) {
        // Корректируем текст
        labels[itext].setText("<html>" + x_labels[itext] + "</html>");
        panel.add(new JLabel(snum), "top");
        panel.add(labels[itext], "top, wrap");
    }

    /**
     * Расширение ссылки.
     */
    private class Link extends JXHyperlink {

        /**
         * Конструктор.
         * @param title Название (как будет выглядеть).
         * @param url Адрес ссылки.
         */
        Link(String title, String url) {
            super();
            setURI(URI.create(url));
            setText(title);
            this.setToolTipText(url);
        }
    }

    /**
     * Инициализация графических компонент.
     */
    public void createUI() {
        setLayout(new MigLayout("ins 5, fill"));

        panel = new JVScrolledPanel(new MigLayout("ins 20, gap 10", "grow"));
        panel.setOpaque(false);
        scroll = new JScrollPane(panel);
        scroll.getViewport().setBackground(scrollBackground);
        add(scroll, "grow");

        int n = 0, gn = 0;
        JPanel p1 = addGroupPanel(gn++, "[right][]");
        for (int i = 0; i < 5; i++) {
            addLine(p1, n++, n++);
        }

        JPanel p2 = addGroupPanel(gn++, "[10:10:10, right][]");
        addListLine(p2, n++);
        p2.add(labels[n++], "skip, spanx, split 2, left, bottom");
        p2.add(linkJDK, "wrap");
        addListLine(p2, n++);
        p2.add(labels[n++], "skip, spanx, split 3, left, bottom");
        p2.add(linkFFMpegWin, "bottom");
        p2.add(linkFFMpegLinux, "bottom, wrap");
        addListLine(p2, n++);

        JPanel p3 = addGroupPanel(gn++, "[10:10:10, right][]");
        addLine(p3, n++);
        for (int i = 0; i < 2; i++) {
            addListLine(p3, n++);
        }
        addLine(p3, n++);
        for (int i = 0; i < 8; i++) {
            addListLine(p3, n++);
        }
        addLine(p3, n++);
        addListLine(p3, n++);
        addLine(p3, n++);
        addListLine(p3, n++);

        JPanel p4 = addGroupPanel(gn++, "[25:25:25, left][]");
        addNumListLine(p4, "1", n++);
        addNumListLine(p4, "1.1", n++);
        addNumListLine(p4, "1.2", n++);
        addNumListLine(p4, "2", n++);
        addNumListLine(p4, "2.1", n++);
        addNumListLine(p4, "2.2", n++);
        addNumListLine(p4, "2.3", n++);
        addNumListLine(p4, "2.4", n++);
        addNumListLine(p4, "3", n++);
        addNumListLine(p4, "4", n++);
    }

    /**
     * Актуализация контента при смене языка отображения.
     */
    public void updateLocale() {
        for (int i = 0; i < labelsGr.length; i++) {
            labelsGr[i].setText(buildGroupText(x_groups[i]));
        }
        for (int i = 0; i < labels.length; i++) {
            labels[i].setText("<html>" + x_labels[i] + "</html>");
        }
    }
}
