package dvrextract;

/**
 * Процесс обработки данных.
 * 
 * @author lex
 */
public class DataProcessor {

    // Процесс FFMPEG обрабатывающий данные.
    private static Process process;
    // Последний обработанный фрейм.
    private static Frame frame;
    // Текущий обрабатываемый файл.
    private static FileInfo info;
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Обработка данных.
     */
    public static void process() {
        process = null;

        // 1. Проверка всех сканированных файлов, составление хронологического 
        // списка файлов на обработку (по критериям отбора).
        int cam = App.srcCamSelect;
        CamInfo ci = App.srcCams[cam];

        for (int i = 0; i < ci.files.size(); i++) {
            FileInfo fi = ci.files.get(i);
            if (fi.frameFirst.time
                    
            processFile(ci.files.get(i));
        }
    }

// * 2. Цикл обработки (последовательно обрабатываем все файлы списка):
// * 2.1. Берем фрейм из файла.
// * 2.2. Записываем данные фрейма в процесс ffmpeg.
// * 2.3. Проверяем и сохраняем выходные данные из ffmpeg.
// * 3. Закрываем входной поток ffmpeg. Сохраняем выходные данные. Закрываем процесс.
    private static void processFile(FileInfo info) {
        if (process == null) {
            // Компилируем командную строку для ffmpeg.
            StringBuilder sb = new StringBuilder("ffmpeg -i - " + App.destVideoOptions + " " + App.destAudioOptions + " -");

            // Стартуем процесс обработки.
            try {
                process = Runtime.getRuntime().exec("ffmpeg -i - " + App.destVideoOptions + " " + App.destAudioOptions + " -");
                processIn = process.getInputStream();
                processOut = process.getOutputStream();
            } catch (IOException ex) {
                if (process != null) {
                    process.destroy();
                }
            }
        }
    }

    /**
     * Процесс обработки данных.
     */
    private class TaskProcess extends Task.Thread {

        @Override
        public void fireStart() {
            super.fireStart();
        }

        @Override
        public void fireStop() {
            super.fireStop();
        }

        @Override
        protected void task() {
        }
    }
}
