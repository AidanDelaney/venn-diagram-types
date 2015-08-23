package org.eulerdiagrams.ConcreteDiagram;

import math.geom2d.Point2D;
import org.eulerdiagrams.AbstractDiagram.AbstractContour;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class TestConcreteZone {
    @Test
    public void testEquals() {
        assertThat(ConcreteZone.Top.getInstance(), equalTo(ConcreteZone.Top.getInstance()));
    }

    @Test
    public void testHashCode() {
        assertThat(ConcreteZone.Top.getInstance().hashCode(), equalTo(ConcreteZone.Top.getInstance().hashCode()));

        AbstractContour a1 = new AbstractContour("A");
        AbstractContour a2 = new AbstractContour("A");

        assertThat(a1.hashCode(), equalTo(a2.hashCode()));

        ConcreteCircle c1 = new ConcreteCircle(a1, new Point2D(0, 0), 5);
        ConcreteCircle c2 = new ConcreteCircle(a2, new Point2D(0, 0), 5);

        assertThat(c1.hashCode(), equalTo(c2.hashCode()));

        ConcreteZone z1 = new ConcreteZone(c1);
        ConcreteZone z2 = new ConcreteZone(c2);

        assertThat(z1.hashCode(), equalTo(z2.hashCode()));
    }
}
