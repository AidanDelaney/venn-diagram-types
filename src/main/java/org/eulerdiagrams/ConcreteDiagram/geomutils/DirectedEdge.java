package org.eulerdiagrams.ConcreteDiagram.geomutils;

import org.eulerdiagrams.ConcreteDiagram.ConcreteZone;

import java.io.Serializable;

public class DirectedEdge implements Cloneable, Serializable {
    public ConcreteZone source, target;
    public DirectedEdge() {
    }

    public DirectedEdge(ConcreteZone source, ConcreteZone target) {
        this.source = source;
        this.target = target;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DirectedEdge)) {
            return false;
        }

        DirectedEdge other = (DirectedEdge) obj;

        boolean sourceE = false;
        if(null == source) {
            sourceE = (null == other.source);
        } else {
            sourceE = source.equals(other.source);
        }

        boolean targetE = false;
        if(null == target) {
            targetE = (null == other.target);
        } else {
            targetE = target.equals(other.target);
        }
        return sourceE && targetE;
    }

    @Override public int hashCode() {
        int hash = 1;
        hash = hash *  17 + (null == source ? 0: source.hashCode());
        hash = hash * 31 + (null == target ? 0 : target.hashCode());
        return hash;
    }

    @Override public String toString() {
        StringBuilder b = new StringBuilder();
        if(null == source) {
            b.append("null");
        } else {
            b.append(source.toString());
        }
        b.append(" -> ");
        if(null == target) {
            b.append("null");
        } else {
            b.append(target.toString());
        }

        return b.toString();
    }
}