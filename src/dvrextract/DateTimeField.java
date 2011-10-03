package dvrextract;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFormattedTextField;
import javax.swing.text.MaskFormatter;

/**
 *
 * @author lex
 */
public class DateTimeField extends JFormattedTextField {

    private Date time;
    // Маска фильтрации ввода.
    private Pattern pattern;
    private MaskFormatter formatter;

    public DateTimeField() {
        time = new Date();
        try {
            formatter = new MaskFormatter("##.##.#### ##:##:##");
            formatter.setPlaceholderCharacter('_');
        } catch (ParseException ex) {
        }
        pattern = Pattern.compile("^([0-2][0-9]|3[01])\\.(0[0-9]|1[012])\\.(201[01]) ([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$/",
                Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
        addVetoableChangeListener(new VetoableChangeListener() {

            @Override
            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                String s = (String) evt.getNewValue();
                String sD = s.substring(0, 2);
                String sM = s.substring(3, 5);
                String sY = s.substring(6, 10);
                String sHh = s.substring(11, 13);
                String sMm = s.substring(14, 16);
                String sSs = s.substring(17, 19);
                int flag = 0;
                int len = s.length();
                for (int i = 0; i < s.length(); i++) {
                    char c = sD.charAt(i);
                    switch (i) {
                        case 0:
                            if (!isDigit(c,0,3)) {
                                flag = 1;
                            }
                            break;
                    }
                    if (flag > 0) {
                        
                        return;
                    }
                }
            }
        });
    }
    
    boolean isDigit(char c, int d1, int d2) {
        int n = c - '0';
        return (n < d1 || n > d2) ? false : true;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
