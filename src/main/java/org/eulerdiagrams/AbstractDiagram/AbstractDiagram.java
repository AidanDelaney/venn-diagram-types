package org.eulerdiagrams.AbstractDiagram;

import java.util.*;

/**
 * An AbstractDiagram is a set of zones defined over a fixed set of contours.
 *
 * We don't use org.eulerdiagrams.vennom.apCircles.AbstractDiagram as that is expected to be hugely refactored.
 *
 */
public class AbstractDiagram {
    private Set<AbstractContour> contours;
    private Map<AbstractZone, Double> zones = new HashMap<AbstractZone, Double>();

    public AbstractDiagram(Set<AbstractContour> contours) {
        this.contours = contours;
    }

    public boolean addZone(double weight, AbstractContour ... inset) {
        Set<AbstractContour> outzones = new HashSet<AbstractContour>(contours);

        for(AbstractContour c : inset) {
            // if inset contains a contour not in this diagram, then don't add
            // this zone to the diagram.
            if(!outzones.remove(c)) {
                return false;
            }
        }
        zones.put(new AbstractZone(new HashSet<AbstractContour>(Arrays.asList(inset)), outzones), weight);
        return true;
    }

    public final Set<AbstractContour> getContours() {
        return contours;
    }

    public final Map<AbstractZone, Double> getZones() {
        return zones;
    }
}
