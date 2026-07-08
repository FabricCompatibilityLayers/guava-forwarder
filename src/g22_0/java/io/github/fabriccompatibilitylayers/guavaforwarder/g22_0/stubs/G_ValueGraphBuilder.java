package io.github.fabriccompatibilitylayers.guavaforwarder.g22_0.stubs;

import com.google.common.graph.Graph;
import com.google.common.graph.ValueGraphBuilder;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStub;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubs;

@GuavaStubs("com/google/common/graph/ValueGraphBuilder")
public class G_ValueGraphBuilder {
    @GuavaStub(introducedIn = "20.0")
    public static <N> ValueGraphBuilder<N, Object> from(Graph<N> graph) {
        ValueGraphBuilder<Object, Object> builder;

        if (graph.isDirected()) {
            builder = ValueGraphBuilder.directed();
        } else {
            builder = ValueGraphBuilder.undirected();
        }

        return builder.allowsSelfLoops(graph.allowsSelfLoops())
                .nodeOrder(graph.nodeOrder());
    }
}
