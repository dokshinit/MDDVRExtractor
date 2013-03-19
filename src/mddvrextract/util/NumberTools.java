/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Расширенный функционал для операций с числами.
 * 
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class NumberTools {

    static public NumberFormat format0 = new DecimalFormat("#,##0");
    static public NumberFormat format1 = new DecimalFormat("#,##0.0");
    static public NumberFormat format2 = new DecimalFormat("#,##0.00");
    static public NumberFormat format3 = new DecimalFormat("#,##0.000");

    static public NumberFormat getFormat(int digits) {
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

    static public String integerToFormatString(Integer value, NumberFormat f,
            String sNull, String sZero) {
        if (value == null) {
            return sNull;
        }
        if (value == 0.0) {
            return sZero;
        }
        return f.format(value);
    }

    static public String doubleToFormatString(Double value, NumberFormat f,
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
