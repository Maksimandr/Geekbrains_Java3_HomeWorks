package lesson4;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 1. Создать три потока, каждый из которых выводит определенную букву (A, B и C) 5 раз (порядок – ABСABСABС).
 * Используйте wait/notify/notifyAll.
 */
public class HomeWorkApp4 {

    private static final Object lock = new Object();
    private static char currentLetter;

    public static void main(String[] args) {

        // создаем сервис на три потока
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        // начинаем с буквы 'A'
        currentLetter = 'A';

        // задаём потоки
        executorService.execute(() -> {
            printLetter('A', 'B');
        });
        executorService.execute(() -> {
            printLetter('B', 'C');
        });
        executorService.execute(() -> {
            printLetter('C', 'A');
        });

        executorService.shutdown();
    }

    /**
     * Метод для печати буковки и всего такого
     *
     * @param letter     текущая буква
     * @param nextLetter следующая буква
     */
    public static void printLetter(char letter, char nextLetter) {
        synchronized (lock) {
            try {
                for (int i = 0; i < 5; i++) {
                    while (currentLetter != letter) {
                        lock.wait();
                    }
                    System.out.print(letter);
                    currentLetter = nextLetter;
                    lock.notifyAll();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
