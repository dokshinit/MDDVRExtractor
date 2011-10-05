package dvrextract.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFormattedTextField;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;

/**
 *
 * @author lex
 */
public class JDateTimeField extends JFormattedTextField {

    private static SimpleDateFormat formatDt = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private Date time;
    // Маска фильтрации ввода.
    private Pattern patternDt;
    private MaskFormatter formatterDt;

    public JDateTimeField() {
        try {
            formatterDt = new MaskFormatter("##.##.#### ##:##:##");
            formatterDt.setPlaceholderCharacter('_');
            DefaultFormatterFactory factory = new DefaultFormatterFactory(formatterDt);
            setFormatterFactory(factory);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        patternDt = Pattern.compile("^([0-2][0-9]|3[01])\\.(0[0-9]|1[012])\\.(201[01]) ([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$");
        
        addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println("PCHANGE name="+evt.getPropertyName()+" new="+evt.getNewValue());
            }
        });
        
        addVetoableChangeListener(new VetoableChangeListener() {

            @Override
            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                System.out.println("CHANGE name="+evt.getPropertyName()+" new="+evt.getNewValue());
                if (evt.getPropertyName().equals("value")) {
                    String s = (String) evt.getNewValue();

                    String sD = s.substring(0, 2).trim();
                    String sM = s.substring(3, 5).trim();
                    String sY = s.substring(6, 10).trim();
                    String sHh = s.substring(11, 13).trim();
                    String sMm = s.substring(14, 16).trim();
                    String sSs = s.substring(17, 19).trim();

                    int flag = 0;

                    if (sD.length() > 0) {
                        int n = new Integer(sD);
                        if (n < 1 || n > 31) {
                            flag = 1;
                        }
                    } else if (sM.length() > 0) {
                        int n = new Integer(sM);
                        if (n < 1 || n > 12) {
                            flag = 2;
                        }
                    } else if (sY.length() > 0) {
                        int n = new Integer(sY);
                        if (n < 2011 || n > 2011) {
                            flag = 3;
                        }
                    } else if (sHh.length() > 0) {
                        int n = new Integer(sHh);
                        if (n < 0 || n > 23) {
                            flag = 4;
                        }
                    } else if (sMm.length() > 0) {
                        int n = new Integer(sMm);
                        if (n < 0 || n > 59) {
                            flag = 5;
                        }
                    } else if (sSs.length() > 0) {
                        int n = new Integer(sSs);
                        if (n < 0 || n > 59) {
                            flag = 5;
                        }
                    }
                    if (flag > 0) {
                        throw new PropertyVetoException("wrong " + flag, evt);
                    }
                }
            }
        });
         
        //setTime(new Date());
    }

    boolean isDigit(char c, int d1, int d2) {
        int n = c - '0';
        return (n < d1 || n > d2) ? false : true;
    }

    public void setTime(Date time) {
        this.time = time;
        super.setText(formatDt.format(time));
    }
/*
    @Override
    public void setText(String str) {
        try {
            setTime(formatDt.parse(str));
        } catch (ParseException ex) {
        }
    }
*/
    public static void main(String[] args) {
        Pattern p = Pattern.compile("^(0[1-9]|[12][0-9]|3[01])\\.(0[1-9]|1[012])\\.(201[01]) ([01][0-9]|2[0-3]):(0[1-9]|[1-5][0-9]):(0[1-9]|[1-5][0-9])");
        Matcher m = p.matcher("  .12.2011 10:10:10");
        System.out.println("Find = " + m.find());
        System.out.println("GCount=" + m.groupCount() + " gr=" + m.group());
    }
}
/*
        Pattern pD = Pattern.compile("(0[1-9]|[12][0-9]|3[01])");
        Pattern pM = Pattern.compile("(0[1-9]|1[012])");
        Pattern pY = Pattern.compile("(201[01])");
        Pattern ph = Pattern.compile("([01][0-9]|2[0-3])");
        Pattern pm = Pattern.compile("(0[1-9]|[1-5][0-9])");
        Pattern ps = Pattern.compile("(0[1-9]|[1-5][0-9])");
*/