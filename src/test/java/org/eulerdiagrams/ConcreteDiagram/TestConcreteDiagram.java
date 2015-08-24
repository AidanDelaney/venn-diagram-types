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
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.*;

public class TestConcreteDiagram {

    private final static Logger logger = LoggerFactory.getLogger(TestConcreteDiagram.class);

    private AbstractContour a, b, c;

    @Before
    public void setUp() {
        a = new AbstractContour("A");
        b = new AbstractContour("B");
        c = new AbstractContour("C");
    }

    @Test
    public void testVenn2() {
        Node a = new Node(), b = new Node();
        a.setX(-5);
        a.setY(0);
        a.setContour("a");
        a.setLabel("7");
        b.setX(5);
        b.setY(0);
        b.setContour("b");
        b.setLabel("7");

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

        // Build an expected containment graph
        DirectedGraph<ConcreteZone, DefaultEdge> expected = new DefaultDirectedGraph<ConcreteZone, DefaultEdge>(DefaultEdge.class);
        expected.addVertex(ConcreteZone.Top.getInstance());

        AbstractContour aa = new AbstractContour("a");
        ConcreteCircle ca = new ConcreteCircle(aa, new Point2D(-5, 0), 7);
        ConcreteZone zca = new ConcreteZone(Arrays.asList(new ConcreteCircle[]{ca}));

        AbstractContour ab = new AbstractContour("b");
        ConcreteCircle cb = new ConcreteCircle(ab, new Point2D(5, 0), 7);
        ConcreteZone zcb = new ConcreteZone(Arrays.asList(new ConcreteCircle[]{cb}));

        ConcreteZone zcacb = new ConcreteZone(Arrays.asList(new ConcreteCircle[]{ca, cb}));

        expected.addVertex(zca);
        expected.addVertex(zcb);
        expected.addVertex(zcacb);

        expected.addEdge(ConcreteZone.Top.getInstance(), zca);
        expected.addEdge(ConcreteZone.Top.getInstance(), zcb);
        expected.addEdge(zca, zcacb);
        expected.addEdge(zcb, zcacb);

        //assertEquals(expected.toString(), equalTo(getContainmentHeirarchy(cd).toString()));
        assertTrue(graphEdgeAndVertexEqualty(expected, getContainmentHeirarchy(cd)));
    }

    @Test
    public void testVenn2Contained() {
        Vector<ConcreteCircle> circles = new Vector();
        ConcreteCircle ca = new ConcreteCircle(a, new Point2D(-5, 0), 12.0);
        ConcreteCircle cb = new ConcreteCircle(b, new Point2D(5, 0), 12.0);
        ConcreteCircle cc = new ConcreteCircle(c, new Point2D(0, 0), 100.0);
        circles.addAll(Arrays.asList(new ConcreteCircle[]{ca, cb, cc}));

        ConcreteDiagram d = new ConcreteDiagram(circles);

        // Construct expected
        DirectedGraph<ConcreteZone, DefaultEdge> expected = new DefaultDirectedGraph<ConcreteZone, DefaultEdge>(DefaultEdge.class);

        ConcreteZone zc = new ConcreteZone(cc);
        ConcreteZone zac = new ConcreteZone(ca, cc);
        ConcreteZone zbc = new ConcreteZone(cb, cc);
        ConcreteZone zabc = new ConcreteZone(ca, cb, cc);

        expected.addVertex(ConcreteZone.Top.getInstance());
        expected.addVertex(zc);
        expected.addVertex(zac);
        expected.addVertex(zbc);
        expected.addVertex(zabc);

        expected.addEdge(ConcreteZone.Top.getInstance(), zc);

        expected.addEdge(zc, zac);

        expected.addEdge(zc, zbc);

        expected.addEdge(zac, zabc);
        expected.addEdge(zbc, zabc);

        logger.info("expected: " + expected.toString() + " actual: " + getContainmentHeirarchy(d).toString());
        assertTrue(graphEdgeAndVertexEqualty(expected, getContainmentHeirarchy(d)));
    }

