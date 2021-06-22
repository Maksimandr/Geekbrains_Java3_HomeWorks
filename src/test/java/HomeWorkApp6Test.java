import lesson6.HomeWorkApp6;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class HomeWorkApp6Test {

    HomeWorkApp6 homeWorkApp6;

    private static Stream<Arguments> afterFourSource1() {
        List<Arguments> list = new ArrayList<>();
        list.add(Arguments.arguments(new int[]{1, 2, 4, 4, 2, 3, 4, 1, 7}, new int[]{1, 7}));
        list.add(Arguments.arguments(new int[]{4, 2, 3, 3, 2, 3, 3, 4, 7}, new int[]{7}));
        list.add(Arguments.arguments(new int[]{4, 1, 3, 1, 1, 5, 3}, new int[]{1, 3, 1, 1, 5, 3}));
        list.add(Arguments.arguments(new int[]{4, 4, 4, 4}, new int[]{}));
        return list.stream();
    }

    private static Stream<Arguments> afterFourSource2() {
        List<Arguments> list = new ArrayList<>();
        list.add(Arguments.arguments(new int[]{1, 2, 6, 7, 2, 3, 8, 1, 7}));
        list.add(Arguments.arguments(new int[]{8, 2, 3, 3, 2, 3, 3, 6, 7}));
        list.add(Arguments.arguments(new int[]{2, 2, 2, 2}));
        list.add(Arguments.arguments(new int[]{1, 5, 3, 1, 1, 5, 3}));
        return list.stream();
    }

    private static Stream<Arguments> onesAndFoursSource() {
        List<Arguments> list = new ArrayList<>();
        list.add(Arguments.arguments(new int[]{1, 1, 1, 4, 4, 1, 4, 4}, true));
        list.add(Arguments.arguments(new int[]{1, 1, 1, 1, 1, 1}, false));
        list.add(Arguments.arguments(new int[]{4, 4, 4, 4}, false));
        list.add(Arguments.arguments(new int[]{1, 4, 4, 1, 1, 4, 3}, false));
        list.add(Arguments.arguments(new int[]{5, 3, 2, 6, 8, 7, 3}, false));
        return list.stream();
    }

    @BeforeEach
    void init() {
        homeWorkApp6 = new HomeWorkApp6();
    }

    @MethodSource("afterFourSource1")
    @ParameterizedTest
    void arrayAfterFourTest1(int[] testArr, int[] result) {
        int[] methodResult = homeWorkApp6.afterFour(testArr);
        assertArrayEquals(result, methodResult);
    }

    @MethodSource("afterFourSource2")
    @ParameterizedTest
    void arrayAfterFourTest2(int[] testArr) {
        assertThrows(RuntimeException.class, () -> homeWorkApp6.afterFour(testArr));
    }

    @MethodSource("onesAndFoursSource")
    @ParameterizedTest
    void onesAndFoursTest(int[] testArr, boolean result) {
        boolean methodResult = homeWorkApp6.onesAndFours(testArr);
        assertEquals(result, methodResult);
    }
}
