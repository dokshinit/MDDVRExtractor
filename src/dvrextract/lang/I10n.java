package dvrextract.lang;

import dvrextract.App;
import dvrextract.DataProcessor;
import dvrextract.Err;
import dvrextract.FileType;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Интернационализация интерфейса.
 * @author lex
 */
public enum I10n {

    App_x_LAFNotFound, App_x_LAFError, App_x_FFMpegWrong, App_x_CodecsWrong;

    // Текущий язык интерфейса.
    private static Lang language;

    // Возможные языки интерфейса.
    public static enum Lang {

        EN, RU
    }
    private Map<Lang, String> map;

    public String get() {
        return map.get(language);
    }

    I10n() {
        map = new EnumMap<Lang, String>(Lang.class);
    }

    public I10n add(Lang lang, String text) {
        map.put(lang, text);
        return this;
    }
    
    public static void init2() {
        App_x_LAFNotFound.add(Lang.RU, "Не найден L&F");
        App_x_LAFNotFound.add(Lang.EN, "Not found L&F");
    }
    
    /**
     * Формирует текстовое сообщение из аргументов исходя из текущего языка.
     * @param rus Русский текст.
     * @param eng Английский текст.
     * @return Строка на текущем языке (если тек.язык не известен - на русском).
     */
    private static String text(String rus, String eng) {
        switch (language) {
            case EN:
                return eng;
            case RU:
                return rus;
            default:
                return rus;
        }
    }

    public static void init(Lang lang) {

        // По умолчанию все значения выставляются на русском (переделать на английский?).
        language = lang;

        // App
        App.x_LAFNotFound = text("Не найден L&F", "Not found L&F");
        App.x_LAFError = text("Ошибка включения L&F", "Error enable L&F");
        App.x_FFMpegWrong = text("Некорректная работа FFMPEG!", "Incorrect FFMPEG!");
        App.x_CodecsWrong = text("Ошибка запроса кодеков", "Codecs request failed");

        // DataProcessor
        DataProcessor.x_ProcessSource = text("Обработка источника...", "Source processing...");
        DataProcessor.x_ProcessFile = text("Обработка файла (%d из %d)", "File processing (%d of %d)");
        DataProcessor.x_ProcessEnd = text("Процесс кодирования завершён.", "The encoding process is completed.");
        DataProcessor.x_ProcessSourceEnd = text("Обработка источника завершена.", "Source processing is completed.");
        DataProcessor.x_FinalMakeStart = text("Запуск процесса сборки...", "Final build starting...");
        DataProcessor.x_ErrorFinalMakeStart = text("Ошибка запуска процесса сборки!", "Failed to start the build process!");
        DataProcessor.x_FinalMakeStarted = text("Запущен процесс сборки.", "Final build is started.");
        DataProcessor.x_FFMpegVideoInputNotFound = text("FFMpeg process video input not found!", "FFMpeg process video input not found!");
        DataProcessor.x_FFMpegProcessVideoInputFail = text("FFMpeg process video input fail!", "FFMpeg process video input fail!");
        DataProcessor.x_UserPorcessCancel = text("Прерывание процесса пользователем!", "Interrupting by user!");
        DataProcessor.x_ErrorMakeIO = text("Ошибка I/O при сборке!", "I/O error when final building!");
        DataProcessor.x_FinalMakeEnd = text("Процесс сборки завершён.", "The build process is completed.");
        DataProcessor.x_WrongOutVideoFile = text("Неверное имя файла-приёмника видео!", "Wrong name for the video destination!");
        DataProcessor.x_WrongOutAudioFile = text("Неверное имя файла-приёмника аудио!", "Wrong name for the audio destination!");
        DataProcessor.x_WrongOutSubFile = text("Неверное имя файла-приёмника субтитров!", "Wrong name for the subtitles destination!");
        DataProcessor.x_ErrorFFMpegVideoStart = text("Ошибка запуска FFMpeg-video!", "Failed to start FFMpeg-video!");
        DataProcessor.x_ErrorFFMpegAudioStart = text("Ошибка запуска FFMpeg-audio!", "Failed to start FFMpeg-audio!");
        DataProcessor.x_ErrorFFMpegSubStart = text("Ошибка запуска FFMpeg-subtitles!", "Failed to start FFMpeg-subtitles!");
        DataProcessor.x_Delete = text("Удалить", "Delete");
        DataProcessor.x_LeaveAsIs = text("Оставить как есть", "Leave as is");
        DataProcessor.x_WhatDoTemp = text("Что делать с временными файлами?", "What to do with temporary files?");
        DataProcessor.x_Confirmation = text("Подтверждение", "Confirmation");
        DataProcessor.x_Finish = text("Завершить", "Finish");
        DataProcessor.x_WaitFinish = text("Ожидать завершения", "Wait the completion");
        DataProcessor.x_FinishProcess1 = text("Завершить процесс '", "Forcibly terminate the process '");
        DataProcessor.x_FinishProcess2 = text("' принудительно или ожидать ещё ", "' or wait for another ");
        DataProcessor.x_FinishProcess3 = text(" мсек.?", " msec.?");
        DataProcessor.x_FinishedProcess1 = text("Процесс '", "The process '");
        DataProcessor.x_FinishedProcess2 = text("' завершен принудительно!", "' forcibly terminated!");
        DataProcessor.x_CoderStarting = text("Запуск процесса кодирования...", "Starting the process of encoding...");
        DataProcessor.x_CoderStarted = text("Запущен процесс кодирования.", "Started the process of encoding.");

        // Err
        Err.x_InitError = text("Ошибка инициализации лога!", "Log initialization error!");

        // FileType
        FileType.NO.title = text("не определен", "not defined");
        FileType.DIR.title = text("каталог", "directory");
        FileType.EXE.title = text("EXE", "EXE");
        FileType.HDD.title = text("HDD", "HDD");
        
    }
}
