/*
 * Copyright (c) 2011, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

/**
 * Интернационализация интерфейса.
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class I18n {

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
        App.x_LAFNotFound = text(
                "Не найден Look&Feel", "Look&Feel not found"); // Not found LAF
        App.x_LAFError = text(
                "Ошибка включения Look&Feel", "Look&Feel aplying error"); // Error of LAF applying
        App.x_FFMpegWrong = text(
                "Некорректная работа FFMPEG", "FFMPEG not working correctly");
        App.x_CodecsWrong = text(
                "Ошибка запроса кодеков FFMPEG", "Error of FFMPEG codecs request");

        // DataProcessor
        DataProcessor.x_ProcessSource = text(
                "Обработка источника...", "Source processing...");
        DataProcessor.x_ProcessFile = text(
                "Обработка файла (%d из %d)", "File processing (%d of %d)");
        DataProcessor.x_ProcessEnd = text(
                "Процесс кодирования завершён.", "The encoding process is completed.");
        DataProcessor.x_ProcessSourceEnd = text(
                "Обработка источника завершена.", "The source processing is completed.");
        DataProcessor.x_FinalMakeStart = text(
                "Запуск процесса сборки...", "Starting of process of final build...");
        DataProcessor.x_ErrorFinalMakeStart = text(
                "Ошибка запуска процесса сборки!", "Error of starting of final build process!");
        DataProcessor.x_FinalMakeStarted = text(
                "Запущен процесс сборки.", "Running the final build process.");
        DataProcessor.x_FFMpegVideoInputNotFound = text(
                "Источник видео не найден!", "Video source not found!");
        DataProcessor.x_FFMpegProcessVideoInputFail = text(
                "Ошибка при открытии источника видео!", "Error opening video source!");
        DataProcessor.x_UserPorcessCancel = text(
                "Прерывание процесса пользователем!", "Process interrupt occurred by the user!");
        DataProcessor.x_ErrorMakeIO = text(
                "Ошибка I/O при сборке!", "Final build I/O error!");
        DataProcessor.x_FinalMakeEnd = text(
                "Процесс сборки завершён.", "Final buid process is completed.");
        DataProcessor.x_WrongOutVideoFile = text(
                "Неверное имя файла-приёмника видео!", "Wrong name for the video destination file!");
        DataProcessor.x_WrongOutAudioFile = text(
                "Неверное имя файла-приёмника аудио!", "Wrong name for the audio destination file!");
        DataProcessor.x_WrongOutSubFile = text(
                "Неверное имя файла-приёмника субтитров!", "Wrong name for the subtitles destination file!");
        DataProcessor.x_ErrorFFMpegStart = text(
                "Ошибка запуска!", "Failed to start!");
        DataProcessor.x_Delete = text("Удалить", "Delete");
        DataProcessor.x_LeaveAsIs = text("Оставить как есть", "Leave as is");
        DataProcessor.x_WhatDoTemp = text(
                "Что делать с временными файлами?", "What to do with temporary files?");
        DataProcessor.x_Confirmation = text("Подтверждение", "Confirmation");
        DataProcessor.x_Finish = text("Завершить", "Finish");
        DataProcessor.x_WaitFinish = text("Ожидать завершения", "Expect completion");
        DataProcessor.x_FinishProcess = text(
                "Завершить процесс '%s' принудительно или ожидать ещё %d мсек.?",
                "Terminate the '%s' process or expect another %d ms.?");
        DataProcessor.x_FinishedProcess = text(
                "Процесс '%s' завершен принудительно!", "'%s' process is interrupted!");
        DataProcessor.x_CoderStarting = text(
                "Запуск процесса кодирования...", "Starting the process of encoding...");
        DataProcessor.x_CoderStarted = text(
                "Запущен процесс кодирования.", "Started the process of encoding.");

        // Err
        Err.x_InitError = text(
                "Ошибка инициализации журнала ошибок!", "Failure to initialize the error log!");

        // FileType
        FileType.NO.title = text("не определен", "not defined");
        FileType.DIR.title = text("каталог", "directory");
        FileType.EXE.title = text("EXE", "EXE");
        FileType.HDD.title = text("HDD", "HDD");

        // Files
        Files.x_BuildFileList = text(
                "Построение списка файлов источника...", "Building a list of source files...");
        Files.x_FileScaning = text("Сканирование файла (%d из %d)", "Scanning file (%d of %d)");
        Files.x_ScanFinish = text("Сканирование источника завершено", "Scanning a source is completed");
        Files.x_ScanFinishBreak = text("прервано", "interrupted");
        Files.x_SourceScaning = text("Сканирование источника...", "Scanning a source...");

        // GUIFileInfoPanel
        GUIFileInfoPanel.x_Cams = text("Камеры", "Cameras");
        GUIFileInfoPanel.x_Duration = text("Длительность", "Duration");
        GUIFileInfoPanel.x_DurationFormat = text(
                "%d час. %d мин. %d сек. %d мсек.", "%d h. %d m. %d s. %d ms.");
        GUIFileInfoPanel.x_End = text("Конец", "End");
        GUIFileInfoPanel.x_FirstKeyFrame = text("Первый ключевой кадр.", "The first key frame.");
        GUIFileInfoPanel.x_Freq = text("Частота", "Frequency");
        GUIFileInfoPanel.x_FreqFormat = text("%d кадр./сек.", "%d fps.");
        GUIFileInfoPanel.x_HintChangeZoom = text(
                "Изменение масштаба происходит по нажатию кнопки мыши на кадре.",
                "Zooming is done by pressing the mouse button on the frame.");
        GUIFileInfoPanel.x_NO = text("НЕТ", "NO");
        GUIFileInfoPanel.x_Name = text("Имя", "Name");
        GUIFileInfoPanel.x_Resolution = text("Разрешение", "Resolution");
        GUIFileInfoPanel.x_ResolutionFormat = text("%d x %d", "%d x %d");
        GUIFileInfoPanel.x_Size = text("Размер", "Size");
        GUIFileInfoPanel.x_Bytes = text("байт", "bytes");
        GUIFileInfoPanel.x_Start = text("Начало", "Start");
        GUIFileInfoPanel.x_Type = text("Тип", "Type");

        // GUIFileSelectDialog
        GUIFileSelectDialog.x_Cancel = text("Отмена", "Cancel");
        GUIFileSelectDialog.x_CancelSelect = text("Отмена выбора", "Cancel selection");
        GUIFileSelectDialog.x_DatailView = text("Детальный вид", "Detail view");
        GUIFileSelectDialog.x_Date = text("Дата/время", "Date/time");
        GUIFileSelectDialog.x_Detail = text("Детальный", "Detail");
        GUIFileSelectDialog.x_Fefresh = text("Обновить", "Refresh");
        GUIFileSelectDialog.x_FileName = text("Имя файла:", "File name:");
        GUIFileSelectDialog.x_FileType = text("Тип файлов:", "Type of files:");
        GUIFileSelectDialog.x_GoUp = text("На уровень вверх", "Level up");
        GUIFileSelectDialog.x_Home = text("Домашний каталог", "Home");
        GUIFileSelectDialog.x_List = text("Списком", "List");
        GUIFileSelectDialog.x_ListView = text("В виде списка", "List view");
        GUIFileSelectDialog.x_Name = text("Имя", "Name");
        GUIFileSelectDialog.x_Path = text("Каталог:", "Path");
        GUIFileSelectDialog.x_Select = text("Выбрать", "Select");
        GUIFileSelectDialog.x_SelectFile = text("Выбрать файл", "Select file");
        GUIFileSelectDialog.x_Size = text("Размер", "Size");
        GUIFileSelectDialog.x_View = text("Вид", "View");
        GUIFileSelectDialog.x_CantCreate = text(
                "Файл/каталог не может быть создан!",
                "File/directory can not be created!");
        GUIFileSelectDialog.x_Error = text("Ошибка", "Error");
        GUIFileSelectDialog.x_NotFound = text(
                "Выбранный файл/каталог не существует!",
                "Selected file/directory does not exist!");

        // GUIFilesPanel
        GUIFilesPanel.x_End = text("Конец", "End");
        GUIFilesPanel.x_Name = text("Имя", "Name");
        GUIFilesPanel.x_Size = text("Размер", "Size");
        GUIFilesPanel.x_Start = text("Начало", "Start");
        GUIFilesPanel.x_Type = text("Тип", "Type");

        // GUILogPanel
        GUILogPanel.x_Date = text("Дата/время", "Date/time");
        GUILogPanel.x_Message = text("Сообщение", "Message");

        // GUI_Main
        GUI_Main.x_TitleSuffix = text("(ознакомительная версия)", "(trial version)");
        GUI_Main.x_LabelInfo = text("Инфо:", "Info:");
        GUI_Main.x_TabSource = text("Источник", "Source");
        GUI_Main.x_TabProcess = text("Обработка", "Process");
        GUI_Main.x_TabState = text("Состояние", "State");
        GUI_Main.x_TabLog = text("Лог", "Log");
        GUI_Main.x_TabHelp = text("Справка", "Help");
        GUI_Main.x_Confirmation = text("Подтверждение", "Confirmation");
        GUI_Main.x_ExitQuest = text("Выйти из программы?", "Exit the program?");
        GUI_Main.x_Yes = text("Да", "Yes");
        GUI_Main.x_No = text("Нет", "No");
        GUI_Main.x_ButtonProcess = text("Обработка", "Process");
        GUI_Main.x_ButtonInterrupt = text("Прервать", "Cancel");

        // GUI_SourceSelect
        GUI_SourceSelect.x_All = text("< все >", "< all >");
        GUI_SourceSelect.x_Cam = text("Камера", "Camera");
        GUI_SourceSelect.x_GoScan = text("Сканировать", "Scan");
        GUI_SourceSelect.x_Hint = text(
                "Выбор конкретной камеры может существенно<br>уменьшить время сканирования источника при<br>больших объёмах данных!",
                "The choice of a particular camera can<br>significantly reduce the source scanning<br>time for large volumes of data!");
        GUI_SourceSelect.x_Select = text("Выбор", "Select");
        GUI_SourceSelect.x_Source = text("Источник", "Source");
        GUI_SourceSelect.x_SourceScan = text("Сканирование источника", "Scanning the source");
        GUI_SourceSelect.x_Type = text("Тип", "Type");
        GUI_SourceSelect.x_SelectSource = text(
                "Выбор файла/каталога источника", "Select the source file/directory");

        // GUI_TabAbout (интернационализируется внутри!)

        // GUI_TabProcess
        GUI_TabProcess.x_Audio = text("Аудио", "Audio");
        GUI_TabProcess.x_Cam = text("Камера", "Camera");
        GUI_TabProcess.x_Codec = text("Кодек", "Codec");
        GUI_TabProcess.x_CustomOption = text("Ручные настройки", "Custom settings");
        GUI_TabProcess.x_Evaluate = text("Оценка", "Evaluation"); // calculate
        GUI_TabProcess.x_File = text("Файл", "File");
        GUI_TabProcess.x_Format = text("Формат", "Format");
        GUI_TabProcess.x_FramePerSec = text("Кадр/сек", "FPS");
        GUI_TabProcess.x_Mode = text("Режим", "Mode");
        GUI_TabProcess.x_NotSave = text("Не сохранять", "Do not save");
        GUI_TabProcess.x_Period1 = text("Период c", "Period from");
        GUI_TabProcess.x_Period2 = text("по", "to");
        GUI_TabProcess.x_Select = text("Выбор", "Select");
        GUI_TabProcess.x_Resolution = text("Разрешение", "Resolution");
        GUI_TabProcess.x_Source = text("Источник", "Source");
        GUI_TabProcess.x_Sub = text("Титры", "Subtitles");
        GUI_TabProcess.x_ToFile = text("В файл", "In file");
        GUI_TabProcess.x_ToVideo = text("В видео", "In video");
        GUI_TabProcess.x_Video = text("Видео", "Video");
        GUI_TabProcess.x_WOConvert = text("Без преобразования", "Without transcoding");
        GUI_TabProcess.x_CalcEnd = text(
                "Подсчёт данных для обработки завершён.", "Calculation of data to process is completed.");
        GUI_TabProcess.x_CalcStart = text(
                "Подсчёт данных для обработки...", "Calculation of data to process...");
        GUI_TabProcess.x_SelectDestFile = text(
                "Выбор файла приёмника", "Select destination file");
        GUI_TabProcess.x_NotePreDecoding = text(
                "Декодируется как", "Decoded as");
        GUI_TabProcess.x_NoteSimple = text(
                "Кодек-видео - MPEG4, кодек-аудио - PCM16BIT, субтитры - файл SRT.",
                "Video-codec - MPEG4, audio-codec - PCM16BIT, subtitles - SRT file.");
        GUI_TabProcess.x_CheckExpert = text(
                "Расширенные настройки", "Advanced settings");

        // GUI_TabSource
        GUI_TabSource.x_Cam = text("Камера", "Camera");
        GUI_TabSource.x_NotSelected = text("не выбрана", "not defined");
        GUI_TabSource.x_Select = text("Выбор", "Select");
        GUI_TabSource.x_Source = text("Источник", "Source");
        GUI_TabSource.x_Type = text("Тип", "Type");

        // SourceFileFilter
        SourceFileFilter.instALL.description = text("Все файлы DVR", "All DVR files");
        SourceFileFilter.instEXE.description = text("Файлы EXE-архивов DVR", "DVR EXE-archives files");
        SourceFileFilter.instHDD.description = text("Файлы HDD DVR", "DVR HDD files");

        // Task
        Task.x_CanNotStarted = text("Задание не может быть запущено!", "Task can not run!");
        Task.x_Error = text("Ошибка", "Error");

        // 
        GUI_TabAbout.x_groups = new String[]{
            text("О программе"),
            text("Системные требования"),
            text("Возможности"),
            text("Типовый порядок работы")
        };

        GUI_TabAbout.x_labels = new String[]{
            text("<b>Назначение</b>"),
            text("Конвертация/извлечение данных видеонаблюдения регистраторов Microdigital."),
            text("<b>Версия</b>"),
            text(App.version + " от " + App.versionDate),
            text("<b>Автор</b>"),
            text("Докшин Алексей (все права принадлежат автору)"),
            text("<b>Контакты</b>"),
            text("dant.it@gmail.com"),
            text("<b>Лицензия</b>"),
            text("Ограниченная ознакомительная версия для ООО \"ЭМ ДИ РУС\""),
            text("Для запуска программы необходим Java SE (JRE/JDK) v1.6.x."),
            text("Страница загрузки:"),
            text("Для обработки и транскодирования требуется ffmpeg v0.8.x (при более ранних версиях возможно будет недоступно сохранение аудио и внедрение аудио/субтитров в видео)."),
            text("Страница загрузки:"),
            text("Детальная информация по установке, настройкам и работе содержится в файле <b>readme.txt</b>. Также освещён процесс извлечения данных с HDD регистратора."),
            text("<b>Исходные данные:</b>"),
            text("Файлы архивов выгружаемые видеорегистратором (*.exe)."),
            text("Файлы файловой системы видеорегистратора (da#####)."),
            text("<b>Обработка:</b>"),
            text("Выбор в качестве источника файла или каталога (при выборе каталога сканируются и принимаются к обработке все файлы каталога и вложенных подкаталогов подходящих форматов)."),
            text("Выбор в качестве источника каталога. При этом сканируются и принимаются к обработке все файлы каталога и вложенных подкаталогов, которые являются источниками. Обработка в порядке возрастания времени первого кадра файла."),
            text("Выбор диапазона времени для сохраняемых данных. Диапазон может покрывать как часть файла, так и несколько файлов - на выходе будет один файл."),
            text("Сохранение видео/аудио без перекодирования (аудио декодируется в PCM в любом случае!)."),
            text("Сохранение видео/аудио с перекодировкой в выбранный формат."),
            text("Сохранение информации о дате и времени в субтитрах."),
            text("Сохранение аудио/видео/субтитров в отдельные файлы."),
            text("Сохранение аудио и субтитров в файл видео."),
            text("<b>Восстановление:</b>"),
            text("При повреждении файла битые кадры исключаются из ряда, возможно появление артефактов."),
            text("<b>В планах:</b>"),
            text("Расширенная обработка повреждённых файлов."),
            text("На закладке <b><i>Источник</i></b>:"),
            text("Выбирается источник после чего происходит его предварительное сканирование."),
            text("Выбирается обрабатываемая камера из списка доступных."),
            text("На закладке <b><i>Обработка</i></b>:"),
            text("Выбирается период сохраняемых данных."),
            text("Выбирается файл-приёмник для видео и если нужно - формат кодирования, размер кадра, частоту кадров."),
            text("Выбирается режим обработки аудио и если нужно - файл-приёмник, формат кодирования."),
            text("Выбирается режим создания субтитров и если нужно - файл-приёмник, формат кодирования."),
            text("Переход на закладку <b><i>Лог</i></b> для контроля (не обязательно)."),
            text("Запуск обработки (возможно сделать находясь на любой закладке).")
        };
    }
}
