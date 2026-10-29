package com.breakinblocks.justenoughcraftingtables.config;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.tuple.Pair;

public final class ConfigSpec {
    private final Map<String, Value<?>> values = new LinkedHashMap<>();
    private Path path;
    public Map<String, Value<?>> entries() { return Collections.unmodifiableMap(values); }
    public void load(String side) {
        path = FabricLoader.getInstance().getConfigDir().resolve(JustEnoughCraftingTables.MOD_ID + "-" + side + ".properties");
        if (Files.exists(path)) {
            Properties props = new Properties();
            try (var reader = Files.newBufferedReader(path)) {
                props.load(reader);
                values.forEach((key, value) -> {
                    String raw = props.getProperty(key);
                    if (raw != null) {
                        try { value.parse(raw); }
                        catch (IllegalArgumentException e) { JustEnoughCraftingTables.LOGGER.warn("Invalid config value {}={}; using default", key, raw); }
                    }
                });
            } catch (IOException e) { throw new IllegalStateException("Cannot read " + path, e); }
        }
        save();
    }
    public void save() {
        if (path == null) return;
        Properties props = new Properties();
        values.forEach((key, value) -> props.setProperty(key, value.get().toString()));
        try {
            Files.createDirectories(path.getParent());
            try (var writer = Files.newBufferedWriter(path)) { props.store(writer, "Just Enough Crafting Tables"); }
        } catch (IOException e) { throw new IllegalStateException("Cannot write " + path, e); }
    }
    public static class Value<T> {
        private T value;
        private ConfigSpec owner;
        public void save() { owner.save(); }
        private final Function<String, T> parser;
        Value(T value, Function<String, T> parser) { this.value = value; this.parser = parser; }
        public T get() { return value; }
        public boolean accepts(Object candidate) { try { parser.apply(candidate.toString()); return true; } catch (IllegalArgumentException e) { return false; } }
        public void set(T value) { this.value = parser.apply(value.toString()); }
        void parse(String raw) { value = parser.apply(raw); }
    }
    public static final class BooleanValue extends Value<Boolean> {
        BooleanValue(boolean value) { super(value, raw -> { if (!raw.equalsIgnoreCase("true") && !raw.equalsIgnoreCase("false")) throw new IllegalArgumentException(raw); return Boolean.parseBoolean(raw); }); }
    }
    public static final class IntValue extends Value<Integer> {
        IntValue(int value, int min, int max) { super(value, raw -> { int parsed = Integer.parseInt(raw); if (parsed < min || parsed > max) throw new IllegalArgumentException(raw); return parsed; }); }
    }
    public static final class EnumValue<E extends Enum<E>> extends Value<E> {
        EnumValue(E value) { super(value, raw -> Enum.valueOf(value.getDeclaringClass(), raw)); }
    }
    public static final class Builder {
        private final ConfigSpec spec = new ConfigSpec();
        private final Deque<String> path = new ArrayDeque<>();
        public <T> Pair<T, ConfigSpec> configure(Function<Builder, T> factory) { return Pair.of(factory.apply(this), spec); }
        public Builder push(String key) { path.addLast(key); return this; }
        public Builder pop() { path.removeLast(); return this; }
        public Builder translation(String key) { return this; }
        private <V extends Value<?>> V add(String name, V value) { ((Value<?>) value).owner = spec; spec.values.put(String.join(".", path) + "." + name, value); return value; }
        public BooleanValue define(String name, boolean value) { return add(name, new BooleanValue(value)); }
        public IntValue defineInRange(String name, int value, int min, int max) { return add(name, new IntValue(value, min, max)); }
        public <E extends Enum<E>> EnumValue<E> defineEnum(String name, E value) { return add(name, new EnumValue<>(value)); }
    }
}
