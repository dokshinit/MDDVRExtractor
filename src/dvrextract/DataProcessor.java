package dvrextract;

/**
 * Процесс обработки данных.
 * 
 * @author lex
 */
public class DataProcessor {

    /**
     * Обработка данных.
     */
    public void process() {
        // 1. Проверка всех сканированных файлов, составление хронологического 
        // списка файлов на обработку (по критериям отбора).
        int cam = App.srcCamSelect;
        CamInfo ci = App.srcCams[cam];
        
        for (int i = 0; i < ci.files.size(); i++) {
            processFile(ci.files.get(i));
        }
    }
    
// * 2. Цикл обработки (последовательно обрабатываем все файлы списка):
// * 2.1. Берем фрейм из файла.
// * 2.2. Записываем данные фрейма в процесс ffmpeg.
// * 2.3. Проверяем и сохраняем выходные данные из ffmpeg.
// * 3. Закрываем входной поток ffmpeg. Сохраняем выходные данные. Закрываем процесс.
    
    private void processFile(FileInfo info) {
        
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
