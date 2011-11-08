package dvrextract;

import dvrextract.gui.GUI;
import dvrextract.gui.JVScrolledPanel;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;

/**
 * Вкладка "О программе".
 * @author lex
 */
public final class GUI_TabAbout extends JPanel {

    /**
     * Конструктор.
     */
    public GUI_TabAbout() {
        init();
    }

    private static final TitleBorder titleBorder = new TitleBorder(new Color(0x404080));
    private static final Color panelBg = new Color(0xfffef6);
    private static final Color titleBg = new Color(0xBFCFFF);
    private static final Color scrollBg = new Color(0xe8e5d9);
    
    JPanel addPanel(String s, String pBcond) {
        JPanel p = new JPanel(new MigLayout("ins 0", "grow", "[]5[]"));
        p.setBackground(panelBg);

        JPanel pT = new JPanel(new MigLayout("ins 3", "grow"));
        pT.add(GUI.createLabel("<html><font color=#4040BF style='font-size: 16pt; font-weight: bold'>" + s + "</font></html>"), "center");
        pT.setBorder(titleBorder);
        pT.setBackground(titleBg);

        JPanel pB = new JPanel(new MigLayout("ins 0", pBcond));
        pB.setBackground(panelBg);

        p.add(pT, "growx, wrap");
        p.add(pB, "gapleft 15, gapright 15, gapbottom 5, growx");
        panel.add(p, "grow, wrap");
        return pB;
    }

    void addLine(JPanel p, String s1, String s2) {
        p.add(GUI.createLabel("<html><b>" + s1 + "</b></html>"), "right, top");
        p.add(GUI.createLabel("<html>" + s2 + "</html>"), "left, top, wrap");
    }

    void addLine(JPanel p, String s) {
        p.add(GUI.createLabel("<html>" + s + "</html>"), "spanx, top, wrap");
    }
    JScrollPane scroll;
    JVScrolledPanel panel;

    
    /**
     * Инициализация графических компонент.
     */
    private void init() {

        //setBackground(new Color(0xF0F0F0));
        setLayout(new MigLayout("ins 5, fill"));
        
        panel = new JVScrolledPanel(new MigLayout("ins 20, gap 10", "grow"));
        panel.setOpaque(false);
        scroll = new JScrollPane(panel);
        scroll.getViewport().setBackground(scrollBg);
        add(scroll, "grow");
        //scroll.setBorder(new LineBorder(new Color(0xA0A0C0)));

        JPanel p1 = addPanel("О программе", "");
        addLine(p1, "Версия", "0.9b от 07.11.2011");
        addLine(p1, "Автор", "Докшин Алексей");
        addLine(p1, "Контакты", "dant.it@gmail.com");

        JPanel p2 = addPanel("Назначение", "");
        addLine(p2, "Конвертация/извлечение данных видеонаблюдения регистраторов Microdigital.");

        JPanel p3 = addPanel("Возможности", "[10:10:10][]");
        addLine(p3, "<b>Исходные данные:</b>");
        addLine(p3, "*", "Файлы архивов выгружаемые видеорегистратором (*.exe).");
        addLine(p3, "*", "Файлы файловой системы видеорегистратора (da#####).");
        addLine(p3, "<b>Обработка:</b>");
        addLine(p3, "*", "Выбор в качестве источника файла или каталога (при выборе каталога сканируются и принимаются к обработке все файлы каталога и вложенных подкаталогов подходящих форматов).");
        addLine(p3, "*", "Выбор в качестве источника каталога. При этом сканируются и принимаются к обработке все файлы каталога и вложенных подкаталогов, которые являются источниками. Обработка в порядке возрастания времени первого кадра файла.");
        addLine(p3, "*", "Выбор диапазона времени для сохраняемых данных. Диапазон может покрывать как часть файла, так и несколько файлов - на выходе будет один файл.");
        addLine(p3, "*", "Сохранение видео/аудио без перекодирования.");
        addLine(p3, "*", "Сохранение видео/аудио с перекодировкой в выбранный формат.");
        addLine(p3, "*", "Сохранение информации о дате и времени в субтитрах.");
        addLine(p3, "*", "Сохранение аудио/видео/субтитров в отдельные файлы.");
        addLine(p3, "*", "Сохранение аудио и субтитров в файл видео.");
        addLine(p3, "<b>Восстановление:</b>");
        addLine(p3, "*", "При повреждении файла битые кадры исключаются из ряда, возможно появление артефактов.");
        addLine(p3, "<b>В планах:</b>");
        addLine(p3, "*", "Расширенная обработка повреждённых файлов.");
    }
}
