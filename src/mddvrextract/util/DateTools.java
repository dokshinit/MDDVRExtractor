/*
 * Copyright (c) 2011-2013, Aleksey Nikolaevich Dokshin. All right reserved.
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

    /**
     * Предопределенный формат для вывода даты с номером года 2 знака.
     */
    public static SimpleDateFormat formatShort = new SimpleDateFormat("dd.MM.yy");
    /**
     * Предопределенный формат для вывода даты с номером года 4 знака.
     */
    public static SimpleDateFormat formatFull = new SimpleDateFormat("dd.MM.yyyy");

    /**
     * Возвращает формат для преобразования даты в строку в соответствии с
     * флагом.
     *
     * @param isShort Флаг - короткий формат (true) или длинный (false).
     * @return Форматер.
     */
    public static SimpleDateFormat getFormat(boolean isShort) {
        if (isShort) {
            return formatShort;
        } else {
            return formatFull;
        }
    }

    /**
     * Комбинирует дату из двух дат. Из первого параметра берёт дату, из второго
     * - время.
     *
     * @param date Дата.
     * @param time Время.
     * @return Комбинированная дата.
     */
    public static Date getDT(Date date, Date time) {
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
     *
     * @param date Дата.
     * @param count Количество дней. Может быть отрицательным для уменьшения.
     * @return Инкрементированная дата.
     */
    public static Date addDay(Date date, int count) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, count);
        return c.getTime();
    }

    /**
     * Создает дату по указанным дню, месяцу и году.
     *
     * @param day День.
     * @param month Месяц.
     * @param year Год.
     * @return Дата.
     */
    public static Date getDate(int day, int month, int year) {
        //TimeZone curZone = TimeZone.getDefault();
        Calendar d = Calendar.getInstance();
        d.clear();
        d.set(year, month - 1, day);
        return d.getTime();
    }

    /**
     * Возвращает дату, переданную в значении.
     *
     * @param value Значение.
     * @return Если значение - дата, то возвращается дата, если нет - null.
     */
    public static Date getDateFromValue(Object value) {
        if ((value != null) && (value instanceof Date)) {
            return (Date) value;
        } else {
            return null;
        }
    }

    /**
     * Возвращает значение из переданной даты.
     *
     * @param date Дата.
     * @return Если дата не null, то возвращается дата, если нет - целое = 0.
     */
    public static Object getValueFromDate(Date date) {
        if (date != null) {
            return date;
        } else {
            return (Integer) 0;
        }
    }

    /**
     * Возвращает дату начала месяца.
     *
     * @param date Исходная дата.
     * @return Дата начала месяца.
     */
    public static Date getStartMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, 1);
        return c.getTime();
    }

    /**
     * Возвращает дату конца месяца.
     *
     * @param date Исходная дата.
     * @return Дата конца месяца.
     */
    public static Date getEndMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.roll(Calendar.DAY_OF_MONTH, -1);
        return c.getTime();
    }

    /**
     * Возвращает дату по указанным числовым данным.
     * @param year Год.
     * @param month Месяц (1..12)!
     * @param day День.
     * @return Дата.
     */
    public static Date get(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month + 1, day);
        return c.getTime();
    }
}
