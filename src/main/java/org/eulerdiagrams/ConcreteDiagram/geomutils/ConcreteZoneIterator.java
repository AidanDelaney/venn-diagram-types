package org.eulerdiagrams.ConcreteDiagram.geomutils;

import java.util.*;

import org.eulerdiagrams.AbstractDiagram.AbstractContour;
import org.eulerdiagrams.AbstractDiagram.AbstractZone;
import org.eulerdiagrams.ConcreteDiagram.ConcreteCircle;
import org.eulerdiagrams.utils.Pair;

import math.geom2d.conic.Circle2D;

public class ConcreteZoneIterator implements Iterable<Pair<AbstractZone, Optional<SplitArcBoundary>>>, Iterator<Pair<AbstractZone, Optional<SplitArcBoundary>>> {
    private int maxSize = 0;
    private List<ConcreteCircle> arr = new Vector<>();
    private BitSet bset = null;

    private Iterator<Pair<AbstractZone, Optional<SplitArcBoundary>>> zoneITerator;

    public ConcreteZoneIterator(Collection<ConcreteCircle> set) {
        this.maxSize = set.size();
        arr.addAll(set);
        bset = new BitSet(arr.size() + 1);
        incrementBitSet(); // don't start with all 0's as this creates the 
                           // "outside" zone.

        generateZoneList();
    }

    public void generateZoneList () {
        List<Pair<AbstractZone, Optional<SplitArcBoundary>>> zones = new Vector<>();

        // We now check to see if each combination of contours generates a zone.  If so, we add it to the zone list.
        while (!bset.get(arr.size())) {
            Pair<AbstractZone, Optional<SplitArcBoundary>> zone = getNextZone();
            if(zone.cdr.isPresent()) {
                zones.add(zone);
            }
            incrementBitSet();
        }
        zoneITerator = zones.iterator();
    }

    @Override
    public boolean hasNext() {
        return zoneITerator.hasNext();
    }

    public Pair<AbstractZone, Optional<SplitArcBoundary>> next() {
        return zoneITerator.next();
    }

    public Pair<AbstractZone, Optional<SplitArcBoundary>> getNextZone() {
        Set<Circle2D> inset = new HashSet<>();
        Set<AbstractContour> inLabels = new HashSet<>();
        Set<Circle2D> outset = new HashSet<>();
        Set<AbstractContour> outLabels = new HashSet<>();

        for (int i = 0; i < arr.size(); i++) {
            if (bset.get(i)) {
                inset.add(arr.get(i).getCircle());
                inLabels.add(arr.get(i).getContour());
            } else {
                outset.add(arr.get(i).getCircle());
                outLabels.add(arr.get(i).getContour());
            }
        }

        // if any incircle is contained within an outcircle, then this zone doesn't exist
        // An incircle is contained within an outcircle if for any outciricle the distince between in's centre and out's
        // circle plus the radius of in, is greater than the radius of out.
        boolean contained = outset.stream().anyMatch(out -> inset.stream().anyMatch(in -> out.center().distance(in.center()) + in.radius() < out.radius()));
        if(contained) {
            return new Pair<>(new AbstractZone(inLabels, outLabels), Optional.empty());
        }

        Optional<SplitArcBoundary> boundary;
        try {
            boundary = Optional.of(new SplitArcBoundary(inset, outset));
        } catch (IllegalArgumentException iae) {
            boundary = Optional.empty();
        }
        return new Pair<AbstractZone, Optional<SplitArcBoundary>>(new AbstractZone(inLabels, outLabels), boundary);
    }

    protected void incrementBitSet() {
        for (int i = 0; i < bset.size(); i++) {
            if (!bset.get(i)) {
                bset.set(i);
                break;
            } else
                bset.clear(i);
        }
    }

    protected int countBitSet() {
        int count = 0;
        for (int i = 0; i < bset.size(); i++) {
            if (bset.get(i)) {
                count++;
            }
        }
        return count;

    }

    protected String printBitSet() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < bset.size(); i++) {
            if (bset.get(i)) {
                builder.append('1');
            } else {
                builder.append('0');
            }
        }
        return builder.toString();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not Supported!");
    }

    @Override
    public Iterator<Pair<AbstractZone, Optional<SplitArcBoundary>>> iterator() {
        return this;
    }
}
