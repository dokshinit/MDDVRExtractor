/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author lex
 */
public class InputData {

    private String name = null;
    private RandomAccessFile in = null;

    public InputData(String fileName) throws FileNotFoundException, IOException {
        name = fileName;
        in = new RandomAccessFile(name, "r");
        in.seek(0);
    }
    
    public long getSize() throws IOException {
        return in.length();
    }

    public void seek(long n) throws IOException {
        in.seek(n);
    }

    public void skip(int n) throws IOException {
        while (n > 0) {
            n -= in.skipBytes(n);
        }
    }

    public void read(byte[] ba, int size) throws IOException {
        int readed = 1, pos = 0;
        while (readed >= 0 && pos < size) {
            readed = in.read(ba, pos, size - pos);
            pos += readed;
        }
    }

    public void close() throws IOException {
        if (in != null) {
            in.close();
        }
    }
}
