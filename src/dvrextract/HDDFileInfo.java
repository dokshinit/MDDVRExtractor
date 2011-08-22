/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import java.util.Comparator;
import java.util.Date;

/**
 *
 * @author lex
 */
public class HDDFileInfo {

    public String fileName;
    public long fileSize;
    public int camNumber;
    public Frame frameFirst;
    public Frame frameLast;
    //
    public boolean isSelected; // для интерактивного выбора

    private static final Comparator<HDDFileInfo> comparator = new Comparator<HDDFileInfo>() {

        @Override
        public int compare(HDDFileInfo o1, HDDFileInfo o2) {
            long t1 = o1.frameFirst.time.getTime();
            long t2 = o2.frameFirst.time.getTime();
            return t1 == t2 ? 0 : (t1 < t2 ? -1 : 1);
        }
    };

    public static Comparator<HDDFileInfo> getComparator() {
        return comparator;
    }
}
