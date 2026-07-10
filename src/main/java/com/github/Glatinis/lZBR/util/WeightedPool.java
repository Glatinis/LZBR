package com.github.Glatinis.lZBR.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// An immutable pool of values, each with a relative weight, that can be sampled at random. Weights
// need not sum to anything; entries with a non-positive weight are dropped. When every weight is zero
// the pool falls back to a uniform draw. Shared by the loot rarity roll and the mob-type roll.
public final class WeightedPool<T> {
    private final List<T> values;
    private final double[] cumulativeWeights;
    private final double totalWeight;

    private WeightedPool(List<T> values, double[] cumulativeWeights, double totalWeight) {
        this.values = values;
        this.cumulativeWeights = cumulativeWeights;
        this.totalWeight = totalWeight;
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public int size() {
        return values.size();
    }

    // Returns a weighted-random value, or null if the pool is empty.
    public T roll(Random random) {
        if (values.isEmpty()) return null;
        if (totalWeight <= 0) return values.get(random.nextInt(values.size()));

        double target = random.nextDouble() * totalWeight;
        for (int i = 0; i < values.size(); i++) {
            if (target < cumulativeWeights[i]) return values.get(i);
        }
        return values.get(values.size() - 1);
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static final class Builder<T> {
        private final List<T> values = new ArrayList<>();
        private final List<Double> weights = new ArrayList<>();

        public Builder<T> add(T value, double weight) {
            if (weight > 0) {
                values.add(value);
                weights.add(weight);
            }
            return this;
        }

        public WeightedPool<T> build() {
            double[] cumulative = new double[values.size()];
            double running = 0;
            for (int i = 0; i < values.size(); i++) {
                running += weights.get(i);
                cumulative[i] = running;
            }
            return new WeightedPool<>(List.copyOf(values), cumulative, running);
        }
    }
}
