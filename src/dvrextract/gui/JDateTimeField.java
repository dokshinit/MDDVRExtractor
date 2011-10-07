package dvrextract.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;

/**
 * Граф.элемент - Поле ввода даты\времени по маске.
 * @author lex
 */
public class JDateTimeField extends JFormattedTextField {

    // Форматер для преобразований.
    private SimpleDateFormat formatDt;
    // Хранение даты\времени (как эквивалент текстового поля).
    private Date time;
    // Минимальная\максимальная дата (для ограничения диапазона ввода дат).
    private Date timeMin, timeMax;

    /**
     * Конструктор.
     * @param fmt Строка формата для ввода (если null - по умолчению).
     * @param dt Начальное значение (если null - текущая дата).
     */
    public JDateTimeField(String fmt, Date dt) {
        setFormat(fmt);
        InputMap im = getInputMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
        ActionMap am = getActionMap();
        am.put("escape", new EscapeAction());
        timeMin = null;
        timeMax = null;
        setTime(dt);
    }

    /**
     * Конструктор.
     * @param fmt Строка формата даты-времени для ввода.
     */
    public JDateTimeField(String fmt) {
        this(fmt, null);
    }

    /**
     * Конструктор.
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
     * Включение\выключение режима блокирующей проверки значения поля.
     * При включении - не даёт сменить фокус пока не введено правильное значение.
     * При отключении - неверные значения исправляются откатом к пред.значению 
     * (с проеркой на валидность).
     * @param isOn Режим.
     */
    public void setLockingVerify(boolean isOn) {
        setInputVerifier(isOn ? new DateTimeInputVerifier() : null);
    }
    
    /**
     * Устанавливает формат вводимых данных (в том числе маску ввода).
     * @param fmt Строка формата для ввода (если null - формат по умолчанию).
     */
    public void setFormat(String fmt) {
        if (fmt == null) {
            fmt = "dd.MM.yyyy HH:mm:ss";
        }
        formatDt = new SimpleDateFormat(fmt);
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
                    sb.append('#');
                    break;
                default:
                    sb.append(fmt.charAt(i));
            }
        }
        try {
            MaskFormatter formatterDt = new MaskFormatter(sb.toString());
            formatterDt.setPlaceholderCharacter('0');
            DefaultFormatterFactory factory = new DefaultFormatterFactory(formatterDt);
            setFormatterFactory(factory);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Возвращает верную для действующих ограничений дату путём сдвигания 
     * указанной даты в действующий диапазон (если необходимо).
     * @param dt Проверяемая на валидность дата.
     * @return Верная дата (если исодная была верной, то она же и 
     * возвращается - удобно для проверки на "коррекцию").
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
     * @param dt Дата.
     */
    public void setTime(Date dt) {
        //time = getValidDate(dt);
        setText(formatDt.format(time));
    }

    /**
     * Возвращает текущую дату поля.
     * @return Текущая дата.
     */
    public Date getTime() {
        return time;
    }

    /**
     * Установка минимальной даты.
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
     * Установка текста элемента напрямую. Строка парсится согласно формату,
     * полученная дата проверяется на валидность и в элемент ставится её 
     * представление. Т.е. в результате может быть совсем не исходной строкой.
     * По формированию даты при ошибках - см. getValidDate().
     * Нежелательно использование вовне! Необходимо использовать setTime().
     * @param s Строка с датой.
     */
    @Override
    public void setText(String s) {
        System.out.println("setText=" + s);
        Date dt = parse(s);
        if (dt == null) {
            dt = (time != null) ? time : new Date();
        }
        time = getValidDate(dt);
        super.setText(formatDt.format(time));
    }

    /**
     * Преобразование строки в дату по текущему формату. С проверкой 
     * идентичности при обратном преобразовании.
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
            if (dt != null) {
                return dt == getValidDate(dt);
            }
            return false;
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
