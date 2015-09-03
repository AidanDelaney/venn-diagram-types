package org.eulerdiagrams.ConcreteDiagram.geomutils;

import math.geom2d.conic.Circle2D;
import math.geom2d.conic.CircleArc2D;
import math.geom2d.domain.BoundaryPolyCurve2D;
import org.eulerdiagrams.ConcreteDiagram.ConcreteZone;

import math.geom2d.Point2D;
import math.geom2d.Shape2D;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    public static Optional<Pair<ConcreteZone, ConcreteZone>> intersects(BoundaryPolyCurve2D<CircleArc2D> arclist, Circle2D circle) {
        for(CircleArc2D a : arclist) {
            //Point2D.Double i = a.
        }
        return Optional.empty();
    }

    public static class CCWPointComparitor implements Comparator<Pair<Point2D, ?>> {
        private Point2D centroid;
        public CCWPointComparitor(Point2D centroid) {
            this.centroid = centroid;
        }

        public int compare(Pair<Point2D, ?> p1, Pair<Point2D, ?> p2) {
            double angle1 = Math.atan2(p1.car.y() - centroid.y(), p1.car.x() - centroid.x());
            double angle2 = Math.atan2(p2.car.y() - centroid.y(), p2.car.x() - centroid.x());

            if(angle1 < angle2) return 1;
            else if (angle2 > angle1) return -1;
            return 0;
        }

    }

    public static class CCWCircleArc2DComparitor implements Comparator<CircleArc2D> {
        private Point2D centroid;
        public CCWCircleArc2DComparitor(Point2D centroid) {
            this.centroid = centroid;
        }

        public int compare(CircleArc2D ca1, CircleArc2D ca2) {
            double angle1 = Math.atan2(ca1.point(0).y() - centroid.y(), ca1.point(0).x() - centroid.x());
            double angle2 = Math.atan2(ca2.point(0).y() - centroid.y(), ca2.point(0).x() - centroid.x());

            if(angle1 < angle2) return 1;
            else if (angle2 > angle1) return -1;
            return 0;
        }
    }

    public static class CCWWingedIntersectionComparitor implements Comparator<WingedIntersection> {
        private Point2D centroid;
        public CCWWingedIntersectionComparitor(Point2D centroid) {
            this.centroid = centroid;
        }

        public int compare(WingedIntersection p1, WingedIntersection p2) {
            double angle1 = Math.atan2(p1.getIntersectionPoint().y() - centroid.y(), p1.getIntersectionPoint().x() - centroid.x());
            double angle2 = Math.atan2(p2.getIntersectionPoint().y() - centroid.y(), p2.getIntersectionPoint().x() - centroid.x());

            if(angle1 < angle2) return 1;
            else if (angle2 > angle1) return -1;
            return 0;
        }
    }

    public static Optional<List<BoundaryPolyCurve2D<CircleArc2D>>> intersections(final BoundaryPolyCurve2D<CircleArc2D> a, final BoundaryPolyCurve2D<CircleArc2D> b) {
        // First do a broad-phase cheap collision detection
        boolean containedB = b.vertices().parallelStream().anyMatch(x -> a.contains(x));
        boolean containedA = a.vertices().parallelStream().anyMatch(x -> b.contains(x));

        if(!containedB || containedA) {
            return Optional.empty();
        }

        // Now we move on to narrow phase. There are two cases:
        // The case where either a is wholly contained in b (or vice-versa), and
        // the case where a intersects with b.

        // The contained case is straightforward
        Vector<BoundaryPolyCurve2D<CircleArc2D>> minimalRegions = new Vector();
        if(containedB) {
            minimalRegions.add(b);
            return Optional.of(minimalRegions);
        }

        if(containedA) {
            minimalRegions.add(a);
            return Optional.of(minimalRegions);
        }

        // Now we know that a intersects b in some way
        Optional<Collection<Point2D>> ntixs = nonTangentalIntersections(a, b);

        if(!ntixs.isPresent()) { // There are only tangental intersections between a and b.
            return Optional.empty();
        }

        BoundaryPolyCurve2D<CircleArc2D> ca = a, cb = b;  // because we want to modify the final parameters
        for(Point2D p: ntixs.get()) {
            ca = split(a, p);
            cb = split(b, p);
        }

        return Optional.empty();
    }

    /**
     * FIXME: Should really return Optional<Point2D>
     * @param arcs
     * @return
     */
    private static Point2D getCentroid(BoundaryPolyCurve2D<CircleArc2D> arcs) {
        Point2D p = new Point2D(0, 0);

        if(null == arcs || arcs.size() <= 0 ) {
            return p;
        }

        for(CircleArc2D arc: arcs) {
            p = p.plus(arc.point(0));
        }

        return new Point2D(p.x() / arcs.size(), p.y() / arcs.size());
    }

    protected static Optional<CircleArc2D> findArcContaining (BoundaryPolyCurve2D<CircleArc2D> curve, Point2D point) {
        for(CircleArc2D arc: curve) {
            if(arc.contains(point)) {
                return Optional.of(arc);
            }
        }
        return Optional.empty();
    }

    /**
     * Calclulates the intersection between two BoundaryPolyCurve2D<CircleArc2d>s under the constraint that both curves have an
     * arc that either starts or ends at point p.  Point p is a point where the two curves intersect.  We must be
     * guaranteed that both curves contain an arc that starts on p and an arc that ends on p.
     * @param p1
     * @param p2
     * @param p
     * @return
     */
    private static Optional<BoundaryPolyCurve2D<CircleArc2D>> getMinIntersectedRegion(BoundaryPolyCurve2D<CircleArc2D> p1, BoundaryPolyCurve2D<CircleArc2D> p2, Collection<Point2D> ixs, Point2D p) {
        // Find the index of an arc that either starts or ends on p
        int i = -1;
        for(CircleArc2D arc: p1) {
            if(p.equals(arc.firstPoint())) {
                i = p1.indexOf(arc);
            }
        }

        if(-1 == i) {
            return Optional.empty();
        }

        // Now either p1[i+1] or p1[i-1] are contained in p2.  "Follow" whichever one is, until we get to an arc that
        // ends on any intersection point from ixs.  Essentially, trace around the curve boundary until you come to the
        // next intersection point, then make the decision as to which next arc to follow.
        Collection<CircleArc2D> visited = new Vector<>();
        BoundaryPolyCurve2D<CircleArc2D> intersection = new BoundaryPolyCurve2D<>();
        Pair<Optional<CircleArc2D>, Optional<CircleArc2D>> next = next(p1, p2, visited, p);
        while (next.car.isPresent()) {
            visited.add(next.cdr.get());
            intersection.add(next.car.get());
            next = next(p1, p2, visited, next.car.get().lastPoint());
        }

        return Optional.of(intersection);
    }

    /**
     * Find the set of arcs that are incident to a point.  Either they start on the point or end on the point.
     * @param boundary1
     * @param boundary2
     * @param point
     * @return
     */
    private static Set<CircleArc2D> findAdjacentArcs(BoundaryPolyCurve2D<CircleArc2D> boundary1, BoundaryPolyCurve2D<CircleArc2D> boundary2, Point2D point) {
        // The predicate expressing incidence is (x -> x.firstPoint()..almostEquals(point, Shape2D.ACCURACY) || x.lastPoint()..almostEquals(point, Shape2D.ACCURACY))
        Set<CircleArc2D> adjacentb1 = boundary1.curves().stream().filter(x -> x.firstPoint().almostEquals(point, Shape2D.ACCURACY) || x.lastPoint().almostEquals(point, Shape2D.ACCURACY)).collect(Collectors.toSet());
        Set<CircleArc2D> adjacentb2 = boundary2.curves().stream().filter(x -> x.firstPoint().almostEquals(point, Shape2D.ACCURACY) || x.lastPoint().almostEquals(point, Shape2D.ACCURACY)).collect(Collectors.toSet());
        
        Set<CircleArc2D> union = adjacentb1;
        union.addAll(adjacentb2);
        return union;
    }

    private static BoundaryPolyCurve2D<CircleArc2D> toggle(BoundaryPolyCurve2D<CircleArc2D> curve, BoundaryPolyCurve2D<CircleArc2D> option1, BoundaryPolyCurve2D<CircleArc2D> option2) {
        if(curve == option1) {
            return option2;
        }
        return option1;
    }

    protected static boolean contains(BoundaryPolyCurve2D<CircleArc2D> boundary, CircleArc2D arc) {
        // Represent the arc as three points
        Point2D start = arc.firstPoint(), last = arc.lastPoint(), mid = arc.point(0.5);

        // now check that each is within or on the boundary
        boolean s = boundary.isInside(start) || boundary.contains(start);
        boolean m = boundary.isInside(mid) || boundary.contains(mid);
        boolean l = boundary.isInside(last) || boundary.contains(last);
        return s && m && l;
    }

    /**
     * Return the first Circle2D on this boundary that starts, or ends on mark and has not ben visited.
     * @param boundary
     * @param visited
     * @param mark
     * @return
     */
    protected static Optional<CircleArc2D> directionlessFind(BoundaryPolyCurve2D<CircleArc2D> boundary, Collection<CircleArc2D> visited, Point2D mark) {
        for(CircleArc2D arc: boundary) {
            if((arc.firstPoint().almostEquals(mark, Shape2D.ACCURACY) || arc.lastPoint().almostEquals(mark, Shape2D.ACCURACY)) && !visited.contains(arc)) {
                return Optional.of(arc);
            }
        }
        return Optional.empty();
    }

    /**
     * Next implements a restricted DFS on the input "graph".  We find an arc that (a) is connected to the last seen arc
     * and (b) is within the boundaries of both BoundaryPolyCurve2D.  We then return both the newly seen arc and a new arc
     * that is the same as the newly seen arc, but is in the same direction as the arc at the top of the stack.  It's expected
     * that the caller wants to add the second of this pair to the collection of visited arcs.
     * @return
     */
    protected static Pair<Optional<CircleArc2D>, Optional<CircleArc2D>> next(BoundaryPolyCurve2D<CircleArc2D> boundary1, BoundaryPolyCurve2D<CircleArc2D> boundary2, Collection<CircleArc2D> visited, Point2D mark) {

        Set<CircleArc2D> incident = findAdjacentArcs(boundary1, boundary2, mark);
        // filter out those arcs not in the intersection
        incident = incident.stream().filter(x -> contains(boundary1, x) && contains(boundary2, x)).collect(Collectors.toSet());
        // filter out those arcs that have been visited
        incident = incident.stream().filter(x -> !visited.contains(x)).collect(Collectors.toSet());
        // Now, incident contains 0 or 1 contours, or in the initial case were visited is empty, may contain 2 contours.

        Optional<CircleArc2D> retVal = incident.stream().findFirst();

        // make a copy of the arc
        Optional<CircleArc2D> visitMark = Optional.empty();
        if(retVal.isPresent()) {
            visitMark = Optional.of(new CircleArc2D(retVal.get().supportingCircle(), retVal.get().getStartAngle(), retVal.get().getAngleExtent()));
        }

        // make the arc retVal start at mark
        if(retVal.isPresent() && !retVal.get().firstPoint().almostEquals(mark, Shape2D.ACCURACY)) {
            // FIXME: slight worry that this might introduce discontinuity <= ACCURACY
            retVal = Optional.of(retVal.get().reverse());
        }
        return new Pair<Optional<CircleArc2D>, Optional<CircleArc2D>>(retVal, visitMark);
    }

    /**
     * Split the BoundaryPolyCurve2D at the given point.  Splitting means that we divide the arc on which p lies into two arcs.
     * @param arcs The BoundaryPolyCurve2D to split.
     * @param p The point at which to split arcs
     * @return if p lies on an arc in arcs, then a copy of arcs with the require split arc.  Otherwise, return a copy of
     *         arcs.
     */
    protected static BoundaryPolyCurve2D<CircleArc2D> split(BoundaryPolyCurve2D<CircleArc2D> arcs, Point2D p) {
        BoundaryPolyCurve2D<CircleArc2D> rval = new BoundaryPolyCurve2D<>();
        for(CircleArc2D arc: arcs) {
            double arcPos = arc.position(p);
            // split the arc if this point is on the arc, but not one of the extremes of the arc
            if(arc.contains(p) && 0 != arcPos && 1 != arcPos) {
                // split arc
                CircleArc2D a1, a2;
                a1 = arc.subCurve(0, arcPos);
                a2 = arc.subCurve(arcPos, 1);
                rval.add(a1);
                rval.add(a2);
            } else {
                rval.add(arc);
            }
        }

        return rval;
    }

    public static Optional<Collection<Point2D>> nonTangentalIntersections (BoundaryPolyCurve2D<CircleArc2D> pc1, BoundaryPolyCurve2D<CircleArc2D> pc2) {
        Collection<Point2D> ixs = new Vector<Point2D>();
        for(CircleArc2D a1 : pc1) {
            for(CircleArc2D a2 : pc2) {
                Optional<Collection<Point2D>> is = a1.nonTangentalIntersections(a2);
                if(is.isPresent()) {
                    ixs.addAll(is.get());
                }
            }
        }

        if(ixs.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(ixs);
    }
}
