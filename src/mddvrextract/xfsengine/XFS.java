/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract.xfsengine;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * Файловая система XFS.
 *
 * <pre>
 * Вариант с кэшированием и ленивой ступенчатой инициализацией:
 * 1. Тело узла - считывается при открытии узла.
 * 2. Экстенты узла - считываются при первом чтении данных узла (или при
 *    открытии узла, если формат не дерево).
 * Для каталога:
 * 3. Список узлов каталога - при первой команде list (или при открытии узла,
 *    если формат локальный), при этом считывания узлов не происходит!.
 *
 * TODO: Реализовать поиск файла по всей ФС - вынести в инструменты.
 * TODO: Реализовать копирование файла из ФС в обычный файл - вынести в
 * инструменты.
 * </pre>
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class XFS {

    /**
     * Устройство.
     */
    public Device device;
    /**
     * Описатель суперблока ФС.
     */
    public SuperBlock sblock;
    /**
     * Корневой узел.
     */
    public Node root;

    /**
     * Открытие ФС на указанном устройстве.
     *
     * @param dev Устройство XFS.
     * @throws IOException Ошибка ввода/вывода.
     * @throws XFSException Ошбика XFS.
     */
    public XFS(Device dev) throws IOException, XFSException {
        device = dev;
        sblock = new SuperBlock();
    }

    /**
     * Закрытие ФС. Происходит только отвязка от устройства, суперблока и
     * корневого узла.
     *
     * @throws IOException Ошибка ввода/вывода.
     */
    public void close() throws IOException {
        sblock = null;
        device = null;
        root = null;
    }

    /**
     * Открытие узла по его номеру. В отличии от окрытия по имени не опирается
     * на корневой узел (!) и его структуру - чтение идёт напрямую минуя общий
     * кэш.
     *
     * @param id Номер узла.
     * @return Узел.
     * @throws IOException Ошибка ввода/вывода.
     * @throws XFSException Ошибка XFS.
     */
    public Node openNode(long id) throws IOException, XFSException {
        return new Node(id);
    }

    /**
     * Открытие узла по его полному имени. Имя распарсивается, и начиная от
     * корневого узла находится искомый узел. При этом информация в считанном
     * объеме кэшируется в корневом узле.
     *
     * @param name Полное имя узла.
     * @return Узел.
     * @throws IOException Ошибка ввода/вывода.
     * @throws XFSException Ошибка XFS.
     */
    public Node openNode(String name) throws IOException, XFSException {
        if (name == null) {
            throw new XFSException("Wrong filename: is null!");
        }
        name = name.trim();
        if (name.isEmpty()) {
            throw new XFSException("Wrong filename: is zero length!");
        }
        if (name.charAt(0) != '/') {
            throw new XFSException("Wrong filename: root absent!");
        }
        if (root == null) {
            root = new Node(sblock.rootino);
        }
        Node n = root;
        int stpos = 1, endpos = 1;
        int len = name.length();
        loop:
        while (stpos < len) {
            if (endpos == len || name.charAt(endpos) == '/') {
                if (endpos - stpos == 0) {
                    // Двойной '/' - пропускаем.
                    stpos = endpos + 1;
                    endpos = stpos;
                } else {
                    if (!n.isDirectory()) {
                        throw new XFSException("Wrong filename: is not dir! " + name.substring(0, endpos));
                    }
                    ArrayList<DirEntry> fileList = n.fileList();
                    for (int i = 0; i < fileList.size(); i++) {
                        DirEntry e = fileList.get(i);
                        if (e.fileName.equals(name.substring(stpos, endpos))) {
                            if (e.node == null) {
                                e.node = openNode(e.idNode);
                            }
                            n = e.node;
                            stpos = endpos + 1;
                            endpos = stpos;
                            continue loop;
                        }
                    }
                    throw new XFSException("Wrong filename: not found! " + name.substring(0, endpos));
                }
            } else {
                endpos++;
            }
        }
        return n;
    }

    /**
     * Суперблок файловой системы. Считывание информации происходит при
     * создании.
     */
    public class SuperBlock {

        /**
         * Маркер.
         */
        public String magic; //[4]
        /**
         * Размер блока ФС в байтах.
         */
        public int bsize;
        /**
         * UUID ФС.
         */
        public int[] uuid; //[8]
        /**
         * Номер корневого узла.
         */
        public long rootino;
        /**
         * Размер группы алокации.
         */
        public int agsize;
        /**
         * Метка ФС.
         */
        public String label; //[12]
        /**
         * log2(Размер блока).
         */
        public int log2_bsize;
        /**
         * log2(Размр сектора).
         */
        public int log2_sect;
        /**
         * log2(Размер узла).
         */
        public int log2_inode;
        /**
         * log2(Узлов в блоке).
         */
        public int log2_inop;
        /**
         * log2(Размер группы алокации).
         */
        public int log2_agblk;
        /**
         * log2(Размер блока каталога в блоках ФС).
         */
        public int log2_dirblk;
        /**
         * Количество блоков ФС в блоке каталога (вычисляется).
         */
        public int dirblocks;
        /**
         * Размер блока каталога в байтах (вычисляется).
         */
        public int dirbsize;
        /**
         * Размер узла в байтах (вычисляется).
         */
        public int nodesize;

        /**
         * Чтение суперблока из устройства класса-родителя.
         *
         * @throws IOException Ошибка ввода/вывода.
         * @throws XFSException Ошибка XFS.
         */
        SuperBlock() throws IOException, XFSException {
            byte[] buffer = new byte[0x100];
            ByteBuffer bb = ByteBuffer.wrap(buffer);
            bb.order(ByteOrder.BIG_ENDIAN);

            device.read(buffer, 0, 0x100); // один сектор

            magic = bbgetstrASCII(bb, 0, 4);
            if (!"XFSB".equals(magic)) {
                throw new XFSException("Wrong XFS marker!");
            }
            bsize = bb.getInt(0x04);
            uuid = new int[8];
            for (int i = 0; i < 8; i++) {
                uuid[i] = bb.getShort(0x20 + i * 2) & 0xFFFF;
            }
            rootino = bb.getLong(0x38);
            agsize = bb.getInt(0x54);
            label = bbgetstrASCII(bb, 0x6C, 12);
            log2_bsize = bb.get(0x78) & 0xFF;
            log2_sect = bb.get(0x79) & 0xFF;
            log2_inode = bb.get(0x7A) & 0xFF;
            log2_inop = bb.get(0x7B) & 0xFF;
            log2_agblk = bb.get(0x7C) & 0xFF;
            log2_dirblk = bb.get(0xC0) & 0xFF;

            dirblocks = 1 << log2_dirblk;
            dirbsize = dirblocks * bsize;
            nodesize = 1 << log2_inode;
        }
    }

    /**
     * Класс - Узел XFS. Поддерживается два типа узла - каталог и файл
     * (остальные без надобности).
     */
    public class Node {

        long id; // Номер узла.
        int type; // Тип узла.
        int format; // Формат хранения доп.данных.
        int version; // Версия структуры каталога (1/2).
        long size; // Размер данных (для каталога - кратно блоку каталога!).
        ArrayList<Extent> ext; // Список экстентов файла/каталога.
        ArrayList<DirEntry> entry; // Список файлов каталога.
        //
        final static int FILE = 1;
        final static int DIR = 2;
        final static int LINK = 3;
        //
        final static int FORMAT_LOCAL = 1;
        final static int FORMAT_EXTENT = 2;
        final static int FORMAT_BTREE = 3;
        // Для ленивой инициализации - заполнения экстентов из дерева.
        int btlevel;
        long btptr;
        // Виртуальный текущий указатель позиции для последовательного чтения.
        long position;

        /**
         * Конструктор. Открытие узла.
         *
         * @param id Номер узла.
         * @throws IOException Ошибка ввода/вывода.
         * @throws XFSException Ошибка XFS.
         */
        public Node(long id) throws IOException, XFSException {
            readMeta(id);
        }

        /**
         * Выводит результат проверки - является ли узел каталогом.
         *
         * @return true - каталог, false - нет.
         */
        public boolean isDirectory() {
            return type == DIR;
        }

        /**
         * Возвращает размер данных узла.
         *
         * @return Размер данных узла в байтах.
         */
        public long getSize() {
            return size;
        }

        /**
         * Возвращает текущий указатель позиции.
         *
         * @return Текущая позиция для чтения.
         */
        public long getPosition() {
            return position;
        }

        /**
         * Позиционирование текущего указателя на заданную позицию.
         *
         * @param pos Новая позиция.
         * @throws IOException Ошибка ввода/вывода.
         */
        public void seek(long pos) throws IOException {
            // Разрешаем позицию +1 в конце файла для смещения туда указателя 
            // после чтения последнего байта файла!
            if (pos < 0 || pos > size) {
                throw new IOException("Out of file! fpos=" + pos + " size=" + size);
            }
            position = pos;
        }

        /**
         * Считывание данных узла.
         *
         * @param buffer Буфер для данных.
         * @param index Начальная позиция в буфере.
         * @param length Размер считываемых данных в байтах.
         * @return Количество считанных байт.
         * @throws IOException Ошибка ввода/вывода.
         * @throws XFSException Ошибка XFS.
         */
        public int read(byte[] buffer, int index, int length) throws IOException, XFSException {
            return readData(buffer, index, position, length);
        }

        /**
         * Считывание метаданных узла и инициализация ими полей.
         *
         * Дополнительных чтений не происходит. В случае дерева - экстенты
         * заполняются позже - при первом чтении данных!
         *
         * @param id Номер узла.
         * @return Ссылка на узел.
         * @throws IOException При ошибках доступа и чтения с устройства.
         * @throws XFSException При ошибках в структуре XFS.
         */
        private Node readMeta(long idNode) throws IOException, XFSException {
            // Считываем тело.
            byte[] buffer = new byte[sblock.bsize];
            ByteBuffer bb = ByteBuffer.wrap(buffer);
            bb.order(ByteOrder.BIG_ENDIAN);

            int pos = 0; // тек.смещение для чтения.
            long fpos = node_fpos(idNode);
            device.read(buffer, fpos, sblock.nodesize);

            // Проверка на маг.число.
            if (!"IN".equals(bbgetstrASCII(bb, pos, 2))) {
                throw new XFSException("Wrong Node marker!");
            }
            id = idNode;
            version = bb.get(pos + 0x04) & 0xFF;
            format = bb.get(pos + 0x05) & 0xFF;
            size = bb.getLong(pos + 0x38); // Размер данных (и у файлов и у каталогов!).

            int mode = bb.getShort(pos + 0x02) & 0xFFFF;
            int nextents = bb.getInt(pos + 0x4C);
            int forkof = (bb.get(pos + 0x52) & 0xFF) << 3; // Смещение от 0x64 до блока А атрибутов.

            if (forkof == 0) {
                // Если forkof не задан, то аттрибутов нет и доступен весь блок!
                forkof = sblock.nodesize - 0x64;
            }

            pos += 0x64; // Начало блока атрибутов U.

            switch (mode & 0xF000) {
                case 0x8000: // Если это файл:
                    type = Node.FILE;
                    break;

                case 0x4000: // Если это каталог:
                    type = Node.DIR;
                    break;

                case 0xA000: // Если это ссылка:
                    throw new XFSException("Wrong Node mode - link not supported!");

                default:
                    throw new XFSException("Wrong Node mode - unknown!");
            }

            switch (format) {
                case FORMAT_LOCAL: // Локальный (для локальных данных).
                    if (type == Node.DIR) {
                        ext = new ArrayList<Extent>();
                        ext.add(new Extent(0, fpos + pos, forkof)); // Весь блок атрибутов U.
                    }
                    return this;

                case FORMAT_EXTENT: // Экстенты
                    ext = new ArrayList<Extent>(nextents);
                    // Считываем список экстентов - все в теле ноды.
                    for (int i = 0; i < nextents; i++, pos += 16) {
                        long hi = bb.getLong(pos);
                        long lo = bb.getLong(pos + 8);
                        ext.add(new Extent(lo, hi));
                    }
                    return this;

                case FORMAT_BTREE: // Дерево
                    ext = null; // Ленивая инициализация.
                    btlevel = bb.getShort(pos) & 0xFFFF;
                    pos += 4;
                    int ptroffset = ((forkof - 4) >> 4) << 3; // ((div 8) div 2) * 8
                    btptr = bb.getLong(pos + ptroffset);
                    return this;

                default:
                    throw new XFSException("Wrong Node format - unknown!");
            }
        }

        /**
         * Считывание метаданных-дерева узла и инициализация ими полей.
         * Происходит при первом чтении данных из файла.
         *
         * @return Ссылка на узел.
         * @throws IOException При ошибках доступа и чтения с устройства.
         * @throws XFSException При ошибках в структуре XFS.
         */
        private Node readMetaTree() throws IOException, XFSException {
            if (ext != null || format != FORMAT_BTREE) {
                return this;
            }
            // Дерево
            byte[] buffer = new byte[sblock.bsize];
            ByteBuffer bb = ByteBuffer.wrap(buffer);
            bb.order(ByteOrder.BIG_ENDIAN);

            // Доходим до первого нижнего элемента.
            while (btlevel > 1) { // Ноды BMAP.
                device.read(buffer, fsb_to_fpos(btptr), sblock.bsize);
                int pos = 4;
                btlevel = bb.getShort(pos) & 0xFFFF;
                pos += 4 + 2 * 8;  // Начало массивов.
                btptr = bb.getLong(pos + (((sblock.bsize - pos) >> 4) << 3)); // На уровень ниже.
            }
            ext = new ArrayList<Extent>();
            // Пробегаем по цепочке листьев.
            while (btptr != -1) { // Ноды BMAP.
                device.read(buffer, fsb_to_fpos(btptr), sblock.bsize);
                int pos = 4;
                btlevel = bb.getShort(pos) & 0xFFFF;
                int num = bb.getShort(pos + 2) & 0xFFFF;
                btptr = bb.getLong(pos + 4 + 8); // Следующий.
                pos += 4 + 2 * 8; // Начало списка экстентов.
                for (int i = 0; i < num; i++, pos += 16) {
                    long hi = bb.getLong(pos);
                    long lo = bb.getLong(pos + 8);
                    ext.add(new Extent(lo, hi));
                }
            }
            return this;
        }

        /**
         * Считывание блока данных из файла (узла).
         *
         * @param fpos Начальная позиция в файле.
         * @param buffer Буфер для данных.
         * @param index Начальная позиция в буфере.
         * @param length Размер блока данных.
         * @return Кол-во считанных байт. -1 - ошибка.
         * @throws IOException Ошибка ввода/вывода.
         * @throws XFSException Ошибка XFS.
         */
        public int readData(byte[] buffer, int index, long fpos, int length) throws IOException, XFSException {
            if (fpos < 0 || fpos > size) {
                throw new IOException("Out of file! fpos=" + fpos + " size=" + size);
            }
            if (fpos == size) {
                return -1; // End of file.
            }
            if (fpos + length >= size) {
                length = (int) (size - fpos);
            }
            if (length == 0) {
                return -1;
            }
            if (ext == null && format == FORMAT_BTREE) {
                readMetaTree();
            }
            if (ext == null) {
                throw new XFSException("Extents is null!");
            }

            boolean isfound;
            int readed = 0;
            long ext_block = 0, ext_offset = 0, ext_size = 0, ext_endoffset = 0;
            for (int ne = 0; length > 0; ne++) {
                // Подходящий экстент.
                for (isfound = false; ne < ext.size(); ne++) {
                    ext_offset = ext.get(ne).offset;
                    ext_block = ext.get(ne).block;
                    ext_size = ext.get(ne).size;
                    ext_endoffset = ext_offset + ext_size; // Смещение до первого байта выходящего за экстент.
                    if (ext_offset >= size) { // Вышли за пределы файла.
                        break;
                    }
                    if (ext_endoffset > fpos) { // Экстент подходит
                        isfound = true;
                        break;
                    }
                }
                if (isfound) {
                    if (ext_offset <= fpos) {
                        // Считываем с начала блока...
                        if (ext_endoffset >= fpos + length) {
                            // ... и до конца блока.
                            readed += device.read(buffer, index, ext_block + (fpos - ext_offset), length);
                            break; // Считали всё.
                        } else {
                            // ... и до конца экстента.
                            long sz = ext_endoffset - fpos;
                            readed += device.read(buffer, index, ext_block + (fpos - ext_offset), (int) sz);
                            length -= sz;
                            index += sz;
                            fpos += sz;
                        }
                    } else {
                        // Заполняем щель нулями от начала блока до начала экстента.
                        // Или до конца блока - что быстрее будет достигнуто.
                        for (; fpos < ext_offset && length > 0; fpos++, index++, length--, readed++) {
                            buffer[index] = 0;
                        }
                        if (length == 0) { // Если конец блока - "считали" всё.
                            break;
                        }

                        // Считываем с начала экстента...
                        if (ext_endoffset >= fpos + length) {
                            // ... и до конца блока.
                            readed += device.read(buffer, index, ext_block, length);
                            break; // Считали всё.
                        } else {
                            // ... и до конца экстента.
                            readed += device.read(buffer, index, ext_block, (int) ext_size);
                            length -= ext_size;
                            index += ext_size;
                            fpos += ext_size;
                        }
                    }
                } else {
                    // Заполняем конечную щель нулями.
                    for (; length > 0; index++, length--, readed++) {
                        buffer[index] = 0;
                    }
                    break;
                }
            }
            return readed;
        }

        /**
         * Получение списка файлов каталога. Без создания узлов! Узел создаётся
         * при первом обращении к нему через openNode(filename).
         *
         * @return Список дескрипторов узлов каталога.
         * @throws IOException Ошибка ввода/вывода.
         * @throws XFSException Ошбика XFS.
         */
        public ArrayList<DirEntry> fileList() throws IOException, XFSException {
            // Если не каталог - выходим.
            if (type != DIR) {
                return null;
            }
            // Если уже считан список файлов - возвращаем его.
            if (entry != null) {
                return entry;
            }

            byte[] buffer = new byte[sblock.dirbsize];
            ByteBuffer bb = ByteBuffer.wrap(buffer);
            bb.order(ByteOrder.BIG_ENDIAN);
            int pos, count;

            // Считываем тело и заполняем список файлов.
            switch (format) {
                case Node.FORMAT_LOCAL: // Локальный
                    device.read(buffer, ext.get(0).block, (int) ext.get(0).size);
                    pos = 0;
                    if (version == 2) {
                        int count4 = bb.get(pos) & 0xFF;
                        int count8 = bb.get(pos + 1) & 0xFF;
                        boolean is64 = count8 != 0;
                        count = (is64 ? count8 : count4) + 2; // "." и ".."
                        long idparent = is64 ? bb.getLong(pos + 2) : bb.getInt(pos + 2);
                        pos += 2 + (is64 ? 8 : 4);
                        entry = new ArrayList<DirEntry>(count);
                        entry.add(new DirEntry(".", id));
                        entry.add(new DirEntry("..", idparent));
                        for (int i = 2; i < count; i++) {
                            int len = bb.get(pos) & 0xFF;
                            //int offset = bb.getShort(pos + 1) & 0xFFFF;
                            DirEntry e = new DirEntry();
                            e.fileName = bbgetstrUTF(bb, pos + 3, len);
                            e.idNode = is64 ? bb.getLong(pos + 3 + len) : bb.getInt(pos + 3 + len);
                            entry.add(e);
                            pos += 3 + len + (is64 ? 8 : 4);
                        }
                    } else if (version == 1) {
                        // Все значения у DVR XFS почему-то выровнены по 4 байта, кроме строки!
                        count = (bb.get(pos) & 0xFF) + 2; // "." и ".."
                        long idparent = bb.getInt(pos + 4) & 0xFFFFFFFFL;
                        pos += 4 + 4;
                        entry = new ArrayList<DirEntry>(count);
                        entry.add(new DirEntry(".", id));
                        entry.add(new DirEntry("..", idparent));
                        for (int i = 2; i < count; i++) {
                            int len = bb.get(pos) & 0xFF;
                            pos += 4 + 4;
                            //int offset = bb.getShort(pos + 1) & 0xFFFF;
                            DirEntry e = new DirEntry();
                            e.fileName = bbgetstrUTF(bb, pos, len);
                            e.idNode = bb.getInt(pos + len) & 0xFFFFFFFFL;
                            entry.add(e);
                            pos += len + 4;
                        }
                    }
                    return entry;

                case Node.FORMAT_EXTENT: // Экстенты
                case Node.FORMAT_BTREE: // Дерево
                    // Проходим по всем "блокам" данных.
                    entry = new ArrayList<DirEntry>();
                    for (int fpos = 0; fpos < size; fpos += sblock.dirbsize) {
                        readData(buffer, 0, fpos, sblock.dirbsize);

                        // Проверка на маг.число.
                        String mark = bbgetstrASCII(bb, 0, 4);
                        if ("XD2B".equals(mark)) { // Block DIR 
                            count = bb.getInt(sblock.dirbsize - 8) - bb.getInt(sblock.dirbsize - 4);
                            pos = 16;
                            for (int i = 0; i < count; i++) {
                                if (bb.getShort(pos) == -1) {
                                    // Свободный блок - пропускаем.
                                    pos += bb.getShort(pos + 2) & 0xFFFF;
                                } else {
                                    long n = bb.getLong(pos);
                                    int len = bb.get(pos + 8) & 0xFF;
                                    entry.add(new DirEntry(bbgetstrUTF(bb, pos + 9, len), n));
                                    pos += ((9 + len + 2 + 7) >> 3) << 3; // выравниваем до кратности 8!
                                }
                            }

                        } else if ("XD2D".equals(mark)) { // Leaf DIR & Node DIR
                            pos = 16;
                            for (int i = 0; pos < sblock.dirbsize; i++) {
                                if (bb.getShort(pos) == -1) {
                                    // Свободный блок - пропускаем.
                                    pos += bb.getShort(pos + 2) & 0xFFFF;
                                } else {
                                    long n = bb.getLong(pos);
                                    int len = bb.get(pos + 8) & 0xFF;
                                    entry.add(new DirEntry(bbgetstrUTF(bb, pos + 9, len), n));
                                    pos += ((9 + len + 2 + 7) >> 3) << 3; // выравниваем до кратности 8!
                                }
                            }

                        } else {
                            throw new XFSException("Wrong Node data block marker!");
                        }
                    }
                    return entry;

                default:
                    throw new XFSException("Wrong Node format - unknown!");
            }
        }
    }

    /**
     * Дескриптор узла в каталоге.
     */
    public class DirEntry {

        /**
         * Имя файла (локальное).
         */
        public String fileName;
        /**
         * Номер узла.
         */
        public long idNode;
        /**
         * Узел. Используется для кэширования.
         */
        public Node node;

        /**
         * Конструктор.
         */
        public DirEntry() {
        }

        /**
         * Конструктор.
         *
         * @param name Имя узла.
         * @param id Номер узла.
         */
        public DirEntry(String name, long id) {
            fileName = name;
            idNode = id;
            node = null;
        }

        @Override
        public String toString() {
            return fileName + " [#" + idNode + "]";
        }
    }

    /**
     * Экстент - непрерывный отрезок файла на устройстве.
     */
    class Extent {

        long offset; // Смещение в файле (в байтах).
        long block; // Позиция на диске (в байтах).
        long size; // Размер на диске (в байтах).

        Extent() {
        }

        Extent(long lo, long hi) {
            offset = ext_offset(lo, hi) << sblock.log2_bsize;
            block = fsb_to_fpos(ext_block(lo, hi));
            size = ext_size(lo, hi) << sblock.log2_bsize;
        }

        Extent(long offset, long block, long size) {
            this.offset = offset;
            this.block = block;
            this.size = size;
        }

        @Override
        public String toString() {
            return String.format("offset=%d block=%d size=%d", offset, block, size);
        }
    }

    /**
     * Вычисление номера блока на блочном устройстве, который соответствует
     * заданному блоку файловой системы. Т.е. из-за размера группы алокации
     * отличного от степени двойки номера блока ФС и линейного могут отличаться!
     *
     * @param fsb Номер блока ФС.
     * @return Линейный номер блока блочного устройства.
     */
    long fsb_to_block(long fsb) {
        return (fsb >> sblock.log2_agblk) * sblock.agsize
                + (fsb & ((1L << sblock.log2_agblk) - 1));
    }

    /**
     * Вычисление позиции на блочном устройстве, которая соответствует заданному
     * блоку файловой системы.
     *
     * @param fsb Номер блока ФС.
     * @return Позиция (смещение в байтах от начала устройства).
     */
    long fsb_to_fpos(long fsb) {
        return fsb_to_block(fsb) << sblock.log2_bsize;
    }

    /**
     * Вычисление номера группы алокации узла.
     *
     * @param ino Номер узла.
     * @return Номер группы.
     */
    long node_ag(long ino) {
        return ino >> (sblock.log2_agblk + sblock.log2_inop);
    }

    /**
     * Вычисление номера узла в группе (локальный номер).
     *
     * @param ino Номер узла.
     * @return Локальный номер узла в группе.
     */
    long node_inag(long ino) {
        return ino & ((1L << (sblock.log2_agblk + sblock.log2_inop)) - 1);
    }

    /**
     * Вычисление логического номера блока узла.
     *
     * @param ino Номер узла.
     * @return Номер блока.
     */
    long node_block(long ino) {
        return fsb_to_block(ino >> sblock.log2_inop);
    }

    /**
     * Вычисление смещения от начала блока до узла (в байтах).
     *
     * @param ino Номер узла.
     * @return Смещение в байтах.
     */
    int node_offset(long ino) {
        //long inag = node_inag(ino);
        return (int) ((ino & ((1L << sblock.log2_inop) - 1)) << sblock.log2_inode);
    }

    /**
     * Вычисление логического номера блока узла.
     *
     * @param ino Номер узла.
     * @return Номер блока.
     */
    long node_fpos(long ino) {
        return (node_block(ino) << sblock.log2_bsize) + node_offset(ino);
    }

    /**
     * Вычисление смещения экстента в файле.
     *
     * @param lo Младшие 8 байт.
     * @param hi Старшие 8 байт.
     * @return Смещение в блоках ФС.
     */
    long ext_offset(long lo, long hi) {
        return (hi >> 9) & ((1L << 54) - 1);
    }

    /**
     * Вычисление блока ФС экстента.
     *
     * @param lo Младшие 8 байт.
     * @param hi Старшие 8 байт.
     * @return Номер блока ФС (не линейный!).
     */
    long ext_block(long lo, long hi) {
        return ((lo >> 21) & ((1L << 43) - 1))
                | ((hi & ((1L << 9) - 1)) << 43);
    }

    /**
     * Вычисление размера экстента.
     *
     * @param lo Младшие 8 байт.
     * @param hi Старшие 8 байт.
     * @return Размер в блоках ФС.
     */
    long ext_size(long lo, long hi) {
        return lo & ((1L << 21) - 1);
    }

    /**
     * Считывание строки ASCII из буфера.
     *
     * @param buffer Буфер.
     * @param index Начальная позиция в буфере.
     * @param len Длина тела строки в байтах (!).
     * @return Считанная строка.
     */
    static String bbgetstrASCII(ByteBuffer buffer, int index, int len) {
        String str = "";
        for (int i = 0, c = 1; i < len && c != 0; i++) {
            c = buffer.get(index + i) & 0xFF;
            if (c != 0) {
                str += (char) c;
            }
        }
        return str;
    }

    /**
     * Считывание строки UTF8 из буфера.
     *
     * @param buffer Буфер.
     * @param index Начальная позиция в буфере.
     * @param len Длина тела строки в байтах (!).
     * @return Считанная строка.
     */
    static String bbgetstrUTF(ByteBuffer buffer, int index, int len) {
        try {
            return new String(buffer.array(), index, len, "UTF8");
        } catch (UnsupportedEncodingException ex) {
            return bbgetstrASCII(buffer, index, len);
        }
    }

    /**
     * Исключение - Ошибки XFS.
     */
    public static class XFSException extends Exception {

        /**
         * Конструктор.
         */
        public XFSException() {
        }

        /**
         * Конструктор.
         *
         * @param msg Сообщение.
         */
        public XFSException(String msg) {
            super(msg);
        }
    }
}
