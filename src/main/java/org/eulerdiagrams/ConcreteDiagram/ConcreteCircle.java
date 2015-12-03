package org.eulerdiagrams.ConcreteDiagram;

import java.util.Collection;
import java.util.Optional;

import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;
import org.eulerdiagrams.AbstractDiagram.AbstractContour;
import org.eulerdiagrams.ConcreteDiagram.geomutils.SplitArcBoundary;

public class ConcreteCircle {
    private AbstractContour contour;
    private Circle2D oCircle;

    public ConcreteCircle(AbstractContour c, Circle2D circle) {
        this.contour = c;
        this.oCircle = circle;
    }

    public AbstractContour getContour() {
        return contour;
    }

    public Circle2D getCircle() {
        return oCircle;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((contour == null) ? 0 : contour.hashCode());
        result = prime * result + ((oCircle == null) ? 0 : oCircle.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConcreteCircle other = (ConcreteCircle) obj;
        if (contour == null) {
            if (other.contour != null)
                return false;
        } else if (!contour.equals(other.contour))
            return false;
        if (oCircle == null) {
            if (other.oCircle != null)
                return false;
        } else if (!oCircle.equals(other.oCircle))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return (null == contour) ? "null" : contour.toString();
    }
}
