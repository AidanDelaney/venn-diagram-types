package org.eulerdiagrams.ConcreteDiagram;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

import org.eulerdiagrams.AbstractDiagram.AbstractContour;
import org.eulerdiagrams.AbstractDiagram.AbstractDiagram;
import org.eulerdiagrams.AbstractDiagram.AbstractZone;
import org.eulerdiagrams.utils.NAryTree;
import org.eulerdiagrams.vennom.graph.Graph;
import org.eulerdiagrams.vennom.graph.Node;
import org.eulerdiagrams.vennom.graph.Edge;
import math.geom2d.Point2D;
import math.geom2d.Shape2D;
import math.geom2d.conic.Circle2D;

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
    public void testVenn2Graph() {
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
        cd.getZoneAreaMap();
    }

    @Test
    public void testSingleCircle() {
        Circle2D circleA = new Circle2D(new Point2D(-5, 0), 7.0);
        ConcreteCircle ca = new ConcreteCircle(a, circleA, Arrays.asList());
        AbstractDiagram ad = new AbstractDiagram(new HashSet<>(Arrays.asList(a)));
        ConcreteDiagram d = new ConcreteDiagram(ad, Arrays.asList(ca));
        Map<AbstractZone, Double> areas = d.getZoneAreaMap();

        // For simple diagrams, this is easier than picking out a specific zone
        for(AbstractZone z: areas.keySet()) {
            assertThat(areas.get(z), isOneOf(Double.POSITIVE_INFINITY, Math.PI * 49.0));
        }
    }

    @Test
    public void testVenn2Circles() {
        Circle2D circleA = new Circle2D(new Point2D(-5, 0), 7.0);
        Circle2D circleB = new Circle2D(new Point2D(5, 0), 7.0);
        ConcreteCircle ca = new ConcreteCircle(a, circleA, Arrays.asList(circleB));
        ConcreteCircle cb = new ConcreteCircle(b, circleB, Arrays.asList(circleA));

        AbstractDiagram ad = new AbstractDiagram(new HashSet<>(Arrays.asList(a, b)));
        ConcreteDiagram d = new ConcreteDiagram(ad, Arrays.asList(ca, cb));
        Map<AbstractZone, Double> areas = d.getZoneAreaMap();

        Set<AbstractContour> inset = new HashSet<>(Arrays.asList(a,b));
        AbstractZone zab = new AbstractZone(inset, new HashSet<>());
        Double intersection = areas.get(zab);
        assertThat(intersection, closeTo(27.0, 0.5));

        // 49 is 7^2
        double expected = (Math.PI * 49.0) - intersection;
        AbstractZone za = new AbstractZone(new HashSet<>(Arrays.asList(a)),
                                          new HashSet<>(Arrays.asList(b)));
        assertThat(areas.get(za), closeTo(expected, 0.5));
    }

    @Test
    public void testDisconnected2Circles() {
        Circle2D circleA = new Circle2D(new Point2D(-10, 0), 7.0);
        Circle2D circleB = new Circle2D(new Point2D(10, 0), 7.0);
        ConcreteCircle ca = new ConcreteCircle(a, circleA, Arrays.asList(circleB));
        ConcreteCircle cb = new ConcreteCircle(b, circleB, Arrays.asList(circleA));

        AbstractDiagram ad = new AbstractDiagram(new HashSet<>(Arrays.asList(a, b)));
        ConcreteDiagram d = new ConcreteDiagram(ad, Arrays.asList(ca, cb));
        Map<AbstractZone, Double> map = d.getZoneAreaMap();

        AbstractZone az = new AbstractZone(new HashSet<>(Arrays.asList(a)), new HashSet<>(Arrays.asList(b)));
        System.out.println(map.get(az));
        for(AbstractZone z : map.keySet()) {
            System.out.println(z.toString() + " " + map.get(z));
            if(z.equals(az)) {
                assertThat(153.93791, is(closeTo(map.get(z), 0.001)));
            }
        }
    }
/*
    @Test
    public void testVenn2Contained() {
        Vector<ConcreteCircle> circles = new Vector();
        ConcreteCircle ca = new ConcreteCircle(a, new Point2D(-5, 0), 12.0);
        ConcreteCircle cb = new ConcreteCircle(b, new Point2D(5, 0), 12.0);
        ConcreteCircle cc = new ConcreteCircle(c, new Point2D(0, 0), 100.0);
        circles.addAll(Arrays.asList(ca, cb, cc));

        ConcreteDiagram d = new ConcreteDiagram(circles);

        logger.info(getZones(d).toString());

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
*/

    private static NAryTree<Cluster> getContainmentHeirarchy(ConcreteDiagram d) {
        // Now,  break the access protection on the internal containment
        // hierarchy graph, and check that the graph is correct.
        NAryTree<Cluster> actual = null;
        try {
            Field field = ConcreteDiagram.class.getDeclaredField("containment");
            field.setAccessible(true);
            actual = (NAryTree<Cluster>) field.get(d);
        } catch (NoSuchFieldException nsfe) {
            fail();
        } catch (IllegalAccessException iae) {
            fail();
        }
        return actual;
    }
}
