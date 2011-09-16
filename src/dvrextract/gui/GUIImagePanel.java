package dvrextract.gui;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JPanel;

/**
 * Панель содержащая изображение.
 * @author lex
 */
public class GUIImagePanel extends JPanel {

    // Изображение.
    private Image image;

    /**
     * Конструктор.
     */
    public GUIImagePanel() {
        this.image = null;
    }
    
    /**
     * Конструктор.
     * @param image Изображение. 
     */
    public GUIImagePanel(Image image) {
        this.image = image;
    }

    /**
     * Возвращает изображение панели.
     * @return Изображение.
     */
    public Image getImage() {
        return image;
    }

    /**
     * Устанавливает изображение панели.
     * @param image Изображение.
     */
    public void setImage(Image image) {
        this.image = image;
        validate();
        repaint();
    }

    /**
     * Отрисовка компонента.
     * @param g Контекст.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Отрисовываем поверх всей панели (маштабируемое) изображение.
        if(image != null){
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        }
    }
}
