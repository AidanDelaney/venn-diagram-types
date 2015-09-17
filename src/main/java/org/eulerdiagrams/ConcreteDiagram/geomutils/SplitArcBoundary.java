package org.eulerdiagrams.ConcreteDiagram.geomutils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import math.geom2d.Point2D;
import math.geom2d.Shape2D;
import math.geom2d.conic.Circle2D;
import math.geom2d.conic.CircleArc2D;
import math.geom2d.domain.BoundaryPolyCurve2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.SimplePolygon2D;

/**
 * A SplitArcBoundary has the following properties:
 * <ul>
 *    <li>It represents a continuous and closed Boundary composed of Circular
 *        Arcs.</li>
 *    <li>It can only represent genus 0 2D boundaries -- it is the
 *        responsibility of the caller to ensure that they only create genus 0
 *        boundaries.</li>
 *    <li>The "split" in SplitArcBoundary refers to the fact that all possible
 *        intersections with other circles are the start point of an arc and the
 *        end point of another arc -- this simplifies the calculation of union,
 *        intersection and less (exclusion).</li>
 *    <li>Using union, intersection and less on SplitArcBoundaries that have
 *        been incorrectly initialised will result in incorrect calculations.</li>
 * </ul>
 * 
 * We consider that where two contours meet at a tangent, they are
 * non-intersecting.  This has reprecussions, for example: When you take
 * 
 * <pre>
 * {@code
 * SplitArcBoundary ex1 = a.less(b),
 *                  ex2 = b.less(a);
 * }
 * </pre>
 * 
 * the union of ex1 and ex2 is not necessarily equal to <code>a.union(b)</code>.
 *
 */
public class SplitArcBoundary extends BoundaryPolyCurve2D<CircleArc2D> {

    /**
     * Creates a non-empty boundary.  The boundary consists of circle and is
     * split at each intersection point with others.  It is vitally important
     * that all other possible intersection circles are passed into the
     * constructor.
     * @param circle The boundary that we intend to create.
     * @param others All circles that circle could possibly intersect with.
     */
    public SplitArcBoundary(Circle2D circle, Collection<Circle2D> others) {
        super();
        CircleArc2D ca = new CircleArc2D(circle, 0, Math.PI * 2);
        add(ca);

        Collection<Point2D> ixs = new Vector<>();
        for(Circle2D other: others) {
            CircleArc2D oca = new CircleArc2D(other, 0, Math.PI * 2);
            SplitArcBoundary sab = new SplitArcBoundary();
            sab.add(oca);
            Optional<Collection<Point2D>> ps = intersectionPoints(sab); 

            if(ps.isPresent()) {
                ixs.addAll(ps.get());
            }
        }

        for(Point2D p: ixs) {
            split(p);
        }
    }

    /**
     * Creates an empty Boundary.
     */
    public SplitArcBoundary () {
        super();
    }

    /**
     * Split the SplitArcBoundary at the given point.  Splitting means that we divide the arc on which p lies into two arcs.
     * Calling this modifies the SplitArcBoundary that it is called on.
     * @param arcs The BoundaryPolyCurve2D to split.
     * @param p The point at which to split this Boundary.
     */
    protected void split(Point2D p) {
        // Copy the curves as we want to modify this.curves
        ArrayList<CircleArc2D> theCurves = new ArrayList<>(this.curves);
        for(CircleArc2D arc: theCurves) {
            double arcPos = arc.position(p);
            curves.remove(arc);
            // split the arc if this point is on the arc, but not one of the extremes of the arc
            if(arc.contains(p) && 0 != arcPos && 1 != arcPos) {
                // split arc
                CircleArc2D a1, a2;
                a1 = arc.subCurve(0, arcPos);
                a2 = arc.subCurve(arcPos, arc.getAngleExtent());
                curves.add(a1);
                curves.add(a2);
            } else {
                // copy the arc
                curves.add(arc);
            }
        }
    }

