package net.flectone.sqlite;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class CustomThreadPool {

    private BlockingQueue<Runnable> runnableQueue;
    private List<WorkerThread> threads;
    private AtomicBoolean isThreadPoolShutDownInitiated;

    public CustomThreadPool(final int noOfThreads) {
        this.runnableQueue = new LinkedBlockingQueue<>();
        this.threads = new ArrayList<>(noOfThreads);
        this.isThreadPoolShutDownInitiated = new AtomicBoolean(false);

        for (int i = 1; i <= noOfThreads; i++) {
            WorkerThread thread = new WorkerThread(runnableQueue, this);
            thread.setName("Worker Thread - " + i);
            thread.start();
            threads.add(thread);
        }
    }

    public void execute(Runnable r)  {
        try {
            if (!isThreadPoolShutDownInitiated.get()) {
                runnableQueue.put(r);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        isThreadPoolShutDownInitiated = new AtomicBoolean(true);
    }

    private class WorkerThread extends Thread {

        private BlockingQueue<Runnable> taskQueue;
        private CustomThreadPool threadPool;

        public WorkerThread(BlockingQueue<Runnable> taskQueue, CustomThreadPool threadPool) {
            this.taskQueue = taskQueue;
            this.threadPool = threadPool;
        }

        @Override
        public void run() {
            try {
                while (!threadPool.isThreadPoolShutDownInitiated.get() || !taskQueue.isEmpty()) {
                    Runnable r;
                    while ((r = taskQueue.poll()) != null) {
                        r.run();
                    }
                    Thread.sleep(1);
                }
            } catch (RuntimeException | InterruptedException e) {
                throw new CustomThreadPoolException(e);
            }
        }
    }

    private class CustomThreadPoolException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public CustomThreadPoolException(Throwable t) {
            super(t);
        }
    }

}
