/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract;

import java.io.IOException;
import mddvrextract.xfsengine.XFS.Node;
import mddvrextract.xfsengine.XFS.XFSException;

/**
 * Реализация ридера данных для работы с XFS (в т.ч. DVR XFSv1).
 *
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
     *
     * @param name Имя файла-источника.
     * @throws IOException Ошибка при позиционировании.
     * @throws XFSException Ошибка XFS.
     */
    public NativeXFSReader(String name) throws IOException, XFSException {
        if (name == null) {
            throw new IOException("Name is null!");
        }
        this.name = name;
        in = App.Source.getXFS().openNode(name);
        in.seek(0);
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
        // Фактически файлы отсутствуют - закрывать ничего не надо.
        // Т.к. при закрытии узла (из кэша) в кэше оставался бы нерабочий узел (!!!).
        // Вместо этого в конце работы с ФС закрывается файл устройства!
    }

    @Override
    public void closeSafe() {
    }
}
