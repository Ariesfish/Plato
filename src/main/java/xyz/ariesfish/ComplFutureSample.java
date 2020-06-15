package xyz.ariesfish;

import java.util.concurrent.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ComplFutureSample {
    static ExecutorService service = Executors.newFixedThreadPool(4, new ThreadFactory() {
        int count = 1;
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "custom-executor-" + count++);
        }
    });

    private String delayedUpperCase(String s) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }
        return s.toUpperCase();
    }

    private String delayedLowerCase(String s) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }
        return s.toLowerCase();
    }

    @Test
    public void completedFuture() {
        CompletableFuture completableFuture = CompletableFuture.completedFuture("Message");
        assertTrue(completableFuture.isDone());
        assertEquals("Message", completableFuture.getNow(null));
    }

    @Test
    public void runAsync() {
        CompletableFuture completableFuture = CompletableFuture.runAsync(() -> { // 默认使用ForkJoinPool
                    assertTrue(Thread.currentThread().isDaemon()); // 异步执行
                    try {
                        Thread.sleep(1000);
                    } catch(InterruptedException e) {

                    }
                }
        );
        assertFalse(completableFuture.isDone());
        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {

        }
        assertTrue(completableFuture.isDone());
    }

    @Test
    public void thenApply() {
        CompletableFuture completableFuture = CompletableFuture.completedFuture("Message").thenApply(s -> {
            assertFalse(Thread.currentThread().isDaemon());
            return s.toUpperCase();
        });
        assertEquals("MESSAGE", completableFuture.getNow(null));
    }

    @Test
    public void thenApplyAsync() {
        CompletableFuture completableFuture = CompletableFuture.completedFuture("Message").thenApplyAsync(s -> {
            assertTrue(Thread.currentThread().isDaemon());
            return s.toUpperCase();
        });
        assertNull(completableFuture.getNow(null));
        assertEquals("MESSAGE", completableFuture.join());
    }

    @Test
    public void thenApplyAsyncWithExecutor() {
        CompletableFuture completableFuture = CompletableFuture.completedFuture("message").thenApplyAsync(s -> {
            assertTrue(Thread.currentThread().getName().startsWith("custom-executor-"));
            assertFalse(Thread.currentThread().isDaemon());
            return s.toUpperCase();
        }, service);
        assertNull(completableFuture.getNow(null));
        assertEquals("MESSAGE", completableFuture.join());
    }

    /**
     * Consume last stage result, not return value
     */
    @Test
    public void thenAccept() {
        StringBuilder result = new StringBuilder();
        CompletableFuture.completedFuture("thenAccept message").thenAccept(s -> result.append(s));
        assertTrue(result.length() > 0, "Result was empty");
    }

    @Test
    public void thenAcceptAsync() {
        StringBuilder result = new StringBuilder();
        CompletableFuture completableFuture =  CompletableFuture.completedFuture("thenAccept message").thenAcceptAsync(s -> result.append(s));
        completableFuture.join();
        assertTrue(result.length() > 0, "Result was empty");
    }

    @Test
    public void completeExceptionally() {
        CompletableFuture completableFuture = CompletableFuture.completedFuture("message")
                .thenApplyAsync(String::toUpperCase, CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));
        CompletableFuture exceptionHandler = completableFuture.handle((s, throwable) -> {
            return (throwable != null) ? "message upon cancel" : "";
        });

        // 完成操作并抛出异常，会触发exceptionHandler
        completableFuture.completeExceptionally(new RuntimeException("completed exceptionally"));
        assertTrue(completableFuture.isCompletedExceptionally(), "Was not completed exceptionally");

        try {
            // 等待完成返回结果或者抛出异常
            completableFuture.join();
            fail("Should have thrown an exception");
        } catch(CompletionException ex) {
            assertEquals("completed exceptionally", ex.getCause().getMessage());
        }
        assertEquals("message upon cancel", exceptionHandler.join());
    }

    @Test
    public void cancel() {
        CompletableFuture completableFuture = CompletableFuture.completedFuture("message")
                .thenApplyAsync(String::toUpperCase, CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));
        // 取消操作，返回结果为"cancel message", 正常结果为"MESSAGE"(如果异步操作先执行完)
        CompletableFuture cf2 = completableFuture.exceptionally(throwable -> "cancel message");
        assertTrue(completableFuture.cancel(true), "Was not canceled");
        assertTrue(completableFuture.isCompletedExceptionally(), "Was not completed exceptionally");
        assertEquals("cancel message", cf2.join());
    }

    @Test
    public void applyToEither() {
        String original = "Message";
        CompletableFuture cf1 = CompletableFuture.completedFuture(original).thenApplyAsync(s -> delayedUpperCase(s));
        CompletableFuture cf2 = cf1.applyToEither(
                CompletableFuture.completedFuture(original).thenApplyAsync(s -> delayedLowerCase(s)),
                s -> s + "from applyToEither");
        assertTrue(cf2.join().toString().endsWith("applyToEither"));
    }

    @Test
    public void acceptEither() {
        String original = "Message";
        StringBuilder result = new StringBuilder();
        CompletableFuture cf = CompletableFuture.completedFuture(original)
                .thenApplyAsync(s -> delayedUpperCase(s))
                .acceptEither(CompletableFuture.completedFuture(original).thenApply(s -> s.toLowerCase()), // first completed
                        s -> result.append(s).append(" acceptEither"));
        cf.join();
        assertEquals(result.toString(), "message acceptEither");
    }
}
