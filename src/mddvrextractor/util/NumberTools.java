/*
 * Copyright (c) 2011-2013, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextractor.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Расширенный функционал для операций с числами.
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class NumberTools {

    /**
     * Предопределенный формат для вывода с точностью 0 знаков после запятой.
     */
    public static NumberFormat format0 = new DecimalFormat("#,##0");
    /**
     * Предопределенный формат для вывода с точностью 1 знак после запятой.
     */
    public static NumberFormat format1 = new DecimalFormat("#,##0.0");
    /**
     * Предопределенный формат для вывода с точностью 2 знака после запятой.
     */
    public static NumberFormat format2 = new DecimalFormat("#,##0.00");
    /**
     * Предопределенный формат для вывода с точностью 3 знака после запятой.
     */
    public static NumberFormat format3 = new DecimalFormat("#,##0.000");

    /**
     * Возвращает форматер для заданного кол-ва знаков после зяпятой.
     *
     * @param digits Кол-во знаков после запятой (0-3).
     * @return Форматер.
     */
    public static NumberFormat getFormat(int digits) {
        switch (digits) {
            case 0:
                return format0;
            case 1:
                return format1;
            case 2:
                return format2;
            case 3:
                return format3;
        }
        return format0;
    }

    /**
     * Преобразует целое число в строку с форматированием.
     *
     * @param value Число.
     * @param f Форматер для преобразования в строку.
     * @param sNull Строка выводимая при пустом значении.
     * @param sZero Строка выводимая при нулевом значении.
     * @return Форматированная строка.
     */
    public static String integerToFormatString(Integer value, NumberFormat f,
            String sNull, String sZero) {
        if (value == null) {
            return sNull;
        }
        if (value == 0.0) {
            return sZero;
        }
        return f.format(value);
    }

    /**
     * Преобразует число с плавающей точкой в строку с форматированием.
     *
     * @param value Число с плавающей точкой.
     * @param f Форматер для преобразования в строку.
     * @param sNull Строка выводимая при пустом значении.
     * @param sZero Строка выводимая при нулевом значении.
     * @return Форматированная строка.
     */
    public static String doubleToFormatString(Double value, NumberFormat f,
            String sNull, String sZero) {
        if (value == null) {
            return sNull;
        }
        if (value == 0.0 && sZero != null) {
            return sZero;
        }
        return f.format(value);
    }
}
