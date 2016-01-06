package org.eulerdiagrams.ConcreteDiagram;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import math.geom2d.conic.CircleArc2D;
import org.eulerdiagrams.AbstractDiagram.*;
import org.eulerdiagrams.ConcreteDiagram.geomutils.ConcreteZoneIterator;
import org.eulerdiagrams.ConcreteDiagram.geomutils.SplitArcBoundary;
import org.eulerdiagrams.utils.Pair;
import org.eulerdiagrams.vennom.graph.Graph;
import org.eulerdiagrams.vennom.graph.Node;

import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;

public class ConcreteDiagram {

    private AbstractDiagram abstractDiagram;

    private Map<AbstractZone, Collection<SplitArcBoundary>> zoneMap = new HashMap<>();
    private Map<SplitArcBoundary, Collection<SplitArcBoundary>> children = new HashMap<>();

    public ConcreteDiagram(Graph g, AbstractDiagram ad) {
        abstractDiagram = ad;
        // All containment trees are rooted in ConcreteZone::Top
        List<ConcreteCircle> circles = new Vector<>();

        List<Pair<AbstractContour, Circle2D>> cs = new Vector<>();
        // Foreach node, create a ConcreteCircle
        for (Node n : g.getNodes()) {
            // For some reason, the centres of circles are in integer precision
            Point2D centre = new Point2D(n.getCentre().getX(), n.getCentre().getY());
            // For some reason the radius is stored in a String!
            // Let NullPointer and NumberFormat exceptions propogate upwards.
            double radius = Double.parseDouble(n.getLabel());
            Optional<AbstractContour> oac = ad.findContourByLabel(n.getContour());

            // Ensure we have valid data
            if (oac.isPresent()) {
                cs.add(new Pair<>(oac.get(), new Circle2D(centre, radius)));
            } else {
                throw new IllegalArgumentException("Graph labels and AbstractDiagram contour lables do not match.");
            }
        }

        // This is a fairly expensive way to create Circles, but it makes sense
        // when it simplifies computing boolean operations over circles.
        for(Pair<AbstractContour, Circle2D> p: cs) {
            Collection<Circle2D> others = new Vector<>();
            cs.forEach(x -> others.add(x.cdr));
            others.remove(p.cdr);
            circles.add(new ConcreteCircle(p.car, p.cdr));
        }

        generateClusters(circles);
    }

    public ConcreteDiagram(AbstractDiagram ad, List<ConcreteCircle> circles) {
        abstractDiagram = ad;
        generateClusters(circles);
    }

    private void generateClusters(List<ConcreteCircle> circles) {
        // Generate the Venn set of the input circles.
        ConcreteZoneIterator czvsi = new ConcreteZoneIterator(circles);
        for(Pair<AbstractZone, Optional<SplitArcBoundary>> p : czvsi) {
            if(p.cdr.isPresent()) {
                insert(p.cdr.get());
                if(zoneMap.containsKey(p.car)) {
                    zoneMap.get(p.car).add(p.cdr.get());
                } else {
                    zoneMap.put(p.car, new HashSet<SplitArcBoundary>(){{add(p.cdr.get());}});
                }
            }
        }
    }

    /**
     * Is this SplitArcBoundary a "hole" in a previously found SplitArcBoundary?  If another sab contains each of the
     * underlying circles that form `sab` then `sab` is a hole in that boundary.
     * @param sab
     */
    public void insert(SplitArcBoundary sab) {
        for(SplitArcBoundary b: this.zoneMap.values().stream().flatMap(x -> x.stream()).collect(Collectors.toSet())) {
            // SplitArcBoundary doesn't support .stream(), so old-school setting of boolean flag and looping
            boolean flag = true;
            for(CircleArc2D c: sab) {
                if(!b.bounds(c.supportingCircle())) {
                    flag = false;
                }
            }
            if(flag) { // sab is a hole of b
                if(children.containsKey(b)) {
                    children.get(b).add(sab);
                } else {
                    children.put(b, new HashSet<SplitArcBoundary>(){{add(sab);}});
                }
            }
        }
    }

    public Map<AbstractZone, Double> getZoneAreaMap () {
        Map<AbstractZone, Double> areas = new HashMap<>();

        for(AbstractZone z: zoneMap.keySet()) {
            Collection<SplitArcBoundary> regions = zoneMap.get(z);
            double regionalArea = regions.stream().mapToDouble(SplitArcBoundary::getArea).sum();
            double holeArea = 0.0;

            // Get all the children of regions
            for(SplitArcBoundary r : regions) {
                if(children.containsKey(r)) {
                    holeArea += children.get(r).stream().mapToDouble(SplitArcBoundary::getArea).sum();
                }
            }
            areas.put(z, regionalArea - holeArea);
        }
        return areas;
    }
}
