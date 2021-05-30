package lesson1;

import java.util.Arrays;
import java.util.List;

public class HomeWorkApp1<T> {

    public static <T> T[] swap(int firstIndex, int secondIndex, T... array) {
        if (firstIndex >= array.length || secondIndex >= array.length || firstIndex < 0 || secondIndex < 0) {
            throw new IndexOutOfBoundsException("Проверь передаваемые индексы");
        }
        T swapVariable = array[firstIndex];
        array[firstIndex] = array[secondIndex];
        array[secondIndex] = swapVariable;
        return array;
    }

    public static <T> List<T> ArrayToList(T... array) {
        return Arrays.asList(array);
    }

    public static void main(String[] args) {
        String[] strings = {"a", "b", "c", "d", "e"};
        Integer[] integers = {1, 2, 3, 4, 5};

        //[a, b, c, d, e]
        System.out.println(Arrays.toString(strings));
        //[a, c, b, d, e]
        System.out.println(Arrays.toString(HomeWorkApp1.swap(1, 2, strings)));

        //[1, 2, 3, 4, 5]
        System.out.println(Arrays.toString(integers));
        //[5, 2, 3, 4, 1]
        System.out.println(Arrays.toString(HomeWorkApp1.swap(0, 4, integers)));

        //java.lang.String
        System.out.println(strings.getClass().getName());
        //java.util.Arrays$ArrayList
        System.out.println(ArrayToList(integers).getClass().getName());

    }
}
