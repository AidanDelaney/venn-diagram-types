package org.eulerdiagrams.AbstractDiagram;

import java.util.Set;

/**
 * A Zone is modelled as a pair of contour sets.  The disjoint union of this
 * pair must be the contour set of the AbstractDiagram to which the zone
 * belongs.
 */
public class AbstractZone {
    private Set<AbstractContour> in, out;

    public AbstractZone (Set<AbstractContour> inset, Set<AbstractContour> outset) {
        this.in = inset;
        this.out = outset;
    }

    public final Set<AbstractContour> getInContours() {
        return in;
    }

    public final Set<AbstractContour> getOutContours() {
        return out;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((in == null) ? 0 : in.hashCode());
        result = prime * result + ((out == null) ? 0 : out.hashCode());
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
        AbstractZone other = (AbstractZone) obj;
        if (in == null) {
            if (other.in != null)
                return false;
        } else if (!in.equals(other.in))
            return false;
        if (out == null) {
            if (other.out != null)
                return false;
        } else if (!out.equals(other.out))
            return false;
        return true;
    }

    public String toString() {
        return "(" + in.toString() + ", " + out.toString() + ")";
    }
}
