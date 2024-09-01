package org.eu.todo.web.operations;

import org.eu.todo.web.display.Highlight;
import org.eu.todo.web.display.Statistic;

import java.util.HashMap;
import java.util.Map;
import java.util.function.*;
import java.util.stream.Gatherer;

public class MapCountByGatherer<T, P> implements Gatherer<T, Map<P, Integer>, T> {
    private final Function<T, P> selector;

    public MapCountByGatherer(Function<T, P> extractor) {
        this.selector = extractor;
    }

    @Override
    public Supplier<Map<P, Integer>> initializer() {
        return HashMap::new;
    }

    @Override
    public Integrator<Map<P, Integer>, T, T> integrator() {
        return Integrator.ofGreedy((state, item, _) -> {
            P property = selector.apply(item);
            state.merge(property, 1, Integer::sum);
            return true;
        });
    }

    @Override
    public BiConsumer<Map<P, Integer>, Downstream<? super T>> finisher() {
        return (state, downstream) -> state.forEach((property, count) -> {
            downstream.push((T) new Statistic((Highlight) property, count));
        });
    }
}