package org.eulerdiagrams.ConcreteDiagram.geomutils;

import math.geom2d.conic.Circle2D;

import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Test
    public void testComplexZone() {
        TestUtils tu = new TestUtils();
        TestUtils.SVGWriter svgWriter = tu.new SVGWriter("TestSplitArcBoundary::testComplexZone.svg");
        Graphics2D svgGenerator = svgWriter.getGraphics();

        Circle2D a = new Circle2D(-10, 0, 12);
        Circle2D b = new Circle2D(10, 0, 12);
        Circle2D c = new Circle2D(-7, 0, 7);

        SplitArcBoundary z_a_bc = new SplitArcBoundary(Arrays.asList(a), Arrays.asList(b, c));
        z_a_bc.draw(svgGenerator);
        assertThat(z_a_bc.getArea(), is(closeTo(301.0, 1.0)));

        SplitArcBoundary z_a_b = new SplitArcBoundary(Arrays.asList(a), Arrays.asList(b));
        z_a_b.draw(svgGenerator);

        SplitArcBoundary z_a = new SplitArcBoundary(Arrays.asList(a), Arrays.asList());
        z_a.draw(svgGenerator);

        SplitArcBoundary z_b = new SplitArcBoundary(Arrays.asList(b), Arrays.asList());
        z_b.draw(svgGenerator);

        svgWriter.writeSVG();
    }

    @Test
    public void testFromComplexLiveData() {
        //{\"circles\":[{\"x\":228.449475890545,\"y\":280.096392174177,\"radius\":44.6209405781592,\"label\":\"A\"},
        // {\"x\":318.264012185677,\"y\":264.62309486689,\"radius\":92.0355604168131,\"label\":\"B\"},
        // {\"x\":262.169922300299,\"y\":243.502873710095,\"radius\":72.7213892129234,\"label\":\"D\"},
        // {\"x\":208.206953111216,\"y\":194.551163856882,\"radius\":103.539164844922,\"label\":\"M\"},
        // {\"x\":290.298719607572,\"y\":193.46204146812,\"radius\":74.3939109811923,\"label\":\"S\"},
        // {\"x\":259.61091690469,\"y\":155.764433923835,\"radius\":123.909956662389,\"label\":\"U\"}]}
        TestUtils tu = new TestUtils();
        TestUtils.SVGWriter svgWriter = tu.new SVGWriter("TestSplitArcBoundary::testFromComplexLiveData.svg");
        Graphics2D svgGenerator = svgWriter.getGraphics();

        Circle2D a = new Circle2D(228.449475890545, 280.096392174177, 44.6209405781592);
        Circle2D s = new Circle2D(290.298719607572, 193.46204146812, 74.3939109811923);
        Circle2D d = new Circle2D(262.169922300299, 243.502873710095, 72.7213892129234);
        Circle2D u = new Circle2D(259.61091690469, 155.764433923835, 123.909956662389);

        Circle2D b = new Circle2D(318.264012185677, 264.62309486689, 92.0355604168131);
        Circle2D m = new Circle2D(208.206953111216, 194.551163856882, 103.539164844922);

        //SplitArcBoundary z_asdu_bm = new SplitArcBoundary(Arrays.asList(a, s, d, u), Arrays.asList(b, m));
        //z_asdu_bm.draw(svgGenerator);


        SplitArcBoundary z_a = new SplitArcBoundary(Arrays.asList(a), Arrays.asList());
        z_a.draw(svgGenerator);
        SplitArcBoundary z_as = new SplitArcBoundary(Arrays.asList(a, s), Arrays.asList());
        z_as.draw(svgGenerator);
        SplitArcBoundary z_asd = new SplitArcBoundary(Arrays.asList(a, s, d), Arrays.asList());
        z_asd.draw(svgGenerator);
        SplitArcBoundary z_asdu = new SplitArcBoundary(Arrays.asList(a, s, d, u), Arrays.asList());
        z_asdu.draw(svgGenerator);

        Collection<SplitArcBoundary> boundaries = SplitArcBoundary.splitBoundaries(Arrays.asList(a, s, d), Arrays.asList());
        assertThat(boundaries.size(), is(3));
        assertThat(boundaries.stream().map(x -> x.size()).collect(Collectors.toList()), allOf(is(5)));


        SplitArcBoundary z_asdb = new SplitArcBoundary(Arrays.asList(a, s, d, b), Arrays.asList(u, m));

        svgWriter.writeSVG();
    }
}
