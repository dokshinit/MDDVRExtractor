package dvrextract.gui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Докшин_А_Н
 */
public class FormattedDateValue implements org.jdesktop.swingx.renderer.StringValue {

    DateFormat dateFormat;

    public FormattedDateValue(String format) {
        dateFormat = new SimpleDateFormat(format);
    }

    @Override
    public String getString(Object value) {
        return dateFormat.format((Date) value);
    }
}
