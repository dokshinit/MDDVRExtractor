/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

/**
 *
 * @author lex
 */
public enum FileType {
    
    NO(-1, "не определён"), 
    DIR(0, "каталог"), 
    EXE(1, "EXE"), 
    HDD(2, "HDD");

    public int id; // Код.
    public String title; // Название. 
    
    FileType(int id, String title) {
        this.id = id;
        this.title = title;
    }
}
