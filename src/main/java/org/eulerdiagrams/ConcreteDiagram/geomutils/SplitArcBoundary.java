package org.eulerdiagrams.ConcreteDiagram.geomutils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import org.eulerdiagrams.utils.Pair;

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
 * non-intersecting.  This has repercussions, for example: When you take
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

    private Collection<Circle2D> outside = new HashSet<>();

    /**
     * Creates a non-empty boundary.  The boundary consists of circle and is
     * split at each intersection point with others.  It is vitally important
     * that all other possible intersection circles are passed into the
     * constructor.
     * @param circle The boundary that we intend to create.
     * @param others All circles that circle could possibly intersect with.
     */
    public SplitArcBoundary(Collection<Circle2D> inside, Collection<Circle2D> outside) throws IllegalArgumentException{
        super();
        this.outside = outside;

        if(inside.isEmpty()) {
            throw new IllegalArgumentException();
        }

        Collection<SplitArcBoundary> ins = splitBoundaries(inside, outside);

        // Get one of the boundaries in in and add all it's curves to this
        SplitArcBoundary first = ins.stream().findAny().get(); // we know there's at least one.
        first.curves.forEach(c -> add(c));

        // For all elements of `ins` that are not `first`
        for(SplitArcBoundary other: ins.stream().filter(c -> !c.equals(first)).collect(Collectors.toSet())) {
            Optional<SplitArcBoundary> sab = this.intersection(other);

            if(sab.isPresent()) {  // if it intersects
                this.curves = sab.get().curves;
            } else if (!other.bounds(this)) {  // or if it's not contained
                throw new IllegalArgumentException("This SplitArcBoundary neither intersects with nor is contained by " + other);
            }
        }

        for(Circle2D other : outside) {
            Optional<SplitArcBoundary> sab = this.less(other);
            if(sab.isPresent()) {
                this.curves = sab.get().curves;
            }
        }
    }

    /* package */ static Collection<SplitArcBoundary> splitBoundaries(Collection<Circle2D> inside, Collection<Circle2D> outside) {
        Set<SplitArcBoundary> sabs = new HashSet<>();

        Set<SplitArcBoundary> inputs = inside.stream().map(c -> new SplitArcBoundary() {{add(new CircleArc2D(c, 0, Math.PI * 2));}}).collect(Collectors.toSet());
        inputs.forEach(i -> i.outside = outside);

        // There won't be any intersections if there are 0 or 1 input circles
        if(inputs.size() < 2) {
            return inputs;
        }

        // For each circle, split it at each intersection point with another circle
        for(SplitArcBoundary s1: inputs) {
            for(SplitArcBoundary s2: inputs) {
                if(s1.equals(s2)) continue;

                Collection<Point2D> ps = s1.intersectionPoints(s2);
                ps.forEach(p -> s1.split(p));
                sabs.add(s1);
            }
        }
        return sabs;
    }

    /**
     * Creates an empty Boundary.
     */
    private SplitArcBoundary () {
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

        // The intersection of this and other is outside the union of all circles
        // both are outside.
        Collection<Circle2D> outs = new HashSet<>();
        outs.addAll(outside);
        outs.addAll(other.outside);

        return Optional.of(fromCollection(arcs, arcs.stream().filter(x -> x.curvature(0) > 0).findFirst(), outs)); // get the first CCW curve
    }

    /**
     * Removes the intersecting area of other from this.  FIXME: Does not
     * produce a set of boundaries where the circle splits an existing boundary.
     * @param other
     * @return A collection of new regions formed by removing other from this.
     */
    public Optional<SplitArcBoundary> less(Circle2D other) {
        Set<CircleArc2D> arcs = new HashSet<>();
        arcs.addAll(this.curves);


        Set<Point2D> ixs = arcs.stream().flatMap(a -> a.intersections(new CircleArc2D(other, 0.0, Math.PI * 2)).stream()).collect(Collectors.toSet());

        // if no intersecitons, return this
        switch(ixs.size()) {
        case 0: return Optional.of(this); // not connected in any way
        case 1: return Optional.of(this); // tangentally touching
        case 2: break;// what we want
        default: return Optional.empty(); // case where this is split into more than two zones by other
        }

        // Make sure that the intersection points are in our "Euler graph"
        ixs.forEach(p -> this.split(p));

        SplitArcBoundary osab = new SplitArcBoundary() {{ add(new CircleArc2D(other, 0, Math.PI * 2.0)); }};
        ixs.forEach(p -> osab.split(p));

        // remove the arcs of this that are contained in other
        arcs = this.curves.stream().filter(a -> !osab.bounds(a)).collect(Collectors.toSet());

        // Pick one of these
        Optional<CircleArc2D> first = arcs.stream().findAny();

        // add the intersection arc -- Note: there can be only one.
        arcs.addAll(osab.curves.stream().filter(a -> this.bounds(a)).collect(Collectors.toSet()));

        // The `less` of this and other is outside whatever this is outside and other
        // both are outside.
        Collection<Circle2D> outs = new HashSet<>();
        outs.addAll(outside);
        outs.add(other);
        SplitArcBoundary boundary = SplitArcBoundary.fromCollection(arcs, first, outs);
        return Optional.of(boundary);
    }

    /**
     * Calculates the points of intersection of SplitArcBoundaries.
     * @param other
     * @return
     */
    protected Collection<Point2D> intersectionPoints(SplitArcBoundary other) {
        Collection<Point2D> ixs = new HashSet<Point2D>();
        for(CircleArc2D a1 : this.curves) {
            for(CircleArc2D a2 : other.curves) {
                Optional<Collection<Point2D>> is = nonTangentalIntersections(a1, a2);
                if(is.isPresent()) {
                    ixs.addAll(is.get());
                }
            }
        }

        return ixs;
    }

    public static Optional<Collection<Point2D>> nonTangentalIntersections (CircleArc2D c, CircleArc2D ca) {
        Collection<Point2D> ixs = c.intersections(ca);
        if(ixs.size() >= 2) {
            return Optional.of(ixs);
        }
        return Optional.empty();
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
     * Is this circle containd within the boundary.
     * @return
     */
    public boolean bounds(Circle2D circle) {
        for(CircleArc2D arc: this.curves) {
            // ensure that the distance between the arc midpoint and arc end point to the centre of the circle is
            // greater than the radius.
            if((circle.center().distance(arc.lastPoint()) <= circle.radius()) || circle.center().distance(Utils.midpoint(arc)) <= circle.radius()) {
                return false;
            }
        }
        return true;
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
    protected static SplitArcBoundary fromCollection(Collection<CircleArc2D> arcs, Optional<CircleArc2D> start, Collection<Circle2D> outside) {
        SplitArcBoundary zone = new SplitArcBoundary();
        zone.outside = outside;
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
     * Returns the area of this SplitArcBoundary.  We don't use the curvature of
     * the arc to compute whether the arc chord should be added to or subtracted
     * from the internal area.  Instead we go with the more brain-dead solution
     * of passing in those circles that this Boundary is outside.
     * @param out The circles of which this boundary is outside.
     * @return
     */
    public double getArea() {
        if(this.isEmpty()) return 0.0;

        // if only one arc, then PI* r^2
        if(1  == size()) {
            return Math.PI * Math.pow(get(0).supportingCircle().radius(), 2.0);
        }

        // if only two arcs, then  don't compute the internal polygon (there is
        // none.
        double area = 0.0;

        if(size() > 2) {
            Polygon2D internal = new SimplePolygon2D();
            for(CircleArc2D arc: this) {
                internal.addVertex(arc.lastPoint());
            }

            area = Math.abs(internal.area());
        }

        for(CircleArc2D arc: this) {
            Circle2D sc = arc.supportingCircle();

            if(outside.stream().noneMatch(c -> c.equals(arc.supportingCircle()))) {
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

        if(o == this) return true; // reference equality

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
