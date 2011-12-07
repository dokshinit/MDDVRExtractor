/*
 * Copyright (c) 2011, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

import dvrextract.gui.GUI;
import dvrextract.gui.GUIImagePanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import net.miginfocom.swing.MigLayout;
import util.NumberTools;

/**
 * Панель с информацией о выбранном файле-источнике.
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public final class GUIFileInfoPanel extends JPanel {

    /**
     * Скролл для всей инфо-панели (добавляется в панель - единственный компонет в ней).
     */
    private JScrollPane scrollPane;
    /**
     * Панель со всей информацией (добавляется в скролл).
     */
    private JPanel panelInfo;
    /**
     * Панель изображения-стопкадра с камеры.
     */
    private GUIImagePanel panelImage;
    ////////////////////////////////////////////////////////////////////////////
    // Информация о файле
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Полное имя.
     */
    private Info infoName;
    /**
     * Размер в байтах.
     */
    private Info infoSize;
    /**
     * Тип.
     */
    private Info infoType;
    /**
     * Список номеров камер информация с которых содержится в файле.
     */
    private Info infoCams;
    /**
     * Разрешение видео.
     */
    private Info infoResolution;
    /**
     * Частота кадров в секунду.
     */
    private Info infoFPS;
    /**
     * Время в первом кадре файла.
     */
    private Info infoFirstTime;
    /**
     * Время в последнем кадре файла.
     */
    private Info infoLastTime;
    /**
     * Продолжительность видео (оценочная - как разница между концом и началом!).
     */
    private Info infoAmountTime;
    /**
     * Примечание.
     */
    private JLabel labelNote;
    /**
     * Текущая отображаемая информация о файле.
     */
    private FileInfo curInfo;
    /**
     * Форматер для времени выводимого в инфе.
     */
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    /**
     * Текстовые ресурсы для интерфейса.
     */
    public static String x_Cams, x_Duration, x_DurationFormat, x_End,
            x_FirstKeyFrame, x_Freq, x_FreqFormat, x_HintChangeZoom, x_NO,
            x_Name, x_Resolution, x_ResolutionFormat, x_Size, x_Bytes, x_Start, x_Type;

    /**
     * Конструктор.
     */
    public GUIFileInfoPanel() {
        init();
    }

    /**
     * Для удобства добавления строк отображения параметров.
     */
    class Info {

        JLabel label; // Название.
        JTextField text; // Текстовое поле.
    }

    /**
     * Воспомогательный метод для создания добавление метки+поля в инфу.
     * @param title Наименование в метке.
     * @param size Размер поля.
     * @param add Строка для MigLayout или null - если стандартно.
     * @return Сформированное поле.
     */
    private Info addInfo(String title, int size, String add) {
        Info info = new Info();
        panelInfo.add(info.label = GUI.createLabel(title));
        panelInfo.add(info.text = GUI.createText(size), add != null ? add + ",wrap" : "wrap");
        info.text.setEditable(false);
        return info;
    }

    /**
     * Инициализация графических компонентов.
     */
    private void init() {
        setLayout(new BorderLayout());
        //
        panelImage = new GUIImagePanel("<html><center>" + x_NO + "<br></html>");
        JLabel l = panelImage.getLabel();
        l.setFont(new Font(l.getFont().getName(), Font.BOLD, 50));
        l.setForeground(new Color(0xA0A0B0));
        panelImage.setBackground(new Color(0x8080A0));
        setImageSize();
        panelImage.setBorder(new LineBorder(Color.red));
        panelImage.addMouseListener(new ImageMouseAdapter());
        panelImage.setToolTipText(x_FirstKeyFrame);
        panelImage.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        //
        panelInfo = new JPanel(new MigLayout("", "[]10[right][grow,shrink]"));
        panelInfo.add(panelImage, "spany, top");
        //
        infoName = addInfo(x_Name, 30, "growx");
        infoSize = addInfo(x_Size, 15, null);
        infoType = addInfo(x_Type, 10, null);
        infoCams = addInfo(x_Cams, 30, null);
        infoResolution = addInfo(x_Resolution, 12, null);
        infoFPS = addInfo(x_Freq, 10, null);
        infoFirstTime = addInfo(x_Start, 15, null);
        infoLastTime = addInfo(x_End, 15, null);
        infoAmountTime = addInfo(x_Duration, 22, null);
        labelNote = GUI.createNoteLabel(x_HintChangeZoom);
        panelInfo.add(labelNote, "spanx, pushy, bottom");
        scrollPane = new JScrollPane(panelInfo);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Актуализация контента при смене языка отображения.
     */
    public void updateLocale() {
        panelImage.setLabelText("<html><center>" + x_NO + "<br></html>");
        panelImage.setToolTipText(x_FirstKeyFrame);
        //
        infoName.label.setText(x_Name);
        infoSize.label.setText(x_Size);
        infoType.label.setText(x_Type);
        infoCams.label.setText(x_Cams);
        infoResolution.label.setText(x_Resolution);
        infoFPS.label.setText(x_Freq);
        infoFirstTime.label.setText(x_Start);
        infoLastTime.label.setText(x_End);
        infoAmountTime.label.setText(x_Duration);
        labelNote.setText(GUI.buildNoteLabelText(x_HintChangeZoom));
    }

    /**
     * Отображение инфы заданного файла.
     * @param info Инфа о файле.
     */
    public void displayInfo(FileInfo info) {
        if (info == null) {
            infoName.text.setText("");
            infoSize.text.setText("");
            infoType.text.setText("");
            infoCams.text.setText("");
            infoResolution.text.setText("");
            infoFPS.text.setText("");
            infoFirstTime.text.setText("");
            infoLastTime.text.setText("");
            infoAmountTime.text.setText("");
            panelImage.setImage(null);
        } else {
            if (curInfo != info) {
                infoName.text.setText(info.fileName);
                infoSize.text.setText(NumberTools.doubleToFormatString((double) info.fileSize, NumberTools.format0, "", "") + " " + x_Bytes);
                infoType.text.setText(info.fileType.title);
                infoCams.text.setText(info.getCamsToString());
                if (info.frameFirst != null) {
                    Dimension d = info.frameFirst.getResolution();
                    infoResolution.text.setText(String.format(x_ResolutionFormat, d.width, d.height));
                    infoFPS.text.setText(String.format(x_FreqFormat, info.frameFirst.fps));
                    infoFirstTime.text.setText(dateFormat.format(info.frameFirst.time));
                } else {
                    infoResolution.text.setText("");
                    infoFPS.text.setText("");
                    infoFirstTime.text.setText("");
                }
                if (info.frameLast != null) {
                    infoLastTime.text.setText(dateFormat.format(info.frameLast.time));
                } else {
                    infoLastTime.text.setText("");
                }
                if (info.frameFirst != null && info.frameLast != null) {
                    infoAmountTime.text.setText(timeToString(info.frameLast.time.getTime() - info.frameFirst.time.getTime()));
                } else {
                    infoAmountTime.text.setText("");
                }
                panelImage.setImage(FFMpeg.getFirstFrameImage(info, App.Source.getSelectedCam()));
            }
        }
        curInfo = info;
        setImageSize();
    }

    /**
     * Конвертирует время (считая, что это длительность в мсек) в строку.
     * @param period Длительность в мсек.
     * @return Строка вида: * час. * мин. * сек. * мсек.
     */
    private String timeToString(long period) {
        long h = (period) / (3600 * 1000);
        long m = (period - h * 3600 * 1000) / (60 * 1000);
        long s = (period - h * 3600 * 1000 - m * 60 * 1000) / (1000);
        long ms = (period - h * 3600 * 1000 - m * 60 * 1000 - s * 1000);
        return String.format(x_DurationFormat, h, m, s, ms);
    }
    /** 
     * Текущий размер картинки: true - маленький (x=352,y-сжат), false - полный кадр.
     */
    private boolean isSmallView = true;

    /**
     * Актуализация размера отображения стопкадра.
     */
    public void setImageSize() {
        int x = 704, y = 576;
        if (curInfo != null) {
            Dimension d = curInfo.frameFirst.getResolution();
            x = d.width;
            y = d.height;
        }
        if (isSmallView) {
            double z = 352.0 / x;
            x = (int) (z * x);
            y = (int) (z * y);
        }
        Dimension d = new Dimension(x + 2, y + 2);
        panelImage.setPreferredSize(d);
        panelImage.setMinimumSize(d);
        panelImage.setMaximumSize(d);
        panelImage.setPreferredSize(d);
        panelImage.revalidate();
    }

    /**
     * Адаптер для обработка нажатий кнопок мыши на изображении с камеры.
     */
    private class ImageMouseAdapter extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            // Меняем масштаб картинки.
            isSmallView = isSmallView ? false : true;
            setImageSize();
        }
    }
}
