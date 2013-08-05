/*
 * Copyright (c) 2011-2013, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextractor.gui;

/**
 * Класс исключения при блокировке (накладывании вето).
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class VetoException extends Exception {

    public VetoException() {
    }

    public VetoException(String message) {
        super(message);
    }
}
