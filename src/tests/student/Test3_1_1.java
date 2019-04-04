package tests.student;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import base.Edge;
import base.Graph;

class Test3_1_1 {

  @Test
  void getAllValues() {
    Graph<Integer> graph = new Graph<>();
    graph.addNode(1);
    graph.addNode(2);
    graph.addNode(3);
    graph.addNode(4);
    graph.addEdge(graph.getNode(1), graph.getNode(2));
    graph.addEdge(graph.getNode(1), graph.getNode(3));
    graph.addEdge(graph.getNode(1), graph.getNode(4));

    List<Integer> values = new ArrayList<>();
    values.add(1);
    values.add(2);
    values.add(3);
    values.add(4);
    
    Graph<Integer> emptyGraph = new Graph<>();

    assertEquals(values, graph.getAllValues());
    assertEquals(new ArrayList<>(), emptyGraph.getAllValues());
  }

  @Test
  void getEdges() {
    Graph<Integer> graph = new Graph<>();
    graph.addNode(1);
    graph.addNode(2);
    graph.addNode(3);
    graph.addNode(4);
    graph.addNode(5);
    graph.addEdge(graph.getNode(2), graph.getNode(1));
    graph.addEdge(graph.getNode(1), graph.getNode(3));
    graph.addEdge(graph.getNode(1), graph.getNode(4));

    List<Edge<Integer>> edges = new ArrayList<>();
    edges.add(graph.getEdge(graph.getNode(1), graph.getNode(2)));
    edges.add(graph.getEdge(graph.getNode(1), graph.getNode(3)));
    edges.add(graph.getEdge(graph.getNode(1), graph.getNode(4)));

    assertEquals(edges, graph.getEdges(graph.getNode(1)));
    assertEquals(new ArrayList<>(), graph.getEdges(graph.getNode(5)));
  }
  
  @Test
  void getNode() {
    Graph<Integer> graph = new Graph<>();
    graph.addNode(1);
    
    assertEquals(null, graph.getNode(0));
    assertEquals(Integer.valueOf(1), graph.getNode(1).getValue());
  }


}
