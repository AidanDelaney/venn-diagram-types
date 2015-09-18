package org.eulerdiagrams.ConcreteDiagram;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import org.eulerdiagrams.AbstractDiagram.AbstractContour;
import org.eulerdiagrams.AbstractDiagram.AbstractZone;
import org.eulerdiagrams.ConcreteDiagram.geomutils.SplitArcBoundary;

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

    public double getArea(AbstractZone zone) {
        if(circles.isEmpty() || zone.getInContours().isEmpty()) {
            return Double.POSITIVE_INFINITY; // Top cluster.
        }

        // If all the inzones are not in this cluster, then the area is 0.0.
        Vector<ConcreteCircle> inz = new Vector<>();
        for(AbstractContour ac: zone.getInContours()) {
            ConcreteCircle cc = circles.get(ac);
            if(null == cc) {
                return 0.0;
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
        // Return the area of <in, subset(out)>
        Set<Circle2D> outCircles = outz.stream().map(ConcreteCircle::getCircle).collect(Collectors.toSet()); 
        return sab.getArea(outCircles);
    }

    public boolean add (ConcreteCircle circle) {
        boolean addable = circles.values().stream().anyMatch(c -> c.intersects(circle));
        if(! addable) {
            return false;
        }
        circles.put(circle.getContour(), circle);
        return true;
    }

    public SplitArcBoundary getHull() {
        SplitArcBoundary hull = new SplitArcBoundary();
        for(ConcreteCircle c: circles.values()) {
            Optional<SplitArcBoundary> u = hull.union(c.getBoundary());
            if(u.isPresent()) {
                hull = u.get();
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
