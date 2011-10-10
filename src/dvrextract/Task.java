package dvrextract;

import javax.swing.JOptionPane;

/**
 * Класс для запуска задач.
 * @author lex
 */
public final class Task {

    // Для синхронизации доступа к задаче.
    private static final Object sync = new Object();
    // Флаг выставляемый для индикации задаче, что она должна быть остановлена
    // (сама остановка - на совести задачи).
    private static boolean isTerminate = false;
    // Указатель на процесс запущенной задачи (если ничего не запущено = null).
    private static Thread task = null;

    /**
     * Возвращает объект на который синхронизируется всё в задачах.
     * @return Объект синхронизации.
     */
    public static Object getSync() {
        return sync;
    }

    /**
     * Проверка флага необходимости остановки.
     * @return Флаг: true - остановка задачи, false - нет требования остановки.
     */
    public static boolean isTerminate() {
        synchronized (sync) {
            return isTerminate;
        }
    }

    /**
     * Возвращает статус, выполняется ли задача.
     * @return true - выполняется, false - нет.
     */
    public static boolean isAlive() {
        synchronized (sync) {
            return task != null ? task.isAlive() : false;
        }
    }

    /**
     * Возвращает текущую задачу.
     * @return Задача / null.
     */
    public static Thread getTask() {
        synchronized (sync) {
            return task;
        }
    }

    /**
     * Запуск задачи к исполнению в отдельном потоке.
     * @param t Задача.
     * @return Флаг успешности операции.
     */
    public static boolean start(Thread t) {
        if (t == null) {
            JOptionPane.showMessageDialog(App.mainFrame,
                    "Задание не может быть запущено!", "Ошибка",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        synchronized (sync) {
            if (task != null && task.isAlive()) {
                JOptionPane.showMessageDialog(App.mainFrame, 
                        "Задание не может быть запущено!", "Ошибка",
                        JOptionPane.WARNING_MESSAGE);
                return false;
            }
            task = t;
        }
        task.start();
        return true;
    }

    /**
     * Отмена выполнения задачи (помечает флаг - задача его видит и сама останавливается).
     * @return Флаг успешности операции.
     */
    public static boolean terminate() {
        synchronized (sync) {
            if (task != null && task.isAlive()) {
                isTerminate = true;
                return true;
            }
        }
        return false;
    }

    /**
     * Вызывается при старте задачи в обязательном порядке.
     */
    protected static void fireStart() {
        synchronized (sync) {
            isTerminate = false;
        }
        App.mainFrame.setLocks();
    }

    /**
     * Вызывается при завершении задачи в обязательном порядке.
     */
    protected static void fireStop() {
        synchronized (sync) {
            task = null;
            // Убрать чтобы вовне было видно что задачу тормознули?
            //isTerminate = false; 
        }
        App.mainFrame.setLocks();
    }

    /**
     * Поток задачи. Автоматически вызывает финализацию при завершении.
     */
    public static class Thread extends java.lang.Thread {

        /**
         * Переопределённый метод тела потока с вызовом всех частей задачи.
         */
        @Override
        public final void run() {
            Task.fireStart();
            try {
                fireStart();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                task();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                fireStop();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Task.fireStop();
        }

        /**
         * Тело обработки при начале задачи.
         */
        public void fireStart() {
        }

        /**
         * Тело задачи.
         */
        protected void task() {
        }

        /**
         * Тело обработки при финализации задачи.
         */
        public void fireStop() {
        }
    }
}
