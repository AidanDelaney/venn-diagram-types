package org.eulerdiagrams.ConcreteDiagram;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

import org.eulerdiagrams.AbstractDiagram.AbstractContour;
import org.eulerdiagrams.AbstractDiagram.AbstractDiagram;
import org.eulerdiagrams.AbstractDiagram.AbstractZone;
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
        ConcreteCircle ca = new ConcreteCircle(a, circleA);
        AbstractDiagram ad = new AbstractDiagram(new HashSet<>(Arrays.asList(a)));
        ConcreteDiagram d = new ConcreteDiagram(ad, Arrays.asList(ca));
        Map<AbstractZone, Double> areas = d.getZoneAreaMap();

        // For simple diagrams, this is easier than picking out a specific zone
        for(AbstractZone z: areas.keySet()) {
            assertThat(areas.get(z), isOneOf(Double.POSITIVE_INFINITY, Math.PI * 49.0));
        }
    }
}
