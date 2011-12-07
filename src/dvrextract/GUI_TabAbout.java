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
    public static final String dot = "*";
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
            labelsGr[i] = GUI.createLabel(x_groups[i]);
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
    private JLabel createGroupLabel(String title) {
        return GUI.createLabel("<html><font color=#4040BF style='font-size: 16pt; font-weight: bold'>"
                + title + "</font></html>");
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
        labels[ihead].setText("<html><b>" + x_labels[ihead] + "</b></html>");
        labels[itext].setText("<html>" + x_labels[itext] + "</html>");
        panel.add(labels[ihead], "top");
        panel.add(labels[itext], "top, wrap");
    }

    private void addLine(JPanel panel, int itext) {
        // Корректируем текст
        labels[itext].setText("<html><b>" + x_labels[itext] + "</b></html>");
        panel.add(labels[itext], "spanx, left, top, wrap");
    }

    private void addListLine(JPanel panel, int itext) {
        // Корректируем текст
        labels[itext].setText("<html>" + x_labels[itext] + "</html>");
        panel.add(new JLabel(dot), "top");
        panel.add(labels[itext], "top, wrap");
    }

    /**
     * Добавляет на панель одиночный текстовый элемент.
     * @param panel Панель.
     * @param text Текст.
     */
    private void addLine(JPanel panel, String text) {
        panel.add(GUI.createLabel("<html>" + text + "</html>"), "spanx, left, top, wrap");
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
        addLine(p3, "Исходные данные:");
        addLine(p3, dot, "Файлы архивов выгружаемые видеорегистратором (*.exe).");
        addLine(p3, dot, "Файлы файловой системы видеорегистратора (da#####).");
        addLine(p3, "<b>Обработка:</b>");
        addLine(p3, dot, "Выбор в качестве источника файла или каталога (при выборе каталога сканируются и принимаются к обработке все файлы каталога и вложенных подкаталогов подходящих форматов).");
        addLine(p3, dot, "Выбор в качестве источника каталога. При этом сканируются и принимаются к обработке все файлы каталога и вложенных подкаталогов, которые являются источниками. Обработка в порядке возрастания времени первого кадра файла.");
        addLine(p3, dot, "Выбор диапазона времени для сохраняемых данных. Диапазон может покрывать как часть файла, так и несколько файлов - на выходе будет один файл.");
        addLine(p3, dot, "Сохранение видео/аудио без перекодирования (аудио декодируется в PCM в любом случае!).");
        addLine(p3, dot, "Сохранение видео/аудио с перекодировкой в выбранный формат.");
        addLine(p3, dot, "Сохранение информации о дате и времени в субтитрах.");
        addLine(p3, dot, "Сохранение аудио/видео/субтитров в отдельные файлы.");
        addLine(p3, dot, "Сохранение аудио и субтитров в файл видео.");
        addLine(p3, "<b>Восстановление:</b>");
        addLine(p3, dot, "При повреждении файла битые кадры исключаются из ряда, возможно появление артефактов.");
        addLine(p3, "<b>В планах:</b>");
        addLine(p3, dot, "Расширенная обработка повреждённых файлов.");

        JPanel p4 = addGroupPanel(labelsGr[gn++], "[20:20:20, left][]");
        addLine(p4, "1", "На закладке <b><i>Источник</i></b>:");
        addLine(p4, "1.1", "Выбирается источник после чего происходит его предварительное сканирование.");
        addLine(p4, "1.2", "Выбирается обрабатываемая камера из списка доступных.");
        addLine(p4, "2", "На закладке <b><i>Обработка</i></b>:");
        addLine(p4, "2.1", "Выбирается период сохраняемых данных.");
        addLine(p4, "2.2", "Выбирается файл-приёмник для видео и если нужно - формат кодирования, размер кадра, частоту кадров.");
        addLine(p4, "2.3", "Выбирается режим обработки аудио и если нужно - файл-приёмник, формат кодирования.");
        addLine(p4, "2.4", "Выбирается режим создания субтитров и если нужно - файл-приёмник, формат кодирования.");
        addLine(p4, "3", "Переход на закладку <b><i>Лог</i></b> для контроля (не обязательно).");
        addLine(p4, "4", "Запуск обработки (возможно сделать находясь на любой закладке).");
    }
}
