package org.eulerdiagrams.ConcreteDiagram;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;

import org.eulerdiagrams.AbstractDiagram.AbstractContour;
import org.junit.Test;

import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;

public class TestConcreteCircle {

    @Test
    public void testVenn2() {
        Circle2D ca = new Circle2D(0,-5, 10);
        Circle2D cb = new Circle2D(0, 5, 10);
        AbstractContour a = new AbstractContour("A");
        AbstractContour b = new AbstractContour("B");
        ConcreteCircle cca = new ConcreteCircle(a, ca, Arrays.asList(cb));
        //ConcreteCircle ccb = new ConcreteCircle(b, cb, Arrays.asList(ca));

        // Invariant, a ConcreteCircle contains n arcs.  We should be able to
        // traverse from the 0'th to the 0'th in n+1 steps.  This is because a
        // circle is circular :)
        int size = cca.getBoundary().size();
        assertThat(size, is(greaterThan(1)));
        Point2D first = cca.getBoundary().get(0).firstPoint();
        Point2D last = cca.getBoundary().get(size - 1).lastPoint();
        assertThat(first.x(), is(closeTo(last.x(), 0.0001)));
        assertThat(first.y(), is(closeTo(last.y(), 0.0001)));
    }
}