    @Test
    public void testConcurrentContained() {
        Vector<ConcreteCircle> circles = new Vector();
        ConcreteCircle ca = new ConcreteCircle(a, new Point2D(-5, 0), 12.0);
        ConcreteCircle cb = new ConcreteCircle(b, new Point2D(-5, 0), 12.0);
        ConcreteCircle cc = new ConcreteCircle(c, new Point2D(0, 0), 100.0);
        circles.addAll(Arrays.asList(new ConcreteCircle[]{ca, cb, cc}));

        ConcreteDiagram d = new ConcreteDiagram(circles);

        // Construct expected
        DirectedGraph<ConcreteZone, DefaultEdge> expected = new DefaultDirectedGraph<ConcreteZone, DefaultEdge>(DefaultEdge.class);

        ConcreteZone zc = new ConcreteZone(cc);
        ConcreteZone zabc = new ConcreteZone(ca, cb, cc);

        expected.addVertex(ConcreteZone.Top.getInstance());
        expected.addVertex(zc);
        expected.addVertex(zabc);

        expected.addEdge(ConcreteZone.Top.getInstance(), zc);
        expected.addEdge(zc, zabc);

        logger.info("expected: " + expected.toString() + " actual: " + getContainmentHeirarchy(d).toString());
        assertTrue(graphEdgeAndVertexEqualty(expected, getContainmentHeirarchy(d)));
    }

    @Test
    public void testVenn3() {
        Vector<ConcreteCircle> circles = new Vector();
        ConcreteCircle ca = new ConcreteCircle(a, new Point2D(-5, 0), 12.0);
        ConcreteCircle cb = new ConcreteCircle(b, new Point2D(5, 0), 12.0);
        ConcreteCircle cc = new ConcreteCircle(c, new Point2D(0, 5), 12.0);
        circles.addAll(Arrays.asList(new ConcreteCircle[]{ca, cb, cc}));

        ConcreteDiagram d = new ConcreteDiagram(circles);

        // Construct expected
        DirectedGraph<ConcreteZone, DefaultEdge> expected = new DefaultDirectedGraph<ConcreteZone, DefaultEdge>(DefaultEdge.class);

        ConcreteZone za = new ConcreteZone(ca);
        ConcreteZone zb = new ConcreteZone(cb);
        ConcreteZone zc = new ConcreteZone(cc);
        ConcreteZone zab = new ConcreteZone(ca, cb);
        ConcreteZone zac = new ConcreteZone(ca, cc);
        ConcreteZone zbc = new ConcreteZone(cb, cc);
        ConcreteZone zabc = new ConcreteZone(ca, cb, cc);

        expected.addVertex(ConcreteZone.Top.getInstance());
        expected.addVertex(za);
        expected.addVertex(zb);
        expected.addVertex(zc);
        expected.addVertex(zab);
        expected.addVertex(zac);
        expected.addVertex(zbc);
        expected.addVertex(zabc);

        expected.addEdge(ConcreteZone.Top.getInstance(), za);
        expected.addEdge(ConcreteZone.Top.getInstance(), zb);
        expected.addEdge(ConcreteZone.Top.getInstance(), zc);

        expected.addEdge(za, zac);
        expected.addEdge(zc, zac);

        expected.addEdge(za, zab);
        expected.addEdge(zb, zab);

        expected.addEdge(zb, zbc);
        expected.addEdge(zc, zbc);

        expected.addEdge(zac, zabc);
        expected.addEdge(zab, zabc);
        expected.addEdge(zbc, zabc);

        logger.info("expected: " + expected.toString() + " actual: " + getContainmentHeirarchy(d).toString());
        assertTrue(graphEdgeAndVertexEqualty(expected, getContainmentHeirarchy(d)));
    }

