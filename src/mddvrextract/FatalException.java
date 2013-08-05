/*
 * Copyright (c) 2011-2013, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract;

/**
 * Исключение при фатальной ошибке.
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public final class FatalException extends Exception {

    /**
     * Конструктор.
     *
     * @param msg Текст сообщения об ошибке.
     */
    public FatalException(String msg) {
        super(msg);
    }
}
