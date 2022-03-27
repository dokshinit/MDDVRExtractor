package javax.swing.plaf.nimbus;

import java.awt.Color;
import javax.swing.UIDefaults;
import java.awt.Dimension;
import java.awt.Insets;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import javax.swing.UIManager;
import static javax.swing.plaf.nimbus.AbstractRegionPainter.PaintContext.CacheMode.*;
/**
 * This class contains all the implementation details related to Nimbus. It
 * contains all the code for initializing the UIDefaults table, as well as for
 * selecting a SynthStyle based on a JComponent/Region pair.
 *
 * @author Richard Bair
 */
public final class ExtNimbusDefaults {

    public static UIDefaults init() {
        UIDefaults d = UIManager.getDefaults();

        // Компонент: SplitPane
        setUIKey("SplitPane:SplitPaneDivider[Enabled].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtSplitPaneDividerPainter", SplitPaneDividerPainter.BACKGROUND_ENABLED, new Insets(3, 0, 3, 0), new Dimension(68, 10), false, AbstractRegionPainter.PaintContext.CacheMode.NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        setUIKey("SplitPane:SplitPaneDivider[Focused].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtSplitPaneDividerPainter", SplitPaneDividerPainter.BACKGROUND_FOCUSED, new Insets(3, 0, 3, 0), new Dimension(68, 10), false, AbstractRegionPainter.PaintContext.CacheMode.NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        setUIKey("SplitPane:SplitPaneDivider[Enabled].foregroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtSplitPaneDividerPainter", SplitPaneDividerPainter.FOREGROUND_ENABLED, new Insets(0, 24, 0, 24), new Dimension(68, 10), true, AbstractRegionPainter.PaintContext.CacheMode.NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        setUIKey("SplitPane:SplitPaneDivider[Enabled+Vertical].foregroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtSplitPaneDividerPainter", SplitPaneDividerPainter.FOREGROUND_ENABLED_VERTICAL, new Insets(5, 0, 5, 0), new Dimension(10, 38), true, AbstractRegionPainter.PaintContext.CacheMode.NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));

        // Компонент: ComboBox
        setUIKey("ComboBox[Disabled].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtComboBoxPainter",ExtComboBoxPainter.BACKGROUND_DISABLED, new Insets(8, 9, 8, 19), new Dimension(83, 24), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));
        setUIKey("ComboBox[Disabled+Pressed].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtComboBoxPainter",ExtComboBoxPainter.BACKGROUND_DISABLED_PRESSED, new Insets(8, 9, 8, 19), new Dimension(83, 24), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));
        setUIKey("ComboBox[Enabled].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtComboBoxPainter",ExtComboBoxPainter.BACKGROUND_ENABLED, new Insets(8, 9, 8, 19), new Dimension(83, 24), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));
        setUIKey("ComboBox[Focused].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtComboBoxPainter",ExtComboBoxPainter.BACKGROUND_FOCUSED, new Insets(8, 9, 8, 19), new Dimension(83, 24), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));
        setUIKey("ComboBox[Focused+MouseOver].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtComboBoxPainter",ExtComboBoxPainter.BACKGROUND_MOUSEOVER_FOCUSED, new Insets(8, 9, 8, 19), new Dimension(83, 24), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));
        setUIKey("ComboBox[MouseOver].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtComboBoxPainter",ExtComboBoxPainter.BACKGROUND_MOUSEOVER, new Insets(8, 9, 8, 19), new Dimension(83, 24), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));
        setUIKey("ComboBox[Focused+Pressed].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtComboBoxPainter",ExtComboBoxPainter.BACKGROUND_PRESSED_FOCUSED, new Insets(8, 9, 8, 19), new Dimension(83, 24), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));
        setUIKey("ComboBox[Pressed].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtComboBoxPainter",ExtComboBoxPainter.BACKGROUND_PRESSED, new Insets(8, 9, 8, 19), new Dimension(83, 24), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));
        setUIKey("ComboBox[Enabled+Selected].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtComboBoxPainter",ExtComboBoxPainter.BACKGROUND_ENABLED_SELECTED, new Insets(8, 9, 8, 19), new Dimension(83, 24), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));
        setUIKey("ComboBox[Disabled+Editable].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtComboBoxPainter",ExtComboBoxPainter.BACKGROUND_DISABLED_EDITABLE, new Insets(6, 5, 6, 17), new Dimension(79, 21), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));
        setUIKey("ComboBox[Editable+Enabled].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtComboBoxPainter",ExtComboBoxPainter.BACKGROUND_ENABLED_EDITABLE, new Insets(6, 5, 6, 17), new Dimension(79, 21), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));
        setUIKey("ComboBox[Editable+Focused].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtComboBoxPainter",ExtComboBoxPainter.BACKGROUND_FOCUSED_EDITABLE, new Insets(5, 5, 5, 5), new Dimension(142, 27), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));
        setUIKey("ComboBox[Editable+MouseOver].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtComboBoxPainter",ExtComboBoxPainter.BACKGROUND_MOUSEOVER_EDITABLE, new Insets(4, 5, 5, 17), new Dimension(79, 21), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));
        setUIKey("ComboBox[Editable+Pressed].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtComboBoxPainter",ExtComboBoxPainter.BACKGROUND_PRESSED_EDITABLE, new Insets(4, 5, 5, 17), new Dimension(79, 21), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));
        // Компонент: ComboBox -> ComboBoxArrowButton
        Object o1 = UIManager.get("ComboBox:\"ComboBox.arrowButton\"[Pressed].foregroundPainter");
        setUIKey("ComboBox:\"ComboBox.arrowButton\"[Enabled].foregroundPainter", o1);
        setUIKey("ComboBox:\"ComboBox.arrowButton\"[MouseOver].foregroundPainter", o1);

        // Компонент: TextField
        setUIKey("TextField[Disabled].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtTextFieldPainter", ExtTextFieldPainter.BACKGROUND_DISABLED, new Insets(5, 5, 5, 5), new Dimension(122, 24), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        setUIKey("TextField[Enabled].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtTextFieldPainter", ExtTextFieldPainter.BACKGROUND_ENABLED, new Insets(5, 5, 5, 5), new Dimension(122, 24), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        setUIKey("TextField[Selected].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtTextFieldPainter", ExtTextFieldPainter.BACKGROUND_SELECTED, new Insets(5, 5, 5, 5), new Dimension(122, 24), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        setUIKey("TextField[Disabled].borderPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtTextFieldPainter", ExtTextFieldPainter.BORDER_DISABLED, new Insets(5, 3, 3, 3), new Dimension(122, 24), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        setUIKey("TextField[Focused].borderPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtTextFieldPainter", ExtTextFieldPainter.BORDER_FOCUSED, new Insets(5, 5, 5, 5), new Dimension(122, 24), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        setUIKey("TextField[Enabled].borderPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtTextFieldPainter", ExtTextFieldPainter.BORDER_ENABLED, new Insets(5, 5, 5, 5), new Dimension(122, 24), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        
        // Компонент: ProgressBar
        setUIKey("ProgressBar[Enabled].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtProgressBarPainter", ProgressBarPainter.BACKGROUND_ENABLED, new Insets(5, 5, 5, 5), new Dimension(29, 19), false, AbstractRegionPainter.PaintContext.CacheMode.FIXED_SIZES, 1.0, 1.0));
        setUIKey("ProgressBar[Disabled].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtProgressBarPainter", ProgressBarPainter.BACKGROUND_DISABLED, new Insets(5, 5, 5, 5), new Dimension(29, 19), false, AbstractRegionPainter.PaintContext.CacheMode.FIXED_SIZES, 1.0, 1.0));
        setUIKey("ProgressBar[Enabled].foregroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtProgressBarPainter", ProgressBarPainter.FOREGROUND_ENABLED, new Insets(5, 5, 5, 5), new Dimension(27, 19), false, AbstractRegionPainter.PaintContext.CacheMode.NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));
        setUIKey("ProgressBar[Enabled+Finished].foregroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtProgressBarPainter", ProgressBarPainter.FOREGROUND_ENABLED_FINISHED, new Insets(5, 5, 5, 5), new Dimension(27, 19), false, AbstractRegionPainter.PaintContext.CacheMode.NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));
        setUIKey("ProgressBar[Enabled+Indeterminate].progressPadding", new Integer(3));
        setUIKey("ProgressBar[Enabled+Indeterminate].foregroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtProgressBarPainter", ProgressBarPainter.FOREGROUND_ENABLED_INDETERMINATE, new Insets(5, 5, 5, 5), new Dimension(30, 13), false, AbstractRegionPainter.PaintContext.CacheMode.NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));
        setUIKey("ProgressBar[Disabled].foregroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtProgressBarPainter", ProgressBarPainter.FOREGROUND_DISABLED, new Insets(5, 5, 5, 5), new Dimension(27, 19), false, AbstractRegionPainter.PaintContext.CacheMode.NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));
        setUIKey("ProgressBar[Disabled+Finished].foregroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtProgressBarPainter", ProgressBarPainter.FOREGROUND_DISABLED_FINISHED, new Insets(5, 5, 5, 5), new Dimension(27, 19), false, AbstractRegionPainter.PaintContext.CacheMode.NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));
        setUIKey("ProgressBar[Disabled+Indeterminate].progressPadding", new Integer(3));
        setUIKey("ProgressBar[Disabled+Indeterminate].foregroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtProgressBarPainter", ProgressBarPainter.FOREGROUND_DISABLED_INDETERMINATE, new Insets(5, 5, 5, 5), new Dimension(30, 13), false, AbstractRegionPainter.PaintContext.CacheMode.NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));
        setUIKey("ProgressBar.textForeground", Color.WHITE);
        
        // Компонент: TableHeader
        setUIKey("TableHeader[Enabled].ascendingSortIconPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtTableHeaderPainter", TableHeaderPainter.ASCENDINGSORTICON_ENABLED, new Insets(0, 0, 0, 2), new Dimension(7, 7), false, FIXED_SIZES, 1.0, 1.0));
        setUIKey("Table.ascendingSortIcon", new ExtNimbusIcon("TableHeader", "ascendingSortIconPainter", 7, 7));
        setUIKey("TableHeader[Enabled].descendingSortIconPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtTableHeaderPainter", TableHeaderPainter.DESCENDINGSORTICON_ENABLED, new Insets(0, 0, 0, 0), new Dimension(7, 7), false, FIXED_SIZES, 1.0, 1.0));
        setUIKey("Table.descendingSortIcon", new ExtNimbusIcon("TableHeader", "descendingSortIconPainter", 7, 7));
        setUIKey("TableHeader:\"TableHeader.renderer\"[Disabled].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtTableHeaderRendererPainter", TableHeaderRendererPainter.BACKGROUND_DISABLED, new Insets(5, 5, 5, 5), new Dimension(22, 20), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        setUIKey("TableHeader:\"TableHeader.renderer\"[Enabled].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtTableHeaderRendererPainter", TableHeaderRendererPainter.BACKGROUND_ENABLED, new Insets(5, 5, 5, 5), new Dimension(22, 20), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        setUIKey("TableHeader:\"TableHeader.renderer\"[Enabled+Focused].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtTableHeaderRendererPainter", TableHeaderRendererPainter.BACKGROUND_ENABLED_FOCUSED, new Insets(5, 5, 5, 5), new Dimension(22, 20), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        setUIKey("TableHeader:\"TableHeader.renderer\"[MouseOver].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtTableHeaderRendererPainter", TableHeaderRendererPainter.BACKGROUND_MOUSEOVER, new Insets(5, 5, 5, 5), new Dimension(22, 20), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        setUIKey("TableHeader:\"TableHeader.renderer\"[Pressed].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtTableHeaderRendererPainter", TableHeaderRendererPainter.BACKGROUND_PRESSED, new Insets(5, 5, 5, 5), new Dimension(22, 20), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        setUIKey("TableHeader:\"TableHeader.renderer\"[Enabled+Sorted].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtTableHeaderRendererPainter", TableHeaderRendererPainter.BACKGROUND_ENABLED_SORTED, new Insets(5, 5, 5, 5), new Dimension(22, 20), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        setUIKey("TableHeader:\"TableHeader.renderer\"[Enabled+Focused+Sorted].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtTableHeaderRendererPainter", TableHeaderRendererPainter.BACKGROUND_ENABLED_FOCUSED_SORTED, new Insets(5, 5, 5, 5), new Dimension(22, 20), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        setUIKey("TableHeader:\"TableHeader.renderer\"[Disabled+Sorted].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtTableHeaderRendererPainter", TableHeaderRendererPainter.BACKGROUND_DISABLED_SORTED, new Insets(5, 5, 5, 5), new Dimension(22, 20), false, NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        setUIKey("TableHeader.textForeground", Color.WHITE);
        
        // Компонент: ScrollBarThumb
        setUIKey("ScrollBar:ScrollBarThumb[Enabled].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtScrollBarThumbPainter", ExtScrollBarThumbPainter.BACKGROUND_ENABLED, new Insets(0, 15, 0, 15), new Dimension(38, 15), false, AbstractRegionPainter.PaintContext.CacheMode.NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));
        setUIKey("ScrollBar:ScrollBarThumb[MouseOver].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtScrollBarThumbPainter", ExtScrollBarThumbPainter.BACKGROUND_MOUSEOVER, new Insets(0, 15, 0, 15), new Dimension(38, 15), false, AbstractRegionPainter.PaintContext.CacheMode.NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));
        setUIKey("ScrollBar:ScrollBarThumb[Pressed].backgroundPainter", new LazyPainter("javax.swing.plaf.nimbus.ExtScrollBarThumbPainter", ExtScrollBarThumbPainter.BACKGROUND_PRESSED, new Insets(0, 15, 0, 15), new Dimension(38, 15), false, AbstractRegionPainter.PaintContext.CacheMode.NINE_SQUARE_SCALE, Double.POSITIVE_INFINITY, 2.0));

        // Компонент: Button
        
        return d;
    }

    public static void setUIKey(String key, Object value) {
        UIManager.getDefaults().remove(key);
        UIManager.getDefaults().put(key, value);
    }

    public static void removeUIKey(String key) {
        UIManager.getDefaults().remove(key);
    }

    public static void showUIKeys() {
        for (Enumeration e = UIManager.getDefaults().keys(); e.hasMoreElements();) {
            Object key = e.nextElement();
            Object value = UIManager.get(key);
            if (key instanceof String && key.toString().startsWith("TextFi")
                    ){//&& key.toString().endsWith("Painter")) {
                //UIManager.put(key, Resources.GUI.font);
                System.out.println("key=" + key + " [" + value + "]");
            }
        }
    }

    /**
     * This class is private because it relies on the constructor of the
     * auto-generated AbstractRegionPainter subclasses. Hence, it is not
     * generally useful, and is private.
     * <p/>
     * LazyPainter is a LazyValue class. It will create the
     * AbstractRegionPainter lazily, when asked. It uses reflection to load the
     * proper class and invoke its constructor.
     */
    public static final class LazyPainter implements UIDefaults.LazyValue {

        private int which;
        private AbstractRegionPainter.PaintContext ctx;
        private String className;

        LazyPainter(String className, int which, Insets insets,
                Dimension canvasSize, boolean inverted) {
            if (className == null) {
                throw new IllegalArgumentException(
                        "The className must be specified");
            }

            this.className = className;
            this.which = which;
            this.ctx = new AbstractRegionPainter.PaintContext(
                    insets, canvasSize, inverted);
        }

        LazyPainter(String className, int which, Insets insets,
                Dimension canvasSize, boolean inverted,
                AbstractRegionPainter.PaintContext.CacheMode cacheMode,
                double maxH, double maxV) {
            if (className == null) {
                throw new IllegalArgumentException(
                        "The className must be specified");
            }

            this.className = className;
            this.which = which;
            this.ctx = new AbstractRegionPainter.PaintContext(
                    insets, canvasSize, inverted, cacheMode, maxH, maxV);
        }

        @Override
        public Object createValue(UIDefaults table) {
            try {
                Class c;
                Object cl;
                // See if we should use a separate ClassLoader
                if (table == null || !((cl = table.get("ClassLoader")) instanceof ClassLoader)) {
                    cl = Thread.currentThread().
                            getContextClassLoader();
                    if (cl == null) {
                        // Fallback to the system class loader.
                        cl = ClassLoader.getSystemClassLoader();
                    }
                }

                c = Class.forName(className, true, (ClassLoader) cl);
                Constructor constructor = c.getConstructor(
                        AbstractRegionPainter.PaintContext.class, int.class);
                if (constructor == null) {
                    throw new NullPointerException(
                            "Failed to find the constructor for the class: "
                            + className);
                }
                return constructor.newInstance(ctx, which);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
