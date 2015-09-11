package org.eulerdiagrams.ConcreteDiagram.geomutils;

import math.geom2d.conic.Circle2D;
import math.geom2d.conic.CircleArc2D;
import math.geom2d.domain.BoundaryPolyCurve2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.SimplePolygon2D;

import org.eulerdiagrams.ConcreteDiagram.ConcreteZone;

import math.geom2d.Point2D;
import math.geom2d.Shape2D;
import math.geom2d.Vector2D;

import java.util.*;
import java.util.stream.Collectors;

public class Utils {
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
            Vector2D v1 = new Vector2D(centroid, midpoint(ca1));
            Vector2D v2 = new Vector2D(centroid, midpoint(ca2));
            Vector2D xAxis = new Vector2D(new Point2D(0,0), new Point2D(1, 0));

            double angle1 = Math.acos(xAxis.dot(v1));
            double angle2 = Math.acos(xAxis.dot(v2));

            if(angle1 < angle2) return 1;
            else if (angle2 > angle1) return -1;
            return 0;
        }
    }

    /**
     * FIXME: Should really return Optional<Point2D>
     * @param arcs
     * @return
     */
    private static Point2D getCentroid(Iterable<CircleArc2D> arcs) {
        Point2D p = new Point2D(0, 0);

        if(null == arcs) {
            return p;
        }

        int size = 0;
        for(CircleArc2D arc: arcs) {
            p = p.plus(arc.point(0));
            size++;
        }

        return new Point2D(p.x() / size, p.y() / size);
    }

    protected static Optional<CircleArc2D> findArcContaining (BoundaryPolyCurve2D<CircleArc2D> curve, Point2D point) {
        for(CircleArc2D arc: curve) {
            if(arc.contains(point)) {
                return Optional.of(arc);
            }
        }
        return Optional.empty();
    }

    protected static Point2D midpoint(CircleArc2D arc) {
        double angle = arc.getAngleExtent() / 2.0;
        return arc.point(angle);
    }

    protected static boolean contains(BoundaryPolyCurve2D<CircleArc2D> boundary, CircleArc2D arc) {
        // Represent the arc as three points
        Point2D start = arc.firstPoint(), last = arc.lastPoint(), mid = midpoint(arc);

        // now check that each is within or on the boundary
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
    protected static Optional<CircleArc2D> directionlessFind(Iterable<CircleArc2D> boundary, Collection<CircleArc2D> visited, Point2D mark) {
        for(CircleArc2D arc: boundary) {
            if((arc.firstPoint().almostEquals(mark, Shape2D.ACCURACY) || arc.lastPoint().almostEquals(mark, Shape2D.ACCURACY)) && !visited.contains(arc)) {
                return Optional.of(arc);
            }
        }
        return Optional.empty();
    }

    protected static Optional<CircleArc2D> directionlessFind(Iterable<CircleArc2D> boundary, Point2D mark) {
        for(CircleArc2D arc: boundary) {
            if(arc.firstPoint().almostEquals(mark, Shape2D.ACCURACY) || arc.lastPoint().almostEquals(mark, Shape2D.ACCURACY)) {
                return Optional.of(arc);
            }
        }
        return Optional.empty();
    }

    // FIXME: Does not work for disconnected zones!
    protected static BoundaryPolyCurve2D<CircleArc2D> intersection(Collection<BoundaryPolyCurve2D<CircleArc2D>> inBoundaries, Collection<BoundaryPolyCurve2D<CircleArc2D>> outBoundaries) {
        Collection<CircleArc2D> ix = intersection(inBoundaries);

        ix = exclusion(fromCollection(ix), outBoundaries);
        return fromCollection(ix);
    }

    protected static BoundaryPolyCurve2D<CircleArc2D> fromCollection(Collection<CircleArc2D> as) {
        BoundaryPolyCurve2D<CircleArc2D> zone = new BoundaryPolyCurve2D<>();
        
        // Cheap(ish) assumes 'as' define a convex curve
        Point2D centroid = getCentroid(as);
        Collections.sort(as.stream().collect(Collectors.toList()), new CCWCircleArc2DComparitor(centroid));
        as.forEach(a -> zone.add(a));

        /*Optional<CircleArc2D> oarc = as.stream().findFirst();
        if(!oarc.isPresent()) {
            return zone;
        }

        if(oarc.get().curvature(0) >=0 ) { // if CCW
            zone.add(oarc.get());
        } else {
            zone.add(oarc.get().reverse());
        }
        as.remove(oarc.get());

        for(int i=0; i< as.size(); i++) {
            oarc = directionlessFind(as, zone.lastPoint());

            if(oarc.isPresent() && !oarc.get().firstPoint().almostEquals(zone.lastPoint(), Shape2D.ACCURACY)) {
                zone.add(oarc.get());
                as.remove(oarc.get());
            } else if (oarc.isPresent()) {
                zone.add(oarc.get().reverse());
                as.remove(oarc.get());
            }
        }*/

        return zone;
    }
    protected static Collection<CircleArc2D> exclusion(BoundaryPolyCurve2D<CircleArc2D> inBoundary, Collection<BoundaryPolyCurve2D<CircleArc2D>> outBoundaries) {
        if(outBoundaries.isEmpty()) {
            return inBoundary.curves();
        }

        // Find those outBoundaries contained in inBoundaries
        Collection<CircleArc2D> ex = new HashSet<>();
        for(BoundaryPolyCurve2D<CircleArc2D> boundary: outBoundaries) {
            ex.addAll(boundary.curves().stream().filter(x -> contains(inBoundary, x)).collect(Collectors.toSet()));
        }

        // filter out curves that are contained in an outBoundary
        ex.addAll(inBoundary.curves().stream().filter(x -> outBoundaries.stream().noneMatch(b -> contains(b, x))).collect(Collectors.toSet()));
        //inBoundary.forEach(c -> ex.add(c));
        return ex;
    }

    protected static Collection<List<BoundaryPolyCurve2D<CircleArc2D>>> clusters(Collection<BoundaryPolyCurve2D<CircleArc2D>> boundaries) {
        if(boundaries.isEmpty()) {
            return new Vector<>();
        }
        
        // This is horrible, a 2D Vector of stuff.
        // -  We're looking for clusters of things that intersect with each other.
        Collection<List<BoundaryPolyCurve2D<CircleArc2D>>> clusters = new Vector<>();
        List<BoundaryPolyCurve2D<CircleArc2D>> first = new Vector<>();
        first.add(boundaries.stream().findFirst().get());
        clusters.add(first);

        nextBoundary: for(BoundaryPolyCurve2D<CircleArc2D> boundary: boundaries) {
            for(Collection<BoundaryPolyCurve2D<CircleArc2D>> cluster: clusters) {
                for(BoundaryPolyCurve2D<CircleArc2D> contour: cluster) {
                    if(nonTangentalIntersections(contour, boundary).isPresent()) {
                        cluster.add(contour);
                        continue nextBoundary;
                    }
                }
            }
        }
        
        return clusters;
    }

    protected static BoundaryPolyCurve2D<CircleArc2D> union(BoundaryPolyCurve2D<CircleArc2D> boundary1, BoundaryPolyCurve2D<CircleArc2D> boundary2) {
        Optional<Collection<Point2D>> ixs = nonTangentalIntersections(boundary1, boundary2);
        if(!ixs.isPresent()) {
            return boundary1;
        }

        // Remove any curve that is within both contours
        Collection<CircleArc2D> union = boundary1.curves();
        union.addAll(boundary2.curves());
        
        Collection<CircleArc2D> intersection = intersection(Arrays.asList(boundary1, boundary2));
        union.removeAll(intersection);
        
        return fromCollection(union);
    }

    protected static Collection<BoundaryPolyCurve2D<CircleArc2D>> union(Collection<BoundaryPolyCurve2D<CircleArc2D>> boundaries) {
        if(boundaries.isEmpty()) {
            return new Vector<>();
        }
        Collection<List<BoundaryPolyCurve2D<CircleArc2D>>> clusters = clusters(boundaries);

        // Now reduce each cluster in turn
        Collection<BoundaryPolyCurve2D<CircleArc2D>> hulls = new Vector<>();
        for(List<BoundaryPolyCurve2D<CircleArc2D>> cluster: clusters) {
            if(1 == cluster.size()) {
                hulls.add(cluster.stream().findFirst().get());
            }  else {
                BoundaryPolyCurve2D<CircleArc2D> hull = cluster.stream().findFirst().get();
                for(BoundaryPolyCurve2D<CircleArc2D> b: cluster.subList(2, cluster.size())) {
                    hull = union(hull, b);
                }
                hulls.add(hull);
            }
        }
        
        return hulls;
    }

    protected static Collection<CircleArc2D> intersection(Collection<BoundaryPolyCurve2D<CircleArc2D>> inBoundaries) {
        final Set<CircleArc2D> iarcs = new HashSet<>();
        inBoundaries.forEach(b -> iarcs.addAll(b.curves()));

        return iarcs.stream().filter(x -> inBoundaries.stream().allMatch(b -> contains(b, x))).collect(Collectors.toSet());
    }

    protected static double area (BoundaryPolyCurve2D<CircleArc2D> boundary, Collection<Circle2D> out) {
        if(boundary.isEmpty()) return 0;

        Polygon2D internal = new SimplePolygon2D();
        for(CircleArc2D arc: boundary) {
            internal.addVertex(arc.lastPoint());
        }

        double area = internal.area();
        for(CircleArc2D arc: boundary) {
            if(out.stream().noneMatch(c -> c.equals(arc.supportingCircle()))) {
                area += arc.getChordArea();
            } else {
                area -= arc.getChordArea();
            }
        }

        return area;
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
                a2 = arc.subCurve(arcPos, arc.getAngleExtent());
                rval.add(a1);
                rval.add(a2);
            } else {
                // copy the arc
                rval.add(new CircleArc2D(arc.supportingCircle().center(), arc.supportingCircle().radius(), arc.getStartAngle(), arc.getAngleExtent()));
            }
        }

        return rval;
    }

    protected static Optional<Collection<Point2D>> nonTangentalIntersections (BoundaryPolyCurve2D<CircleArc2D> pc1, BoundaryPolyCurve2D<CircleArc2D> pc2) {
        Collection<Point2D> ixs = new Vector<Point2D>();
        for(CircleArc2D a1 : pc1) {
            for(CircleArc2D a2 : pc2) {
                Optional<Collection<Point2D>> is = nonTangentalIntersections(a1, a2);
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
    
    public static Optional<Collection<Point2D>> nonTangentalIntersections (CircleArc2D c, CircleArc2D ca) {
        Optional<Collection<Point2D>> ixs = c.intersections(ca);
        if(ixs.isPresent()) {

            // A point is a tangent point if the tangents of this arc and ca (at point p) are parallel.  They're
            // parallel if tangent1 \times tangent2 is 0
            Collection<Point2D> cleaned = ixs.get().stream()
                    .filter(p -> 0.0 != c.tangent(c.position(p)).cross(ca.tangent(ca.position(p))))
                    .collect(Collectors.toList());
            return Optional.of(cleaned);
        }
        return ixs;
    }
}
