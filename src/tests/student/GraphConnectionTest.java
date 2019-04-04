package tests.student;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.jupiter.api.Test;
import base.Graph;

public class GraphConnectionTest {


  @Test
  public void graphConnected() {
    Graph<Integer> graph = new Graph<>();

    // add nodes
    graph.addNode(1);
    graph.addNode(2);
    graph.addNode(3);
    graph.addNode(4);
    graph.addNode(5);

    // add edges
    graph.addEdge(graph.getNode(1), graph.getNode(2));
    graph.addEdge(graph.getNode(2), graph.getNode(3));
    graph.addEdge(graph.getNode(2), graph.getNode(4));
    graph.addEdge(graph.getNode(3), graph.getNode(4));
    graph.addEdge(graph.getNode(1), graph.getNode(5));

    assertTrue(graph.allNodesConnected());
  }

  @Test
  public void unconnectedGraph() {
    Graph<Integer> graph = new Graph<>();

    // add nodes
    graph.addNode(1);
    graph.addNode(2);
    graph.addNode(3);
    graph.addNode(4);
    graph.addNode(5);

    // add edges
    graph.addEdge(graph.getNode(1), graph.getNode(2));
    graph.addEdge(graph.getNode(2), graph.getNode(3));
    graph.addEdge(graph.getNode(2), graph.getNode(4));
    graph.addEdge(graph.getNode(3), graph.getNode(4));

    assertFalse(graph.allNodesConnected());
  }
}
