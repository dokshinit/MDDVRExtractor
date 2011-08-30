package dvrextract;

import java.io.File;
import java.util.regex.Pattern;
import javax.swing.filechooser.FileFilter;

/**
 * Фильтр по файлам для выбора источника.
 * @author lex
 */
public class SourceFileFilter extends FileFilter {

    // Общий фильтр для всех типов файлов.
    public static final SourceFileFilter instALL = new SourceFileFilter("(.+\\.exe$|^da\\d+)", "Все файлы DVR");
    // Фильтр для EXE файлов.
    public static final SourceFileFilter instEXE = new SourceFileFilter(".+\\.exe$", "Файлы EXE-архивы DVR");
    // Фильтр для HDD файлов.
    public static final SourceFileFilter instHDD = new SourceFileFilter("^da\\d+", "Файлы HDD DVR");
    //
    private Pattern pattern; // Маска фильтрации файлов.
    private String description; // Описание.

    public SourceFileFilter(String ptrn, String desc) {
        pattern = Pattern.compile(ptrn, Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
        description = desc;
    }

    // Разрешаем каталоги и файлы HDD.
    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        return pattern.matcher(f.getName()).matches();
    }

    // Описание фильтра.
    @Override
    public String getDescription() {
        return description;
    }

    // Возвращает тип файла.
    public static FileType getType(File f) {
        if (f.isDirectory()) {
            return FileType.DIR;
        } else if (instEXE.accept(f)) {
            return FileType.EXE;
        } else if (instHDD.accept(f)) {
            return FileType.HDD;
        } else {
            return FileType.NO;
        }
    }

    public static SourceFileFilter get(FileType type) {
        switch (type) {
            case EXE:
                return instEXE;
            case HDD:
                return instHDD;
            default:
                return instALL;
        }
    }
}
