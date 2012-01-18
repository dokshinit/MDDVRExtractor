/*
 * Copyright (c) 2011, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;
import javax.swing.filechooser.FileFilter;
import xfsengine.XFS.Node;

/**
 * Фильтр по файлам для выбора источника.
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class SourceFileFilter extends FileFilter implements java.io.FileFilter {

    /**
     * Общий фильтр для всех типов файлов.
     */
    public static final SourceFileFilter instALL = new SourceFileFilter("^(.*[/\\\\])*(.+\\.exe$|^da\\d+)", "Все файлы DVR");
    /**
     * Фильтр для EXE файлов.
     */
    public static final SourceFileFilter instEXE = new SourceFileFilter("^(.*[/\\\\])*.+\\.exe$", "Файлы EXE-архивы DVR");
    /**
     * Фильтр для HDD файлов.
     */
    public static final SourceFileFilter instHDD = new SourceFileFilter("^(.*[/\\\\])*da\\d+", "Файлы HDD DVR");
    /**
     * Маска фильтрации файлов.
     */
    private Pattern pattern;
    /**
     * Описание.
     */
    public String description;

    /**
     * Конструктор.
     * @param ptrn Регулярное выражение для фильтрации.
     * @param desc Описание фильтра.
     */
    private SourceFileFilter(String ptrn, String desc) {
        pattern = Pattern.compile(ptrn, Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
        description = desc;
    }

    /**
     * Метод фильтрации (разрешаем каталоги и файлы по фильтру).
     * @param f Проверяемый файл.
     * @return true - разрешаем, false - запрещаем.
     */
    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        return pattern.matcher(f.getName()).matches();
    }

    public boolean accept(String filename) {
        return pattern.matcher(filename).matches();
    }

    /**
     * Описание фильтра.
     * @return Описание фильтра.
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Возвращает тип файла.
     * @param f Файл.
     * @return Тип файла.
     */
    public static FileType getType(FileDesc f) {
        if (f.id != 0) { // Только XFS HDD!!!
            return FileType.XFS;
        }
        File n = new File(f.name);
        if (n.isDirectory()) {
            return FileType.DIR;
        } else if (instEXE.accept(f.name)) {
            return FileType.EXE;
        } else if (instHDD.accept(f.name)) {
            return FileType.HDD;
        } else {
            return FileType.NO;
        }
    }

    /**
     * Возвращает фильтр по типу файла.
     * @param type Тип файла.
     * @return Фильтр.
     */
    public static SourceFileFilter get(FileType type) {
        switch (type) {
            case EXE:
                return instEXE;
            case HDD:
            case XFS:
                return instHDD;
            default:
                return instALL;
        }
    }

    /**
     * Возвращает фильтр по названию файла.
     * @param file Имя файла.
     * @return Фильтр.
     */
    public static SourceFileFilter get(FileDesc file) {
        return get(getType(file));
    }
}
