package org.eulerdiagrams.ConcreteDiagram.geomutils;

import math.geom2d.Point2D;
import math.geom2d.Shape2D;
import math.geom2d.conic.Circle2D;
import math.geom2d.conic.CircleArc2D;
import math.geom2d.domain.BoundaryPolyCurve2D;
import org.eulerdiagrams.AbstractDiagram.AbstractContour;
import org.eulerdiagrams.AbstractDiagram.AbstractDiagram;
import org.eulerdiagrams.AbstractDiagram.AbstractZone;
import org.eulerdiagrams.ConcreteDiagram.ConcreteCircle;
import org.eulerdiagrams.ConcreteDiagram.ConcreteDiagram;
import org.junit.Test;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;

import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class TestUtils {
    
    public class SVGWriter {
        private SVGGraphics2D svgGenerator = null;
        private String filename;
        public SVGWriter(String filename) {
            this.filename = filename;
            // Get a DOMImplementation
            DOMImplementation domImpl =
                GenericDOMImplementation.getDOMImplementation();
            String svgNamespaceURI = "http://www.w3.org/2000/svg";

            // Create an instance of org.w3c.dom.Document
            Document document = domImpl.createDocument(svgNamespaceURI, "svg", null);

            // Create an instance of the SVG Generator
             svgGenerator = new SVGGraphics2D(document);
        }

        public void writeSVG() {
            try {
                svgGenerator.stream(filename);
            } catch (Exception e) {
                // Do nothing
                e.printStackTrace();
                fail();
            }
        }

        public Graphics2D getGraphics() {
            return svgGenerator;
        }
    }

    @Test
    public void testMidpoint() {
        AbstractContour a = new AbstractContour("A");
        AbstractContour b = new AbstractContour("B");
        Circle2D circleA = new Circle2D(new Point2D(-5, 0), 7.0);
        Circle2D circleB = new Circle2D(new Point2D(5, 0), 7.0);
        ConcreteCircle ca = new ConcreteCircle(a, circleA);
        ConcreteCircle cb = new ConcreteCircle(b, circleB);
        AbstractDiagram ad = new AbstractDiagram(new HashSet<>(Arrays.asList(a, b)));
        ConcreteDiagram d = new ConcreteDiagram(ad, Arrays.asList(ca, cb));

        try{
            Field f = d.getClass().getDeclaredField("zoneMap");
            f.setAccessible(true);

            Map<AbstractZone, Collection<SplitArcBoundary>> map = (Map<AbstractZone, Collection<SplitArcBoundary>>) f.get(d);

            for(SplitArcBoundary s: map.values().stream().flatMap(c -> c.stream()).collect(Collectors.toSet())) {
                assertThat(s.midpoints().stream().map(x -> x.getY()).collect(Collectors.toSet()), everyItem(closeTo(0.0, 0.001)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSimpleMidpoint() {
        Circle2D c = new Circle2D(new Point2D(0, -5), 5);
        CircleArc2D arc = new CircleArc2D(c, 0, Math.PI);

        Point2D mid = Utils.midpoint(arc);

        assertThat(arc.point(0), is(arc.firstPoint()));
        assertThat(arc.point(1), is(arc.lastPoint()));

        assertThat(mid.getX(), is(0));
        assertThat(mid.getY(), is(0));
    }
}
