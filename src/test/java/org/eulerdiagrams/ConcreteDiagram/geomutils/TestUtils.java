package org.eulerdiagrams.ConcreteDiagram.geomutils;

import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;
import math.geom2d.conic.CircleArc2D;
import math.geom2d.domain.BoundaryPolyCurve2D;
import org.junit.Test;

import java.util.Collection;
import java.util.Optional;

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

        CircleArc2D ca1 = new CircleArc2D(c1, 0, Math.PI * 2); // a full circle
        CircleArc2D ca2 = new CircleArc2D(c2, 0, Math.PI * 2); // a full circle

        BoundaryPolyCurve2D<CircleArc2D> curve1 = new BoundaryPolyCurve2D<>(), curve2 = new BoundaryPolyCurve2D<>();
        curve1.add(ca1);
        curve2.add(ca2);

        assertThat(curve1.curves().size(), is(1));

        Optional<Collection<Point2D>> ixs = ca1.intersections(ca2);

        assertThat(ixs, is(not(Optional.empty())));
        assertThat(ixs.get().size(), is(2));

        Point2D ipoint = ixs.get().toArray(new Point2D[0])[0]; // first intersection point

        curve1 = Utils.split(curve1, ipoint);
        assertThat(curve1.curves().size(), is(2));
    }

    @Test
    public void testNext () {
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

        // Find arc on curve 1 that contains ipoint
        Optional<CircleArc2D> oarc = Utils.findArcContaining(curve1, ipoint);
        assertThat(oarc, is(not(Optional.empty())));
        CircleArc2D arc = oarc.get();

        Collection<Point2D> ixs = oixs.get();
        ixs.remove(ipoint);

        CircleArc2D a  = Utils.next(curve1, arc, curve2, new BoundaryPolyCurve2D<CircleArc2D>());
        assertThat(a, is(not(arc)));
        assertThat(curve1.contains(a), is(true));
    }
}
