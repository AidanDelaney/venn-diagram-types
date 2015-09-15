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
import java.awt.Graphics2D;
import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;

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

        SVGWriter svgWriter = new SVGWriter("TestUtils::testSplit.svg");
        Graphics2D svgGenerator = svgWriter.getGraphics();

        // We don't do curve1.draw(svgGenerator); as we want to see each arc.
        for(CircleArc2D c: curve1.curves()) {
            c.draw(svgGenerator);
        }

        svgWriter.writeSVG();
    }
    
    @Test
    public void testNonIntersecting () {
        Circle2D c1 = new Circle2D(0, 0, 10);
        Circle2D c2 = new Circle2D(50, 0, 10);

        CircleArc2D ca1 = new CircleArc2D(c1, 0, Math.PI * 2); // a full circle
        CircleArc2D ca2 = new CircleArc2D(c2, 0, Math.PI * 2); // a full circle

        BoundaryPolyCurve2D<CircleArc2D> curve1 = new BoundaryPolyCurve2D<>(), curve2 = new BoundaryPolyCurve2D<>();
        curve1.add(ca1);
        curve2.add(ca2);

        List<BoundaryPolyCurve2D<CircleArc2D>> inBoundaries = Arrays.<BoundaryPolyCurve2D<CircleArc2D>>asList(curve1, curve2)
                                                     , outBoundaries = Arrays.<BoundaryPolyCurve2D<CircleArc2D>>asList();
        BoundaryPolyCurve2D<CircleArc2D> intersection = Utils.intersection(inBoundaries
                                                                     , outBoundaries);
        assertThat(Utils.area(intersection, Arrays.asList()), is(0.0));
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
        assertThat(ixs.size(), is(2));

        BoundaryPolyCurve2D<CircleArc2D> intersection  = Utils.intersection(Arrays.asList(curve1, curve2), Arrays.asList());

        SVGWriter svgWriter = new SVGWriter("TestUtils::testVenn2Next.svg");
        Graphics2D svgGenerator = svgWriter.getGraphics();
        intersection.draw(svgGenerator);

        svgWriter.writeSVG();
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

        Collection<BoundaryPolyCurve2D<CircleArc2D>> inBoundaries = new Vector<>();
        inBoundaries.add(curve1);
        inBoundaries.add(curve2);
        inBoundaries.add(curve3);

        SVGWriter svgWriter = new SVGWriter("TestUtils::testVenn3Next.svg");
        Graphics2D svgGenerator = svgWriter.getGraphics();

        Vector<BoundaryPolyCurve2D<CircleArc2D>> outBoundaries = new Vector<>();
        BoundaryPolyCurve2D<CircleArc2D> arcs = Utils.intersection(inBoundaries, outBoundaries);
        inBoundaries.remove(curve1);
        outBoundaries.add(curve1);
        BoundaryPolyCurve2D<CircleArc2D> arcsC2C3 = Utils.intersection(inBoundaries, outBoundaries);

        svgGenerator.setColor(new Color(255, 255, 0));
        curve1.draw(svgGenerator);
        curve2.draw(svgGenerator);
        curve3.draw(svgGenerator);
        svgGenerator.setColor(new Color(255, 0, 255));
        for(CircleArc2D c : arcs) {
            c.draw(svgGenerator);
        }
        for(CircleArc2D c : arcsC2C3) {
            svgGenerator.setColor(new Color(255, 0, 0));
            c.draw(svgGenerator);
        }
        
        svgWriter.writeSVG();
    }

    @Test
    public void testTunnel() {
        Circle2D c1 = new Circle2D(0, 0, 50);
        Circle2D c2 = new Circle2D(0, 0, 10);

        CircleArc2D ca1 = new CircleArc2D(c1, 0, Math.PI * 2); // a full circle
        CircleArc2D ca2 = new CircleArc2D(c2, 0, Math.PI * 2); // a full circle
        
        BoundaryPolyCurve2D<CircleArc2D> curve1 = new BoundaryPolyCurve2D<>() 
                                         , curve2 = new BoundaryPolyCurve2D<>();
        curve1.add(ca1);
        curve2.add(ca2);

        List<BoundaryPolyCurve2D<CircleArc2D>> inBoundaries = new Vector<>()
                                                     , outBoundaries = new Vector<>();
        inBoundaries.add(curve1);
        outBoundaries.add(curve2);

        BoundaryPolyCurve2D<CircleArc2D> arcs = Utils.intersection(inBoundaries, outBoundaries);

        SVGWriter svgWriter = new SVGWriter("TestUtils::testTunnel.svg");
        Graphics2D svgGenerator = svgWriter.getGraphics();

        curve1.draw(svgGenerator);
        curve2.draw(svgGenerator);

        svgGenerator.setColor(new Color(255, 0, 0));
        arcs.draw(svgGenerator);

        svgWriter.writeSVG();
    }

    @Test
    public void testComplexZone1() {
        Circle2D c1 = new Circle2D(-45, 0, 50);
        Circle2D c2 = new Circle2D(45, 0, 50);
        Circle2D c3 = new Circle2D(5, 0, 10);

        CircleArc2D ca1 = new CircleArc2D(c1, 0, Math.PI * 2); // a full circle
        CircleArc2D ca2 = new CircleArc2D(c2, 0, Math.PI * 2); // a full circle
        CircleArc2D ca3 = new CircleArc2D(c3, 0, Math.PI * 2); // a full circle
        
        BoundaryPolyCurve2D<CircleArc2D> curve1 = new BoundaryPolyCurve2D<>() 
                                         , curve2 = new BoundaryPolyCurve2D<>()
                                         , curve3 = new BoundaryPolyCurve2D<>();
        curve1.add(ca1);
        curve2.add(ca2);
        curve3.add(ca3);

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

        List<BoundaryPolyCurve2D<CircleArc2D>> inBoundaries = new Vector<>()
                                                     , outBoundaries = new Vector<>();
        inBoundaries.add(curve2);
        outBoundaries.add(curve1);
        outBoundaries.add(curve3);

        BoundaryPolyCurve2D<CircleArc2D> arcs = Utils.intersection(inBoundaries, outBoundaries);

        SVGWriter svgWriter = new SVGWriter("TestUtils::testComplexZone1.svg");
        Graphics2D svgGenerator = svgWriter.getGraphics();

        curve1.draw(svgGenerator);
        curve2.draw(svgGenerator);
        curve3.draw(svgGenerator);

        svgGenerator.setColor(new Color(255, 0, 0));
        arcs.draw(svgGenerator);

        svgWriter.writeSVG();
    }

    @Test
    public void testComplexZone2() {
        Circle2D c1 = new Circle2D(-45, 0, 50);
        Circle2D c2 = new Circle2D(45, 0, 50);
        Circle2D c3 = new Circle2D(8, 0, 10);

        CircleArc2D ca1 = new CircleArc2D(c1, 0, Math.PI * 2); // a full circle
        CircleArc2D ca2 = new CircleArc2D(c2, 0, Math.PI * 2); // a full circle
        CircleArc2D ca3 = new CircleArc2D(c3, 0, Math.PI * 2); // a full circle
        
        BoundaryPolyCurve2D<CircleArc2D> curve1 = new BoundaryPolyCurve2D<>() 
                                         , curve2 = new BoundaryPolyCurve2D<>()
                                         , curve3 = new BoundaryPolyCurve2D<>();
        curve1.add(ca1);
        curve2.add(ca2);
        curve3.add(ca3);

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

        List<BoundaryPolyCurve2D<CircleArc2D>> inBoundaries = new Vector<>()
                                                     , outBoundaries = new Vector<>();
        inBoundaries.add(curve2);
        outBoundaries.add(curve1);
        outBoundaries.add(curve3);

        BoundaryPolyCurve2D<CircleArc2D> arcs = Utils.intersection(inBoundaries, outBoundaries);

        SVGWriter svgWriter = new SVGWriter("TestUtils::testComplexZone2.svg");
        Graphics2D svgGenerator = svgWriter.getGraphics();

        curve1.draw(svgGenerator);
        curve2.draw(svgGenerator);
        curve3.draw(svgGenerator);

        svgGenerator.setColor(new Color(255, 0, 0));
        arcs.draw(svgGenerator);

        svgWriter.writeSVG();
        fail();
    }

    @Test
    public void testDisconnected() {
        Circle2D c1 = new Circle2D(-45, 0, 50);
        Circle2D c2 = new Circle2D(45, 0, 50);
        Circle2D c3 = new Circle2D(0, 0, 10);
        
        CircleArc2D ca1 = new CircleArc2D(c1, 0, Math.PI * 2); // a full circle
        CircleArc2D ca2 = new CircleArc2D(c2, 0, Math.PI * 2); // a full circle
        CircleArc2D ca3 = new CircleArc2D(c3, 0, Math.PI * 2); // a full circle
        
        BoundaryPolyCurve2D<CircleArc2D> curve1 = new BoundaryPolyCurve2D<>() 
                                         , curve2 = new BoundaryPolyCurve2D<>()
                                         , curve3 = new BoundaryPolyCurve2D<>();
        curve1.add(ca1);
        curve2.add(ca2);
        curve3.add(ca3);

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

        List<BoundaryPolyCurve2D<CircleArc2D>> inBoundaries = new Vector<>()
                                                     , outBoundaries = new Vector<>();
        inBoundaries.add(curve1);
        inBoundaries.add(curve2);
        outBoundaries.add(curve3);
        BoundaryPolyCurve2D<CircleArc2D> arcs = Utils.intersection(inBoundaries, outBoundaries);

        SVGWriter svgWriter = new SVGWriter("TestUtils::testDisconnected.svg");
        Graphics2D svgGenerator = svgWriter.getGraphics();

        curve1.draw(svgGenerator);
        curve2.draw(svgGenerator);
        curve3.draw(svgGenerator);

        svgGenerator.setColor(new Color(255, 0, 0));
        arcs.draw(svgGenerator);

        svgWriter.writeSVG();
        fail();
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

    @Test
    public void testNonTangentalIntersections () {
        Circle2D c1 = new Circle2D(-45, 0, 50);
        Circle2D c2 = new Circle2D(45, 0, 50);
        
        CircleArc2D ca1 = new CircleArc2D(c1, 0, Math.PI * 2); // a full circle
        CircleArc2D ca2 = new CircleArc2D(c2, 0, Math.PI * 2); // a full circle
        
        BoundaryPolyCurve2D<CircleArc2D> curve1 = new BoundaryPolyCurve2D<>() 
                                         , curve2 = new BoundaryPolyCurve2D<>();
        curve1.add(ca1);
        curve2.add(ca2);

        Optional<Collection<Point2D>> oixs12 = Utils.nonTangentalIntersections(ca1, ca2);
        assertThat(oixs12, is(not(Optional.empty())));
        assertThat(oixs12.get().size(), is(2));

        for(Point2D p : oixs12.get()) {
            curve1 = Utils.split(curve1, p);
            curve2 = Utils.split(curve2, p);
        }

        oixs12 = Utils.nonTangentalIntersections(ca1, ca2);
        assertThat(oixs12, is(not(Optional.empty())));
        assertThat(oixs12.get().size(), is(2));
    }

    @Test
    public void testSimpleUnion () {
        Circle2D c1 = new Circle2D(-45, 0, 50);
        Circle2D c2 = new Circle2D(45, 0, 50);
        
        CircleArc2D ca1 = new CircleArc2D(c1, 0, Math.PI * 2); // a full circle
        CircleArc2D ca2 = new CircleArc2D(c2, 0, Math.PI * 2); // a full circle
        
        BoundaryPolyCurve2D<CircleArc2D> curve1 = new BoundaryPolyCurve2D<>() 
                                         , curve2 = new BoundaryPolyCurve2D<>();
        curve1.add(ca1);
        curve2.add(ca2);

        Optional<Collection<Point2D>> oixs12 = ca1.intersections(ca2);

        for(Point2D p : oixs12.get()) {
            curve1 = Utils.split(curve1, p);
            curve2 = Utils.split(curve2, p);
        }

        BoundaryPolyCurve2D<CircleArc2D> union = Utils.union(curve1, curve2);
        SVGWriter svgWriter = new SVGWriter("TestUtils::testSimpleUnion.svg");
        Graphics2D svgGenerator = svgWriter.getGraphics();

        curve1.draw(svgGenerator);
        curve2.draw(svgGenerator);

        svgGenerator.setColor(new Color(255, 0, 0));
        union.draw(svgGenerator);

        svgWriter.writeSVG();
        fail();
    }

    @Test
    public void testFromCollection () {
        Circle2D c1 = new Circle2D(-45, 0, 50);
        Circle2D c2 = new Circle2D(45, 0, 50);
        
        CircleArc2D ca1 = new CircleArc2D(c1, 0, Math.PI * 2); // a full circle
        CircleArc2D ca2 = new CircleArc2D(c2, 0, Math.PI * 2); // a full circle
        
        BoundaryPolyCurve2D<CircleArc2D> curve1 = new BoundaryPolyCurve2D<>() 
                                         , curve2 = new BoundaryPolyCurve2D<>();
        curve1.add(ca1);
        curve2.add(ca2);

        Optional<Collection<Point2D>> oixs12 = ca1.intersections(ca2);

        for(Point2D p : oixs12.get()) {
            curve1 = Utils.split(curve1, p);
            curve2 = Utils.split(curve2, p);
        }

        BoundaryPolyCurve2D<CircleArc2D> ordered = Utils.fromCollection(curve1.curves(), Optional.of(curve1.firstCurve()));

        for(int i = 0; i<curve1.size(); i++) {
            assertThat("Difference at curve " + i, ordered.get(i), is(equalTo(curve1.get(i))));
        }
        fail();
    }

    @Test
    public void testExclusion() {
        Circle2D c1 = new Circle2D(-45, 0, 50);
        Circle2D c2 = new Circle2D(45, 0, 50);
        Circle2D c3 = new Circle2D(8, 0, 10);

        CircleArc2D ca1 = new CircleArc2D(c1, 0, Math.PI * 2); // a full circle
        CircleArc2D ca2 = new CircleArc2D(c2, 0, Math.PI * 2); // a full circle
        CircleArc2D ca3 = new CircleArc2D(c3, 0, Math.PI * 2); // a full circle
        
        BoundaryPolyCurve2D<CircleArc2D> curve1 = new BoundaryPolyCurve2D<>() 
                                         , curve2 = new BoundaryPolyCurve2D<>()
                                         , curve3 = new BoundaryPolyCurve2D<>();
        curve1.add(ca1);
        curve2.add(ca2);
        curve3.add(ca3);

        Optional<Collection<Point2D>> oixs12 = ca1.intersections(ca2);
        Optional<Collection<Point2D>> oixs13 = ca1.intersections(ca3);

        for(Point2D p : oixs12.get()) {
            curve1 = Utils.split(curve1, p);
            curve2 = Utils.split(curve2, p);
        }
        for(Point2D p : oixs13.get()) {
            curve1 = Utils.split(curve1, p);
            curve3 = Utils.split(curve3, p);
        }

        List<BoundaryPolyCurve2D<CircleArc2D>> inBoundaries = new Vector<>()
                                                     , outBoundaries = new Vector<>();
        inBoundaries.add(curve2);
        outBoundaries.add(curve1);
        outBoundaries.add(curve3);

        BoundaryPolyCurve2D<CircleArc2D> arcs = Utils.exclusion(curve2, outBoundaries);

        SVGWriter svgWriter = new SVGWriter("TestUtils::testExclusion.svg");
        Graphics2D svgGenerator = svgWriter.getGraphics();

        curve1.draw(svgGenerator);
        curve2.draw(svgGenerator);
        curve3.draw(svgGenerator);

        svgGenerator.setColor(new Color(255, 0, 0));
        arcs.draw(svgGenerator);

        svgWriter.writeSVG();
        fail();
    }
}
