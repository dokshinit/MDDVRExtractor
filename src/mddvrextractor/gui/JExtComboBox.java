/*
 * Copyright (c) 2011-2013, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextractor.gui;

import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

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
 * b.showData(); // Обязательное действие - отображение модели.
 * </pre>
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class JExtComboBox extends JComboBox {

    /**
     * Объекты записей списка.
     */
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
     * Добавление элемента в список (для отображения необходимо вызвать
     * showData()).
     *
     * @param item Элемент списка.
     */
    public void addItem(ExtItem item) {
        items.add(item);
    }

    /**
     * Добавление элемента в список (для отображения необходимо вызвать
     * showData()).
     *
     * @param id Код.
     * @param object Объект.
     */
    public void addItem(int id, Object object) {
        addItem(new ExtItem(id, object));
    }

    /**
     * Возвращает объект с заданным id.
     *
     * @param id Числовой идентификатор объекта.
     * @return Объект.
     */
    public Object getItemObject(int id) {
        for (ExtItem i : items) {
            if (i.id == id) {
                return i.object;
            }
        }
        return null;
    }

    /**
     * Очистка записей из списка (для отображения необходимо вызвать
     * showData()).
     */
    public void removeItems() {
        items.clear();
    }

    /**
     * Возвращает кол-во позиций в подготовленном списке (не кол-во
     * отображенных!).
     *
     * @return Кол-во позиций в списке.
     */
    public int getListItemCount() {
        return items.size();
    }

    /**
     * Устанавливает компоненту постоянную ширину. Имеет смысл если заполнение
     * идет в отдельном потоке и необходимо избежать изменения размера
     * компонента после загрузки.
     *
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
     *
     * @return Текущий элемент списка.
     */
    @Override
    public ExtItem getSelectedItem() {
        return (ExtItem) super.getSelectedItem();
    }

    /**
     * Установка текущего выбранного элемента по его коду.
     *
     * @param id Код.
     * @return Результат выполнения: true - успешно, false - ошибка.
     */
    public boolean setSelectedId(int id) {
        for (ExtItem i : items) {
            if (i.id == id) {
                setSelectedItem(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Класс для формирования данных выпадающего списка
     */
    public static class ExtItem {

        /**
         * Код.
         */
        public int id;
        /**
         * Объект (обязательно должен иметь текстовое представление, которое
         * будет отображаться в списке!).
         */
        public Object object;

        /**
         * Конструктор.
         *
         * @param id Код.
         * @param object Объект.
         */
        public ExtItem(int id, Object object) {
            this.id = id;
            this.object = object;
        }

        /**
         * Представление записи для отображения.
         *
         * @return Название.
         */
        @Override
        public String toString() {
            return object.toString();
        }
    }
}
