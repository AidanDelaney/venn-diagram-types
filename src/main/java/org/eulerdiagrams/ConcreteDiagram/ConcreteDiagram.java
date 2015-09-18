package org.eulerdiagrams.ConcreteDiagram;

import java.util.*;
import java.util.List;

import org.eulerdiagrams.AbstractDiagram.*;
import org.eulerdiagrams.utils.NAryTree;
import org.eulerdiagrams.ConcreteDiagram.geomutils.Pair;
import org.eulerdiagrams.vennom.graph.Graph;
import org.eulerdiagrams.vennom.graph.Node;

import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;

public class ConcreteDiagram {

    private AbstractDiagram abstractDiagram;
    private Vector<Cluster> bins; 

    private NAryTree<Cluster> containment;

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
            circles.add(new ConcreteCircle(p.car, p.cdr, others));
        }

        generateClusters(circles);
    }

    public ConcreteDiagram(AbstractDiagram ad, List<ConcreteCircle> circles) {
        abstractDiagram = ad;
        generateClusters(circles);
    }

    /* package */ void generateClusters(List<ConcreteCircle> circles) {
        bins = new Vector<>();

        for(ConcreteCircle c: circles) {
            boolean added = bins.stream().anyMatch(b -> b.add(c));

            if(!added) {
                bins.add(new Cluster(Arrays.asList(c)));
            }
        }

        generateContainmentHeirarchy();
    }

    /* package */ void generateContainmentHeirarchy() {
        // Add the top level
        containment = new NAryTree<>(Cluster.Top.getInstance());
        
        for(Cluster c : bins) {
            containment.insert(c);
        }
    }

    public Map<AbstractZone, Double> getZoneAreaMap () {
        Map<AbstractZone, Double> areas = new HashMap<>();

        for(AbstractZone az: new VennSetIterator(abstractDiagram.getContours())) {
            //ConcreteZone c = new ConcreteZone(az.getInContours(), az.getOutContours(), circles);
            //areas.put(az, c.getArea());
            double area = 0.0;
            for(Cluster c : bins) {
                area += c.getArea(az);
                System.out.println(az.toString() + " " + c.getArea(az));
            }
            areas.put(az, area);
        }
        return areas;
    }
}