    @Test
    public void testContainmentGraph() {
        DirectedGraph<ConcreteZone, DefaultEdge> x = new DefaultDirectedGraph<>(DefaultEdge.class);
        DirectedGraph<ConcreteZone, DefaultEdge> y = new DefaultDirectedGraph<>(DefaultEdge.class);

        assertTrue(graphEdgeAndVertexEqualty(x, y));

        x.addVertex(ConcreteZone.Top.getInstance());
        y.addVertex(ConcreteZone.Top.getInstance());

        assertTrue(graphEdgeAndVertexEqualty(x, y));

        ConcreteCircle ca = new ConcreteCircle(a, new Point2D(-5, 0), 3.0);
        x.addVertex(new ConcreteZone(ca));
        y.addVertex(new ConcreteZone(ca));

        x.addEdge(ConcreteZone.Top.getInstance(), new ConcreteZone(ca));
        y.addEdge(ConcreteZone.Top.getInstance(), new ConcreteZone(ca));

        assertTrue(graphEdgeAndVertexEqualty(x, y));
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

        assertTrue(graphEdgeAndVertexEqualty(expected, getContainmentHeirarchy(d)));
    }

    @Test
    public void testCrossedTunnel() {
        Vector<ConcreteCircle> circles = new Vector();
        ConcreteCircle ca = new ConcreteCircle(a, new Point2D(-5, 0), 10.0);
        ConcreteCircle cb = new ConcreteCircle(b, new Point2D(-5, 0), 7.0);
        ConcreteCircle cc = new ConcreteCircle(c, new Point2D(7, 0), 7.0);
        circles.addAll(Arrays.asList(new ConcreteCircle[]{ca, cb, cc}));

        ConcreteDiagram d = new ConcreteDiagram(circles);

        // Build expected
        DirectedGraph<ConcreteZone, DefaultEdge> expected = new DefaultDirectedGraph<ConcreteZone, DefaultEdge>(DefaultEdge.class);

        ConcreteZone za = new ConcreteZone(ca);
        ConcreteZone zc = new ConcreteZone(cc);

        ConcreteZone zab = new ConcreteZone(ca, cb);
        ConcreteZone zac = new ConcreteZone(ca, cc);
        ConcreteZone zabc = new ConcreteZone(ca, cb, cc);

        expected.addVertex(ConcreteZone.Top.getInstance());
        expected.addVertex(za);
        expected.addVertex(zc);
        expected.addVertex(zab);
        expected.addVertex(zac);
        expected.addVertex(zabc);

        expected.addEdge(ConcreteZone.Top.getInstance(), za);
        expected.addEdge(ConcreteZone.Top.getInstance(), zc);

        expected.addEdge(za, zab);
        expected.addEdge(za, zac);
        expected.addEdge(zc, zac);
        expected.addEdge(zac, zabc);
        expected.addEdge(zab, zabc);

        logger.info("expected: " + expected.toString() + " actual: " + getContainmentHeirarchy(d).toString());
        assertTrue(graphEdgeAndVertexEqualty(expected, getContainmentHeirarchy(d)));
    }

