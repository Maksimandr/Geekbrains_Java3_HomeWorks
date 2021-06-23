package lesson7;

public class TestClass {

    @BeforeSuite
    public void testMethod1() {
        System.out.println("Тестируем метод 1 (@BeforeSuite)");
    }

    @Test
    public void testMethod2() {
        System.out.println("Тестируем метод 2 (priority = 5 по умолчанию)");
    }

    @Test(priority = 3)
    public void testMethod3() {
        System.out.println("Тестируем метод 3 (priority = 3)");
    }

    @ParametrizedCsvSource(parameters = {
            "1, 1",
            "2, 3",
            "7, 5",
            "4, 4"
    })
    @Test(priority = 3)
    public void testMethod3(int a, int b) {
        System.out.println("Тестируем метод 3 (priority = 3) с параметрами (int a = " + a + ", int b = " + b + "), результат = a + b = " + (a + b));
    }

    @ParametrizedCsvSource(parameters = {
            "1, 1, 3",
            "2, 3, 6",
            "7, 5, 9",
            "4, 4, 8"
    })
    @Test(priority = 3)
    public void testMethod3(int a, int b, int c) {
        System.out.println("Тестируем метод 3 (priority = 3) с параметрами (int a = " + a + ", int b = " + b + ", int c = " + c + "), результат = a + b + c = " + (a + b + c));
    }

    @Test(priority = 8)
    public void testMethod5() {
        System.out.println("Тестируем метод 5 (priority = 8)");
    }

    @Test(priority = 1)
    public void testMethod6() {
        System.out.println("Тестируем метод 6 (priority = 1)");
    }

    @Test(priority = 3)
    public void testMethod7() {
        System.out.println("Тестируем метод 7 (priority = 3)");
    }

    @AfterSuite
    public void testMethod8() {
        System.out.println("Тестируем метод 8 (@AfterSuite)");
    }
}
