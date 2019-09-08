package java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Cocos {
    public static void main(String[] args) throws IOException {
        Files.lines(Paths.get("/Users/lijiaming/company/mine/libcocos2djs.so")).forEach(System.out::println);
    }
}
