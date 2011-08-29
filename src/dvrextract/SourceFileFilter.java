package dvrextract;

import java.io.File;
import java.util.regex.Pattern;
import javax.swing.filechooser.FileFilter;

/**
 * Фильтр по файлам для выбора источника.
 * @author lex
 */
public class SourceFileFilter extends FileFilter {

    // Фильтр для EXE файлов.
    public static final SourceFileFilter instEXE = new SourceFileFilter(".+\\.exe$", "Файлы EXE-архивы DVR");
    // Фильтр для HDD файлов.
    public static final SourceFileFilter instHDD = new SourceFileFilter("^da\\d+", "Файлы HDD DVR");
    //
    private Pattern pattern; // Маска фильтрации файлов.
    private String description; // Описание.

    public SourceFileFilter(String ptrn, String desc) {
        pattern = Pattern.compile(ptrn, Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
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
    public static int getType(File f) {
        if (f.isDirectory()) {
            return 0;
        } else if (instEXE.accept(f)) {
            return 1;
        } else if (instHDD.accept(f)) {
            return 2;
        } else {
            return -1;
        }
    }
    
    public static String getTypeTitle(int type) {
        switch (type) {
            case 0:
                return "каталог";
            case 1:
                return "EXE";
            case 2:
                return "HDD";
            default:
                return "не определён";
        }
    }
    
    public static String getTypeTitle(File f) {
        return getTypeTitle(getType(f));
    }
}