    @Test
    public void testCrossedVenn2() {
        Vector<ConcreteCircle> circles = new Vector();
        ConcreteCircle ca = new ConcreteCircle(a, new Point2D(-5, 0), 6.0);
        ConcreteCircle cb = new ConcreteCircle(b, new Point2D(5, 0), 6.0);
        ConcreteCircle cc = new ConcreteCircle(c, new Point2D(-6, 0), 6.0);
        circles.addAll(Arrays.asList(new ConcreteCircle[]{ca, cb, cc}));

        ConcreteDiagram d = new ConcreteDiagram(circles);

        // Build expected
        DirectedGraph<ConcreteZone, DefaultEdge> expected = new DefaultDirectedGraph<ConcreteZone, DefaultEdge>(DefaultEdge.class);

        ConcreteZone za = new ConcreteZone(ca);
        ConcreteZone zc = new ConcreteZone(cc);

        ConcreteZone zab = new ConcreteZone(ca, cb);
        ConcreteZone zac = new ConcreteZone(ca, cc);
        ConcreteZone zabc = new ConcreteZone(ca, cb, cc);

        expected.addVertex(ConcreteZone.Top.getInstance());
        expected.addVertex(za);
        expected.addVertex(zc);
        expected.addVertex(zab);
        expected.addVertex(zac);
        expected.addVertex(zabc);

        expected.addEdge(ConcreteZone.Top.getInstance(), za);
        expected.addEdge(ConcreteZone.Top.getInstance(), zc);

        expected.addEdge(za, zab);
        expected.addEdge(za, zac);
        expected.addEdge(zc, zac);
        expected.addEdge(zac, zabc);
        expected.addEdge(zab, zabc);

        logger.info("expected: " + expected.toString() + " actual: " + getContainmentHeirarchy(d).toString());
        assertTrue(graphEdgeAndVertexEqualty(expected, getContainmentHeirarchy(d)));
    }

    @Test
    public void testChain3() {
        Vector<ConcreteCircle> circles = new Vector();
        ConcreteCircle ca = new ConcreteCircle(a, new Point2D(-5, 0), 6.0);
        ConcreteCircle cb = new ConcreteCircle(b, new Point2D(5, 0), 6.0);
        ConcreteCircle cc = new ConcreteCircle(c, new Point2D(15, 0), 6.0);
        circles.addAll(Arrays.asList(new ConcreteCircle[]{ca, cb, cc}));

        ConcreteDiagram d = new ConcreteDiagram(circles);

        // Build expected
        DirectedGraph<ConcreteZone, DefaultEdge> expected = new DefaultDirectedGraph<ConcreteZone, DefaultEdge>(DefaultEdge.class);

        ConcreteZone za = new ConcreteZone(ca);
        ConcreteZone zb = new ConcreteZone(cb);
        ConcreteZone zc = new ConcreteZone(cc);

        ConcreteZone zab = new ConcreteZone(ca, cb);
        ConcreteZone zbc = new ConcreteZone(cb, cc);

        expected.addVertex(ConcreteZone.Top.getInstance());
        expected.addVertex(za);
        expected.addVertex(zb);
        expected.addVertex(zc);
        expected.addVertex(zab);
        expected.addVertex(zbc);

        expected.addEdge(ConcreteZone.Top.getInstance(), za);
        expected.addEdge(ConcreteZone.Top.getInstance(), zb);
        expected.addEdge(ConcreteZone.Top.getInstance(), zc);

        expected.addEdge(za, zab);
        expected.addEdge(zb, zab);
        expected.addEdge(zb, zbc);
        expected.addEdge(zc, zbc);

        logger.info("expected: " + expected.toString() + " actual: " + getContainmentHeirarchy(d).toString());
        assertTrue(graphEdgeAndVertexEqualty(expected, getContainmentHeirarchy(d)));
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

        assertTrue(graphEdgeAndVertexEqualty(expected, getContainmentHeirarchy(d)));
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
        return actual;
    }

    private static boolean graphEdgeAndVertexEqualty(DirectedGraph<ConcreteZone, DefaultEdge> g1, DirectedGraph<ConcreteZone, DefaultEdge> g2) {
        // g1.edgeSet().equals(g2.esgeSet()) doesn't work as g1 and g2 are multigraphs.  This is the "equality" we require.
        for(DefaultEdge g1e : g1.edgeSet()) {
            boolean thisEdgeInBoth = false;
            ConcreteZone source = g1.getEdgeSource(g1e);
            ConcreteZone target = g1.getEdgeTarget(g1e);

            // I have not found g1e in g2.
            if(! g2.containsEdge(source, target)) {
                return false;
            }
        }

        return g1.vertexSet().equals(g2.vertexSet());
    }
}
