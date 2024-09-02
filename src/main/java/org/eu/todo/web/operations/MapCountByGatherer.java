package org.eu.todo.web.operations;

import org.eu.todo.web.display.Highlight;
import org.eu.todo.web.display.Statistic;

import java.util.HashMap;
import java.util.Map;
import java.util.function.*;
import java.util.stream.Gatherer;

/**
 * The MapCountByGatherer class is designed to group elements by a specific property and count occurrences.
 * It implements the Gatherer interface which allows it to process collections of objects and produce a result.
 *
 * @param <T> The type of elements being gathered.
 * @param <P> The type of the property used for grouping.
 */
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
    /// Returns an instance of the [Integrator] interface that can be used to
    /// accumulate items into a map where keys are properties extracted from the
    /// items using the provided function and values are counts of those properties.
    ///
    /// @return An instance of the [Integrator] interface.
    public Integrator<Map<P, Integer>, T, T> integrator() {
        return Integrator.ofGreedy((state, item, _) -> {
            P property = selector.apply(item);
            state.merge(property, 1, Integer::sum);
            return true;
        });
    }


    @Override
    /**
     * Returns a consumer that takes the accumulated state of the gatherer and pushes
     * each unique property along with its corresponding count into the downstream collector.
     * Each entry in the map represents a distinct property extracted from the input items,
     * and the value associated with each key indicates the number of times that property was found.
     *
     * @return A bi-consumer that accepts the current state of the gatherer and a downstream collector.
     */
    public BiConsumer<Map<P, Integer>, Downstream<? super T>> finisher() {
        return (state, downstream) -> state.forEach((property, count) -> {
            downstream.push((T) new Statistic((Highlight) property, count));
        });
    }
}