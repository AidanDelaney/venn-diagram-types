package org.eulerdiagrams.AbstractDiagram;

/**
 * Contours represent sets in Euler and Venn diagrams.  They differ by having
 * different labels.  We use this implementation, rather than "raw" strings as
 * it makes the API cleaner and less confusing.
 */
public class AbstractContour {
    private String label;
    public AbstractContour(String s) {
        this.label = s;
    }

    public final String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        return o.equals(label);
    }

    @Override
    public int hashCode() {
        return label.hashCode();
    }
}
