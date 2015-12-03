package org.eulerdiagrams.ConcreteDiagram.geomutils;

import math.geom2d.conic.Circle2D;
import math.geom2d.conic.CircleArc2D;
import math.geom2d.domain.BoundaryPolyCurve2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.SimplePolygon2D;

import org.eulerdiagrams.utils.Pair;

import math.geom2d.Point2D;
import math.geom2d.Shape2D;
import math.geom2d.Vector2D;

import java.util.*;
import java.util.stream.Collectors;

public class Utils {
    public static class CCWPointComparitor implements Comparator<Pair<Point2D, ?>> {
        private Point2D centroid;
        public CCWPointComparitor(Point2D centroid) {
            this.centroid = centroid;
        }

        public int compare(Pair<Point2D, ?> p1, Pair<Point2D, ?> p2) {
            double angle1 = Math.atan2(p1.car.y() - centroid.y(), p1.car.x() - centroid.x());
            double angle2 = Math.atan2(p2.car.y() - centroid.y(), p2.car.x() - centroid.x());

            if(angle1 < angle2) return 1;
            else if (angle2 > angle1) return -1;
            return 0;
        }

    }

    public static class CCWCircleArc2DComparitor implements Comparator<CircleArc2D> {
        private Point2D centroid;
        public CCWCircleArc2DComparitor(Point2D centroid) {
            this.centroid = centroid;
        }

        public int compare(CircleArc2D ca1, CircleArc2D ca2) {
            Vector2D v1 = new Vector2D(centroid, midpoint(ca1));
            Vector2D v2 = new Vector2D(centroid, midpoint(ca2));
            Vector2D xAxis = new Vector2D(new Point2D(0,0), new Point2D(1, 0));

            double angle1 = Math.acos(xAxis.dot(v1));
            double angle2 = Math.acos(xAxis.dot(v2));

            if(angle1 < angle2) return 1;
            else if (angle2 > angle1) return -1;
            return 0;
        }
    }

    protected static Point2D midpoint(CircleArc2D arc) {
        double angle = arc.getAngleExtent() / 2.0;
        return arc.point(angle);
    }
}
