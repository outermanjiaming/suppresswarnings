package com.suppresswarnings.osgi.nn.lambda;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Test {

    public static void main(String[] args) throws Exception {
        List<String> list = Arrays.asList("a", "c", "c", "b", "a", "a", "e", "d");
        List<String> duplicateElements = list.stream()
                .collect(Collectors.toMap(e -> e, e -> 1, (a, b) -> a + b))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 1)
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());

        System.out.println("duplicate elements: " + duplicateElements);
    }
}
