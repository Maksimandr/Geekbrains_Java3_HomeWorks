package lesson1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * 3. Большая задача:
 * a. Есть классы Fruit -> Apple, Orange;(больше фруктов не надо)
 * b. Класс Box в который можно складывать фрукты, коробки условно сортируются по типу фрукта, поэтому в одну коробку
 * нельзя сложить и яблоки, и апельсины;
 * c. Для хранения фруктов внутри коробки можете использовать ArrayList;
 * d. Сделать метод getWeight() который высчитывает вес коробки, зная количество фруктов и вес одного фрукта(вес яблока
 * - 1.0f, апельсина - 1.5f, не важно в каких это единицах);
 * e. Внутри класса коробка сделать метод compare, который позволяет сравнить текущую коробку с той, которую подадут в
 * compare в качестве параметра, true - если их веса равны, false в противном случае(коробки с яблоками мы можем
 * сравнивать с коробками с апельсинами);
 * f. Написать метод, который позволяет пересыпать фрукты из текущей коробки в другую коробку(помним про сортировку
 * фруктов, нельзя яблоки высыпать в коробку с апельсинами), соответственно в текущей коробке фруктов не остается, а в
 * другую перекидываются объекты, которые были в этой коробке;
 * g. Не забываем про метод добавления фрукта в коробку.
 */

public class Box<T extends Fruit> {

    // список фруктов в коробке
    private ArrayList<T> fruits;
    // тип фруктов в коробке
    private String fruitType = null;

    /**
     * Конструктор
     *
     * @param fruits
     */
    public Box(T... fruits) {
        this.fruits = new ArrayList<>(Arrays.asList(fruits));
        fruitType = fruits.getClass().getTypeName();
    }

    /**
     * Метод высчитывает вес коробки
     *
     * @return вес
     */
    public float getWeight() {
        if (!fruits.isEmpty()) {
            return fruits.size() * fruits.get(0).getWeight();
        }
        return 0.0f;
    }

    /**
     * Сравнивает веса текущей и сравниваемой коробок
     *
     * @param box сравниваемая коробка
     * @param <B> тип объекта (наследуется от класса Box)
     * @return true если веса равны
     */
    public <B extends Box> boolean compare(B box) {
        return Math.abs(this.getWeight() - box.getWeight()) < 0.0001;
    }

    /**
     * Метод пересыпает фрукты из текущей коробки в указанную
     *
     * @param box коробка в которую пересыпаются все фрукты
     * @param <B> тип объекта (наследуется от класса Box)
     */
    public <B extends Box> void put(B box) {
        if (!this.getFruitType().equals(box.getFruitType())) {
            throw new IllegalArgumentException("Проверь соответствие типов фруктов в коробках");
        }
        box.addAll(fruits);
        fruits.clear();
    }

//        box.addAll(fruits.toArray()); //не работает, т.к. возвращает массив Object'ов
//        box.addAll(fruits.toArray(new T[0])); // тоже не работает, т.к. не дает создать массив T[0], даже если сделать
// класс Fruit не абстрактным

// хотел сделать такую реализацию, чтобы один или несколько фруктов добавлять в одном методе, но не смог найти способа
// преобразовать коллекцию в массив типа T[]
//        public void addAll(T... fruits) {
//        this.fruits.addAll(Arrays.asList(fruits));
//    }

    /**
     * Добавляет несколько фруктов
     *
     * @param fruits коллекция добавляемых фруктов
     */
    public void addAll(Collection<T> fruits) {
        this.fruits.addAll(fruits);
    }

    /**
     * Добавляет один фрукт
     *
     * @param fruit добавляемый фрукт
     */
    public void add(T fruit) {
        fruits.add(fruit);
    }

    /**
     * геттер для фруктов
     *
     * @return список фруктов в коробке
     */
    public ArrayList<T> getFruits() {
        return fruits;
    }

    /**
     * Возвращает тип фруктов в коробке
     *
     * @return тип фруктов в коробке
     */
    public String getFruitType() {
        return fruitType;
    }

    /**
     * здесь только тесты
     *
     * @param args
     */
    public static void main(String[] args) {
        Box<Apple> box1 = new Box<>(new Apple(), new Apple(), new Apple(), new Apple(), new Apple());
        Box<Apple> box2 = new Box<>(new Apple(), new Apple(), new Apple(), new Apple(), new Apple());
        Box<Orange> box3 = new Box<>(new Orange(), new Orange(), new Orange(), new Orange());
        Box<Orange> box4 = new Box<>();

        System.out.println(box1.getWeight());//5.0
        System.out.println(box2.getWeight());//5.0
        System.out.println(box3.getWeight());//6.0
        System.out.println(box1.compare(box3));//false
        box1.add(new Apple());
        System.out.println(box1.getWeight());//6.0
        System.out.println(box1.compare(box3));//true
        box1.add(new Apple());
        System.out.println(box1.getWeight());//7.0
        System.out.println(box1.compare(box3));//false
        box1.put(box2);
        System.out.println(box1.getWeight());//0.0
        System.out.println(box2.getWeight());//12.0
        box2.put(box1);
        System.out.println(box1.getWeight());//12.0
        System.out.println(box2.getWeight());//0.0

    }
}
