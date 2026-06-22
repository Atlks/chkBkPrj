package org.example;
public class HiLd {

    private static final Object LOCK = new Object();
    private static volatile boolean running = true;

    public static void main(String[] args) throws Exception {
        int contenders = args.length > 0 ? Integer.parseInt(args[0]) : 1600; // 竞争锁的线程数
        long holdMillis = args.length > 1 ? Long.parseLong(args[1]) :600 * 60_000; // 持锁时间
        long runMillis  = args.length > 2 ? Long.parseLong(args[2]) : 60 * 60_000; // 程序总运行时间

        System.out.println("contenders=" + contenders +
                ", holdMillis=" + holdMillis +
                ", runMillis=" + runMillis);

        // 1. 持锁线程：拿到锁后长时间 sleep，不释放
        Thread holder = new Thread(() -> {
            synchronized (LOCK) {
                try {
                    Thread.sleep(holdMillis);
                } catch (InterruptedException ignored) {}
            }
        }, "lock-holder");
        holder.setDaemon(true);
        holder.start();

        // 确保 holder 先拿到锁
        Thread.sleep(1000);

        // 2. 大量竞争锁的线程：不断尝试 synchronized
        for (int i = 0; i < contenders; i++) {
            Thread t = new Thread(() -> {
                while (running) {
                    // 这里的 synchronized 尝试会让线程处于 runnable 状态排队
                    synchronized (LOCK) {
                        // 理论上永远进不来
                    }
                }
            }, "contender-" + i);
            t.setDaemon(true);
            t.start();
        }

        // 3. 运行一段时间后自动退出
        Thread.sleep(runMillis);
        running = false;
        System.out.println("Done.");
    }
}
