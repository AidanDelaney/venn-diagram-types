package org.eulerdiagrams.ConcreteDiagram.geomutils;

import math.geom2d.conic.Circle2D;
import math.geom2d.conic.CircleArc2D;

import org.eulerdiagrams.ConcreteDiagram.geomutils.TestUtils.SVGWriter;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.Matchers.*;

public class TestSplitArcBoundary {

    private SplitArcBoundary saba, sabb, sabc, sabd, sabe, sabf, sabg;

    @Before
    public void before() {
        Circle2D ca = new Circle2D(-50, 0, 60);
        Circle2D cb = new Circle2D(50, 0, 60);
        Circle2D cc = new Circle2D(0, -50, 50);
        Circle2D cd = new Circle2D(10, 0, 5);
        Circle2D ce = new Circle2D(55, 0, 7);
        Circle2D cf = new Circle2D(60, 0, 7);
        Circle2D cg = new Circle2D(5, 0, 20);

        saba = new SplitArcBoundary(ca, Arrays.asList(cb, cc, cd, ce, cf, cg));
        sabb = new SplitArcBoundary(cb, Arrays.asList(ca, cc, cd, ce, cf, cg));
        sabc = new SplitArcBoundary(cc, Arrays.asList(ca, cb, cd, ce, cf, cg));
        sabd = new SplitArcBoundary(cd, Arrays.asList(ca, cb, cc, ce, cf, cg));
        sabe = new SplitArcBoundary(ce, Arrays.asList(ca, cb, cc, cd, cf, cg));
        sabf = new SplitArcBoundary(cf, Arrays.asList(ca, cb, cc, cd, ce, cg));
        sabg = new SplitArcBoundary(cg, Arrays.asList(ca, cb, cc, cd, ce, cf));
    }

    @Test
    public void testUnion() {
        TestUtils tu = new TestUtils();
        TestUtils.SVGWriter svgWriter = tu.new SVGWriter("TestSplitArcBoundary::testUnion.svg");
        Graphics2D svgGenerator = svgWriter.getGraphics();
        
        saba.draw(svgGenerator);
        sabb.draw(svgGenerator);
        sabc.draw(svgGenerator);
        sabd.draw(svgGenerator);
        sabe.draw(svgGenerator);
        sabf.draw(svgGenerator);
        sabg.draw(svgGenerator);

        svgGenerator.setColor(new Color(255, 0, 0));
        Optional<SplitArcBoundary> uae = saba.union(sabe);
        assertThat(uae, is(Optional.empty()));

        svgGenerator.setColor(new Color(0, 255, 0));
        Optional<SplitArcBoundary> ube = sabb.union(sabe);
        assertThat(ube.get(), is(equalTo(sabb)));
        ube.get().draw(svgGenerator);

        svgGenerator.setColor(new Color(0, 0, 255));
        Optional<SplitArcBoundary> ucd = sabc.union(sabd);
        assertThat(ucd, is(not(Optional.empty())));
        ucd.get().draw(svgGenerator);

        svgWriter.writeSVG();
    }

    @Test
    public void testIntersection() {
        TestUtils tu = new TestUtils();
        TestUtils.SVGWriter svgWriter = tu.new SVGWriter("TestSplitArcBoundary::testIntersection.svg");
        Graphics2D svgGenerator = svgWriter.getGraphics();
        
        saba.draw(svgGenerator);
        sabb.draw(svgGenerator);
        sabc.draw(svgGenerator);
        sabd.draw(svgGenerator);
        sabe.draw(svgGenerator);
        sabf.draw(svgGenerator);
        sabg.draw(svgGenerator);

        Optional<SplitArcBoundary> uae = saba.intersection(sabe);
        assertThat(uae, is(Optional.empty()));

        svgGenerator.setColor(new Color(255, 0, 255));
        Optional<SplitArcBoundary> ube = sabb.intersection(sabe);
        assertThat(ube, is(not(Optional.empty())));
        assertThat(ube.get(), is(equalTo(sabe)));
        ube.get().draw(svgGenerator);

        svgGenerator.setColor(new Color(255, 0, 0));
        Optional<SplitArcBoundary> uad = saba.intersection(sabd);
        assertThat(uad, is(not(Optional.empty())));
        uad.get().draw(svgGenerator);

        svgGenerator.setColor(new Color(255, 255, 0));
        Optional<SplitArcBoundary> ucd = sabc.intersection(sabd);
        assertThat(ucd, is(not(Optional.empty())));
        ucd.get().draw(svgGenerator);

        svgWriter.writeSVG();
    }

    @Test
    public void testLess() {
        TestUtils tu = new TestUtils();
        TestUtils.SVGWriter svgWriter = tu.new SVGWriter("TestSplitArcBoundary::testLess.svg");
        Graphics2D svgGenerator = svgWriter.getGraphics();
        
        saba.draw(svgGenerator);
        sabb.draw(svgGenerator);
        sabc.draw(svgGenerator);
        sabd.draw(svgGenerator);
        sabe.draw(svgGenerator);
        sabf.draw(svgGenerator);
        sabg.draw(svgGenerator);

        Optional<Collection<SplitArcBoundary>> lae = saba.less(sabe);
        assertThat(lae, is(Optional.empty()));

        Optional<Collection<SplitArcBoundary>> lad = saba.less(sabd);
        Optional<Collection<SplitArcBoundary>> lda = sabd.less(saba);
        assertThat(lad, is(not(Optional.empty())));
        assertThat(lda, is(not(Optional.empty())));
        SplitArcBoundary bad = lad.get().stream().findFirst().get();
        SplitArcBoundary bda = lda.get().stream().findFirst().get();

        svgGenerator.setColor(new Color(0, 50, 50));
        bad.draw(svgGenerator);
        svgGenerator.setColor(new Color(150, 150, 150));
        bda.draw(svgGenerator);

        svgWriter.writeSVG();
    }

    @Test
    public void testRegionSplittingLess() {
        TestUtils tu = new TestUtils();
        TestUtils.SVGWriter svgWriter = tu.new SVGWriter("TestSplitArcBoundary::testRegionSpllittingLess.svg");
        Graphics2D svgGenerator = svgWriter.getGraphics();
        
        saba.draw(svgGenerator);
        sabb.draw(svgGenerator);
        sabc.draw(svgGenerator);
        sabd.draw(svgGenerator);
        sabe.draw(svgGenerator);
        sabf.draw(svgGenerator);
        sabg.draw(svgGenerator);

        Optional<SplitArcBoundary> uab = saba.intersection(sabb);
        assertThat(uab, is(not(Optional.empty())));

        Optional<Collection<SplitArcBoundary>> lacg = uab.get().less(sabg);
        assertThat(lacg.get().size(), is(2));

        svgGenerator.setColor(new Color(255, 0, 0));
        for(SplitArcBoundary arc: lacg.get()) {
            arc.draw(svgGenerator);
        }

        svgWriter.writeSVG();
    }
}
