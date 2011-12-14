/*
 * Copyright (c) 2011, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Класс для транскодирования аудио DVR (0x23) в "Signed PCM 16bit LE".
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class ADPCM {

    /**
     * Константа - максимальный размер фрейма PCM в байтах.
     */
    public static final int MAX_PCMFRAME_SIZE = 480 * 2;
    /**
     * Ссылка на буфер-источник (кодированные данные).
     */
    private byte[] inBuffer;
    /**
     * Буфер для байт-преобразований с привязкой ко входному буферу.
     */
    private ByteBuffer inBB;
    /**
     * Ссылка на буфер-приёмник (декодированные данные - sPCM16le).
     */
    private byte[] outBuffer;
    /**
     * Буфер для байт-преобразований с привязкой к выходному буферу.
     */
    private ByteBuffer outBB;
    /**
     * ID алгоритма кодирования.
     */
    private final int idCodec;
    /**
     * Текущая позиция во входном буфере.
     */
    private int inPos;
    /**
     * Кол-во "обработанных" байт во входном буфере в процессе декодирования.
     * Смещение inPos от начальной позиции декодирования.
     */
    private int inShift;
    /**
     * Текущая позиция в выходном буфере.
     */
    private int outPos;
    /**
     * Кол-во "обработанных" байт в выходном буфере в процессе декодирования.
     * Смещение outPos от начальной позиции декодирования.
     */
    private int outShift;
    ////////////////////////////////////////////////////////////////////////////
    // Для алгоритма.
    /**
     * Предыдущее значение 16бит.
     */
    private int value;
    /**
     * Текущий индекс.
     */
    private int index;
    /**
     * Длина данных (кол-во 16бит слов).
     */
    private int length;

    /**
     * Конструктор. Сопоставляются входной-выходной буферы, сбрасываются 
     * значения переменных-регистров.
     * @param codec ID алгоритма кодирования (кодека?).
     * @param inBuffer Буфер источник.
     * @param outBuffer Буфер приёмник.
     */
    public ADPCM(int codec, byte[] inBuffer, byte[] outBuffer) {
        if (inBuffer == null || outBuffer == null) {
            throw new Error("Buffers must be not null!");
        }
        // Более строгое условие на кодек, чем в процедуре декодирования.
        if (codec != 0x03 && codec != 0x23 && codec != 0x43) {
            throw new Error("Wrong codec ID! (" + codec + ")");
        }
        idCodec = codec;
        this.inBuffer = inBuffer;
        inBB = ByteBuffer.wrap(this.inBuffer);
        inBB.order(ByteOrder.LITTLE_ENDIAN);
        this.outBuffer = outBuffer;
        outBB = ByteBuffer.wrap(this.outBuffer);
        outBB.order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Возвращает значение текущего смещения во входном буфере с начала 
     * процесса декодирования.
     * @return Смещение.
     */
    public int getInShift() {
        return inShift;
    }

    /**
     * Возвращает значение текущего смещения в выходном буфере с начала 
     * процесса декодирования.
     * @return Смещение.
     */
    public int getOutShift() {
        return outShift;
    }

    /**
     * Конвертирует данные из входного буфера с указанной позиции и записывает
     * декодированные данные в выходной буфер с нулевой позиции.
     * В процессе декодирования вычисляется кол-во обработанных байтов из 
     * входного буфера <b>inShift</b> и кол-во записаных в выходной буфер 
     * <b>outShift</b>.
     * @return Результат выполнения операции: 0-без ошибок, иначе код ошибки.
     */
    public int HI_VOICE_Decode(int inpos, int outpos) {
        try {
            // Init
            inPos = outPos = inShift = outShift = 0;
            value = index = length = 0;
            // Positioning
            if (inpos < 0 || inpos >= inBuffer.length - 4) {
                return -1;
            }
            if (outpos < 0 || outpos >= outBuffer.length) {
                return -1;
            }
            inPos = inpos;
            outPos = outpos;
            // Shifting (skip header).
            inPos += 4;
            inShift += 4;

            // Mode
            if ((inBB.getShort(inpos) & 0x300) != 0x100) {
                return -2;
            }
            // Length
            length = inBB.get(inpos + 2) & 0xFF;
            if (length == 0) {
                return -3;
            }

            /*
            Варианты от [codec-1]
            00 00 01 02 03 04 05 07  07 07 07 07 07 07 07 07
            07 07 07 07 07 07 07 07  07 07 07 07 07 07 07 07
            07 07 01 02 03 04 05 07  07 07 07 07 07 07 07 07
            07 07 07 07 07 07 07 07  07 07 07 07 07 07 07 07
            00 00 01 06
            Переходы
            44 4E 4C 00 7D 4E 4C 00  B1 4E 4C 00 19 4F 4C 00
            93 4F 4C 00 07 50 4C 00  83 50 4C 00 C0 50 4C 00
             */
            switch (idCodec - 1) {
                case 0x00:
                case 0x01:
                case 0x40:
                case 0x41: // n0
                    return -10;
                case 0x02:
                case 0x22:
                case 0x42: // n1
                    if (length * 4 - 7 > 0x1E1) {
                        return -11;
                    }
                    return frameDecode() * 100;
                case 0x03:
                case 0x23: // n2
                    return -10;
                case 0x04:
                case 0x24: // n3
                    return -10;
                case 0x05:
                case 0x25: // n4
                    return -10;
                case 0x06:
                case 0x26: // n5
                    return -10;
                case 0x43: // n6
                    return -10;
                default: // n7 (некорректный ID кодека!)
                    return -20;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return -100000;
        }
    }

    /**
     * Конвертирует фрейм с текущей позиции в выходной буфер.
     * СОХРАННОСТЬ данных во входном буфере НЕ СОБЛЮДАЕТСЯ (!).
     * @return Результат выполнения операции: 0-без ошибок, иначе код ошибки.
     */
    private int frameDecode() {
        switch (idCodec) {
            case 0x03: // uncompress

                value = inBB.getShort(inPos);
                index = inBB.get(inPos + 2);
                inPos += 4;
                inShift += 4;

                // Преобразуем остальное тело в PCM (кроме первых 4 байт).
                // База не сохраняется.
                return uncompress((length - 2) * 2);

            case 0x23: // reorder + uncomress

                value = inBB.getShort(inPos);
                index = inBB.get(inPos + 2);
                inPos += 4;
                inShift += 4;

                // Первые два байта - переносим отдельно и без преобразования.
                outBB.putShort(outPos, (short) value);
                outPos += 2;
                outShift += 2;

                // Меняем местами полубайты в каждом байте тела фрейма.
                int n = (length - 2) * 2; // Кол-во исходных байт фрейма (кроме первых 4 - их уже обработали!).
                for (int i = 0; i < n; i++) {
                    byte a = inBuffer[inPos + i];
                    inBuffer[inPos + i] = (byte) (((a >> 4) & 0xF) + ((a << 4) & 0xF0));
                }
                // Преобразуем остальное тело в PCM (кроме первых 4 байт).
                return uncompress(n);

            case 0x43: // uncompress
                // Преобразуем тело в PCM.
                return uncompress(length * 2);

            default:
                return -1;
        }
    }

    /**
     * Преобразование в PCM.
     * @param count Кол-во байт для преобразования во фрейме (lendata-4).
     * @return Результат выполнения операции: 0-без ошибок, иначе код ошибки.
     */
    private int uncompress(int count) {
        int inByte = 0; // Текущий байт из вх.массива.
        int inHByte; // Текущий полубайт из вх.массива.
        int flag = 0; // Флаг смены полубайта.

        while (count > 0) {
            if (flag == 0) {
                inByte = inBuffer[inPos];
                inPos++;
                inShift++;
                inHByte = (inByte >> 4) & 0xF; // Hi4bit
                count--;
                flag = 1;
            } else {
                inHByte = inByte & 0xF; // Low4bit
                flag = 0;
            }

            int stepsize = stepsizeTable[index];
            int step = stepsize >> 3;
            if ((inHByte & 0x4) != 0) {
                step += stepsize;
            }
            if ((inHByte & 0x2) != 0) {
                step += (stepsize >> 1);
            }
            if ((inHByte & 0x1) != 0) {
                step += (stepsize >> 2);
            }
            value += ((inHByte & 0x8) == 0) ? step : -step;
            value = (value > 0x7FFF) ? 0x7FFF : (((value < 0xFFFF8000) ? 0xFFFF8000 : value));

            outBB.putShort(outPos, (short) value);
            outPos += 2;
            outShift += 2;

            index += indexTable[inHByte];
            index = (index < 0) ? 0 : ((index > 0x58) ? 0x58 : index);
        }
        return 0;
    }
    /**
     * Таблица размеров шагов.
     */
    private static final int[] stepsizeTable = {
        0x0007, 0x0008, 0x0009, 0x000A,
        0x000B, 0x000C, 0x000D, 0x000E,
        0x0010, 0x0011, 0x0013, 0x0015,
        0x0017, 0x0019, 0x001C, 0x001F,
        0x0022, 0x0025, 0x0029, 0x002D,
        0x0032, 0x0037, 0x003C, 0x0042,
        0x0049, 0x0050, 0x0058, 0x0061,
        0x006B, 0x0076, 0x0082, 0x008F,
        0x009D, 0x00AD, 0x00BE, 0x00D1,
        0x00E6, 0x00FD, 0x0117, 0x0133,
        0x0151, 0x0173, 0x0198, 0x01C1,
        0x01EE, 0x0220, 0x0256, 0x0292,
        0x02D4, 0x031C, 0x036C, 0x03C3,
        0x0424, 0x048E, 0x0502, 0x0583,
        0x0610, 0x06AB, 0x0756, 0x0812,
        0x08E0, 0x09C3, 0x0ABD, 0x0BD0,
        0x0CFF, 0x0E4C, 0x0FBA, 0x114C,
        0x1307, 0x14EE, 0x1706, 0x1954,
        0x1BDC, 0x1EA5, 0x21B6, 0x2515,
        0x28CA, 0x2CDF, 0x315B, 0x364B,
        0x3BB9, 0x41B2, 0x4844, 0x4F7E,
        0x5771, 0x602F, 0x69CE, 0x7462,
        0x7FFF};
    /**
     * Таблица изменений индексов.
     */
    private static final int[] indexTable = {
        -1, -1, -1, -1, 2, 4, 6, 8, -1, -1, -1, -1, 2, 4, 6, 8
    };
}
