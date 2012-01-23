/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

import javax.swing.JOptionPane;

/**
 * Класс для запуска задач.
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public final class Task {

    /**
     * Для синхронизации доступа к задаче.
     */
    private static final Object sync = new Object();
    /**
     * Флаг выставляемый для индикации задаче, что она должна быть остановлена
     * (сама остановка - на совести задачи).
     */
    private static boolean isTerminate = false;
    /**
     * Указатель на процесс запущенной задачи (если ничего не запущено = null).
     */
    private static Thread task = null;
    /**
     * Текстовые ресурсы для интерфейса.
     */
    public static String x_CanNotStarted, x_Error;

    /**
     * Возвращает объект на который синхронизируется всё в задачах.
     *
     * @return Объект синхронизации.
     */
    public static Object getSync() {
        return sync;
    }

    /**
     * Проверка флага необходимости остановки.
     *
     * @return Флаг: true - остановка задачи, false - нет требования остановки.
     */
    public static boolean isTerminate() {
        synchronized (sync) {
            return isTerminate;
        }
    }

    /**
     * Возвращает статус, выполняется ли задача.
     *
     * @return true - выполняется, false - нет.
     */
    public static boolean isAlive() {
        synchronized (sync) {
            return task != null ? task.isAlive() : false;
        }
    }

    /**
     * Возвращает текущую задачу.
     *
     * @return Задача / null.
     */
    public static Thread getTask() {
        synchronized (sync) {
            return task;
        }
    }

    /**
     * Запуск задачи к исполнению в отдельном потоке.
     *
     * @param t Задача.
     * @return Флаг успешности операции.
     */
    public static boolean start(Thread t) {
        if (t == null) {
            JOptionPane.showMessageDialog(App.gui, x_CanNotStarted, x_Error,
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        synchronized (sync) {
            if (task != null && task.isAlive()) {
                JOptionPane.showMessageDialog(App.gui, x_CanNotStarted, x_Error,
                        JOptionPane.WARNING_MESSAGE);
                return false;
            }
            task = t;
        }
        task.start();
        return true;
    }

    /**
     * Отмена выполнения задачи (помечает флаг - задача его видит и сама
     * останавливается).
     *
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
        App.gui.validateLocks();
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
        App.gui.validateLocks();
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
                Err.log(ex);
            }
            try {
                task();
            } catch (Exception ex) {
                Err.log(ex);
            }
            try {
                fireStop();
            } catch (Exception ex) {
                Err.log(ex);
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
