package org.eulerdiagrams.ConcreteDiagram;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.eulerdiagrams.AbstractDiagram.AbstractContour;
import org.junit.Test;

import math.geom2d.conic.Circle2D;

public class TestConcreteCircle {

    // @Test -- non-runnable, TODO: delete this test
    public void testVenn2() {
        Circle2D ca = new Circle2D(0,-5, 10);
        Circle2D cb = new Circle2D(0, 5, 10);
        AbstractContour a = new AbstractContour("A");
        AbstractContour b = new AbstractContour("B");
        ConcreteCircle cca = new ConcreteCircle(a, ca);
        //ConcreteCircle ccb = new ConcreteCircle(b, cb, Arrays.asList(ca));

    }
}
