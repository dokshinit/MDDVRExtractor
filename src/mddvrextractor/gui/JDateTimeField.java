/*
 * Copyright (c) 2011-2013, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextractor.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;

/**
 * Граф.элемент - Поле ввода даты\времени по маске.
 *
 * В случае, если какие-то поля отсутствуют в маске, то выставляются по
 * умолчанию (время в 0, дата в 1).
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class JDateTimeField extends JFormattedTextField {

    /**
     * Форматер для преобразований.
     */
    private SimpleDateFormat formatDt;
    /**
     * Хранение даты\времени (как эквивалент текстового поля).
     */
    private Date time;
    /**
     * Минимальная дата (для ограничения диапазона ввода дат).
     */
    private Date timeMin;
    /**
     * Максимальная дата (для ограничения диапазона ввода дат).
     */
    private Date timeMax;
    /**
     * Индикатор режима блокирования фокуса при неверном вводе.
     */
    private boolean isLockFocus;

    /**
     * Конструктор.
     *
     * @param fmt Строка формата для ввода (если null - по умолчению).
     * @param dt Начальное значение (если null - текущая дата).
     */
    public JDateTimeField(String fmt, Date dt) {
        isLockFocus = false;
        if (!setFormat(fmt)) {
            setFormat(null);
        }
        setInputVerifier(new DateTimeInputVerifier());
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
        getActionMap().put("escape", new EscapeAction());
        timeMin = null;
        timeMax = null;
        setTime(dt);
    }

    /**
     * Конструктор.
     *
     * @param fmt Строка формата даты-времени для ввода.
     */
    public JDateTimeField(String fmt) {
        this(fmt, null);
    }

    /**
     * Конструктор.
     *
     * @param dt Начальное значение даты.
     */
    public JDateTimeField(Date dt) {
        this(null, dt);
    }

    /**
     * Констркутор (все параметры - по умолчанию).
     */
    public JDateTimeField() {
        this(new Date());
    }

    /**
     * Включение\выключение режима блокирующей проверки значения поля. При
     * включении - не даёт сменить фокус пока не введено правильное значение.
     * При отключении - неверные значения исправляются откатом к пред.значению
     * (с проеркой на валидность).
     *
     * @param isOn Режим.
     */
    public void setLockingVerify(boolean isOn) {
        isLockFocus = isOn;
    }

    /**
     * Устанавливает формат вводимых данных (в том числе маску ввода).
     *
     * @param fmt Строка формата для ввода (если null - формат по умолчанию).
     * @return Состояние выполнения true-успешно, false-ошибка.
     */
    public boolean setFormat(String fmt) {
        if (fmt == null || fmt.isEmpty()) {
            fmt = "dd.MM.yyyy HH:mm:ss";
        }
        // Генерируем из формата маску для ввода.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fmt.length(); i++) {
            switch (fmt.charAt(i)) {
                case 'd':
                case 'M':
                case 'y':
                case 'H':
                case 'm':
                case 's':
                case 'S':
                    sb.append('#');
                    break;
                default:
                    sb.append(fmt.charAt(i));
            }
        }
        // В случае, если маска не верна и выбросит исключение - просто 
        // невозможно будет вводить значения.
        try {
            MaskFormatter formatterDt = new MaskFormatter(sb.toString());
            formatterDt.setPlaceholderCharacter('0');
            formatDt = new SimpleDateFormat(fmt);
            DefaultFormatterFactory factory = new DefaultFormatterFactory(formatterDt);
            setFormatterFactory(factory);
            return true;
        } catch (ParseException ex) {
            return false;
        }
    }

    /**
     * Возвращает верную для действующих ограничений дату путём сдвигания
     * указанной даты в действующий диапазон (если необходимо).
     *
     * @param dt Проверяемая на валидность дата.
     * @return Верная дата (если исодная была верной, то она же и возвращается -
     * удобно для проверки на "коррекцию").
     */
    public Date getValidDate(Date dt) {
        if (dt == null) {
            dt = new Date();
        }
        if (timeMin != null && dt.before(timeMin)) {
            dt = timeMin;
        }
        if (timeMax != null && dt.after(timeMax)) {
            dt = timeMax;
        }
        return dt;
    }

    /**
     * Устанавливает значение поля указанной датой. Если дата выходит за
     * диапазон разрешенных, но присвоениея не происходит!
     *
     * @param dt Дата.
     */
    public void setTime(Date dt) {
        setText(formatDt.format(dt));
    }

    /**
     * Возвращает текущую дату поля.
     *
     * @return Текущая дата.
     */
    public Date getTime() {
        return time;
    }

    /**
     * Установка минимальной даты.
     *
     * @param dt Дата.
     */
    public void setMinTime(Date dt) {
        timeMin = dt;
        if (dt != null) {
            if (timeMax != null && timeMax.before(dt)) {
                timeMax = dt;
            }
            if (time != null && time.before(dt)) {
                setTime(dt);
            }
        }
    }

    /**
     * Установка максимальной даты.
     *
     * @param dt Дата.
     */
    public void setMaxTime(Date dt) {
        timeMax = dt;
        if (dt != null) {
            if (timeMin != null && timeMin.after(dt)) {
                timeMin = dt;
            }
            if (time != null && time.after(dt)) {
                setTime(dt);
            }
        }
    }

    /**
     * Установка текста элемента напрямую.
     *
     * Строка парсится согласно формату, полученная дата проверяется на
     * валидность и в элемент ставится её представление. Т.е. в результате может
     * быть совсем не исходной строкой. По формированию даты при ошибках - см.
     * getValidDate(). Нежелательно использование вовне! Необходимо использовать
     * setTime().
     *
     * @param s Строка с датой.
     */
    @Override
    public void setText(String s) {
        Date oldValue = time;
        //System.out.println("setText=" + s);
        Date dt = parse(s);
        if (dt == null) {
            dt = (time != null) ? time : new Date();
        }
        time = getValidDate(dt);
        int n = getCaretPosition();
        super.setText(formatDt.format(time));
        setCaretPosition(Math.min(n, getText().length()));
        if (!time.equals(oldValue)) {
            // Вызываем листенеры с событием изменения значения времени.
            ActionEvent e = new ActionEvent(this, ID_TIMECHANGE, "change_time");
            for (ActionListener a : listenerList.getListeners(ActionListener.class)) {
                a.actionPerformed(e);
            }
        }
    }
    /**
     * ID события изменения значения времени.
     */
    public final static int ID_TIMECHANGE = 1;

    /**
     * Преобразование строки в дату по текущему формату. С проверкой
     * идентичности при обратном преобразовании.
     *
     * @param s Строка с датой по формату.
     * @return Дата, в случае ошибки = null.
     */
    protected Date parse(String s) {
        if (s != null) {
            try {
                // Проверка на реальность даты.
                Date dt = formatDt.parse(s);
                // Проверка на обратное преобразование (если дата распарсена 
                // корректно, то обратное преобразование даст исходную строку).
                if (!s.equals(formatDt.format(dt))) {
                    return null;
                }
                // В случае успешного преобразования - возвращаем дату.
                return dt;
            } catch (ParseException ex) {
            }
        }
        return null;
    }

    /**
     * Верификатор ввода (соответствие формату, полноте ввода и диапазону).
     */
    private class DateTimeInputVerifier extends InputVerifier {

        @Override
        public boolean verify(JComponent input) {
            Date dt = parse(getText());
            Date validdt = getValidDate(dt);
            //System.out.println("Verify! dt=" + formatDt.format(time));
            if (dt == validdt) {
                return true;
            }
            return !isLockFocus;
        }
    }

    /**
     * Действие на нажатие Esc (откат).
     */
    private class EscapeAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent ae) {
            int n = getCaretPosition();
            setTime(time);
            setCaretPosition(n);
        }
    }
}
