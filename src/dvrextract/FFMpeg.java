package dvrextract;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/**
 * Класс для работы с функционалом FFMpeg.
 * @author lex
 */
public final class FFMpeg {

    // Список кодеков.
    private static ArrayList<FFCodec> codecs = new ArrayList<FFCodec>();

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
            e.printStackTrace();
        } finally {
            if (pr != null) {
                pr.destroy();
            }
        }
    }

    public static ArrayList<FFCodec> getCodecs() {
        return codecs;
    }

    /**
     * Парсит строку и в случае успеха - добавляет кодек в список.
     * @param s Строка с кодеком.
     */
    private static void addCodec(String s) {
        String name = "", title = "";
        boolean isDecode = false, isEncode = false, isVideo = false, isAudio = false, isSub = false;

        if (s.length() < 11) {
            return;
        }

        char c = s.charAt(0);
        if (c != ' ') {
            return;
        }

        c = s.charAt(1);
        if (c == ' ') {
        } else if (c == 'D') {
            isDecode = true;
        } else {
            return;
        }

        c = s.charAt(2);
        if (c == ' ') {
        } else if (c == 'E') {
            isEncode = true;
        } else {
            return;
        }

        c = s.charAt(3);
        if (c == ' ') {
        } else if (c == 'V') {
            isVideo = true;
        } else if (c == 'A') {
            isAudio = true;
        } else if (c == 'S') {
            isSub = true;
        } else {
            return;
        }

        c = s.charAt(4);
        if (c != ' ' && c != 'S') {
            return;
        }

        c = s.charAt(5);
        if (c != ' ' && c != 'D') {
            return;
        }

        c = s.charAt(6);
        if (c != ' ' && c != 'T') {
            return;
        }

        c = s.charAt(7);
        if (c != ' ') {
            return;
        }

        c = s.charAt(8);
        if (c == ' ') {
            return;
        }

        int pos = s.indexOf(' ', 8);
        name = s.substring(8, pos).trim();
        title = s.substring(pos + 1).trim();
        // Отсеиваем те, что предназначены для создания снапшотов.
        if (title.indexOf("image") != -1) {
            return;
        }
        codecs.add(new FFCodec(name, title, isDecode, isEncode, isVideo, isAudio, isSub));
//        App.log("FFMpeg: CODEC=" + name + ", " + title + ", ["
//                + (isDecode ? 'D' : ' ') + (isEncode ? 'E' : ' ')
//                + (isVideo ? 'V' : ' ') + (isAudio ? 'A' : ' ') + (isSub ? 'S' : ' ') + "]");
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
                InputData in = new InputData(info.fileName);
                in.seek(frame.pos + frame.getHeaderSize());
                byte[] ba = new byte[frame.videoSize];
                in.read(ba, frame.videoSize);
                in.close();

                pr = Runtime.getRuntime().exec("ffmpeg -i - -r 1 -s 704x576 -f image2 -");
                InputStream is = pr.getInputStream();
                OutputStream os = pr.getOutputStream();
                os.write(ba, 0, ba.length);
                os.close(); // Вынуждаем обработать данные.
                image = ImageIO.read(is);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (pr != null) {
                    pr.destroy();
                }
            }
        }
        return image;
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
        public FFCodec(String name, String title, boolean isDecode, boolean isEncode, boolean isVideo, boolean isAudio, boolean isSub) {
            this.name = name;
            this.title = title;
            this.isDecode = isDecode;
            this.isEncode = isEncode;
            this.isVideo = isVideo;
            this.isAudio = isAudio;
            this.isSub = isSub;
        }
    }
}
