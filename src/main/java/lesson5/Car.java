package lesson5;

public class Car implements Runnable {

    private static int CARS_COUNT;

    static {
        CARS_COUNT = 0;
    }

    private Race race;
    private int speed;
    private String name;

    public String getName() {
        return name;
    }

    public int getSpeed() {
        return speed;
    }

    public Car(Race race, int speed) {
        this.race = race;
        this.speed = speed;
        CARS_COUNT++;
        this.name = "Участник #" + CARS_COUNT;

    }

    @Override
    public void run() {
        try {
            System.out.println(this.name + " готовится");
            Thread.sleep(500 + (int) (Math.random() * 800));
            System.out.println(this.name + " готов");
            // подтверждаем готовность к старту гоки
            HomeWorkApp5.cyclicBarrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < race.getStages().size(); i++) {
            race.getStages().get(i).go(this);
        }
        // по завершении гонки используем lock чтобы безопасно проверить финишировал ли уже кто-то до этого, если нет - победа
        HomeWorkApp5.lock.lock();
        if (HomeWorkApp5.countDownLatch.getCount() == HomeWorkApp5.CARS_COUNT) {
            System.out.println(this.name + " - WIN");
        }
        HomeWorkApp5.countDownLatch.countDown();
        HomeWorkApp5.lock.unlock();
    }
}