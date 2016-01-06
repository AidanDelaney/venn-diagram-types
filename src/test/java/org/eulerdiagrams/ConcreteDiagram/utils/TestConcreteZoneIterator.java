package org.eulerdiagrams.ConcreteDiagram.utils;

import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;
import org.eulerdiagrams.AbstractDiagram.*;
import org.eulerdiagrams.ConcreteDiagram.*;
import org.eulerdiagrams.ConcreteDiagram.geomutils.ConcreteZoneIterator;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class TestConcreteZoneIterator {
    @Test
    public void testSingleContour() {
        AbstractContour a = new AbstractContour("A");
        Circle2D circleA = new Circle2D(new Point2D(-5, 0), 7.0);
        ConcreteCircle ca = new ConcreteCircle(a, circleA);
        AbstractDiagram ad = new AbstractDiagram(new HashSet<>(Arrays.asList(a)));
        ConcreteDiagram d = new ConcreteDiagram(ad, Arrays.asList(ca));

        ConcreteZoneIterator czvi = new ConcreteZoneIterator(Arrays.asList(ca));

        int size = 0;
        while(czvi.hasNext()) {
            czvi.next();
            size++;
        }
        assertThat(size, is(1));
    }

    @Test
    public void testTunnelDiagram() {
        AbstractContour a = new AbstractContour("A");
        AbstractContour b = new AbstractContour("B");
        Circle2D circleA = new Circle2D(new Point2D(-5, 0), 7.0);
        Circle2D circleB = new Circle2D(new Point2D(-5, 0), 2.0);
        ConcreteCircle ca = new ConcreteCircle(a, circleA);
        ConcreteCircle cb = new ConcreteCircle(b, circleB);
        AbstractDiagram ad = new AbstractDiagram(new HashSet<>(Arrays.asList(a, b)));
        ConcreteDiagram d = new ConcreteDiagram(ad, Arrays.asList(ca, cb));

        ConcreteZoneIterator czvi = new ConcreteZoneIterator(Arrays.asList(ca, cb));

        int size = 0;
        while(czvi.hasNext()) {
            czvi.next();
            size++;
        }
        assertThat(size, is(2));
    }

    @Test
    public void testVenn2() {
        AbstractContour a = new AbstractContour("A");
        AbstractContour b = new AbstractContour("B");
        Circle2D circleA = new Circle2D(new Point2D(-5, 0), 7.0);
        Circle2D circleB = new Circle2D(new Point2D(5, 0), 7.0);
        ConcreteCircle ca = new ConcreteCircle(a, circleA);
        ConcreteCircle cb = new ConcreteCircle(b, circleB);
        AbstractDiagram ad = new AbstractDiagram(new HashSet<>(Arrays.asList(a, b)));
        ConcreteDiagram d = new ConcreteDiagram(ad, Arrays.asList(ca, cb));

        ConcreteZoneIterator czvi = new ConcreteZoneIterator(Arrays.asList(ca, cb));

        int size = 0;
        while(czvi.hasNext()) {
            czvi.next();
            size++;
        }
        assertThat(size, is(3));
    }

    @Test
    public void testVenn3() {
        AbstractContour a = new AbstractContour("A");
        AbstractContour b = new AbstractContour("B");
        AbstractContour c = new AbstractContour("C");
        Circle2D circleA = new Circle2D(new Point2D(-5, 0), 7.0);
        Circle2D circleB = new Circle2D(new Point2D(5, 0), 7.0);
        Circle2D circleC = new Circle2D(new Point2D(0, 5), 7.0);
        ConcreteCircle ca = new ConcreteCircle(a, circleA);
        ConcreteCircle cb = new ConcreteCircle(b, circleB);
        ConcreteCircle cc = new ConcreteCircle(c, circleC);
        AbstractDiagram ad = new AbstractDiagram(new HashSet<>(Arrays.asList(a, b, c)));
        ConcreteDiagram d = new ConcreteDiagram(ad, Arrays.asList(ca, cb, cc));

        ConcreteZoneIterator czvi = new ConcreteZoneIterator(Arrays.asList(ca, cb, cc));

        int size = 0;
        while(czvi.hasNext()) {
            czvi.next();
            size++;
        }
        assertThat(size, is(7));
    }

    @Test
    public void testCrossedVenn2() {
        AbstractContour a = new AbstractContour("A");
        AbstractContour b = new AbstractContour("B");
        AbstractContour c = new AbstractContour("C");
        AbstractContour d = new AbstractContour("D");
        ConcreteCircle ca = new ConcreteCircle(a, new Circle2D(0, -10, 12));
        ConcreteCircle cb = new ConcreteCircle(b, new Circle2D(0, 10, 12));
        ConcreteCircle cc = new ConcreteCircle(c, new Circle2D(-10, 0, 12));
        ConcreteCircle cd = new ConcreteCircle(d, new Circle2D(10, 0, 12));

        AbstractDiagram ad = new AbstractDiagram(new HashSet(){{add(a);add(b);add(c);add(d);}});

        ConcreteDiagram diagram = new ConcreteDiagram(ad, Arrays.asList(ca, cb, cc, cd));

        ConcreteZoneIterator czvi = new ConcreteZoneIterator(Arrays.asList(ca, cb, cc, cd));

        int size = 0;
        while(czvi.hasNext()) {
            czvi.next();
            size++;
        }
        assertThat(size, is(13));
    }
}
