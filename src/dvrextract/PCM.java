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
 * Класс для транскодирования аудио DVR в "Signed PCM 16bit LE".
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class PCM {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {

        byte[] g_inBuf = new byte[168];
        ByteBuffer bi = ByteBuffer.wrap(g_inBuf);
        bi.order(ByteOrder.LITTLE_ENDIAN);
        byte[] g_outBuf = new byte[1024];

        FileInputStream is = new FileInputStream("/home/work/files/DVRVIDEO/audio1.raw");
        File fos = new File("/home/work/files/DVRVIDEO/audio1.pcm");
        fos.delete();
        FileOutputStream os = new FileOutputStream(fos);

        PCM p = new PCM();
        State state = new State();
        state.coder = 0x23;

        while (true) {
            if (is.available() == 0) {
                break;
            }

            int len = is.read(g_inBuf, 0, 4);
            if (len != 4) {
                System.out.println("Error! Cant read 4b: read = " + len);
                break;
            }
            len = bi.get(2) & 0xFF;
            if (len > 122) {
                System.out.println("Error! Len = " + len);
                break;
            }
            if (is.read(g_inBuf, 4, len * 2) != len * 2) {
                System.out.println("Error! Cant read body: read = " + len);
                break;
            }
            int res = p.HI_VOICE_Decode(state, g_inBuf, g_outBuf);
            if (res > 0) {
                os.write(g_outBuf, 0, res);
            }
            //break;
        }
        is.close();
        os.close();
    }

    ////////////////////////////////////////////////////////////////////////////
    static class State {

        int coder;   // w16
        int valPrev; // w16
        int index;   // w8
    }

    int HI_VOICE_Decode(State state, byte[] inBuf, byte[] outBuf) {
        ByteBuffer bi = ByteBuffer.wrap(inBuf);
        bi.order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer bo = ByteBuffer.wrap(outBuf);
        bo.order(ByteOrder.LITTLE_ENDIAN);

        int res = 0;
        int resLen = 0; // local
        if (state == null) {
            return -1;
        }
        int codec = state.coder;
        if (inBuf == null) {
            return -1;
        }
        if (outBuf == null) {
            return -1;
        }
        int result = 0;

        int mode = bi.getShort(0) & 0x300; // mode
        if (mode != 0x100) {
            return -1;
        }

        int len = bi.get(2) & 0xFF; // len
        if (len == 0) {
            return -1;
        }

        if (codec - 1 > 0x43) {
            return -1;
        }

        byte[] algTypes = {
            0, 0, 1, 2, 3, 4, 5, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 1, 2, 3, 4, 5, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            0, 0, 1, 6};
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
        switch (codec - 1) {
            case 0x00:
            case 0x01:
            case 0x40:
            case 0x41: // n0
                break;
            case 0x02:
            case 0x22:
            case 0x42: // n1
                if (len * 4 - 7 > 0x1E1) {
                    return -1;
                }

                res = FrameConvert(state, inBuf, outBuf, codec);
                // ?????

                break;
            case 0x03:
            case 0x23: // n2
                break;
            case 0x04:
            case 0x24: // n3
                break;
            case 0x05:
            case 0x25: // n4
                break;
            case 0x06:
            case 0x26: // n5
                break;
            case 0x43: // n6
                break;
            default: // n7
                break;
        }

        return res;
    }
    int MAXFRAMESIZE = 480; // words

    int FrameConvert(State state, byte[] inBuf, byte[] outBuf, int codec) {
        ByteBuffer bi = ByteBuffer.wrap(inBuf);
        bi.order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer bo = ByteBuffer.wrap(outBuf);
        bo.order(ByteOrder.LITTLE_ENDIAN);

        int res = 0;
        byte[] inFrame = new byte[MAXFRAMESIZE * 2 + 4];
        ByteBuffer bif = ByteBuffer.wrap(inFrame);
        bif.order(ByteOrder.LITTLE_ENDIAN);
        int len = bi.get(2) & 0xFF;

        for (int i = 0; i < len * 2; i++) {
            inFrame[i] = inBuf[i + 4];
        }
        //

        switch (codec) {
            case 0x03: // uncompress

                state.valPrev = bif.getShort(0);
                state.index = bif.get(2);

                // Преобразуем остальное тело в PCM (кроме первых 4 байт).
                // База не сохраняется.
                res += uncompress(inFrame, 4, outBuf, 0, (len - 2) * 2, state);
                break;

            case 0x23: // reorder + uncomress

                state.valPrev = bif.getShort(0);
                state.index = bif.get(2);

                // Первые два байта - переносим отдельно и без преобразования.
                bo.putShort(0, (short) state.valPrev);
                res += 2;

                // Меняем местами полубайты в каждом байте тела фрейма.
                int n = (len - 2) * 2; // Кол-во исходных БАЙТ (кроме первых 4!).
                for (int i = 0; i < n; i++) {
                    byte a = inFrame[i + 4];
                    inFrame[i + 4] = (byte) (((a >> 4) & 0xF) + ((a << 4) & 0xF0));
                }
                // Преобразуем остальное тело в PCM (кроме первых 4 байт).
                res += uncompress(inFrame, 4, outBuf, 2, n, state);
                break;

            case 0x43: // uncompress
                // Преобразуем тело в PCM.
                res += uncompress(inFrame, 0, outBuf, 0, len * 2, state);
                break;

            default:
                return -1;
        }


        return res;
    }

    /**
     * Преобразование ADPCM в PCM.
     * @param inFrame Массив с телом фрейма.
     * @param inPos Стартовая позиция в фрейме.
     * @param outBuf Массив для данных PCM.
     * @param outPos Стартовая позиция в массиве PCM.
     * @param count Кол-во байт для преобразования во фрейме (lendata-4).
     * @param state Состояние.
     * @return Кол-во байт записанных в PCM массив.
     */
    int uncompress(byte[] inFrame, int inPos, byte[] outBuf, int outPos, int count, State state) {
        ByteBuffer bo = ByteBuffer.wrap(outBuf);
        bo.order(ByteOrder.LITTLE_ENDIAN);

        int n = 0; // Кол-во байт записанных при обработке в массив PCM.
        int value = state.valPrev; // Текущее значение.
        int index = state.index; // Текущий индекс.
        int inByte = 0; // Текущий байт из вх.массива.
        int inHByte; // Текущий полубайт из вх.массива.
        int flag = 0; // Флаг смены полубайта.

        while (count > 0) {
            if (flag == 0) {
                inByte = inFrame[inPos++];
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

            bo.putShort(outPos, (short) value);
            outPos += 2;
            n += 2;

            index += indexTable[inHByte];
            index = (index < 0) ? 0 : ((index > 0x58) ? 0x58 : index);
        }
        return n;
    }
    /**
     * Таблица размеров шагов.
     */
    int[] stepsizeTable = {
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
    int[] indexTable = {
        -1, -1, -1, -1, 2, 4, 6, 8, -1, -1, -1, -1, 2, 4, 6, 8
    };
}
