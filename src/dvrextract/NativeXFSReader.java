/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

import java.io.FileNotFoundException;
import java.io.IOException;
import xfsengine.XFS.Node;
import xfsengine.XFS.XFSException;

/**
 * Реализация ридера данных для работы с XFS (в т.ч. DVR XFSv1).
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
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
        try {
            return in.read(ba, index, size);
        } catch (XFSException ex) {
            throw new IOException(ex.getMessage(), ex);
        }
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
