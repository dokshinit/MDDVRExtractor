/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract.lang;

import dvrextract.App;
import dvrextract.DataProcessor;
import dvrextract.Err;

/**
 *
 * @author lex
 */
public class Lang {
    // App
    public static String app_err1, app_err2, app_err3, app_err4;
    //
    
    public static void init(int lang) {
        if (lang <= 0) { // Русский
            
            // App
            App.x_LAFNotFound = "Не найден L&F";
            App.x_LAFError = "Ошибка включения L&F";
            App.x_FFMpegWrong = "Некорректная работа FFMPEG!";
            App.x_CodecsWrong = "Ошибка запроса кодеков";
            
            // Err
            Err.x_InitError = "Ошибка инициализации лога!";
            
            // DataProcessor
            DataProcessor.x_ProcessSource = "Обработка источника...";
            DataProcessor.x_ProcessFile = "Обработка файла (%d из %d)";
            DataProcessor.x_ProcessEnd = "Процесс кодирования завершён.";
            DataProcessor.x_ProcessSourceEnd = "Обработка источника завершена.";
            DataProcessor.x_FinalMakeStart = "Запуск процесса сборки...";
            DataProcessor.x_ErrorFinalMakeStart = "Ошибка запуска процесса сборки!";
            DataProcessor.x_FinalMakeStarted = "Запущен процесс сборки.";
            DataProcessor.x_FFMpegVideoInputNotFound = "FFMpeg process video input not found!";
            DataProcessor.x_FFMpegProcessVideoInputFail = "FFMpeg process video input fail!";
            DataProcessor.x_UserPorcessCancel = "Прерывание процесса пользователем!";
            DataProcessor.x_ErrorMakeIO = "Ошибка IO при сборке!";
            DataProcessor.x_FinalMakeEnd = "Процесс сборки завершён.";
            DataProcessor.x_WrongOutVideoFile = "Неверное имя файла-приёмника видео!";
            DataProcessor.x_WrongOutAudioFile = "Неверное имя файла-приёмника аудио!";
            DataProcessor.x_WrongOutSubFile = "Неверное имя файла-приёмника субтитров!";
            DataProcessor.x_ErrorFFMpegVideoStart = "Ошибка запуска FFMpeg-video!";
            DataProcessor.x_ErrorFFMpegAudioStart = "Ошибка запуска FFMpeg-audio!";
            DataProcessor.x_ErrorFFMpegSubStart = "Ошибка запуска FFMpeg-subtitles!";
            DataProcessor.x_Delete = "Удалить";
            DataProcessor.x_LeaveAsIs = "Оставить как есть";
            DataProcessor.x_WhatDoTemp = "Что делать с временными файлами?";
            DataProcessor.x_Confirmation = "Подтверждение";
            DataProcessor.x_Finish = "Завершить";
            DataProcessor.x_WaitFinish = "Ожидать завершения";
            DataProcessor.x_FinishProcess1 = "Завершить процесс '";
            DataProcessor.x_FinishProcess2 = "' принудительно или ожидать ещё ";
            DataProcessor.x_FinishProcess3 = " мсек.?";
            DataProcessor.x_FinishedProcess1 = "Процесс '";
            DataProcessor.x_FinishedProcess2 = "' завершен принудительно!";
            DataProcessor.x_CoderStarting = "Запуск процесса кодирования...";
            DataProcessor.x_CoderStarted = "Запущен процесс кодирования.";
            
        } else if (lang == 1) { // English
            
            // App
            App.x_LAFNotFound = "Not found L&F";
            App.x_LAFError = "Error enable L&F";
            App.x_FFMpegWrong = "Incorrect FFMPEG!";
            App.x_CodecsWrong = "Codecs request failed";
            
            // Err
            Err.x_InitError = "Log initialization error!";
            
            // DataProcessor
            DataProcessor.x_ProcessSource = "Source processing...";
            DataProcessor.x_ProcessFile = "File processing (%d of %d)";
            DataProcessor.x_ProcessEnd = "The encoding process is completed.";
            DataProcessor.x_ProcessSourceEnd = "Source processing is completed.";
            DataProcessor.x_FinalMakeStart = "Final build starting...";
            DataProcessor.x_ErrorFinalMakeStart = "Failed to start the build process!";
            DataProcessor.x_FinalMakeStarted = "Final build is started.";
            DataProcessor.x_FFMpegVideoInputNotFound = "FFMpeg process video input not found!";
            DataProcessor.x_FFMpegProcessVideoInputFail = "FFMpeg process video input fail!";
            DataProcessor.x_UserPorcessCancel = "Interrupting by user!";
            DataProcessor.x_ErrorMakeIO = "IO error when final building!";
            DataProcessor.x_FinalMakeEnd = "The build process is completed.";
            DataProcessor.x_WrongOutVideoFile = "Wrong name for the video destination!";
            DataProcessor.x_WrongOutAudioFile = "Wrong name for the audio destination!";
            DataProcessor.x_WrongOutSubFile = "Wrong name for the subtitles destination!";
            DataProcessor.x_ErrorFFMpegVideoStart = "Failed to start FFMpeg-video!";
            DataProcessor.x_ErrorFFMpegAudioStart = "Failed to start FFMpeg-audio!";
            DataProcessor.x_ErrorFFMpegSubStart = "Failed to start FFMpeg-subtitles!";
            DataProcessor.x_Delete = "Delete";
            DataProcessor.x_LeaveAsIs = "Leave as is";
            DataProcessor.x_WhatDoTemp = "What to do with temporary files?";
            DataProcessor.x_Confirmation = "Confirmation";
            DataProcessor.x_Finish = "Finish";
            DataProcessor.x_WaitFinish = "Wait the completion";
            DataProcessor.x_FinishProcess1 = "Forcibly terminate the process '";
            DataProcessor.x_FinishProcess2 = "' or wait for another ";
            DataProcessor.x_FinishProcess3 = " msec.?";
            DataProcessor.x_FinishedProcess1 = "The process '";
            DataProcessor.x_FinishedProcess2 = "' forcibly terminated!";
            DataProcessor.x_CoderStarting = "Starting the process of encoding...";
            DataProcessor.x_CoderStarted = "Started the process of encoding.";
            
        }
    }
}
