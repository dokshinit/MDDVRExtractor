package dvrextract;

/**
 * Хранение данных и операции с ними.
 * @author lex
 */
public class Data {
    ////////////////////////////////////////////////////////////////////////////
    // Константы.
    ////////////////////////////////////////////////////////////////////////////
    // Максимальное кол-во обрабатываемых камер.

    public static final int MAXCAMS = 16;

    ////////////////////////////////////////////////////////////////////////////
    // Информация о источнике:
    ////////////////////////////////////////////////////////////////////////////
    public static class Src {
        // Подразумевается, что источником может быть или одиночный файл или
        // каталог. При этом каждый файл распознаётся исходя из имени файла:
        // по шалону *.exe - файл архива, по шаблону da*. - файл hdd.
        //
        // Каталог или файл.

        private static String name = "";
        // Тип источника: 0-EXE, 1-HDD
        private static FileType type = FileType.NO;
        // Ограничение одной камерой (если = 0 - без ограничений).
        private static int limitedCam = 0;
        // Текущая выбранная камера для которой отображаются файлы.
        private static int selectedCam = 0;
        // Массив разделения источников по камерам.
        private static CamInfo[] cams = new CamInfo[MAXCAMS];

        private Src() {
            for (int i = 0; i < MAXCAMS; i++) {
                cams[i] = new CamInfo();
            }
        }

        public static String getName() {
            return name;
        }

        public static FileType getType() {
            return type;
        }

        public static int getLimitedCam() {
            return limitedCam;
        }

        public static int getSelectedCam() {
            return selectedCam;
        }

        public static CamInfo getCamInfo(int n) {
            return cams[n];
        }

        public static void setLimitedCam(int cam) {
            limitedCam = cam;
        }

        public static void setSelectedCam(int cam) {
            selectedCam = cam;
        }

        public static void set(String filename, FileType filetype, int limit) {
            name = filename;
            type = type;
            limitedCam = limit;
            selectedCam = 0;
            for (int i = 0; i < MAXCAMS; i++) {
                cams[i].clear();
            }
            //App.mainFrame.displaySource();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Информация об обработке:
    ////////////////////////////////////////////////////////////////////////////
    public static class Dst {

        private static String destName = "";
    }

    /**
     * Инициализация начальных значений.
     */
    public static void init() {
        
    }
    
    
    public void setSource(String path, int limit) {
        
    }
    
    public void setSelectedCam(int cam) {
        
    }
    
    public void setDestination(String path) {
        
    }
}
