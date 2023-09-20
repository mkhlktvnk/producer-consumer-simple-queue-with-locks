package org.example;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    private static final Integer PRODUCER_RATE_PER_SEC = 10; // количество генерируемых сообщений в секунду
    private static final Integer CONSUMER_RATE_PER_SEC = 1; // количество обрабатываемых сообщений в секунду
    private static final Integer MAX_QUEUE_SIZE = 10; // максимальное количество сообщений в очереди
    private static final Integer PRODUCERS_COUNT = 10; // количество производителей
    private static final Integer CONSUMERS_COUNT = 1; // количество потребителей

    public static void main(String[] args) {
        Queue<String> queue = new LinkedList<>();
        Lock lock = new ReentrantLock();

        for (int i = 0; i < PRODUCERS_COUNT; i++) {
            Producer producer = new Producer(queue, lock, PRODUCER_RATE_PER_SEC, MAX_QUEUE_SIZE);
            Thread producerThread = new Thread(producer);
            producerThread.start();
        }

        for (int i = 0; i < CONSUMERS_COUNT; i++) {
            Consumer consumer = new Consumer(queue, lock, CONSUMER_RATE_PER_SEC);
            Thread consumerThread = new Thread(consumer);
            consumerThread.start();
        }
    }
}

class Producer implements Runnable {

    private final Queue<String> queue;
    private final Lock lock;
    private final long rateLimiter;
    private final int maxQueueSize;

    public Producer(Queue<String> queue, Lock lock, int rate, int maxQueueSize) {
        this.queue = queue;
        this.lock = lock;
        this.rateLimiter = 1000 / rate;
        this.maxQueueSize = maxQueueSize;
    }

    @Override
    public void run() {
        while (true) {
            lock.lock();
            try {
                if (queue.size() < maxQueueSize) {
                    String message = UUID.randomUUID().toString();
                    queue.offer(message);
                    printState(message, queue.size());
                }
            } finally {
                lock.unlock();
            }
            try {
                Thread.sleep(rateLimiter);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void printState(String message, int queueSize) {
        System.out.println("-------------Producer-------------");
        System.out.println("Message: " + message);
        System.out.println("Queue size:" + queueSize);
    }
}


class Consumer implements Runnable {

    private final Queue<String> queue;
    private final Lock lock;
    private final long rateLimiter;

    public Consumer(Queue<String> queue, Lock lock, int rate) {
        this.queue = queue;
        this.lock = lock;
        this.rateLimiter = 1000 / rate;
    }

    @Override
    public void run() {
        while (true) {
            lock.lock();
            try {
                if (!queue.isEmpty()) {
                    String message = queue.poll();
                    printState(message, queue.size());
                }
            } finally {
                lock.unlock();
            }
            try {
                Thread.sleep(rateLimiter);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void printState(String message, int queueSize) {
        System.out.println("-------------Consumer-------------");
        System.out.println("Message: " + message);
        System.out.println("Queue size:" + queueSize);
    }
}