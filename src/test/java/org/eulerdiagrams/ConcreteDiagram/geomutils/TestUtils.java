package org.eulerdiagrams.ConcreteDiagram.geomutils;

import math.geom2d.Point2D;
import math.geom2d.Shape2D;
import math.geom2d.conic.Circle2D;
import math.geom2d.conic.CircleArc2D;
import math.geom2d.domain.BoundaryPolyCurve2D;
import org.junit.Test;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;

import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;

import java.awt.Color;
import java.io.*;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class TestUtils {
    @Test
    public void testNonIntersection() {
        Circle2D c = new Circle2D(0, 0, 10);

        // Two non overlapping arcs on the same circle (for convienece).
        CircleArc2D a = new CircleArc2D(c, Math.PI/2, Math.PI/4);
        CircleArc2D b = new CircleArc2D(c, Math.PI, Math.PI/4);

        BoundaryPolyCurve2D<CircleArc2D> pc1 = new BoundaryPolyCurve2D<>();
        pc1.add(a);

        BoundaryPolyCurve2D<CircleArc2D> pc2 = new BoundaryPolyCurve2D<>();
        pc2.add(b);

        assertThat(Utils.intersections(pc1, pc2), is(Optional.empty()));
    }

    @Test
    public void testSplit () {
        Circle2D c1 = new Circle2D(0, 0, 10);
        Circle2D c2 = new Circle2D(5, 0, 10);

        CircleArc2D ca1 = new CircleArc2D(c1, 0, Math.PI); // a semi-circle
        CircleArc2D ca2 = new CircleArc2D(c2, 0, Math.PI); // a semi-circle

        BoundaryPolyCurve2D<CircleArc2D> curve1 = new BoundaryPolyCurve2D<>(), curve2 = new BoundaryPolyCurve2D<>();
        curve1.add(ca1);
        curve2.add(ca2);

        assertThat(curve1.curves().size(), is(1));

        Optional<Collection<Point2D>> ixs = ca1.intersections(ca2);

        assertThat(ixs, is(not(Optional.empty())));
        assertThat(ixs.get().size(), is(1));

        Point2D ipoint = ixs.get().toArray(new Point2D[0])[0]; // first intersection point

        curve1 = Utils.split(curve1, ipoint);
        curve2 = Utils.split(curve2, ipoint);
        assertThat(curve1.curves().size(), is(2));
        assertThat(curve1.firstCurve().lastPoint().almostEquals(curve1.lastCurve().firstPoint(), Shape2D.ACCURACY), is(true));

        assertThat(curve2.curves().size(), is(2));
        assertThat(curve2.firstCurve().lastPoint().almostEquals(curve2.lastCurve().firstPoint(), Shape2D.ACCURACY), is(true));

        // Get a DOMImplementation
        DOMImplementation domImpl =
            GenericDOMImplementation.getDOMImplementation();
        String svgNamespaceURI = "http://www.w3.org/2000/svg";

        // Create an instance of org.w3c.dom.Document
        Document document = domImpl.createDocument(svgNamespaceURI, "svg", null);

        // Create an instance of the SVG Generator
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        // We don't do curve1.draw(svgGenerator); as we want to see each arc.
        for(CircleArc2D c: curve1.curves()) {
            c.draw(svgGenerator);
        }

        
        try {
            String filename = "TestUtils::testSplit.svg";
            File f = new File(filename);
            svgGenerator.stream(filename);
        } catch (Exception e) {
            // Do nothing
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testVenn2Next () {
        Circle2D c1 = new Circle2D(0, 0, 100);
        Circle2D c2 = new Circle2D(50, 0, 100);

        CircleArc2D ca1 = new CircleArc2D(c1, 0, Math.PI * 2); // a full circle
        CircleArc2D ca2 = new CircleArc2D(c2, 0, Math.PI * 2); // a full circle

        
        BoundaryPolyCurve2D<CircleArc2D> curve1 = new BoundaryPolyCurve2D<>(), curve2 = new BoundaryPolyCurve2D<>();
        curve1.add(ca1);
        curve2.add(ca2);

        assertThat(curve1.contains(ca1), is(true));

        Optional<Collection<Point2D>> oixs = ca1.intersections(ca2);

        for(Point2D p : oixs.get()) {
            curve1 = Utils.split(curve1, p);
            curve2 = Utils.split(curve2, p);
        }
        assertThat(curve1.curves().size(), is(3));
        assertThat(curve2.curves().size(), is(3));

        Point2D ipoint = oixs.get().toArray(new Point2D[0])[0]; // first intersection point

        // Find arc on curve 1 that contains ipoint
        Optional<CircleArc2D> oarc = Utils.findArcContaining(curve1, ipoint);
        assertThat(oarc, is(not(Optional.empty())));
        CircleArc2D arc = oarc.get();

        Collection<Point2D> ixs = oixs.get();

        BoundaryPolyCurve2D<CircleArc2D> visited = new BoundaryPolyCurve2D<CircleArc2D>();
        Optional<CircleArc2D> a  = Utils.next(curve1, curve2, visited, ipoint);
        assertThat(a, is(not(Optional.empty())));

        visited.add(a.get());

        a = Utils.next(curve1, curve2, visited, a.get().lastPoint());
        assertThat(a, is(not(Optional.empty())));

        visited.add(a.get());

        a = Utils.next(curve1, curve2, visited, a.get().lastPoint());
        assertThat(a, is(not(Optional.empty())));
        visited.add(a.get());

        a = Utils.next(curve1, curve2, visited, a.get().lastPoint());
        assertThat(a, is(Optional.empty()));

        // Get a DOMImplementation
        DOMImplementation domImpl =
            GenericDOMImplementation.getDOMImplementation();
        String svgNamespaceURI = "http://www.w3.org/2000/svg";

        // Create an instance of org.w3c.dom.Document
        Document document = domImpl.createDocument(svgNamespaceURI, "svg", null);

        // Create an instance of the SVG Generator
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        visited.draw(svgGenerator);

        
        try {
            String filename = "TestUtils::testVenn2Next.svg";
            File f = new File(filename);
            svgGenerator.stream(filename);
        } catch (Exception e) {
            // Do nothing
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testVenn3Next () {
        Circle2D c1 = new Circle2D(0, 0, 100);
        Circle2D c2 = new Circle2D(50, 0, 100);
        Circle2D c3 = new Circle2D(0, 50, 100);

        CircleArc2D ca1 = new CircleArc2D(c1, 0, Math.PI * 2); // a full circle
        CircleArc2D ca2 = new CircleArc2D(c2, 0, Math.PI * 2); // a full circle
        CircleArc2D ca3 = new CircleArc2D(c3, 0, Math.PI * 2); // a full circle

        
        BoundaryPolyCurve2D<CircleArc2D> curve1 = new BoundaryPolyCurve2D<>()
                , curve2 = new BoundaryPolyCurve2D<>()
                , curve3 = new BoundaryPolyCurve2D<>();
        curve1.add(ca1);
        curve2.add(ca2);
        curve3.add(ca3);

        assertThat(curve1.contains(ca1), is(true));

        Optional<Collection<Point2D>> oixs12 = ca1.intersections(ca2);
        Optional<Collection<Point2D>> oixs13 = ca1.intersections(ca3);
        Optional<Collection<Point2D>> oixs23 = ca2.intersections(ca3);

        for(Point2D p : oixs12.get()) {
            curve1 = Utils.split(curve1, p);
            curve2 = Utils.split(curve2, p);
        }
        for(Point2D p : oixs13.get()) {
            curve1 = Utils.split(curve1, p);
            curve3 = Utils.split(curve3, p);
        }
        for(Point2D p : oixs23.get()) {
            curve2 = Utils.split(curve2, p);
            curve3 = Utils.split(curve3, p);
        }
        assertThat(curve1.curves().size(), is(5));
        assertThat(curve2.curves().size(), is(5));
        assertThat(curve3.curves().size(), is(5));

        Point2D ipoint = oixs12.get().toArray(new Point2D[0])[0];

        // Find arc on curve 1 that contains ipoint
        Optional<CircleArc2D> oarc = Utils.findArcContaining(curve1, ipoint);
        assertThat(oarc, is(not(Optional.empty())));
        CircleArc2D arc = oarc.get();

        Collection<BoundaryPolyCurve2D<CircleArc2D>> boundaries = new Vector<>();
        boundaries.add(curve1);
        boundaries.add(curve2);
        boundaries.add(curve3);

        BoundaryPolyCurve2D<CircleArc2D> visited = new BoundaryPolyCurve2D<CircleArc2D>();
        Optional<CircleArc2D> a = Utils.next(boundaries, visited, ipoint);
        while(a.isPresent()) {
            visited.add(a.get());
            a = Utils.next(boundaries, visited, ipoint);
        }

        // Get a DOMImplementation
        DOMImplementation domImpl =
            GenericDOMImplementation.getDOMImplementation();
        String svgNamespaceURI = "http://www.w3.org/2000/svg";

        // Create an instance of org.w3c.dom.Document
        Document document = domImpl.createDocument(svgNamespaceURI, "svg", null);

        // Create an instance of the SVG Generator
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        
        Vector<BoundaryPolyCurve2D<CircleArc2D>> outBoundaries = new Vector<>();
        //BoundaryPolyCurve2D<CircleArc2D> arcs = Utils.intersection(boundaries, outBoundaries);
        boundaries.remove(curve1);
        outBoundaries.add(curve1);
        BoundaryPolyCurve2D<CircleArc2D> arcsC2C3 = Utils.intersection(boundaries, outBoundaries);

        svgGenerator.setColor(new Color(255, 255, 0));
        curve1.draw(svgGenerator);
        curve2.draw(svgGenerator);
        curve3.draw(svgGenerator);
        //for(CircleArc2D c : arcs) {
            //c.draw(svgGenerator);
        //}
        for(CircleArc2D c : arcsC2C3) {
            svgGenerator.setColor(new Color(255, 0, 0));
            c.draw(svgGenerator);
        }

        
        try {
            String filename = "TestUtils::testVenn3Next.svg";
            File f = new File(filename);
            svgGenerator.stream(filename);
        } catch (Exception e) {
            // Do nothing
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testDirectionlessFind () {
        Circle2D c1 = new Circle2D(0, 0, 10);
        Circle2D c2 = new Circle2D(5, 0, 10);

        CircleArc2D ca1 = new CircleArc2D(c1, 0, Math.PI * 2); // a full circle
        CircleArc2D ca2 = new CircleArc2D(c2, 0, Math.PI * 2); // a full circle

        BoundaryPolyCurve2D<CircleArc2D> curve1 = new BoundaryPolyCurve2D<>(), curve2 = new BoundaryPolyCurve2D<>();
        curve1.add(ca1);
        curve2.add(ca2);

        assertThat(curve1.contains(ca1), is(true));

        Optional<Collection<Point2D>> oixs = ca1.intersections(ca2);

        for(Point2D p : oixs.get()) {
            curve1 = Utils.split(curve1, p);
            curve2 = Utils.split(curve2, p);
        }
        assertThat(curve1.curves().size(), is(3));
        assertThat(curve2.curves().size(), is(3));

        Point2D ipoint = oixs.get().toArray(new Point2D[0])[0]; // first intersection point

        Optional<CircleArc2D> oc = Utils.directionlessFind(curve1, new Vector<CircleArc2D>(), ipoint);

        assertThat(oc.isPresent(), is(true));
    }
}
