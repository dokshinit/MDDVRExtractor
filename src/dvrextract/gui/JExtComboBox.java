package dvrextract.gui;

import java.awt.Dimension;
import java.util.*;
import javax.swing.*;

/**
 * <pre><b>Расширенный ComboBox</b>
 * Формирования модели данных - заполнение программным путем.
 * Данные формируются и применяются в основном потоке.
 *
 * <u><b>Пример:</b></u>
 * class JModeComboBox extends JExtComboBox {
 *
 *     public JModeComboBox() throws FaultException {
 *         super(false);
 *         init();
 *         addItem(0, "Режим 1");
 *         addItem(1, "Режим 2");
 *         addItem(2, "Режим 3");
 *         showData();
 *     }
 * }
 * 
 * JModeComboBox b = new JModeComboBox();
 * b.showData();
 * 
 * </pre>
 * @author Докшин_А_Н
 */
public class JExtComboBox extends JComboBox {

    // Объекты записей списка.
    protected ArrayList items;

    /**
     * Конструктор.
     */
    public JExtComboBox() {
        items = new ArrayList();
        setEnabled(false);
    }

    /**
     * Применение модели - отображение списка.
     */
    public void showData() {
        setModel(new DefaultComboBoxModel(items.toArray()));
        setEnabled(true);
    }

    /**
     * Добавление элемента в список
     * (для отображения необходимо вызвать showData()).
     * @param item Элемент списка.
     */
    public void addItem(ExtItem item) {
        items.add(item);
    }

    /**
     * Добавление элемента в список
     * (для отображения необходимо вызвать showData()).
     * @param id Код.
     * @param title Название.
     */
    public void addItem(int id, String title) {
        addItem(new ExtItem(id, title));
    }

    /**
     * Очистка записей из списка 
     * (для отображения необходимо вызвать showData()).
     */
    public void removeItems() {
        items.clear();
    }

    /**
     * Устанавливает компоненту постоянную ширину.
     * Имеет смысл если заполнение идет в отдельном потоке и необходимо 
     * избежать изменения размера компонента после загрузки.
     * @param width Ширина.
     */
    public void setConstWidth(int width) {
        Dimension d = getPreferredSize();
        d.width = width;
        setPreferredSize(d);
        d = getMaximumSize();
        d.width = width;
        setMaximumSize(d);
    }

    /**
     * Возвращает текущий выбранный элемент.
     * @return Текущий элемент списка.
     */
    @Override
    public ExtItem getSelectedItem() {
        return (ExtItem) super.getSelectedItem();
    }

    /**
     * Класс для формирования данных выпадающего списка
     */
    public class ExtItem {

        // Код.
        public int id;
        // Название.
        public String title;

        /**
         * Конструктор.
         * @param id Код.
         * @param title Название.
         */
        public ExtItem(int id, String title) {
            this.id = id;
            this.title = title;
        }

        /**
         * Представление записи для отображения.
         * @return Название.
         */
        @Override
        public String toString() {
            return title;
        }
    }
}
