package org.eulerdiagrams.ConcreteDiagram;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

import org.eulerdiagrams.AbstractDiagram.AbstractContour;
import org.eulerdiagrams.AbstractDiagram.AbstractDiagram;
import org.eulerdiagrams.vennom.graph.Graph;
import org.eulerdiagrams.vennom.graph.Node;
import org.eulerdiagrams.vennom.graph.Edge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import math.geom2d.Point2D;

import org.jgrapht.graph.DefaultEdge;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.*;

public class TestConcreteDiagram {

    private AbstractContour a, b, c;

    @Before
    public void setUp() {
        a = new AbstractContour("A");
        b = new AbstractContour("B");
    }

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

        DirectedGraph<ConcreteZone, DefaultEdge> actual = getContainmentHeirarchy(cd);

        // Build an expected containment graph
        DirectedGraph<ConcreteZone, DefaultEdge> expected = new DefaultDirectedGraph<ConcreteZone, DefaultEdge>(DefaultEdge.class);

        assertThat(expected, is(equalTo(getContainmentHeirarchy(cd))));
    }

    @Test
    public void testContainmentGraph() {
        DirectedGraph<ConcreteZone, DefaultEdge> x = new DefaultDirectedGraph<>(DefaultEdge.class);
        DirectedGraph<ConcreteZone, DefaultEdge> y = new DefaultDirectedGraph<>(DefaultEdge.class);

        assertThat(x, equalTo(y));

        x.addVertex(ConcreteZone.Top.getInstance());
        y.addVertex(ConcreteZone.Top.getInstance());

        assertThat(x, equalTo(y));

        ConcreteCircle ca = new ConcreteCircle(a, new Point2D(-5, 0), 3.0);
        x.addVertex(new ConcreteZone(ca));
        y.addVertex(new ConcreteZone(ca));

        x.addEdge(ConcreteZone.Top.getInstance(), new ConcreteZone(ca));
        y.addEdge(ConcreteZone.Top.getInstance(), new ConcreteZone(ca));

        assertThat(x, equalTo(y));
    }

    @Test
    public void testDisconnected2() {
        Vector<ConcreteCircle> circles = new Vector();
        ConcreteCircle ca = new ConcreteCircle(a, new Point2D(-5, 0), 3.0);
        ConcreteCircle cb = new ConcreteCircle(b, new Point2D(5, 0), 3.0);
        circles.addAll(Arrays.asList(new ConcreteCircle[]{ca, cb}));

        ConcreteZone za = new ConcreteZone(ca);
        ConcreteZone zb = new ConcreteZone(cb);
        ConcreteDiagram d = new ConcreteDiagram(circles);

        // Build an expected containment graph
        DirectedGraph<ConcreteZone, DefaultEdge> expected = new DefaultDirectedGraph<ConcreteZone, DefaultEdge>(DefaultEdge.class);
        expected.addVertex(ConcreteZone.Top.getInstance());
        expected.addVertex(za);
        expected.addVertex(zb);

        expected.addEdge(ConcreteZone.Top.getInstance(), za);
        expected.addEdge(ConcreteZone.Top.getInstance(), zb);

        assertTrue(graphEdgeAndVertexEqualty(expected, getContainmentHeirarchy(d)));
    }

    @Test
    public void testContained2() {
        Vector<ConcreteCircle> circles = new Vector();
        ConcreteCircle ca = new ConcreteCircle(a, new Point2D(-5, 0), 10.0);
        ConcreteCircle cb = new ConcreteCircle(b, new Point2D(-5, 0), 3.0);
        circles.addAll(Arrays.asList(new ConcreteCircle[]{ca, cb}));

        ConcreteZone za = new ConcreteZone(ca);
        ConcreteZone zab = new ConcreteZone(ca, cb);
        ConcreteDiagram d = new ConcreteDiagram(circles);

        // Build an expected containment graph
        DirectedGraph<ConcreteZone, DefaultEdge> expected = new DefaultDirectedGraph<ConcreteZone, DefaultEdge>(DefaultEdge.class);
        expected.addVertex(ConcreteZone.Top.getInstance());
        expected.addVertex(za);
        expected.addVertex(zab);

        expected.addEdge(ConcreteZone.Top.getInstance(), za);
        expected.addEdge(za, zab);

        assertThat(expected, is(equalTo(getContainmentHeirarchy(d))));
    }

    @Test
    public void testContained3() {
        Vector<ConcreteCircle> circles = new Vector();
        ConcreteCircle ca = new ConcreteCircle(a, new Point2D(-5, 0), 10.0);
        ConcreteCircle cb = new ConcreteCircle(b, new Point2D(-5, 0), 3.0);
        ConcreteCircle cc = new ConcreteCircle(c, new Point2D(-5, 0), 1.0);
        circles.addAll(Arrays.asList(new ConcreteCircle[]{ca, cb, cc}));

        ConcreteZone za = new ConcreteZone(ca);
        ConcreteZone zab = new ConcreteZone(ca, cb);
        ConcreteZone zabc = new ConcreteZone(ca, cb, cc);
        ConcreteDiagram d = new ConcreteDiagram(circles);

        // Build an expected containment graph
        DirectedGraph<ConcreteZone, DefaultEdge> expected = new DefaultDirectedGraph<ConcreteZone, DefaultEdge>(DefaultEdge.class);
        expected.addVertex(ConcreteZone.Top.getInstance());
        expected.addVertex(za);
        expected.addVertex(zab);
        expected.addVertex(zabc);

        expected.addEdge(ConcreteZone.Top.getInstance(), za);
        expected.addEdge(za, zab);
        expected.addEdge(zab, zabc);

        assertThat(expected, is(equalTo(getContainmentHeirarchy(d))));
    }

    @Test
    public void testConcurrent3() {
        Vector<ConcreteCircle> circles = new Vector();
        ConcreteCircle ca = new ConcreteCircle(a, new Point2D(-5, 0), 10.0);
        ConcreteCircle cb = new ConcreteCircle(b, new Point2D(-5, 0), 10.0);
        ConcreteCircle cc = new ConcreteCircle(c, new Point2D(-5, 0), 10.0);
        circles.addAll(Arrays.asList(new ConcreteCircle[]{ca, cb, cc}));

        ConcreteZone zabc = new ConcreteZone(ca, cb, cc);
        ConcreteDiagram d = new ConcreteDiagram(circles);

        // Build an expected containment graph
        DirectedGraph<ConcreteZone, DefaultEdge> expected = new DefaultDirectedGraph<ConcreteZone, DefaultEdge>(DefaultEdge.class);
        expected.addVertex(ConcreteZone.Top.getInstance());
        expected.addVertex(zabc);

        expected.addEdge(ConcreteZone.Top.getInstance(), zabc);

        assertTrue(graphEdgeAndVertexEqualty(expected, getContainmentHeirarchy(d)));
    }

    private static DirectedGraph<ConcreteZone, DefaultEdge> getContainmentHeirarchy(ConcreteDiagram d) {
        // Now,  break the access protection on the internal containment
        // hierarchy graph, and check that the graph is correct.
        DirectedGraph<ConcreteZone, DefaultEdge> actual = null;
        try {
            Field field = ConcreteDiagram.class.getDeclaredField("containment");
            field.setAccessible(true);
            actual = (DirectedGraph<ConcreteZone, DefaultEdge>) field.get(d);
        } catch (NoSuchFieldException nsfe) {
            fail();
        } catch (IllegalAccessException iae) {
            fail();
        }
        System.out.println(actual.toString());
        return actual;
    }

    private static boolean graphEdgeAndVertexEqualty(DirectedGraph<ConcreteZone, DefaultEdge> g1, DirectedGraph<ConcreteZone, DefaultEdge> g2) {
        // g1.edgeSet().equals(g2.esgeSet()) doesn't work as g1 and g2 are multigraphs.  This is the "equality" we require.
        for(DefaultEdge g1e : g1.edgeSet()) {
            boolean thisEdgeInBoth = false;
            ConcreteZone source = g1.getEdgeSource(g1e);
            ConcreteZone target = g1.getEdgeTarget(g1e);
            for(DefaultEdge g2e : g2.edgeSet()) {
                thisEdgeInBoth |= g2.containsEdge(source, target);
            }
            // I have not found g1e in g2.
            if(! thisEdgeInBoth) {
                return false;
            }
        }

        return g1.vertexSet().equals(g2.vertexSet());
    }
}
