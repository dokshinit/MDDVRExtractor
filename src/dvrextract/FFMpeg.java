package dvrextract;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 * Класс для работы с функционалом FFMpeg.
 * @author lex
 */
public final class FFMpeg {

    // Список кодеков.
    private static final ArrayList<FFCodec> codecs = new ArrayList<FFCodec>();
    // Паттерн для "парсинга" инфы о кодеке из вывода "ffmpeg -codecs".
    // Исключаем из списка форматы изображений (image).
    private static final Pattern patternCodec =
            Pattern.compile("^\\ ([D\\ ])([E\\ ])([VAS])([S\\ ])([D\\ ])([T\\ ])\\ "
            + "(\\w{1,})\\s{1,}((?:[\\S&&[^iJG]]|i(?!mage)|J(?!PEG)|G(?!IF)|\\s){1,})\\s*$");

    /**
     * Возвращает текущий списо кодеков.
     * @return 
     */
    public static ArrayList<FFCodec> getCodecs() {
        return codecs;
    }

    /**
     * Проверяет инициализирован ли FFMpeg (если проблемы, то список кодеков будет пуст).
     * @return true - всё в порядке, false - ffmpeg не работает корректно.
     */
    public static boolean isWork() {
        return !codecs.isEmpty();
    }

    /**
     * Инициализация. Заполняет список кодеков.
     */
    public static void init() {
        Process pr = null;
        try {
            codecs.clear();
            pr = Runtime.getRuntime().exec("ffmpeg -codecs -");
            InputStream is = pr.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String s = null;
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
     * @param s Строка с кодеком.
     */
    private static void addCodec(String s) {
        Matcher m = patternCodec.matcher(s);
        // Отфильтровываем по схеме.
        if (m.matches()) {
            char c = m.group(3).trim().charAt(0);
            codecs.add(new FFCodec(m.group(7), m.group(8),
                    !m.group(1).trim().isEmpty(), !m.group(2).trim().isEmpty(),
                    c == 'V', c == 'A', c == 'S'));
        }
    }

    /**
     * Класс для храрнения инфы о кодеках FFMpeg.
     */
    public static class FFCodec {

        // Имя кодека.
        public String name;
        // Название кодека.
        public String title;
        // Модификаторы.
        public boolean isDecode, isEncode, isVideo, isAudio, isSub;

        /**
         * Конструктор.
         */
        public FFCodec(String name, String title, boolean isDecode, boolean isEncode,
                boolean isVideo, boolean isAudio, boolean isSub) {
            this.name = name;
            this.title = title;
            this.isDecode = isDecode;
            this.isEncode = isEncode;
            this.isVideo = isVideo;
            this.isAudio = isAudio;
            this.isSub = isSub;
        }
    }

    /**
     * Возвращает изображение из первого ключевого кадра файла для указанной камеры.
     * @param info Инфо о файле.
     * @param cam Номер камеры.
     * @return Изображение (в случае неудачи = null).
     */
    public static BufferedImage getFirstFrameImage(FileInfo info, int cam) {
        BufferedImage image = null;
        Frame frame = Files.getFirstMainFrame(info, cam);
        Process pr = null;
        if (frame != null) {
            try {
                InputFile in = new InputFile(info.fileName);
                in.seek(frame.pos + frame.getHeaderSize());
                byte[] ba = new byte[frame.videoSize];
                in.read(ba, frame.videoSize);
                in.close();
                Dimension d = info.frameFirst.getResolution();

                pr = Runtime.getRuntime().exec("ffmpeg -i - -r 1 -s " + d.width + "x" + d.height + " -f image2 -");
                InputStream is = pr.getInputStream();
                OutputStream os = pr.getOutputStream();
                os.write(ba, 0, ba.length);
                // Вынуждаем обработать данные (если не закрыть - во вх.потоке 
                // данные не появляются, даже если делать flush()!).
                os.close();
                // Поток тормознут, если exitValue не вызывает исключение!
                image = ImageIO.read(is);
            } catch (IOException e) {
                Err.log(e);
            } finally {
                if (pr != null) {
                    pr.destroy(); // На всякий случай.
                }
            }
        }
        return image;
    }
}
