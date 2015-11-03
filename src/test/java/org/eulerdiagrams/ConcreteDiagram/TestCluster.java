package org.eulerdiagrams.ConcreteDiagram;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;

import org.eulerdiagrams.AbstractDiagram.AbstractContour;
import org.eulerdiagrams.AbstractDiagram.AbstractZone;
import org.junit.Test;

import math.geom2d.conic.Circle2D;

import static org.hamcrest.Matchers.*;

public class TestCluster {

    @Test
    public void testClusterContainingVenn2() {
        Circle2D ca = new Circle2D(0,-5, 10);
        Circle2D cb = new Circle2D(0, 5, 10);

        AbstractContour a = new AbstractContour("A");
        AbstractContour b = new AbstractContour("B");
        ConcreteCircle cca = new ConcreteCircle(a, ca, Arrays.asList(cb));
        ConcreteCircle ccb = new ConcreteCircle(b, cb, Arrays.asList(ca));
        Cluster c = new Cluster(Arrays.asList(cca, ccb));

        AbstractZone za = new AbstractZone(new HashSet<AbstractContour>(){{add(a);}}, new HashSet<AbstractContour>(){{add(b);}});
        AbstractZone zab = new AbstractZone(new HashSet<AbstractContour>(){{add(a);add(b);}}, new HashSet<AbstractContour>());
        double area = c.getArea(zab);
        assertThat(area, is(closeTo(122.83, 0.01))); // hand calculated
        area += c.getArea(za);
        assertThat(area, is(closeTo(Math.PI * 10 * 10, 0.001)));
    }
}
