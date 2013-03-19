/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Расширение функционала операций с датами.
 * 
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class DateTools {

    static public SimpleDateFormat formatShort = new SimpleDateFormat("dd.MM.yy");
    static public SimpleDateFormat formatFull = new SimpleDateFormat("dd.MM.yyyy");

    static public SimpleDateFormat getFormat(boolean isShort) {
        if (isShort) {
            return formatShort;
        } else {
            return formatFull;
        }
    }

    /**
     * Комбинирует дату из двух дат. Из первого параметра берёт дату, из
     * второго - время.
     * @param date Дата.
     * @param time Время.
     * @return Комбинированная дата.
     */
    static public Date getDT(Date date, Date time) {
        // ВНИМАНИЕ!!!
        // Переносим именно поля даты из первого аргумента во второй!
        // Т.к. при операциях с полями времени заморочки с UTS и поясами,
        // что может вызвать ошибки с неправильным преобразованием времени! (гемор)
        Calendar d = Calendar.getInstance();
        d.setTime(date);
        Calendar t = Calendar.getInstance();
        t.setTime(time);
        t.set(Calendar.YEAR, d.get(Calendar.YEAR));
        t.set(Calendar.MONTH, d.get(Calendar.MONTH));
        t.set(Calendar.DAY_OF_MONTH, d.get(Calendar.DAY_OF_MONTH));
        return t.getTime();
    }

    /**
     * Добавляет к дате заданное количество дней.
     * @param date Дата.
     * @param count Количество дней. Может быть отрицательным для уменьшения.
     * @return Инкрементированная дата.
     */
    static public Date addDay(Date date, int count) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, count);
        return c.getTime();
    }

    /**
     * Создает дату по указанным дню, месяцу и году.
     * @param day День.
     * @param month Месяц.
     * @param year Год.
     * @return Дата.
     */
    static public Date getDate(int day, int month, int year) {
        //TimeZone curZone = TimeZone.getDefault();
        Calendar d = Calendar.getInstance();
        d.clear();
        d.set(year, month - 1, day);
        return d.getTime();
    }

    /**
     * Возвращает дату, переданную в значении.
     * @param value Значение.
     * @return Если значение - дата, то возвращается дата, если нет - null.
     */
    static public Date getDateFromValue(Object value) {
        if ((value != null) && (value instanceof Date)) {
            return (Date) value;
        } else {
            return null;
        }
    }

    /**
     * Возвращает значение из переданной даты.
     * @param date Дата.
     * @return Если дата не null, то возвращается дата, если нет - целое = 0.
     */
    static public Object getValueFromDate(Date date) {
        if (date != null) {
            return date;
        } else {
            return (Integer) 0;
        }
    }

    static public Date getStartMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, 1);
        return c.getTime();
    }

    static public Date getEndMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.roll(Calendar.DAY_OF_MONTH, -1);
        return c.getTime();
    }
}
