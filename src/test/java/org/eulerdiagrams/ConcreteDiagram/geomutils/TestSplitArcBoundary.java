package org.eulerdiagrams.ConcreteDiagram.geomutils;

import math.geom2d.conic.Circle2D;
import math.geom2d.conic.CircleArc2D;

import org.eulerdiagrams.ConcreteDiagram.Cluster;
import org.eulerdiagrams.ConcreteDiagram.ConcreteDiagram;
import org.eulerdiagrams.ConcreteDiagram.geomutils.TestUtils.SVGWriter;
import org.eulerdiagrams.utils.NAryTree;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.Matchers.*;

public class TestSplitArcBoundary {

    @Test
    public void testSplitBoundary() {
        Collection<SplitArcBoundary> actual = SplitArcBoundary.splitBoundaries(Arrays.asList(new Circle2D(0, 0, 10)), Arrays.asList());

        assertThat(actual.size(), is(1));
    }

    @Test
    public void testIntersectionContained() {
        TestUtils tu = new TestUtils();
        TestUtils.SVGWriter svgWriter = tu.new SVGWriter("TestSplitArcBoundary::testIntersectionContained.svg");
        Graphics2D svgGenerator = svgWriter.getGraphics();

        Circle2D a = new Circle2D(0, 0, 40);
        Circle2D b = new Circle2D(0, 0, 10);

        SplitArcBoundary r_a = new SplitArcBoundary(Arrays.asList(a), Arrays.asList(b));
        SplitArcBoundary r_b = new SplitArcBoundary(Arrays.asList(b), Arrays.asList(a));
        SplitArcBoundary r_ab = new SplitArcBoundary(Arrays.asList(a,b), Arrays.asList());
        r_a.draw(svgGenerator);
        r_b.draw(svgGenerator);
        r_ab.draw(svgGenerator);

        assertThat(r_b, is(equalTo(r_ab)));
        assertThat(r_a, is(not(equalTo(r_b))));
        assertThat(r_a.bounds(r_b), is(true));

        svgWriter.writeSVG();
    }

    @Test
    public void testLessContained() {
        TestUtils tu = new TestUtils();
        TestUtils.SVGWriter svgWriter = tu.new SVGWriter("TestSplitArcBoundary::testLessContained.svg");
        Graphics2D svgGenerator = svgWriter.getGraphics();

        Circle2D a = new Circle2D(0, 0, 40);
        Circle2D b = new Circle2D(0, 0, 10);

        SplitArcBoundary r_a = new SplitArcBoundary(Arrays.asList(a), Arrays.asList(b));
        SplitArcBoundary r_b = new SplitArcBoundary(Arrays.asList(b), Arrays.asList(a));
        SplitArcBoundary r_ab = r_b.less(a).get();
        r_a.draw(svgGenerator);
        r_b.draw(svgGenerator);
        r_ab.draw(svgGenerator);

        assertThat(r_b, is(equalTo(r_ab)));
        assertThat(r_a, is(not(equalTo(r_b))));
        assertThat(r_a.bounds(r_b), is(true));

        svgWriter.writeSVG();
    }

    @Test
    public void testIntersectionVenn2() {
        TestUtils tu = new TestUtils();
        TestUtils.SVGWriter svgWriter = tu.new SVGWriter("TestSplitArcBoundary::testIntersectionVenn2.svg");
        Graphics2D svgGenerator = svgWriter.getGraphics();

        Circle2D a = new Circle2D(-20, 0, 40);
        Circle2D b = new Circle2D(20, 0, 40);

        SplitArcBoundary r_a = new SplitArcBoundary(Arrays.asList(a), Arrays.asList(b));
        SplitArcBoundary r_b = new SplitArcBoundary(Arrays.asList(b), Arrays.asList(a));
        SplitArcBoundary r_ab = new SplitArcBoundary(Arrays.asList(a,b), Arrays.asList());
        //r_a.draw(svgGenerator);
        r_b.draw(svgGenerator);
        r_ab.draw(svgGenerator);

        assertThat(r_a.getArea() + r_ab.getArea(), is(closeTo(40 * 40 * Math.PI, 0.1)));

        svgWriter.writeSVG();
    }


    @Test
    public void testLessVenn2() {
        TestUtils tu = new TestUtils();
        TestUtils.SVGWriter svgWriter = tu.new SVGWriter("TestSplitArcBoundary::testLessVenn2.svg");
        Graphics2D svgGenerator = svgWriter.getGraphics();

        Circle2D a = new Circle2D(-20, 0, 40);
        Circle2D b = new Circle2D(20, 0, 40);

        SplitArcBoundary r_a = new SplitArcBoundary(Arrays.asList(a), Arrays.asList());
        SplitArcBoundary r_b = new SplitArcBoundary(Arrays.asList(b), Arrays.asList());
        SplitArcBoundary z_a = r_a.less(b).get();
        SplitArcBoundary z_b = r_b.less(a).get();
        SplitArcBoundary z_ab = new SplitArcBoundary(Arrays.asList(a,b), Arrays.asList());
        //r_a.draw(svgGenerator);
        //r_b.draw(svgGenerator);
        z_a.draw(svgGenerator);
        z_b.draw(svgGenerator);

        assertThat(r_a.getArea(), is(closeTo(40 * 40 * Math.PI, 0.1)));
        assertThat(r_b.getArea(), is(closeTo(40 * 40 * Math.PI, 0.1)));
        assertThat(z_a.getArea() + z_ab.getArea(), is(closeTo(r_a.getArea(), 0.1)));
        assertThat(z_b.getArea() + z_ab.getArea(), is(closeTo(r_b.getArea(), 0.1)));

        svgWriter.writeSVG();
    }

    @Test
    public void testDisconnected() {
        Circle2D a = new Circle2D(-20, 0, 10);
        Circle2D b = new Circle2D(20, 0, 10);

        SplitArcBoundary z_a = new SplitArcBoundary(Arrays.asList(a), Arrays.asList(b));
        SplitArcBoundary z_b = new SplitArcBoundary(Arrays.asList(b), Arrays.asList(a));

        Optional<SplitArcBoundary> ix_a = z_a.intersection(z_b);
        Optional<SplitArcBoundary> ix_b = z_b.intersection(z_a);

        assertThat(ix_a, is(equalTo(Optional.empty())));
        assertThat(ix_b, is(equalTo(Optional.empty())));

        Optional<SplitArcBoundary> less_a = z_a.less(b);
        Optional<SplitArcBoundary> less_b = z_b.less(a);

        assertThat(less_a.get(), is(equalTo(z_a)));
        assertThat(less_b.get(), is(equalTo(z_b)));
    }
}
