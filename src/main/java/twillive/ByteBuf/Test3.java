package twillive.ByteBuf;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class Test3 {
    public static void main(String[] args) {
//线程不安全的更新
        //        Person person = new Person();
//        for (int i = 0; i < 10; i++) {
//            Thread thread = new Thread(() -> {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                System.out.println(person.age++);
//
//            });
//            thread.start();
//        }
        Person person1 = new Person();
        AtomicIntegerFieldUpdater<Person> updater = AtomicIntegerFieldUpdater.newUpdater(Person.class, "age");
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(updater.getAndIncrement(person1));

            });
            thread.start();
        }
    }
}

class Person {
    volatile int age = 1;
}
