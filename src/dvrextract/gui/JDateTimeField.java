package dvrextract.gui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;

/**
 *
 * @author lex
 */
public class JDateTimeField extends JFormattedTextField {

    private SimpleDateFormat formatDt;
    private Date time;

    public void setFormat(String fmt) {
        // Форматер для преобразований
        formatDt = new SimpleDateFormat(fmt);
        // Маска для ввода
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<fmt.length(); i++) {
            switch (fmt.charAt(i)) {
                case 'd':
                case 'M':
                case 'y':
                case 'H':
                case 'm':
                case 's': 
                    sb.append('#');
                    break;
                default:
                    sb.append(fmt.charAt(i));
            }
        }
        try {
            MaskFormatter formatterDt = new MaskFormatter(sb.toString());
            formatterDt.setPlaceholderCharacter('0');
            DefaultFormatterFactory factory = new DefaultFormatterFactory(formatterDt);
            setFormatterFactory(factory);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }
    
    public JDateTimeField() {
        setFormat("dd.MM.yyyy HH:mm:ss");
        
        //"^([0-2][0-9]|3[01])\\.(0[0-9]|1[012])\\.(201[01]) ([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$");

        setInputVerifier(new InputVerifier() {

            @Override
            public boolean verify(JComponent input) {
                System.out.println("VERIFY");
                if (isValid(getText())) {
                    return true;
                }
                return false;
            }

            @Override
            public boolean shouldYieldFocus(JComponent input) {
                return super.shouldYieldFocus(input);
            }
        });

        setTime(new Date());
    }

    public void setTime(Date time) {
        this.time = time;
        super.setText(formatDt.format(time));
    }

    public boolean isValid(String s) {
        if (s == null) {
            return false;
        }

        String sD = s.substring(0, 2).trim();
        String sM = s.substring(3, 5).trim();
        String sY = s.substring(6, 10).trim();
        String sHh = s.substring(11, 13).trim();
        String sMm = s.substring(14, 16).trim();
        String sSs = s.substring(17, 19).trim();

        int flag = 0;
        int nd = 0, nm = 0, ny = 0, nhh = 0, nmm = 0, nss = 0;

        if (sD.length() > 0) {
            nd = new Integer(sD);
            if (nd < 1 || nd > 31) {
                return false;
            }
        }
        if (sM.length() > 0) {
            nm = new Integer(sM);
            if (nm < 1 || nm > 12) {
                return false;
            }
        }
        if (sY.length() > 0) {
            ny = new Integer(sY);
            if (ny < 2011 || ny > 2011) {
                return false;
            }
        }
        if (sHh.length() > 0) {
            nhh = new Integer(sHh);
            if (nhh < 0 || nhh > 23) {
                return false;
            }
        }
        if (sMm.length() > 0) {
            nmm = new Integer(sMm);
            if (nmm < 0 || nmm > 59) {
                return false;
            }
        }
        if (sSs.length() > 0) {
            nss = new Integer(sSs);
            if (nss < 0 || nss > 59) {
                return false;
            }
        }
        try {
            // Проверка на реальность даты.
            Date dt = formatDt.parse(s);
            System.out.println(formatDt.format(dt));
            if (!s.equals(formatDt.format(dt))) {
                return false;
            }
        } catch (ParseException ex) {
            return false;
        }
        
        System.out.println("s=" + s + " #" + nd + "." + nm + "." + ny + " " + nhh + ":" + nmm + ":" + nss);
        return true;
    }
    
    
    
}
