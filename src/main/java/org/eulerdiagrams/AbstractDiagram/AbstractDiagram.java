package org.eulerdiagrams.AbstractDiagram;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AbstractDiagram {
    protected Set<AbstractContour> contours;
    private Set<AbstractZone> zones = new HashSet<AbstractZone>();

    public AbstractDiagram(Set<AbstractContour> contours) {
        this.contours = contours;
    }

    public boolean addZone(AbstractContour ... inset) {
        Set<AbstractContour> outzones = new HashSet<AbstractContour>(contours);

        for(AbstractContour c : inset) {
            // if inset contains a contour not in this diagram, then don't add
            // this zone to the diagram.
            if(!outzones.remove(c)) {
                return false;
            }
        }
        zones.add(new AbstractZone(new HashSet<AbstractContour>(Arrays.asList(inset)), outzones));
        return true;
    }

    public final Set<AbstractContour> getContours() {
        return contours;
    }

    public Set<AbstractZone> getZones() {
        return zones;
    }
}
