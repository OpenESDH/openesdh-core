package dk.openesdh.repo.utils;

import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;

public class JSONArrayCollector {

    public static <T> Collector<T, ?, org.json.simple.JSONArray> simple() {
        return Collector.of(
                org.json.simple.JSONArray::new, // supplier
                org.json.simple.JSONArray::add, // accumulator
                combinerSimpleJSONArray(),
                finisher());

    }

    public static Collector<org.json.JSONObject, ?, org.json.JSONArray> json() {
        return Collector.of(
                org.json.JSONArray::new, // supplier
                org.json.JSONArray::put, // accumulator
                combinerJSONArray(),
                finisher());

    }

    private static BinaryOperator<org.json.simple.JSONArray> combinerSimpleJSONArray() {
        return (left, right) -> {
            left.addAll(right);
            return left;
        };
    }

    private static BinaryOperator<org.json.JSONArray> combinerJSONArray() {
        return (left, right) -> {
            left.put(right);
            return left;
        };
    }

    private static <I, R> Function<I, R> finisher() {
        return i -> (R) i;
    }

}
