package org.eulerdiagrams.ConcreteDiagram;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eulerdiagrams.AbstractDiagram.AbstractContour;
import org.eulerdiagrams.AbstractDiagram.AbstractDiagram;
import org.eulerdiagrams.vennom.graph.Graph;
import org.eulerdiagrams.vennom.graph.Node;
import org.eulerdiagrams.vennom.graph.Edge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import org.junit.Test;
import static org.hamcrest.Matchers.*;

public class TestConcreteDiagram {

    @Test
    public void testVenn2() {
        // Example taken from running K-ttlebr-cke
        Node a = new Node(), b = new Node();
        a.setX(303);
        a.setY(187);
        a.setContour("a");
        a.setLabel("6.90988298");
        b.setX(439);
        b.setY(110);
        b.setContour("b");
        b.setLabel("6.90988298");

        Edge e = new Edge(a,b);

        Graph g = new Graph();
        g.addNode(a);
        g.addNode(b);
        g.addEdge(e);

        AbstractContour ac = new AbstractContour("a");
        AbstractContour bc = new AbstractContour("b");
        Set<AbstractContour> contours = new HashSet<AbstractContour>(Arrays.asList(ac,bc));
        AbstractDiagram ad = new AbstractDiagram(contours);

        ConcreteDiagram cd = new ConcreteDiagram(g, ad);

        // Now,  break the access protection on the internal containment
        // hierarchy graph, and check that the graph is correct.
        DirectedGraph<ConcreteZone, DefaultEdge> actual = null;
        try {
            Field field = ConcreteDiagram.class.getDeclaredField("containment");
            field.setAccessible(true);
            actual = (DirectedGraph<ConcreteZone, DefaultEdge>) field.get(cd);
        } catch (NoSuchFieldException nsfe) {
            fail();
        } catch (IllegalAccessException iae) {
            fail();
        }

        // Build an expected containment graph
        DirectedGraph<ConcreteZone, DefaultEdge> expected = new DefaultDirectedGraph<ConcreteZone, DefaultEdge>(DefaultEdge.class);

        assertThat(expected, equalTo(actual));
    }

}
