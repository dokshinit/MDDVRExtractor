/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 * Класс для работы с функционалом FFMpeg.
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public final class FFMpeg {

    /**
     * Список кодеков.
     */
    private static final ArrayList<FFCodec> codecs = new ArrayList<FFCodec>();
    /**
     * Паттерн для "парсинга" инфы о кодеке из вывода "ffmpeg -codecs".
     * Исключаем из списка форматы изображений (image).
     */
    private static final Pattern patternCodec =
            Pattern.compile("^\\ ([D\\.\\ ])([E\\.\\ ])([VAS\\.])([IS\\.\\ ])([LD\\.\\ ])([ST\\.\\ ])\\ "
            + "(\\w{1,})\\s{1,}((?:[\\S&&[^iJG]]|i(?!mage)|J(?!PEG)|G(?!IF)|\\s){1,})\\s*$");
    /**
     * Флаг наличия нужного кодека для финальной сборки аудио (в видео), а также
     * процессинга аудио.
     */
    public static boolean isAudio_pcm_s16le = false;
    /**
     * Флаг наличия нужного кодека для финальной сборки субтитров (в видео).
     */
    public static boolean isSub_srt = false;
    /**
     * Имя файла для запуска FFMpeg, может быть как просто имя файла или как
     * путь и имя файла - для случая, когда исполняемый файл находится в
     * каталоге ./ffmpeg.
     */
    public static String execName = "ffmpeg";

    /**
     * Возвращает текущий списо кодеков.
     *
     * @return
     */
    public static ArrayList<FFCodec> getCodecs() {
        return codecs;
    }

    /**
     * Проверяет инициализирован ли FFMpeg (если проблемы, то список кодеков
     * будет пуст).
     *
     * @return true - всё в порядке, false - ffmpeg не работает корректно.
     */
    public static boolean isWorking() {
        return !codecs.isEmpty();
    }

    /**
     * Инициализация. Заполняет список кодеков.
     */
    public static void init() {
        Process pr = null;
        try {
            execName = App.isWindows ? "ffmpeg.exe" : "ffmpeg";
            // Проверяем, есть ли исполняемый файл в локальном каталоге.
            // Если есть - устанавливаем абсолютный путь для его запуска.
            try {
                String s = new File(".").getCanonicalPath() + File.separator
                        + "ffmpeg" + File.separator + execName;
                if (App.isDebug) {
                    System.out.println("ffmpeg check = " + s);
                }
                if (new File(s).exists()) {
                    execName = s;
                }
            } finally {
                if (App.isDebug) {
                    System.out.println("ffmpeg exec = " + execName);
                }
            }

            codecs.clear();
            pr = new Cmd("-codecs", "-").startProcess();
            InputStream is = pr.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String s;
            int flag = 0;
            while ((s = br.readLine()) != null) {
                if (flag == 0 && s.startsWith("Codecs")) {
                    flag = 1;
                    continue;
                }
                if (flag > 0) {
                    if (s.isEmpty()) {
                        break;
                    }
                    addCodec(s);
                }
            }
            pr.destroy();
        } catch (IOException e) {
            Err.log(e);
        } finally {
            if (pr != null) {
                pr.destroy();
            }
        }
    }

    /**
     * Парсит строку и в случае успеха - добавляет кодек в список.
     *
     * @param s Строка с кодеком.
     */
    private static void addCodec(String s) {
        Matcher m = patternCodec.matcher(s);
        // Отфильтровываем по схеме.
        if (m.matches()) {
            char c = m.group(3).trim().charAt(0);
            String name = m.group(7);
            codecs.add(new FFCodec(name, m.group(8),
                    !m.group(1).trim().isEmpty(), !m.group(2).trim().isEmpty(),
                    c == 'V', c == 'A', c == 'S'));
            if (c == 'A' && name.equals("pcm_s16le")) {
                isAudio_pcm_s16le = true;
            }
            if (c == 'S' && name.equals("srt")) {
                isSub_srt = true;
            }
        }
    }

    /**
     * Класс для храрнения инфы о кодеках FFMpeg.
     */
    public static class FFCodec {

        /**
         * Имя кодека.
         */
        public String name;
        /**
         * Название кодека.
         */
        public String title;
        /**
         * Модификаторы.
         */
        public boolean isDecode, isEncode, isVideo, isAudio, isSub;

        /**
         * Конструктор.
         *
         * @param name Имя кодека.
         * @param title Название.
         * @param isDecode Возможность декодирования.
         * @param isEncode Возможность кодирования.
         * @param isVideo Кодек видео.
         * @param isAudio Кодек аудио.
         * @param isSub Кодек субтитров.
         */
        public FFCodec(String name, String title, boolean isDecode, boolean isEncode,
                boolean isVideo, boolean isAudio, boolean isSub) {
            this.name = name;
            this.title = (title == null || title.trim().length() == 0) ? name : title;
            this.isDecode = isDecode;
            this.isEncode = isEncode;
            this.isVideo = isVideo;
            this.isAudio = isAudio;
            this.isSub = isSub;
        }
    }

    static void flushInput(InputStream is) {
        try {
            byte[] buffer = new byte[512];
            int i;
            while ((i = is.available()) > 0) {
                int len = Math.min(i, buffer.length);
                is.read(buffer, 0, len);
            }
        } catch (IOException ex) {
        }
    }

    /**
     * Возвращает изображение из первого ключевого кадра файла для указанной
     * камеры.
     *
     * @param info Инфо о файле.
     * @param cam Номер камеры.
     * @return Изображение (в случае неудачи = null).
     */
    public static BufferedImage getFirstKeyFrameImage(FileInfo info, int cam) {
        BufferedImage image = null;
        Frame frame = Files.getFirstKeyFrame(info, cam);
        Process process = null;
        if (frame != null) {
            try {
                InputFile in = new InputFile(info.fileName);
                in.seek(frame.pos + frame.getHeaderSize());
                byte[] ba = new byte[frame.videoSize];
                in.read(ba, frame.videoSize);
                in.close();

                Dimension d = info.frameFirst.getResolution();
                Cmd cmd = new Cmd("-f", "h264", "-r", "1", "-i", "-", "-f", "image2",
                        "-dframes", "1", "-s", "" + d.width + "x" + d.height, "-");
                if (App.isDebug) {
                    App.log("ImageExtract: " + cmd.toString());
                }

                process = cmd.startProcess();
                InputStream is = process.getInputStream();
                OutputStream os = process.getOutputStream();

                os.write(ba, 0, ba.length);
                // Вынуждаем обработать данные (если не закрыть - во вх.потоке 
                // данные не появляются, даже если делать flush()!).
                os.flush();
                os.close();

                // Поток тормознут, если exitValue не вызывает исключение!
                image = ImageIO.read(is);
                is.close();
            } catch (Exception e) {
                Err.log(e);
            } finally {
                if (process != null) {
                    process.destroy(); // На всякий случай.
                }
            }
        }
        return image;
    }

    /**
     * Воспомогательный класс для удобной "постройки" команд для ffmpeg.
     */
    public static class Cmd {

        /**
         * Токены команды (неделимые параметры).
         */
        private ArrayList<String> tokens = new ArrayList<String>();

        /**
         * Конструктор - создаёт команду по умолчанию с токеном для запуска
         * ffmpeg.
         */
        public Cmd() {
            this(true);
        }

        /**
         * Конструктор - создаёт команду (с токеном для запуска ffmpeg или
         * пустую).
         *
         * @param isAddFfmpeg Флаг создания токена: true - создать, false - не
         * создавать.
         */
        public Cmd(boolean isAddFfmpeg) {
            if (isAddFfmpeg) {
                tokens.add(execName);
            }
        }

        /**
         * Конструктор - создаёт команду с токеном запуска ffmpeg и добавляет
         * все токены из аргументов.
         *
         * @param ar Токены.
         */
        public Cmd(String... ar) {
            this();
            tokens.addAll(Arrays.asList(ar));
        }

        /**
         * Добавление перечисленных токенов в команду.
         *
         * @param ar Токены.
         * @return Ссылка на себя для сцепки.
         */
        public Cmd add(String... ar) {
            tokens.addAll(Arrays.asList(ar));
            return this;
        }

        /**
         * Добавление токенов из указанной команды.
         *
         * @param c Команда.
         * @return Ссылка на себя для сцепки.
         */
        public Cmd add(Cmd c) {
            tokens.addAll(c.getCollection());
            return this;
        }

        /**
         * Возвращает массив строк токенов.
         *
         * @return Массив строк токенов.
         */
        public String[] getArray() {
            return tokens.toArray(new String[1]);
        }

        /**
         * Возвращает токены в виде коллекции строк.
         *
         * @return Коллекция строк.
         */
        public Collection<String> getCollection() {
            return tokens;
        }

        /**
         * Замещение в токенах команды параметров помещенных оригинальными
         * маркерами.
         *
         * @param sfps Строка для подстановки - частота.
         * @param ssize Строка для подстановки - размеры.
         * @return Ссылка на себя для сцепки.
         */
        public Cmd replaceOrigs(String sfps, String ssize) {
            for (int i = 0; i < tokens.size(); i++) {
                if (tokens.get(i).equals("{origfps}")) {
                    tokens.set(i, sfps);
                } else if (tokens.get(i).equals("{origsize}")) {
                    tokens.set(i, ssize);
                }
            }
            return this;
        }

        /**
         * Переопределение представления в качестве строки.
         *
         * @return Строка состоящая из токенов разделённых пробелами.
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (String t : tokens) {
                sb.append(t).append(" ");
            }
            return sb.toString().trim();
        }

        /**
         * Старт процесса выполнения команды.
         *
         * @return Процесс выполнения команды.
         * @throws IOException Ошибка выполнения команды.
         */
        public Process startProcess() throws IOException {
            Process p = Runtime.getRuntime().exec(getArray());
            if (App.isWindows) {
                p.getErrorStream().close(); // Без этого не начинается процессинг в винде!!!
            }
            int res = 0;
            try {
                res = p.exitValue();
            } catch (IllegalThreadStateException ex) {
            }
            return res == 0 ? p : null;
        }
    }
}
