package lesson7;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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

    private static final Class<BeforeSuite> beforeSuiteClass = BeforeSuite.class;
    private static final Class<AfterSuite> afterSuiteClass = AfterSuite.class;
    private static final Class<Test> testClass = Test.class;
    private static final Class<ParametrizedCsvSource> parametrizedCsvSourceClass = ParametrizedCsvSource.class;

    public static void start(Class clazz) {

        Method beforeSuiteHolder = null;
        Method afterSuiteHolder = null;

        Method[] methods = clazz.getDeclaredMethods();
        List<Method> methodList = new ArrayList<>();

        for (Method method : methods) {
            if (method.getAnnotation(beforeSuiteClass) != null) {
                if (beforeSuiteHolder != null) {
                    throw new RuntimeException("Аннотация BeforeSuite может быть только одна.");
                }
                beforeSuiteHolder = method;
            }
            if (method.getAnnotation(afterSuiteClass) != null) {
                if (afterSuiteHolder != null) {
                    throw new RuntimeException("Аннотация AfterSuite может быть только одна.");
                }
                afterSuiteHolder = method;
            }
            if (method.getAnnotation(testClass) != null) {
                methodList.add(method);
            }
        }

        // сортируем список тестов по приоритету (1 - максимальный приоритет)
        methodList.sort((m1, m2) -> {
            int p1 = m1.getAnnotation(testClass).priority();
            int p2 = m2.getAnnotation(testClass).priority();
            return Integer.compare(p1, p2);
        });

        // добавляем методы с аннотациями BeforeSuite и AfterSuite в начало и конец списка (если они есть)
        if (beforeSuiteHolder != null) {
            methodList.add(0, beforeSuiteHolder);
        }
        if (afterSuiteHolder != null) {
            methodList.add(afterSuiteHolder);
        }

        //  создаем объект класса TestClass через конструктор по умолчанию и вызываем все методы по очереди
        try {
            TestClass calcTest = (TestClass) clazz.getConstructor().newInstance();
            for (Method m : methodList) {
                // если метод без параметров
                if (m.getAnnotation(parametrizedCsvSourceClass) == null) {
                    m.invoke(calcTest);
                } else {
                    // если метод с параметрами типа int
                    String[] strings = m.getAnnotation(parametrizedCsvSourceClass).parameters();
                    for (String s : strings) {
                        String[] splitStr = s.split(",");
                        Integer[] integers = new Integer[splitStr.length];
                        for (int i = 0; i < splitStr.length; i++) {
                            integers[i] = Integer.parseInt(splitStr[i].trim());
                        }
                        m.invoke(calcTest, integers);
                    }
                }
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        start(TestClass.class);
    }
}
