/*
 * Copyright (c) 2011-2013, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract;

/**
 * Класс для представленя файла (дескриптор).
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class FileDesc {

    /**
     * Тип файловой системы узла: FS, XFS.
     */
    public int type;
    /**
     * Имя файла (с полным путём).
     */
    public String name;
    /**
     * Тип ФС - обычный файл файловой системы.
     */
    public final static int FS = 0;
    /**
     * Тип ФС - файл на блочном устройстве с файловой системой XFS.
     */
    public final static int XFS = 1;

    /**
     * Конструктор для файла XFS.
     *
     * @param name Имя файла.
     * @param type Тип файла (см. type).
     */
    public FileDesc(String name, int type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Конструктор для обычного файла.
     *
     * @param name Имя файла.
     */
    public FileDesc(String name) {
        this(name, FS);
    }

    /**
     * Конструктор для не заданного обычного файла.
     */
    public FileDesc() {
        this("");
    }

    /**
     * Конструктор для копии.
     *
     * @param obj Исходный объект копию которого нужно сделать.
     */
    public FileDesc(FileDesc obj) {
        this(obj.name, obj.type);
    }

    @Override
    public String toString() {
        switch (type) {
            case FS:
                return name;
            case XFS:
                return "xfs:" + name;
            default:
                return name;
        }
    }
}
