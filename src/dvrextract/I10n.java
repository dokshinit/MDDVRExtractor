/*
 * Copyright (c) 2011, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

/**
 * Интернационализация интерфейса.
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class I10n {

    /**
     * Текущий язык интерфейса.
     */
    private static Lang language;

    /**
     * Возможные языки интерфейса.
     */
    public static enum Lang {
        /**
         * Русский язык.
         */
        RU,
        /**
         * Английский язык.
         */
        EN
    }

    /**
     * Возвращает текущий устновленный язык интерфейса.
     * @return Язык интерфейса.
     */
    public static Lang getLanguage() {
        return language;
    }

    /**
     * Формирует текстовое сообщение из аргументов исходя из текущего языка.
     * При мультиязычности - текст на разных языках идёт аргументами (!)
     * @param rus Русский текст.
     * @return Строка на текущем языке (если тек.язык не известен - на русском).
     */
    private static String text(String rus) {
        switch (language) {
            case RU:
                return rus;
            default:
                return rus;
        }
    }

    private static String text(String rus, String eng) {
        switch (language) {
            case RU:
                return rus;
            case EN:
                return eng;
            default:
                return eng;
        }
    }
    
    /**
     * Инициализация графических компонент.
     * @param lang Язык интерфейса.
     */
    public static void init(Lang lang) {

        // По умолчанию все значения выставляются на английском.
        language = lang;

        // App
        App.x_LAFNotFound = text("Не найден Look&Feel", "Look&Feel not found"); // Not found LAF
        App.x_LAFError = text("Ошибка включения Look&Feel", "Look&Feel aplying error"); // Error of LAF applying
        App.x_FFMpegWrong = text("Некорректная работа FFMPEG", "FFMPEG not working correctly");
        App.x_CodecsWrong = text("Ошибка запроса кодеков FFMPEG", "Error of FFMPEG codecs request");

        // DataProcessor
        DataProcessor.x_ProcessSource = text("Обработка источника...", "Source processing...");
        DataProcessor.x_ProcessFile = text("Обработка файла (%d из %d)", "File processing (%d of %d)");
        DataProcessor.x_ProcessEnd = text("Процесс кодирования завершён.", "The encoding process is completed.");
        DataProcessor.x_ProcessSourceEnd = text("Обработка источника завершена.", "The source processing is completed.");
        DataProcessor.x_FinalMakeStart = text("Запуск процесса сборки...","Starting of process of final build...");
        DataProcessor.x_ErrorFinalMakeStart = text("Ошибка запуска процесса сборки!", "Error of starting of final build process!");
        DataProcessor.x_FinalMakeStarted = text("Запущен процесс сборки.","");
        DataProcessor.x_FFMpegVideoInputNotFound = text("FFMpeg process video input not found!");
        DataProcessor.x_FFMpegProcessVideoInputFail = text("FFMpeg process video input fail!");
        DataProcessor.x_UserPorcessCancel = text("Прерывание процесса пользователем!");
        DataProcessor.x_ErrorMakeIO = text("Ошибка I/O при сборке!");
        DataProcessor.x_FinalMakeEnd = text("Процесс сборки завершён.");
        DataProcessor.x_WrongOutVideoFile = text("Неверное имя файла-приёмника видео!");
        DataProcessor.x_WrongOutAudioFile = text("Неверное имя файла-приёмника аудио!");
        DataProcessor.x_WrongOutSubFile = text("Неверное имя файла-приёмника субтитров!");
        DataProcessor.x_ErrorFFMpegVideoStart = text("Ошибка запуска FFMpeg-video!");
        DataProcessor.x_ErrorFFMpegAudioStart = text("Ошибка запуска FFMpeg-audio!");
        DataProcessor.x_ErrorFFMpegSubStart = text("Ошибка запуска FFMpeg-subtitles!");
        DataProcessor.x_Delete = text("Удалить");
        DataProcessor.x_LeaveAsIs = text("Оставить как есть");
        DataProcessor.x_WhatDoTemp = text("Что делать с временными файлами?");
        DataProcessor.x_Confirmation = text("Подтверждение");
        DataProcessor.x_Finish = text("Завершить");
        DataProcessor.x_WaitFinish = text("Ожидать завершения");
        DataProcessor.x_FinishProcess1 = text("Завершить процесс '");
        DataProcessor.x_FinishProcess2 = text("' принудительно или ожидать ещё ");
        DataProcessor.x_FinishProcess3 = text(" мсек.?");
        DataProcessor.x_FinishedProcess1 = text("Процесс '");
        DataProcessor.x_FinishedProcess2 = text("' завершен принудительно!");
        DataProcessor.x_CoderStarting = text("Запуск процесса кодирования...");
        DataProcessor.x_CoderStarted = text("Запущен процесс кодирования.");

        // Err
        Err.x_InitError = text("Ошибка инициализации лога!");

        // FileType
        FileType.NO.title = text("не определен");
        FileType.DIR.title = text("каталог");
        FileType.EXE.title = text("EXE");
        FileType.HDD.title = text("HDD");

        // Files
        Files.x_BuildFileList = text("Построение списка файлов источника...");
        Files.x_FileScaning = text("Сканирование файла (%d из %d)");
        Files.x_ScanFinish = text("Сканирование источника завершено");
        Files.x_ScanFinishBreak = text(" (прервано)");
        Files.x_SourceScaning = text("Сканирование источника...");

        // GUIFileInfoPanel
        GUIFileInfoPanel.x_Cams = text("Камеры");
        GUIFileInfoPanel.x_Duration = text("Длительность");
        GUIFileInfoPanel.x_DurationFormat = text("%d час. %d мин. %d сек. %d мсек.");
        GUIFileInfoPanel.x_End = text("Конец");
        GUIFileInfoPanel.x_FirstKeyFrame = text("Первый ключевой кадр.");
        GUIFileInfoPanel.x_Freq = text("Частота");
        GUIFileInfoPanel.x_FreqFormat = text("%d кадр./сек.");
        GUIFileInfoPanel.x_HintChangeZoom = text("Изменение масштаба происходит по нажатию кнопки мыши на кадре.");
        GUIFileInfoPanel.x_NO = text("НЕТ");
        GUIFileInfoPanel.x_Name = text("Имя");
        GUIFileInfoPanel.x_Resolution = text("Разрешение");
        GUIFileInfoPanel.x_ResolutionFormat = text("%d x %d");
        GUIFileInfoPanel.x_Size = text("Размер");
        GUIFileInfoPanel.x_Start = text("Начало");
        GUIFileInfoPanel.x_Type = text("Тип");

        // GUIFileSelectDialog
        GUIFileSelectDialog.x_Cancel = text("Отмена");
        GUIFileSelectDialog.x_CancelSelect = text("Отмена выбора");
        GUIFileSelectDialog.x_DatailView = text("Детальный вид");
        GUIFileSelectDialog.x_Date = text("Дата/время");
        GUIFileSelectDialog.x_Detail = text("Детальный");
        GUIFileSelectDialog.x_Fefresh = text("Обновить");
        GUIFileSelectDialog.x_FileName = text("Имя файла:");
        GUIFileSelectDialog.x_FileType = text("Тип файлов:");
        GUIFileSelectDialog.x_GoUp = text("На уровень вверх");
        GUIFileSelectDialog.x_Home = text("Домашний каталог");
        GUIFileSelectDialog.x_List = text("Списком");
        GUIFileSelectDialog.x_ListView = text("В виде списка");
        GUIFileSelectDialog.x_Name = text("Имя");
        GUIFileSelectDialog.x_Path = text("Каталог:");
        GUIFileSelectDialog.x_Select = text("Выбрать");
        GUIFileSelectDialog.x_SelectFile = text("Выбрать файл");
        GUIFileSelectDialog.x_Size = text("Размер");
        GUIFileSelectDialog.x_View = text("Вид");
        GUIFileSelectDialog.x_CantCreate = text("Файл/каталог не может быть создан!");
        GUIFileSelectDialog.x_Error = text("Ошибка");
        GUIFileSelectDialog.x_NotFound = text("Выбранный файл/каталог не существует!");

        // GUIFilesPanel
        GUIFilesPanel.x_End = text("Конец");
        GUIFilesPanel.x_Name = text("Имя");
        GUIFilesPanel.x_Size = text("Размер");
        GUIFilesPanel.x_Start = text("Начало");
        GUIFilesPanel.x_Type = text("Тип");

        // GUILogPanel
        GUILogPanel.x_Date = text("Дата/время");
        GUILogPanel.x_Message = text("Сообщение");

        //GUI_Main
        GUI_Main.x_Confirmation = text("Подтверждение", "Confirmation");
        GUI_Main.x_ExitQuest = text("Выйти из программы?", "Exit the program?");
        GUI_Main.x_Help = text("Справка", "Help");
        GUI_Main.x_Info = text("Инфо:", "Info:");
        GUI_Main.x_Log = text("Лог", "Log");
        GUI_Main.x_No = text("Нет", "No");
        GUI_Main.x_Process = text("Обработка", "Process");
        GUI_Main.x_Source = text("Источник", "Source");
        GUI_Main.x_State = text("Состояние", "State");
        GUI_Main.x_Yes = text("Да", "Yes");
        GUI_Main.x_Interrupt = text("Прервать", "Cancel");

        // GUI_SourceSelect
        GUI_SourceSelect.x_All = text("< все >");
        GUI_SourceSelect.x_Cam = text("Камера");
        GUI_SourceSelect.x_GoScan = text("Сканировать");
        GUI_SourceSelect.x_Hint = text("Выбор конкретной камеры может существенно<br>уменьшить время сканирования источника при<br>больших объёмах данных!");
        GUI_SourceSelect.x_NotIndent = text("не определён");
        GUI_SourceSelect.x_Select = text("Выбор");
        GUI_SourceSelect.x_Source = text("Источник");
        GUI_SourceSelect.x_SourceScan = text("Сканирование источника");
        GUI_SourceSelect.x_Type = text("Тип");
        GUI_SourceSelect.x_SelectSource = text("Выбор файла/каталога источника");

        // GUI_TabAbout (интернационализируется внутри!)

        // GUI_TabProcess
        GUI_TabProcess.x_Audio = text("Аудио");
        GUI_TabProcess.x_Cam = text("Камера");
        GUI_TabProcess.x_Codec = text("Кодек");
        GUI_TabProcess.x_CustomOption = text("Ручные настройки");
        GUI_TabProcess.x_Evaluate = text("Оценка");
        GUI_TabProcess.x_File = text("Файл");
        GUI_TabProcess.x_Format = text("Формат");
        GUI_TabProcess.x_FramePerSec = text("Кадр/сек");
        GUI_TabProcess.x_Mode = text("Режим");
        GUI_TabProcess.x_NotIndent = text("не выбрана");
        GUI_TabProcess.x_NotSave = text("Не сохранять");
        GUI_TabProcess.x_Period1 = text("Период c");
        GUI_TabProcess.x_Period2 = text("по");
        GUI_TabProcess.x_Select = text("Выбор");
        GUI_TabProcess.x_Size = text("Размер");
        GUI_TabProcess.x_Source = text("Источник");
        GUI_TabProcess.x_Sub = text("Титры");
        GUI_TabProcess.x_ToFile = text("В файл");
        GUI_TabProcess.x_ToVideo = text("В видео");
        GUI_TabProcess.x_Video = text("Видео");
        GUI_TabProcess.x_WOConvert = text("Без преобразования");
        GUI_TabProcess.x_CalcEnd = text("Подсчёт данных для обработки завершён.");
        GUI_TabProcess.x_CalcStart = text("Подсчёт данных для обработки...");
        GUI_TabProcess.x_SelectDestFile = text("Выбор файла приёмника");
        GUI_TabProcess.x_NotePreDecoding = text("Предекодируется как 'Signed PCM 16bit LowEndian'");
        GUI_TabProcess.x_NoteSimple = text("Кодек-видео - MPEG4, кодек-аудио - PCM16BIT, субтитры - файл SRT.");
        
        // GUI_TabSource
        GUI_TabSource.x_Cam = text("Камера");
        GUI_TabSource.x_NotIndent = text("не определён");
        GUI_TabSource.x_NotSelect = text("не выбрана");
        GUI_TabSource.x_Select = text("Выбор");
        GUI_TabSource.x_Source = text("Источник");
        GUI_TabSource.x_Type = text("Тип");

        // SourceFileFilter
        SourceFileFilter.instALL.description = text("Все файлы DVR");
        SourceFileFilter.instEXE.description = text("Файлы EXE-архивы DVR");
        SourceFileFilter.instHDD.description = text("Файлы HDD DVR");

        // Task
        Task.x_CanNotStarted = text("Задание не может быть запущено!");
        Task.x_Error = text("Ошибка");
    }
}
