package lesson1;

import java.util.Arrays;
import java.util.List;

/**
 * 1. Написать метод, который меняет два элемента массива местами.(массив может быть любого ссылочного типа);
 * 2. Написать метод, который преобразует массив в ArrayList;
 */

public class HomeWorkApp1 {

    /**
     * метод, который меняет два элемента массива местами.(массив может быть любого ссылочного типа)
     *
     * @param firstIndex  индекс первого элемента
     * @param secondIndex индекс второго элемента
     * @param array       изменяемый массив
     * @param <T>         любой ссылочный тип
     * @return изменённый массив
     */
    public static <T> T[] swap(int firstIndex, int secondIndex, T... array) {
        if (firstIndex >= array.length || secondIndex >= array.length || firstIndex < 0 || secondIndex < 0) {
            throw new IndexOutOfBoundsException("Проверь передаваемые индексы");
        }
        T swapVariable = array[firstIndex];
        array[firstIndex] = array[secondIndex];
        array[secondIndex] = swapVariable;
        return array;
    }

    /**
     * метод, который преобразует массив в ArrayList
     *
     * @param array исходный массив
     * @param <T>   любой ссылочный тип
     * @return ArrayList на основе исходного массива
     */
    public static <T> List<T> arrayToList(T... array) {
        return Arrays.asList(array);
    }

    public static void main(String[] args) {
        String[] strings = {"a", "b", "c", "d", "e"};
        Integer[] integers = {1, 2, 3, 4, 5};

        //[a, b, c, d, e]
        System.out.println(Arrays.toString(strings));
        //[a, c, b, d, e]
        System.out.println(Arrays.toString(swap(1, 2, strings)));

        //[1, 2, 3, 4, 5]
        System.out.println(Arrays.toString(integers));
        //[5, 2, 3, 4, 1]
        System.out.println(Arrays.toString(swap(0, 4, integers)));

        //java.lang.String[]
        System.out.println(strings.getClass().getTypeName());
        //java.util.Arrays$ArrayList
        System.out.println(arrayToList(integers).getClass().getTypeName());

    }
}
