package org.eulerdiagrams.ConcreteDiagram;

import static org.junit.Assert.*;

import java.util.*;

import org.eulerdiagrams.AbstractDiagram.AbstractContour;
import org.eulerdiagrams.AbstractDiagram.AbstractDiagram;
import org.eulerdiagrams.AbstractDiagram.AbstractZone;
import org.eulerdiagrams.ConcreteDiagram.geomutils.ConcreteZoneIterator;
import org.eulerdiagrams.ConcreteDiagram.geomutils.SplitArcBoundary;
import org.eulerdiagrams.utils.Pair;
import org.eulerdiagrams.vennom.graph.Graph;
import org.eulerdiagrams.vennom.graph.Node;
import org.eulerdiagrams.vennom.graph.Edge;
import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;

import org.junit.Before;
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
    public void testVenn2() {
        Circle2D circleA = new Circle2D(new Point2D(-5, 0), 7.0);
        Circle2D circleB = new Circle2D(new Point2D(5, 0), 7.0);
        ConcreteCircle ca = new ConcreteCircle(a, circleA);
        ConcreteCircle cb = new ConcreteCircle(b, circleB);
        AbstractDiagram ad = new AbstractDiagram(new HashSet<>(Arrays.asList(a, b)));
        ConcreteDiagram d = new ConcreteDiagram(ad, Arrays.asList(ca, cb));

        Map<AbstractZone, Double> areas = d.getZoneAreaMap();

        double areaA = 0, areaB = 0, areaAB = 0;

        for(AbstractZone z: areas.keySet()) {
            assertThat(areas.get(z), isOneOf(areaA, areaB, areaAB));
        }
    }

    @Test
    public void testSingleCircle() {
        Circle2D circleA = new Circle2D(new Point2D(-5, 0), 7.0);
        ConcreteCircle ca = new ConcreteCircle(a, circleA);
        AbstractDiagram ad = new AbstractDiagram(new HashSet<>(Arrays.asList(a)));
        ConcreteDiagram d = new ConcreteDiagram(ad, Arrays.asList(ca));
        Map<AbstractZone, Double> areas = d.getZoneAreaMap();

        // For simple diagrams, this is easier than picking out a specific zone
        for(AbstractZone z: areas.keySet()) {
            assertThat(areas.get(z), is(Math.PI * 49.0));
        }
    }

    @Test
    public void testSimpleTunnel() {
        Circle2D circleA = new Circle2D(new Point2D(-5, 0), 7.0);
        Circle2D circleB = new Circle2D(new Point2D(-5, 0), 2.0);
        ConcreteCircle ca = new ConcreteCircle(a, circleA);
        ConcreteCircle cb = new ConcreteCircle(b, circleB);
        AbstractDiagram ad = new AbstractDiagram(new HashSet<>(Arrays.asList(a, b)));
        ConcreteDiagram d = new ConcreteDiagram(ad, Arrays.asList(ca, cb));
        Map<AbstractZone, Double> areas = d.getZoneAreaMap();

        ConcreteZoneIterator czvsi = new ConcreteZoneIterator(Arrays.asList(ca, cb));
        assertThat(czvsi.hasNext(), is(true));
        Pair<AbstractZone, Optional<SplitArcBoundary>> pzo = czvsi.next();
        assertThat(czvsi.hasNext(), is(true));
        pzo = czvsi.next();
        assertThat(czvsi.hasNext(), is(false));

        double areaB = 2.0 * 2.0 * Math.PI;
        double areaA = (7.0 * 7.0 * Math.PI) - areaB;
        for(AbstractZone z: areas.keySet()) {
            assertThat(areas.get(z), isOneOf(areaA, areaB));
        }
    }

    @Test
    public void testComplexTunnel() {
        Circle2D circleA = new Circle2D(new Point2D(-5, 0), 10.0);
        Circle2D circleB = new Circle2D(new Point2D(-5, 0), 7.0);
        Circle2D circleC = new Circle2D(new Point2D(-5, 0), 2.0);
        ConcreteCircle ca = new ConcreteCircle(a, circleA);
        ConcreteCircle cb = new ConcreteCircle(b, circleB);
        ConcreteCircle cc = new ConcreteCircle(c, circleC);
        AbstractDiagram ad = new AbstractDiagram(new HashSet<>(Arrays.asList(a, b, c)));
        ConcreteDiagram d = new ConcreteDiagram(ad, Arrays.asList(ca, cb, cc));
        Map<AbstractZone, Double> areas = d.getZoneAreaMap();

        double areaC = 2.0 * 2.0 * Math.PI;
        double areaB = (7.0 * 7.0 * Math.PI) - areaC;
        double areaA = (10.0 * 10.0 * Math.PI) - areaB;
        for(AbstractZone z: areas.keySet()) {
            assertThat(areas.get(z), isOneOf(areaA, areaB, areaC));
        }
    }

    @Test
    public void testSplitZone() {
        Circle2D circleA = new Circle2D(new Point2D(-2, 0), 4.0);
        Circle2D circleB = new Circle2D(new Point2D(2, 0), 4.0);
        Circle2D circleC = new Circle2D(new Point2D(0, 0), 2.0);
        ConcreteCircle ca = new ConcreteCircle(a, circleA);
        ConcreteCircle cb = new ConcreteCircle(b, circleB);
        ConcreteCircle cc = new ConcreteCircle(c, circleC);
        AbstractDiagram ad = new AbstractDiagram(new HashSet<>(Arrays.asList(a, b, c)));
        ConcreteDiagram d = new ConcreteDiagram(ad, Arrays.asList(ca, cb, cc));
        Map<AbstractZone, Double> areas = d.getZoneAreaMap();

        // There should be 5 zones as cc splits the intersection of ca and cb.
        assertThat(areas.keySet().size(), is(5));
    }

    /**
     * This example is the vennom output on a specific instance of Venn 4.
     */
    @Test
    public void testVennomOutput_001() {
        AbstractContour a = new AbstractContour("A");
        AbstractContour b = new AbstractContour("B");
        AbstractContour c = new AbstractContour("C");
        AbstractContour d = new AbstractContour("D");
        ConcreteCircle c1 = new ConcreteCircle(a, new Circle2D(227.0044, 246.0087, 9.690274));
        ConcreteCircle c2 = new ConcreteCircle(b, new Circle2D(233.7481, 259.2041, 9.690274));
        ConcreteCircle c3 = new ConcreteCircle(c, new Circle2D(235.7133, 249.7398, 10.01337));
        ConcreteCircle c4 = new ConcreteCircle(d, new Circle2D(233.5342, 256.0473, 9.690274));

        AbstractDiagram ad = new AbstractDiagram(new HashSet(){{add(a);add(b);add(c);add(d);}});

        ConcreteDiagram cd = new ConcreteDiagram(ad, Arrays.asList(c1, c2, c3, c4));
        Map<AbstractZone, Double> m = cd.getZoneAreaMap();

        assertThat(m.values(), everyItem(greaterThanOrEqualTo(0.0)));
    }

    @Test
    public void testCrossedVenn2() {
        AbstractContour a = new AbstractContour("A");
        AbstractContour b = new AbstractContour("B");
        AbstractContour c = new AbstractContour("C");
        AbstractContour d = new AbstractContour("D");
        ConcreteCircle c1 = new ConcreteCircle(a, new Circle2D(0, -10, 12));
        ConcreteCircle c2 = new ConcreteCircle(b, new Circle2D(0, 10, 12));
        ConcreteCircle c3 = new ConcreteCircle(c, new Circle2D(-10, 0, 12));
        ConcreteCircle c4 = new ConcreteCircle(d, new Circle2D(10, 0, 12));

        AbstractDiagram ad = new AbstractDiagram(new HashSet(){{add(a);add(b);add(c);add(d);}});

        ConcreteDiagram cd = new ConcreteDiagram(ad, Arrays.asList(c1, c2, c3, c4));
        Map<AbstractZone, Double> m = cd.getZoneAreaMap();

        assertThat(m.values(), everyItem(greaterThanOrEqualTo(0.0)));
    }
}
