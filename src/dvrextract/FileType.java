/*
 * Copyright (c) 2011, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

/**
 * Типы обрабатываемых файлов.
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public enum FileType {

    /**
     * Не определён или не задан.
     */
    NO(-1, "не определён"),
    /**
     * Каталог - могут быть внутри файлы лубых типов.
     */
    DIR(0, "каталог"),
    /**
     * Файл выгрузки видео EXE.
     */
    EXE(1, "EXE"),
    /**
     * Файл хранения видео на HDD.
     */
    HDD(2, "HDD");
    /**
     * Код.
     */
    public int id;
    /**
     * Название.
     */
    public String title;

    /**
     * Конструктор.
     * @param id Код.
     * @param title Название.
     */
    FileType(int id, String title) {
        this.id = id;
        this.title = title;
    }
}
