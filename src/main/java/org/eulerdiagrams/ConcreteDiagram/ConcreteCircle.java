package org.eulerdiagrams.ConcreteDiagram;

import java.util.Collection;
import java.util.Optional;

import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;
import org.eulerdiagrams.AbstractDiagram.AbstractContour;
import org.eulerdiagrams.ConcreteDiagram.geomutils.SplitArcBoundary;

public class ConcreteCircle {
    private AbstractContour contour;
    private SplitArcBoundary circle;
    private Circle2D oCircle;

    public ConcreteCircle(AbstractContour c, Circle2D circle, Collection<Circle2D> others) {
        this.contour = c;
        this.circle = new SplitArcBoundary(circle, others);
        this.oCircle = circle;
    }

    public SplitArcBoundary getBoundary() {
        return circle;
    }

    public AbstractContour getContour() {
        return contour;
    }

    public Circle2D getCircle() {
        return oCircle;
    }

    public boolean intersects(ConcreteCircle other) {
        Optional<SplitArcBoundary> ixs = circle.intersection(other.circle);
        
        return ixs.isPresent();
    }
    @Override
    public boolean equals(Object o) {
        if(null == o) {
            return false;
        }

        if(! (o instanceof ConcreteCircle)) return false;
        ConcreteCircle other = (ConcreteCircle) o;

        // Internal contours *could* be null
        if((null == contour  && null != other.contour) ||
                (null != contour  && null == other.contour) ) {
            return false;
        }

        if(null == contour) {
            return circle.equals(other.circle);
        }
        return contour.equals(other.contour) && circle.equals(other.circle);
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash *  17 + ((null == contour) ? 0 : contour.hashCode());
        hash = hash * 31 + ((null == circle) ? 0 : circle.hashCode());
        return hash;
    }

    @Override
    public String toString() {
        return (null == contour) ? "null" : contour.toString();
    }
}
