package org.eulerdiagrams.AbstractDiagram;

import java.util.*;

/**
 * A WeigthedAbstractDiagram is a set of zone/weight pairs defined over a fixed 
 * set of contours.
 *
 */
public class WeightedAbstractDiagram extends AbstractDiagram {
    private Map<AbstractZone, Double> w_zones = new HashMap<AbstractZone, Double>();

    public WeightedAbstractDiagram(Set<AbstractContour> contours) {
        super(contours);
    }

    @Override
    public boolean addZone(AbstractContour ... inset) {
        return addZone(1.0, inset);
    }

    @Override
    public Set<AbstractZone> getZones() {
        return w_zones.keySet();
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
        w_zones.put(new AbstractZone(new HashSet<AbstractContour>(Arrays.asList(inset)), outzones), weight);
        return true;
    }


    public final Map<AbstractZone, Double> getWeightedZones() {
        return w_zones;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("(");
        sb.append(contours.toString());
        sb.append(", {");
        for(Iterator<AbstractZone> i = getZones().iterator(); i.hasNext();) {
            AbstractZone z = i.next();
            sb.append("(" + z.toString() + ", " + w_zones.get(z) + ")");
            if(i.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append("})");
        return sb.toString();
    }
}
