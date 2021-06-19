package lesson7;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 1. Создать класс, который может выполнять «тесты», в качестве тестов выступают классы с наборами методов
 * с аннотациями @Test. Для этого у него должен быть статический метод start(), которому в качестве параметра
 * передается или объект типа Class, или имя класса. Из «класса-теста» вначале должен быть запущен метод с
 * аннотацией @BeforeSuite, если такой имеется, далее запущены методы с аннотациями @Test, а по завершению
 * всех тестов – метод с аннотацией @AfterSuite. К каждому тесту необходимо также добавить приоритеты
 * (int числа от 1 до 10), в соответствии с которыми будет выбираться порядок их выполнения, если приоритет
 * одинаковый, то порядок не имеет значения. Методы с аннотациями @BeforeSuite и @AfterSuite должны присутствовать
 * в единственном экземпляре, иначе необходимо бросить RuntimeException при запуске «тестирования».
 */
public class HomeWorkApp7 {

    public static void start(Class clazz) {

        Class beforeSuiteClass = BeforeSuite.class;
        Class afterSuiteClass = AfterSuite.class;
        Class testClass = Test.class;

        List<Annotation[]> annotationList = new ArrayList<>();
        Method[] methods = clazz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            annotationList.add(methods[i].getDeclaredAnnotations());
        }

        // аннотации @BeforeSuite и @AfterSuite должны присутствовать в единственном экземпляре
        if (annotationList.stream().map(a -> Arrays.toString(a)).filter(a -> a.contains(beforeSuiteClass.getName())).count() > 1) {
            throw new RuntimeException("Аннотация BeforeSuite может быть только одна.");
        } else if (annotationList.stream().map(a -> Arrays.toString(a)).filter(a -> a.contains(afterSuiteClass.getName())).count() > 1) {
            throw new RuntimeException("Аннотация AfterSuite может быть только одна.");
        }

        // если аннотация @BeforeSuite присутствует, ставим метод которому она принадлежит в начало
        for (int i = 0; i < methods.length; i++) {
            if (Arrays.stream(methods[i].getDeclaredAnnotations()).map(a -> a.toString()).anyMatch(a -> a.contains(beforeSuiteClass.getName()))) {
                if (i != 0) {
                    Method temp = methods[0];
                    methods[0] = methods[i];
                    methods[i] = temp;
                }
                break;
            }
        }

        // если аннотация @AfterSuite присутствует, ставим метод которому она принадлежит в конец
        for (int i = 0; i < methods.length; i++) {
            if (Arrays.stream(methods[i].getDeclaredAnnotations()).map(a -> a.toString()).anyMatch(a -> a.contains(afterSuiteClass.getName()))) {
                if (i != methods.length - 1) {
                    Method temp = methods[methods.length - 1];
                    methods[methods.length - 1] = methods[i];
                    methods[i] = temp;
                }
                break;
            }
        }

        // все методы у которых есть аннотация @Test сортируем по приоритету от 1 до 10 (1 - максимальный приоритет)
        for (int i = 0; i < methods.length - 1; i++) {
            for (int j = i + 1; j < methods.length; j++) {
                if (Arrays.stream(methods[i].getDeclaredAnnotations()).map(a -> a.toString()).anyMatch(a -> a.contains(testClass.getName()))
                        & Arrays.stream(methods[j].getDeclaredAnnotations()).map(a -> a.toString()).anyMatch(a -> a.contains(testClass.getName()))) {
                    if (methods[i].getAnnotation(Test.class).priority() > methods[j].getAnnotation(Test.class).priority()) {
                        Method temp = methods[i];
                        methods[i] = methods[j];
                        methods[j] = temp;
                    }
                }
            }
        }

        //  создаем объект аласса TestClass через конструктор по умолчанию и вызываем все методы по очереди
        try {
            TestClass calcTest = (TestClass) clazz.getConstructor().newInstance();
            for (Method m : methods) {
                m.invoke(calcTest);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        start(TestClass.class);
    }
}
