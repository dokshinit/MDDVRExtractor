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
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class JExtComboBox extends JComboBox {

    // Объекты записей списка.
    protected ArrayList<ExtItem> items;

    /**
     * Конструктор.
     */
    public JExtComboBox() {
        items = new ArrayList<ExtItem>();
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
     * @param object Объект.
     */
    public void addItem(int id, Object object) {
        addItem(new ExtItem(id, object));
    }

    /**
     * Очистка записей из списка 
     * (для отображения необходимо вызвать showData()).
     */
    public void removeItems() {
        items.clear();
    }

    /**
     * Возвращает кол-во позиций в подготовленном списке (не кол-во отображенных!).
     * @return Кол-во позиций в списке.
     */
    public int getListItemCount() {
        return items.size();
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
    
    public void setSelectedId(int id) {
        for (ExtItem i : items) {
            if (i.id == id) {
                setSelectedItem(i);
                return;
            }
        }
        setSelectedItem(null);
    }

    /**
     * Класс для формирования данных выпадающего списка
     */
    public static class ExtItem {

        // Код.
        public int id;
        // Объект (обязательно должен иметь текстовое представление, которое 
        // будет отображаться в списке!).
        public Object object;

        /**
         * Конструктор.
         * @param id Код.
         * @param object Объект.
         */
        public ExtItem(int id, Object object) {
            this.id = id;
            this.object = object;
        }

        /**
         * Представление записи для отображения.
         * @return Название.
         */
        @Override
        public String toString() {
            return object.toString();
        }
    }
}