    /**
     * The union of two Boundaries is the external boundary of both, when taken
     * together.
     * @param other
     * @return
     */
    public Optional<SplitArcBoundary> union(SplitArcBoundary other) {
        if(this.curves.isEmpty()) {
            return Optional.of(other);
        } else if (other.curves.isEmpty()) {
            return Optional.of(this);
        }

        Optional<SplitArcBoundary> ix = intersection(other);
        if(!ix.isPresent()) {
            // either disconnected or one is fully contained in the other
            boolean thisContained = this.curves.stream().allMatch(x -> other.contains(x));
            if(thisContained) return Optional.of(other);

            boolean otherContained = other.curves.stream().allMatch(x -> this.contains(x));
            if(otherContained) return Optional.of(this);

            return Optional.empty();
        }

        Set<CircleArc2D> arcs = new HashSet<>();
        arcs.addAll(this.curves());
        arcs.addAll(other.curves());
        arcs.removeAll(ix.get().curves());

        return Optional.of(fromCollection(arcs, arcs.stream().findFirst()));
    }

    public Optional<SplitArcBoundary> intersection(SplitArcBoundary other) {
        if(this.curves.isEmpty()) {
            return Optional.empty();
        } else if (other.curves.isEmpty()) {
            return Optional.empty();
        }

        Set<CircleArc2D> iarcs = new HashSet<>();
        iarcs.addAll(curves);
        iarcs.addAll(other.curves);

        Collection<CircleArc2D> arcs = iarcs.stream().filter(x -> other.bounds(x)).collect(Collectors.toSet());
        arcs = arcs.stream().filter(x -> this.bounds(x)).collect(Collectors.toSet());

        if(arcs.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(fromCollection(arcs, arcs.stream().filter(x -> x.curvature(0) > 0).findFirst())); // get the first CCW curve
    }

    /**
     * Removes the intersecting area of other from this.
     * @param other
     * @return A collection of new regions formed by removing other from this.
     */
    public Optional<Collection<SplitArcBoundary>> less(SplitArcBoundary other) {
        Set<CircleArc2D> arcs = new HashSet<>();
        arcs.addAll(this.curves);


        Optional<SplitArcBoundary> ix = this.intersection(other);
        if(!ix.isPresent()) {
            return Optional.empty();
        }

        // Remove curves from the intersection that are contained in this curves
        // list.
        Collection<CircleArc2D> ixCurvesOnThis = ix.get().curves.stream().filter(x -> this.contains(x)).collect(Collectors.toSet());
        // ix's curves become those curves on other, not in this. 
        ix.get().curves().removeAll(ixCurvesOnThis);

        arcs.removeAll(ixCurvesOnThis);
        arcs.addAll(ix.get().curves());

        if(arcs.isEmpty()) {
            return Optional.empty();
        }

        List<SplitArcBoundary> boundaries = new Vector<>();
        do {
            SplitArcBoundary boundary = fromCollection(arcs, arcs.stream().findFirst());
            boundaries.add(boundary);

            // Essentially collection.removeAll, but we have to account for
            // edges that have changed direction.
            for(CircleArc2D arc: boundary.curves) {
                if(arcs.contains(arc)) {
                    arcs.remove(arc);
                } else {
                    // This doesn't work as we need to use almostEquals for
                    // double comparisons.
                    // arcs.remove(arc.reverse());
                    arcs = arcs.stream().filter(x -> ! x.almostEquals(arc.reverse(), Shape2D.ACCURACY)).collect(Collectors.toSet());
                }
            }
        } while (! arcs.isEmpty());

        return Optional.of(boundaries);
    }

    /**
     * Calculates the points of intersection of SplitArcBoundaries.
     * @param other
     * @return
     */
    protected Optional<Collection<Point2D>> intersectionPoints(SplitArcBoundary other) {
        Collection<Point2D> ixs = new HashSet<Point2D>();
        for(CircleArc2D a1 : this.curves) {
            for(CircleArc2D a2 : other.curves) {
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

    /**
     * A boundary contains an arc regardless of the orientation (CW/CCW) of that
     * arc.
     */
    public boolean contains(CircleArc2D arc) {
        return curves.contains(arc) || curves.contains(arc.reverse());
    }

    /**
     * Does this boundary contain the given arc either on the boundary or
     * within the interior.
     * @param arc
     * @return
     */
    public boolean bounds(CircleArc2D arc) {
        // Represent the arc as three points
        Point2D start = arc.firstPoint(), last = arc.lastPoint(), mid = Utils.midpoint(arc);

        // now check that each is within or on the boundary
        // now check that each is within or on the boundary
        boolean s = isInside(start) || contains(start);
        boolean m = isInside(mid)   || contains(mid);
        boolean l = isInside(last)  || contains(last);
        return s && m && l;
    }

    /**
     * Does this boundary contain the other boundary within in it.  The
     * definition of within, in this case, also includes being on the boundary.
     * @param other
     * @return
     */
    public boolean bounds(SplitArcBoundary other) {
        return other.curves.stream().allMatch(a -> this.bounds(a));
    }
    /**
     * Turns a collection of arcs that describe a contiguous boundary into a 
     * contiguous boundary.  This is not offered as a constructor as it's not
     * reasonable to "resplit" the Collection of CircleArc2D's every time we
     * with to construct a new boundary.
     * @param arcs The caller *must* guarantee that arcs describe a contiguous
     *             boundary.
     * @param start The first arc in the boundary that will be "stitched"
     *              together.  We need this as only the calling code knows which
     *              arcs are CCW - one of these needs to be the start arc.
     * @return
     */
    protected static SplitArcBoundary fromCollection(Collection<CircleArc2D> arcs, Optional<CircleArc2D> start) {
        SplitArcBoundary zone = new SplitArcBoundary();
        Collection<CircleArc2D> as = new Vector<>();
        as.addAll(arcs);

        zone.add(start.get());
        Optional<CircleArc2D> oarc = start;
        as.remove(oarc.get());

        final int size = as.size();
        for(int i=0; i< size; i++) {
            oarc = directionlessFind(as, zone.lastPoint());

            if(oarc.isPresent()) { // we have a *major* issue if this isn't true.
                if(oarc.get().firstPoint().almostEquals(zone.lastPoint(), Shape2D.ACCURACY)) {
                    zone.add(oarc.get());
                    as.remove(oarc.get());
                } else {
                    zone.add(oarc.get().reverse());
                    as.remove(oarc.get());
                }
            }
        }
        return zone;
    }

    /**
     * Finds an arc that starts or ends at mark.
     * @param boundary
     * @param mark
     * @return
     */
    protected static Optional<CircleArc2D> directionlessFind(Iterable<CircleArc2D> boundary, Point2D mark) {
        for(CircleArc2D arc: boundary) {
            if(arc.firstPoint().almostEquals(mark, Shape2D.ACCURACY) || arc.lastPoint().almostEquals(mark, Shape2D.ACCURACY)) {
                return Optional.of(arc);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the area of this SplitArcBoundary.
     * @param out The circles of which this boundary is outside.
     * @return
     */
    public double getArea(Collection<Circle2D> out) {
        if(this.isEmpty()) return 0;

        Polygon2D internal = new SimplePolygon2D();
        for(CircleArc2D arc: this) {
            internal.addVertex(arc.lastPoint());
        }

        double area = Math.abs(internal.area());
        for(CircleArc2D arc: this) {
            if(out.stream().noneMatch(c -> c.equals(arc.supportingCircle()))) {
                area += Math.abs(arc.getChordArea());
            } else {
                area -= Math.abs(arc.getChordArea());
            }
        }

        return area;
    }

    /**
     * Equals is more generic than the superclass equals, as we don't require
     * the curves to be in the same order.
     * @param o
     * @return
     */
    public boolean equals(Object o) {
        if(null == o) return false;
        
        if(! (o instanceof SplitArcBoundary)) return false;

        SplitArcBoundary other = (SplitArcBoundary) o;
        if(this.curves.size() != other.curves.size()) return false;

        for(CircleArc2D arc: this.curves) {
            if(!other.contains(arc)) return false;
        }

        for(CircleArc2D arc: other.curves) {
            if(!this.contains(arc)) return false;
        }

        return true;
    }

    public int hashCode() {
        return curves.hashCode() + (closed?0:1);
    }
}
