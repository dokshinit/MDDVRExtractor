/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract.gui;

/**
 * Класс исключения при ошибке выполнения операции.
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class FaultException extends Exception {

    public FaultException() {
    }

    public FaultException(String message) {
        super(message);
    }
}
