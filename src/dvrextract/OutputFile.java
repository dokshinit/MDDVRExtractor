/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author lex
 */
public class OutputFile {

    private String name = null;
    private FileOutputStream fout = null;

    public OutputFile(String fileName) throws FileNotFoundException {
        name = fileName;
        if (name != null) {
            File ff = new File(name);
            if (ff.exists()) {
                ff.delete();
            }
            fout = new FileOutputStream(name);
        }
    }

    public void write(byte[] ba, int offset, int size) throws IOException {
        if (fout != null) {
            fout.write(ba, offset, size);
        }
    }

    public void close() throws IOException {
        if (fout != null) {
            fout.flush();
            fout.close();
        }
    }
}
