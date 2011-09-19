package dvrextract.gui;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 * Панель содержащая изображение.
 * @author lex
 */
public class GUIImagePanel extends JPanel {

    // Изображение.
    private Image image;
    private JLabel label;

    /**
     * Конструктор.
     */
    public GUIImagePanel() {
        image = null;
        setLayout(new MigLayout("fill","[center]","[center]"));
        add(label = new JLabel());
    }
    
    public GUIImagePanel(String title) {
        this();
        setLabelText(title);
    }
    
    public final void setLabelText(String title) {
        label.setText(title);
    }
    
    public final JLabel getLabel() {
        return label;
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
