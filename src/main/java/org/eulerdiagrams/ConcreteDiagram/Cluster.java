package org.eulerdiagrams.ConcreteDiagram;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import org.eulerdiagrams.AbstractDiagram.AbstractContour;
import org.eulerdiagrams.AbstractDiagram.AbstractZone;
import org.eulerdiagrams.ConcreteDiagram.geomutils.SplitArcBoundary;
import org.eulerdiagrams.utils.Pair;

import math.geom2d.conic.Circle2D;

public class Cluster implements Comparable<Cluster> {

    private Map<AbstractContour, ConcreteCircle> circles;
    //private Vector<ConcreteCircle> circles;

    public static final class Top extends Cluster {
        private static Top instance = new Top();

        private Top() {
            super(new Vector<>());
        }

        public static Top getInstance() {
            return instance;
        }

        public boolean bounds(Cluster other) {
            return true;
        }

        public int compareTo(Cluster other) {
            return -1;
        }

        @Override
        public String toString() {
            return "top";
        }
    }

    public Cluster(Collection<ConcreteCircle> circles) {
        this.circles = new HashMap<>();
        for(ConcreteCircle c: circles) {
            this.circles.put(c.getContour(), c);
        }
    }

    /** 
     * If this cluster contains a concrete zone describable by the AbstractZone
     * then return both the boundary of the concrete zone and the circles (of
     * this cluster) which the zone is outside.
     * 
     * This __is__ pretty nasty looking.  However, it can then be used by other
     * methods that want to do something with the boundary of the concrete zone.
     * 
     * @param zone
     * @return
     */
    public Optional<Pair<SplitArcBoundary, Collection<Circle2D>>> contains(AbstractZone zone) {
        // If all the inzones are not in this cluster, then the area is 0.0.
        Vector<ConcreteCircle> inz = new Vector<>();
        for(AbstractContour ac: zone.getInContours()) {
            ConcreteCircle cc = circles.get(ac);
            if(null == cc) {
                return Optional.empty();
            } else {
                inz.add(cc);
            }
        }

        // Circles not in this cluster are automatically outside, so we don't
        // have to worry about them.
        Vector<ConcreteCircle> outz = new Vector<>();
        for(AbstractContour ac: zone.getOutContours()) {
            ConcreteCircle cc = circles.get(ac);
            if(null != cc) {
                outz.add(cc);
            }
        }

        SplitArcBoundary sab = inz.get(0).getBoundary();
        inz.remove(0);
        for(ConcreteCircle cc : inz) {
            Optional<SplitArcBoundary> osab = sab.intersection(cc.getBoundary());
            if(osab.isPresent()) {
                sab = osab.get();
            }
        }

        for(ConcreteCircle cc: outz) {
            Optional<Collection<SplitArcBoundary>> osabs = sab.less(cc.getBoundary());
            // FIXME: the findFirst below makes the assumption that the `less`
            //        operation doesn't split a zone.
            if(osabs.isPresent()) {
                sab = osabs.get().stream().findFirst().get();
            }
        }
        // Return the area of <in, subset(out)>
        Set<Circle2D> outCircles = outz.stream().map(ConcreteCircle::getCircle).collect(Collectors.toSet());
        return Optional.of(new Pair<>(sab, outCircles));
    }

    /**
     * Get the area of a specific zone in a cluster.  This does not recurse down
     * into clusters contained within the zone.
     * @param zone
     * @return
     */
    public double getArea(AbstractZone zone) {
        if(circles.isEmpty() || zone.getInContours().isEmpty()) {
            return Double.POSITIVE_INFINITY; // Top cluster.
        }
        Optional<Pair<SplitArcBoundary, Collection<Circle2D>>> op = contains(zone);

        if(!op.isPresent()) {
            return 0.0;
        }

        return op.get().car.getArea(op.get().cdr);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((circles == null) ? 0 : circles.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Cluster)) {
            return false;
        }
        Cluster other = (Cluster) obj;
        if (circles == null) {
            if (other.circles != null) {
                return false;
            }
        } else if (!circles.equals(other.circles)) {
            return false;
        }
        return true;
    }

    public boolean add (ConcreteCircle circle) {
        boolean addable = circles.values().stream().anyMatch(c -> c.intersects(circle));
        if(! addable) {
            return false;
        }
        circles.put(circle.getContour(), circle);
        return true;
    }

    /**
     * The hull of a Cluster is the external boundary.  We know that all circles
     * in a cluster intersect.  So we need to iterate through all circles 
     * consuming them until either the remaining circles are contained in the
     * boundary or can be unioned with the boundary.  This process is guaranteed
     * to terminate, but the upper-bound is not fun.
     * @return
     */
    public SplitArcBoundary getHull() {
        SplitArcBoundary hull = new SplitArcBoundary();
        Collection<ConcreteCircle> cs = circles.values();

        while(!cs.isEmpty()) {
            //
            // check to see if the only circles left are those contained in the
            // hull.
            //
            // yup, a hack, but I don't think anything bad will come of this.
            final SplitArcBoundary h = hull;
            if(cs.stream().allMatch(c -> h.bounds(c.getBoundary()))) {
                break;
            }

            for(ConcreteCircle c: circles.values()) {
                Optional<SplitArcBoundary> u = hull.union(c.getBoundary());
                if(u.isPresent()) {
                    cs.remove(c);
                    hull = u.get();
                }
            }
        }
        return hull;
    }

    public boolean bounds(Cluster other) {
        return this.getHull().bounds(other.getHull());
    }

    @Override
    public int compareTo(Cluster other) {
        if(this.bounds(other)) {
            return -1;
        }
        if(other.bounds(this)) {
            return 1;
        }
        return 0;
    }
}
