/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import java.io.FileNotFoundException;
import java.io.IOException;
import xfsengine.XFS.Node;
import xfsengine.XFS.XFSException;

/**
 *
 * @author lex
 */
public class NativeXFSReader implements NativeReader {

    /**
     * Имя файла.
     */
    private String name;
    /**
     * Узел для доступа к данным.
     */
    private Node in;

    /**
     * Конструктор.
     * @param fileDesc Имя файла-источника.
     * @throws FileNotFoundException Ошибка при отсутствии файла.
     * @throws IOException Ошибка при позиционировании.
     */
    public NativeXFSReader(long id, String name) throws IOException, XFSException  {
        this.name = name;
        in = null;
        if (name != null && id > 0) {
            in = App.Source.getXFS().openNode(id);
            in.seek(0);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() throws IOException {
        return in.getSize();
    }

    @Override
    public void seek(long n) throws IOException {
        in.seek(n);
    }

    @Override
    public void skip(int n) throws IOException {
        in.seek(in.getPosition() + n);
    }

    @Override
    public int read(byte[] ba, int index, int size) throws IOException {
        return in.read(ba, index, size);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public void closeSafe() {
        try {
            close();
        } catch (IOException ex) {
        }
    }
}
