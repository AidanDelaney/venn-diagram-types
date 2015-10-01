package org.eulerdiagrams.AbstractDiagram;

import java.util.*;

import org.eulerdiagrams.AbstractDiagram.AbstractContour;
import org.eulerdiagrams.AbstractDiagram.AbstractZone;

public class VennSetIterator implements Iterable<AbstractZone>, Iterator<AbstractZone> {
    private int maxSize = 0;
    private List<AbstractContour> arr = new Vector<>();
    private BitSet bset = null;

    public VennSetIterator(Collection<AbstractContour> set) {
        this.maxSize = set.size();
        arr.addAll(set);
        bset = new BitSet(arr.size() + 1);
    }

    @Override
    public boolean hasNext() {
        return !bset.get(arr.size());
    }

    @Override
    public AbstractZone next() {
        Set<AbstractContour> inset = new HashSet<>();
        Set<AbstractContour> outset = new HashSet<>();

        for (int i = 0; i < arr.size(); i++) {
            if (bset.get(i)) {
                inset.add(arr.get(i));
            } else {
                outset.add(arr.get(i));
            }
        }

        incrementBitSet();

        return new AbstractZone(inset, outset);
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
    public Iterator<AbstractZone> iterator() {
        return this;
    }
}
