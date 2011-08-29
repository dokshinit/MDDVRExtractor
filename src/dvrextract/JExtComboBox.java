package dvrextract;

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
 *     }
 *
 *     public void addItem(ResultSet rs) throws Exception {
 *         items.add(new ExtItem(0, "Наличные"));
 *         items.add(new ExtItem(1, "МК-деньги (без СБ и Петрол)"));
 *         items.add(new ExtItem(2, "МК-Сбербанк"));
 *         items.add(new ExtItem(3, "МК-Петрол"));
 *         items.add(new ExtItem(4, "МК-ответ-хранение (без разбивки по клиентам!)"));
 *     }
 * }
 * </pre>
 * @author Докшин_А_Н
 */
public class JExtComboBox extends JComboBox {

    // Список
    protected ArrayList items;          // Объекты записей списка.

    /**
     * Конструктор.
     * @param isAddItem_All Флаг - добавлять в список элемент "Все".
     */
    public JExtComboBox() {
        items = new ArrayList();
        setEnabled(false);
    }
    
    /**
     * Применение модели.
     */
    protected void showData() {
        setModel(new DefaultComboBoxModel(items.toArray()));
        setEnabled(true);
    }

    public void addItem(int id, String title) {
        items.add(new ExtItem(id, title));
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

        public int id;
        public String title;

        public ExtItem(int id, String title) {
            this.id = id;
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    /**
     * Метод генерации события.
     */
    public void fireAfterLoad() {
    }
}
