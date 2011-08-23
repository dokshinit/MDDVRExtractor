/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import javax.swing.*;
import java.awt.*;

public class ImagePanel extends JPanel {

    private Image image;

    public ImagePanel() {
    }
    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if(image != null){
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        }
    }
}
