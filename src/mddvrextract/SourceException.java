/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract;

/**
 * Исключение при ошибках источника.
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public final class SourceException extends Exception {

    /**
     * Конструктор.
     *
     * @param msg Текст сообщения об ошибке.
     */
    public SourceException(String msg) {
        super(msg);
    }
}
