package lesson6;

import java.util.Arrays;

/**
 * 2. Написать метод, которому в качестве аргумента передается не пустой одномерный целочисленный массив.
 * Метод должен вернуть новый массив, который получен путем вытаскивания из исходного массива элементов,
 * идущих после последней четверки. Входной массив должен содержать хотя бы одну четверку, иначе в методе
 * необходимо выбросить RuntimeException. Написать набор тестов для этого метода (по 3-4 варианта входных данных).
 * Вх: [ 1 2 4 4 2 3 4 1 7 ] -> вых: [ 1 7 ].
 * 3. Написать метод, который проверяет состав массива из чисел 1 и 4. Если в нем нет хоть одной четверки или
 * единицы, то метод вернет false; Написать набор тестов для этого метода (по 3-4 варианта входных данных).
 * [ 1 1 1 4 4 1 4 4 ] -> true
 * [ 1 1 1 1 1 1 ] -> false
 * [ 4 4 4 4 ] -> false
 * [ 1 4 4 1 1 4 3 ] -> false
 */
public class HomeWorkApp6 {

    public int[] afterFour(int[] arr) {
        int value = 4;
        int index = -1;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == value) {
                index = i;
            }
        }
        if (index < 0) {
            throw new RuntimeException("Входной массив должен содержать хотя бы одну " + value);
        }
        return Arrays.copyOfRange(arr, index + 1, arr.length);
    }

    public boolean onesAndFours(int[] arr) {
        int value1 = 1;
        int value2 = 4;
        boolean value1Exist = false;
        boolean value2Exist = false;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == value1) {
                value1Exist = true;
                continue;
            } else if (arr[i] == value2) {
                value2Exist = true;
                continue;
            }
            return false;
        }
        if (value1Exist && value2Exist) {
            return true;
        }
        return false;
    }
}
