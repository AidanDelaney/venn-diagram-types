# Euler Diagram Types

This package calculates the almost exact area of regions within Euler diagrams that are drawn with circles.

# Usage

The best documentation on the usage of this library is to be found in the test classes.  You should look at these to figure out how to create a new diagram.

The following is a minimal example of programmatically creating a diagram and requesting the areas.  In the example `g` is an `org.eulerdiagrams.vennom.graph.Graph`.

```java
        AbstractContour ac = new AbstractContour("a");
        AbstractContour bc = new AbstractContour("b");
        Set<AbstractContour> contours = new HashSet<AbstractContour>(Arrays.asList(ac,bc));
        AbstractDiagram ad = new AbstractDiagram(contours);

        ConcreteDiagram cd = new ConcreteDiagram(g, ad);
        cd.getZoneAreaMap();
```

In the next example we don't use the external `vennom` package:

```java
        Circle2D circleA = new Circle2D(new Point2D(-5, 0), 7.0);
        Circle2D circleB = new Circle2D(new Point2D(5, 0), 7.0);
        ConcreteCircle ca = new ConcreteCircle(a, circleA);
        ConcreteCircle cb = new ConcreteCircle(b, circleB);
        AbstractDiagram ad = new AbstractDiagram(new HashSet<>(Arrays.asList(a, b)));
        ConcreteDiagram d = new ConcreteDiagram(ad, Arrays.asList(ca, cb));

        Map<AbstractZone, Double> areas = d.getZoneAreaMap();
```

# Limitations

  * Only deals with diagrams drawn with circles.
  * There are some edge cases that are not yet handled correctly.
  * Only works with pierced atomic diagrams.  This limitation will be fixed soon.

The above limitations are captured in two intentionally failing tests.  You should look at these for more detail.
