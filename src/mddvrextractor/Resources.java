/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mddvrextractor;

import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author lex
 */
public class Resources {

    /**
     * Сопоставляется при инициализации, если ресурсы в jar.
     */
    private static JarFile jar = null;
    public static String x_AccessError, x_EnvironmentNotInit, x_AccessToEmbeddedError;

    /**
     * Возвращает поток ресурса (из файла или из jar архива).
     *
     * @param name Имя файла.
     * @return Поток для чтения.
     * @throws IOException Исключение при создании потока.
     */
    public static InputStream getStream(String name) throws Exception {
        try {
            String sep = File.separator;
            if (App.isJarRun) {
                //JarFile jar = new JarFile(App.dir + sep + App.jar);
                return jar.getInputStream(jar.getJarEntry(name));
            } else {
                return new FileInputStream(App.dir + sep + "build" + sep + "classes" + sep + name);
            }
        } catch (Exception ex) {
            throw new Exception(x_AccessError);
        }
    }

    /**
     * Возвращает ресурс-изображение.
     *
     * @param name Имя ресурса (без расширения).
     * @return Изображение (null - если ошибка).
     */
    public static Image getImage(String name) {
        try {
            // Внутри ресурса разделитель всегда обратный слеш!
            return ImageIO.read(getStream("mddvrextractor/resources/" + name + ".png"));
        } catch (Exception ex) {
            return null;
        }
    }

    public static ImageIcon getImageIcon(String name) {
        Image image = getImage(name);
        return image == null ? null : new ImageIcon(image);
    }

    /**
     * Возвращает ресурс-шрифт.
     *
     * @param name Имя ресурса (без расширения).
     * @return Шрифт (null - если ошибка).
     */
    public static Font getFont(boolean isttf, String name) {
        try {
            // Внутри ресурса разделитель всегда обратный слеш!
            return Font.createFont(
                    isttf ? Font.TRUETYPE_FONT : Font.TYPE1_FONT,
                    getStream("mddvrextractor/resources/" + name + (isttf ? ".ttf" : ".pfb")));
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Инициализация ресурсов.
     *
     * @throws FaultException Исключение при ошибках.
     */
    public static void init() throws Exception {
        if (!App.isEnvironmentInit) {
            throw new Exception(x_EnvironmentNotInit);
        }
        try {
            if (App.isJarRun) {
                jar = new JarFile(App.dir + File.separator + App.jar);
            }
        } catch (Exception ex) {
            throw new Exception(x_AccessToEmbeddedError);
        }

        GUI.init();
    }
    ////////////////////////////////////////////////////////////////////////////
    // РЕСУРСЫ
    ////////////////////////////////////////////////////////////////////////////
    //

    /**
     * Ресурсы приложения.
     */
    public static class GUI {

        public static ImageIcon imageAboutLogo, imageLogo, imageFlagRus, imageFlagEng;
        public static Font font, fontBold;

        /**
         * Инициализация ресурсов.
         */
        static void init() {
            // Загрузка изображений из ресурсов.
            imageAboutLogo = getImageIcon("TabAbout.Logo");
            imageLogo = getImageIcon("TabPane.Logo");
            imageFlagRus = getImageIcon("TabPane.Flag.Rus");
            imageFlagEng = getImageIcon("TabPane.Flag.Eng");

            font = getFont(true, "Font.Europa.Plain");
            if (font != null) {
                font = font.deriveFont(12.0f);
            }
            fontBold = getFont(true, "Font.Europa.Bold");
            if (fontBold != null) {
                fontBold = fontBold.deriveFont(Font.PLAIN, 11.0f);
            }
        }
    }
}
