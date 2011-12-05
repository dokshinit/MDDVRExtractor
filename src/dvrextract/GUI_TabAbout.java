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
    /**
     * Скролл для всей закладки (добавляется в панель - единственный компонет в ней).
     */
    private JScrollPane scroll;
    /**
     * Панель скролируемая только вертикально (добавляется в скролл).
     */
    private JVScrolledPanel panel;

    /**
     * Конструктор.
     */
    public GUI_TabAbout() {
    }

    /**
     * Добавляет панель группы и возвращает ссылку на панель контента группы.
     * @param title Название группы.
     * @param bodyConditions Дополнительные параметры геометрии панели контента.
     * @return Ссылка на панель контента группы.
     */
    private JPanel addPanel(String title, String bodyConditions) {
        JPanel p = new JPanel(new MigLayout("ins 0", "grow", "[]5[]"));
        p.setBackground(groupBackground);

        JPanel pT = new JPanel(new MigLayout("ins 3", "grow"));
        JLabel l;
        pT.add(l = GUI.createLabel("<html><font color=#4040BF style='font-size: 16pt; font-weight: bold'>" + title + "</font></html>"), "center");

        pT.setBorder(groupTitleBorder);
        pT.setBackground(groupTitleBackground);

        JPanel pB = new JPanel(new MigLayout("ins 0", bodyConditions));
        pB.setBackground(groupBackground);

        p.add(pT, "growx, wrap");
        p.add(pB, "gapleft 15, gapright 15, gapbottom 5, growx");
        panel.add(p, "grow, wrap");
        return pB;
    }

    /**
     * Добавляет на панель парный текстовый элемент - заголовок + текст.
     * @param panel Панель.
     * @param head Заголовок.
     * @param text Текст.
     */
    private void addLine(JPanel panel, String head, String text) {
        panel.add(GUI.createLabel("<html><b>" + head + "</b></html>"), "top");
        panel.add(GUI.createLabel("<html>" + text + "</html>"), "top, wrap");
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

        //setBackground(new Color(0xF0F0F0));
        setLayout(new MigLayout("ins 5, fill"));

        panel = new JVScrolledPanel(new MigLayout("ins 20, gap 10", "grow"));
        panel.setOpaque(false);
        scroll = new JScrollPane(panel);
        scroll.getViewport().setBackground(scrollBackground);
        add(scroll, "grow");
        //scroll.setBorder(new LineBorder(new Color(0xA0A0C0)));

        JPanel p1 = addPanel("О программе", "[right][]");
        addLine(p1, "Назначение", "Конвертация/извлечение данных видеонаблюдения регистраторов Microdigital.");
        addLine(p1, "Версия", App.version + " от " +App.versionDate);
        addLine(p1, "Автор", "Докшин Алексей (все права принадлежат автору)");
        addLine(p1, "Контакты", "dant.it@gmail.com");
        addLine(p1, "Лицензия", "Ограниченная ознакомительная версия для ООО \"ЭМ ДИ РУС\"");

        JPanel p2 = addPanel("Системные требования", "[10:10:10, right][]");
        addLine(p2, "*", "Для запуска программы необходим Java SE (JRE/JDK) v1.6.x.");
        p2.add(GUI.createLabel("Страница загрузки:"), "skip, spanx, split 2, left, bottom");
        p2.add(new Link("Oracle Java SE (JRE/JDK)", "http://www.oracle.com/technetwork/java/javase/downloads/index.html"), "wrap");
        addLine(p2, "*", "Для обработки и транскодирования требуется ffmpeg v0.8.x (при более ранних версиях возможно будет недоступно сохранение аудио и внедрение аудио/субтитров в видео). ");
        p2.add(GUI.createLabel("Страница загрузки:"), "skip, spanx, split 3, left, bottom");
        p2.add(new Link("Linux", "http://ffmpeg.org/download.html"), "bottom");
        p2.add(new Link("Windows", "http://ffmpeg.zeranoe.com/builds/"), "bottom, wrap");
        addLine(p2, "*", "Детальная информация по установке, настройкам и работе содержится в файле <b>readme.txt</b>. Также освещён процесс извлечения данных с HDD регистратора.");

        JPanel p3 = addPanel("Возможности", "[10:10:10, right][]");
        addLine(p3, "<b>Исходные данные:</b>");
        addLine(p3, "*", "Файлы архивов выгружаемые видеорегистратором (*.exe).");
        addLine(p3, "*", "Файлы файловой системы видеорегистратора (da#####).");
        addLine(p3, "<b>Обработка:</b>");
        addLine(p3, "*", "Выбор в качестве источника файла или каталога (при выборе каталога сканируются и принимаются к обработке все файлы каталога и вложенных подкаталогов подходящих форматов).");
        addLine(p3, "*", "Выбор в качестве источника каталога. При этом сканируются и принимаются к обработке все файлы каталога и вложенных подкаталогов, которые являются источниками. Обработка в порядке возрастания времени первого кадра файла.");
        addLine(p3, "*", "Выбор диапазона времени для сохраняемых данных. Диапазон может покрывать как часть файла, так и несколько файлов - на выходе будет один файл.");
        addLine(p3, "*", "Сохранение видео/аудио без перекодирования (аудио декодируется в PCM в любом случае!).");
        addLine(p3, "*", "Сохранение видео/аудио с перекодировкой в выбранный формат.");
        addLine(p3, "*", "Сохранение информации о дате и времени в субтитрах.");
        addLine(p3, "*", "Сохранение аудио/видео/субтитров в отдельные файлы.");
        addLine(p3, "*", "Сохранение аудио и субтитров в файл видео.");
        addLine(p3, "<b>Восстановление:</b>");
        addLine(p3, "*", "При повреждении файла битые кадры исключаются из ряда, возможно появление артефактов.");
        addLine(p3, "<b>В планах:</b>");
        addLine(p3, "*", "Расширенная обработка повреждённых файлов.");

        JPanel p4 = addPanel("Типовый порядок работы", "[20:20:20, left][]");
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
