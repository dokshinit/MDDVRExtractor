/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

/**
 * Класс для представленя файла.
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class FileDesc {

    /**
     * Номер узла XFS. Если = 0 - обычный файл файловой системы.
     */
    public long id;
    /**
     * Имя файла (с полным путём). 
     * Для узла играет только роль представления, доступ идёт по номеру узла!
     */
    public String name;

    /**
     * Конструктор для файла XFS.
     * @param id Номер узла XFS.
     * @param name Имя файла.
     */
    public FileDesc(long id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Конструктор для обычного файла.
     * @param name Имя файла.
     */
    public FileDesc(String name) {
        this(0, name);
    }

    @Override
    public String toString() {
        return id == 0 ? name : (name+" ["+id+"]");
    }
}
