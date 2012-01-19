/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

import dvrextract.LogTableModel.Type;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Лог для отслеживания ошибок!
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public final class Err {

    /**
     * Логгер для ведения лога об исключениях.
     */
    private static final Logger errLog = Logger.getLogger(App.class.getName());
    /**
     * Текстовые ресурсы для интерфейса.
     */
    public static String x_InitError;
            
    /**
     * Инициализация.
     */
    public static void init() {
        try {
            errLog.setLevel(Level.ALL);
            Handler handler = new FileHandler("DVRExtract.err.log", 0, 10);
            handler.setLevel(Level.ALL);
            errLog.addHandler(handler);
        } catch (Exception ex) {
            App.log(Type.ERROR, x_InitError); // Лог ошибок будет выключен!
        }
    }

    /**
     * Вывод стоки в лог.
     * @param msg Строка.
     */
    public static void log(String msg) {
        if (errLog != null) {
            errLog.log(Level.ALL, msg);
        }

    }

    /**
     * Вывод исключения в лог (со стеком).
     * @param ex Исключение.
     */
    public static void log(Throwable ex) {
        if (errLog != null) {
            //errLog.throwing(src, method, ex);
            synchronized (ex) {
                StringBuilder sb = new StringBuilder(ex.toString());
                sb.append("\n");
                StackTraceElement[] trace = ex.getStackTrace();
                for (int i = 0; i < trace.length; i++) {
                    sb.append("\tat ").append(trace[i]).append("\n");
                }
                Throwable ourCause = ex.getCause();
                if (ourCause != null) {
                    addCause(sb, ourCause, trace);
                }
                errLog.log(Level.ALL, sb.toString());
            }
        }
    }

    /**
     * Добавление инфы о субисключении в построитель строки.
     * @param sb Построитель.
     * @param cause Субисключение.
     * @param causedTrace Стек трассировки.
     */
    private static void addCause(StringBuilder sb, Throwable cause, StackTraceElement[] causedTrace) {

        StackTraceElement[] trace = cause.getStackTrace();
        int m = trace.length - 1, n = causedTrace.length - 1;
        while (m >= 0 && n >= 0 && trace[m].equals(causedTrace[n])) {
            m--;
            n--;
        }
        int framesInCommon = trace.length - 1 - m;

        sb.append("Caused by: ").append(cause.toString()).append("\n");
        for (int i = 0; i <= m; i++) {
            sb.append("\tat ").append(trace[i]).append("\n");
        }
        if (framesInCommon != 0) {
            sb.append("\t... ").append(framesInCommon).append(" more\n");
        }

        // Recurse if we have a cause
        Throwable ourCause = cause.getCause();
        if (ourCause != null) {
            addCause(sb, ourCause, trace);
        }
    }
}
