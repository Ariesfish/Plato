package xyz.ariesfish;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// 使用ConcurrentHashMap
public class CHMDemo {
    public static ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>();

    public static void process(Path file) {
        try (var in = new Scanner(file)) {
            while (in.hasNext()) {
                String word = in.next();
                map.merge(word, 1L, Long::sum); // 如果不存在该word, 则value设为1, 存在则计算和再设为value
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Set<Path> descendants(Path rootDir) throws IOException {
        try (Stream<Path> entries = Files.walk(rootDir)) { // 遍历指定目录下的所有文件
            return entries.collect(Collectors.toSet());
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(processors);
        Path pathToRoot = Path.of(".");
        for (Path p : descendants(pathToRoot)) {
            if (p.getFileName().toString().endsWith(".java")) {
                executor.execute(() -> process(p));
                System.out.println(p.getFileName().toString());
            }
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);
        map.forEach((k,v) -> { // 批操作
            if (v >= 10)
                System.out.println(k + " occurs " + v + " times");
        });
    }
}
