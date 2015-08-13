package org.eulerdiagrams.ConcreteDiagram;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

import org.eulerdiagrams.AbstractDiagram.AbstractContour;
import org.eulerdiagrams.AbstractDiagram.AbstractDiagram;
import org.eulerdiagrams.ConcreteDiagram.geomutils.DirectedEdge;
import org.eulerdiagrams.vennom.graph.Graph;
import org.eulerdiagrams.vennom.graph.Node;
import org.eulerdiagrams.vennom.graph.Edge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import math.geom2d.Point2D;

import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.*;

public class TestConcreteDiagram {

    private AbstractContour a, b, c;

    @Before
    public void setUp() {
        a = new AbstractContour("A");
        b = new AbstractContour("B");
        c = new AbstractContour("C");
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

        DirectedGraph<ConcreteZone, DirectedEdge> actual = getContainmentHeirarchy(cd);

        // Build an expected containment graph
        DirectedGraph<ConcreteZone, DirectedEdge> expected = new DefaultDirectedGraph<ConcreteZone, DirectedEdge>(DirectedEdge.class);

        assertThat(expected, is(equalTo(getContainmentHeirarchy(cd))));
    }

    @Test
    public void testContainmentGraph() {
        DirectedGraph<ConcreteZone, DirectedEdge> x = new DefaultDirectedGraph<>(DirectedEdge.class);
        DirectedGraph<ConcreteZone, DirectedEdge> y = new DefaultDirectedGraph<>(DirectedEdge.class);

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
        DirectedGraph<ConcreteZone, DirectedEdge> expected = new DefaultDirectedGraph<ConcreteZone, DirectedEdge>(DirectedEdge.class);
        expected.addVertex(ConcreteZone.Top.getInstance());
        expected.addVertex(za);
        expected.addVertex(zb);

        DirectedEdge t_za = new DirectedEdge(ConcreteZone.Top.getInstance(), za);
        DirectedEdge t_zb = new DirectedEdge(ConcreteZone.Top.getInstance(), zb);
        expected.addEdge(ConcreteZone.Top.getInstance(), za, t_za);
        expected.addEdge(ConcreteZone.Top.getInstance(), zb, t_zb);

        assertThat(expected, equalTo(getContainmentHeirarchy(d)));
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
        DirectedGraph<ConcreteZone, DirectedEdge> expected = new DefaultDirectedGraph<ConcreteZone, DirectedEdge>(DirectedEdge.class);
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
        DirectedGraph<ConcreteZone, DirectedEdge> expected = new DefaultDirectedGraph<ConcreteZone, DirectedEdge>(DirectedEdge.class);
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
        DirectedGraph<ConcreteZone, DirectedEdge> expected = new DefaultDirectedGraph<ConcreteZone, DirectedEdge>(DirectedEdge.class);
        expected.addVertex(ConcreteZone.Top.getInstance());
        expected.addVertex(zabc);

        DirectedEdge edge = new DirectedEdge(ConcreteZone.Top.getInstance(), zabc);
        expected.addEdge(ConcreteZone.Top.getInstance(), zabc, edge);

        assertThat(expected, is(equalTo(getContainmentHeirarchy(d))));
    }

    private static DirectedGraph<ConcreteZone, DirectedEdge> getContainmentHeirarchy(ConcreteDiagram d) {
        // Now,  break the access protection on the internal containment
        // hierarchy graph, and check that the graph is correct.
        DirectedGraph<ConcreteZone, DirectedEdge> actual = null;
        try {
            Field field = ConcreteDiagram.class.getDeclaredField("containment");
            field.setAccessible(true);
            actual = (DirectedGraph<ConcreteZone, DirectedEdge>) field.get(d);
        } catch (NoSuchFieldException nsfe) {
            fail();
        } catch (IllegalAccessException iae) {
            fail();
        }
        System.out.println(actual.toString());
        return actual;
    }
}
