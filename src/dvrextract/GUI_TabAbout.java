/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

import dvrextract.gui.GUI;
import dvrextract.gui.GroupBorder;
import dvrextract.gui.JVScrolledPanel;
import dvrextract.gui.RoundPanel;
import java.awt.Component;
import java.net.URI;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXHyperlink;

/**
 * Вкладка "О программе".
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public final class GUI_TabAbout extends JPanel {

    /**
     * Панель прокрутки для всей закладки (добавляется в панель - единственный
     * компонет в ней).
     */
    private JScrollPane scroll;
    /**
     * Панель скролируемая только вертикально (добавляется в скролл).
     */
    private JVScrolledPanel panel;
    /**
     * Текстовые ресурсы для интерфейса (названия панелей групп и строки
     * контента).
     */
    public static String[] x_groups, x_labels;
    //
    private JLabel[] labelsGr;
    private JLabel[] labels;
    private Link linkJDK, linkFFMpegWin, linkFFMpegLinux;
    private GroupPanel[] gp;

    /**
     * Конструктор.
     */
    public GUI_TabAbout() {
        gp = new GroupPanel[x_groups.length];
        labelsGr = new JLabel[x_groups.length];
        for (int i = 0; i < x_groups.length; i++) {
            labelsGr[i] = GUI.createLabel(x_groups[i]);
        }
        labels = new JLabel[x_labels.length];
        for (int i = 0; i < x_labels.length; i++) {
            labels[i] = GUI.createLabel(x_labels[i]);
        }
        linkJDK = new Link("Oracle Java SE (JRE/JDK)",
                "http://www.oracle.com/technetwork/java/javase/downloads/index.html");
        linkFFMpegWin = new Link("Linux", "http://ffmpeg.org/download.html");
        linkFFMpegLinux = new Link("Windows", "http://ffmpeg.zeranoe.com/builds/");
    }
    GroupPanel gpVideo, gpAudio, gpSub, gpSource;

    /**
     * Панель группы элементов.
     */
    private class GroupPanel extends RoundPanel {

        /**
         * Рамка окна.
         */
        GroupBorder border;
        /**
         * Панель контента.
         */
        JPanel content;

        /**
         * Конструктор.
         *
         * @param title Название панели.
         * @param cond Доп.уловия на столбцы для менеджера раскладки для панели
         * контента.
         */
        public GroupPanel(String title, String cond) {
            super(new MigLayout("ins 3", "grow", ""), 16);

            setBorder(border = new GroupBorder(title, true,
                    GUI.About.Group.gradient1, GUI.About.Group.gradient2));

            setForeground(GUI.About.Group.fgtitle);
            setBackground(GUI.About.Group.bgcontent);

            content = new JPanel(new MigLayout("", cond));
            content.setForeground(GUI.About.Group.fgcontent);
            content.setBackground(GUI.About.Group.bgcontent);
            super.add(content, "gapleft 10, gapright 10, growx");
        }

        @Override
        public Component add(Component comp) {
            return content.add(comp);
        }

        @Override
        public void add(Component comp, Object constraints) {
            content.add(comp, constraints);
        }

        /**
         * Добавляет на панель парный текстовый элемент - заголовок + текст.
         *
         * @param panel Панель.
         * @param head Заголовок.
         * @param text Текст.
         */
        private void addLine(int ihead, int itext) {
            // Корректируем текст
            labels[ihead].setText("<html>" + x_labels[ihead] + "</html>");
            labels[itext].setText("<html>" + x_labels[itext] + "</html>");
            content.add(labels[ihead], "top");
            content.add(labels[itext], "top, wrap");
        }

        /**
         * Добавляет на панель текстовый элемент.
         * @param itext Текст.
         */
        private void addLine(int itext) {
            // Корректируем текст
            labels[itext].setText("<html>" + x_labels[itext] + "</html>");
            content.add(labels[itext], "spanx, left, top, wrap");
        }

        /**
         * Добавляет на панель строку списка (спиосок со свездочками).
         * @param itext Текст.
         */
        private void addListLine(int itext) {
            // Корректируем текст
            labels[itext].setText("<html>" + x_labels[itext] + "</html>");
            content.add(new JLabel("<html><b>*</b></html>"), "top");
            content.add(labels[itext], "top, wrap");
        }

        /**
         * Добавляет на панель строку нумерованного списка.
         * @param snum Текст-номер.
         * @param itext Текст.
         */
        private void addNumListLine(String snum, int itext) {
            // Корректируем текст
            labels[itext].setText("<html>" + x_labels[itext] + "</html>");
            content.add(new JLabel(snum), "top");
            content.add(labels[itext], "top, wrap");
        }
    }

    /**
     * Добавление панели группы на подложку.
     *
     * @param title Метка названия.
     * @return Панель-тело для наполнения группы.
     */
    private GroupPanel addGroupPanel(int ind, String cond) {
        GroupPanel group = new GroupPanel(x_groups[ind], cond);
        panel.add(group, "spanx, grow, gapbottom 5, wrap");
        return group;

    }

    /**
     * Расширение ссылки.
     */
    private class Link extends JXHyperlink {

        /**
         * Конструктор.
         *
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
        scroll.getViewport().setBackground(GUI.About.bgscroll);
        add(scroll, "grow");

        int n = 0, gn = 0;
        gp[0] = addGroupPanel(gn++, "[right][]");
        for (int i = 0; i < 5; i++) {
            gp[0].addLine(n++, n++);
        }

        gp[1] = addGroupPanel(gn++, "[10:10:10, right][]");
        gp[1].addListLine(n++);
        gp[1].add(labels[n++], "skip, spanx, split 2, left, bottom");
        gp[1].add(linkJDK, "wrap");
        gp[1].addListLine(n++);
        gp[1].add(labels[n++], "skip, spanx, split 3, left, bottom");
        gp[1].add(linkFFMpegWin, "bottom");
        gp[1].add(linkFFMpegLinux, "bottom, wrap");
        gp[1].addListLine(n++);

        gp[2] = addGroupPanel(gn++, "[10:10:10, right][]");
        gp[2].addLine(n++);
        for (int i = 0; i < 2; i++) {
            gp[2].addListLine(n++);
        }
        gp[2].addLine(n++);
        for (int i = 0; i < 8; i++) {
            gp[2].addListLine(n++);
        }
        gp[2].addLine(n++);
        gp[2].addListLine(n++);
        gp[2].addLine(n++);
        gp[2].addListLine(n++);

        gp[3] = addGroupPanel(gn++, "[25:25:25, left][]");
        gp[3].addNumListLine("1", n++);
        gp[3].addNumListLine("1.1", n++);
        gp[3].addNumListLine("1.2", n++);
        gp[3].addNumListLine("2", n++);
        gp[3].addNumListLine("2.1", n++);
        gp[3].addNumListLine("2.2", n++);
        gp[3].addNumListLine("2.3", n++);
        gp[3].addNumListLine("2.4", n++);
        gp[3].addNumListLine("3", n++);
        gp[3].addNumListLine("4", n++);
    }

    /**
     * Актуализация контента при смене языка отображения.
     */
    public void updateLocale() {
        for (int i = 0; i < labelsGr.length; i++) {
            gp[i].border.setTitle(x_groups[i]);
        }
        for (int i = 0; i < labels.length; i++) {
            labels[i].setText("<html>" + x_labels[i] + "</html>");
        }
    }
}
