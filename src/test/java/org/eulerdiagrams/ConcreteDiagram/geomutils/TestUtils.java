package org.eulerdiagrams.ConcreteDiagram.geomutils;

import math.geom2d.Point2D;
import math.geom2d.Shape2D;
import math.geom2d.conic.Circle2D;
import math.geom2d.conic.CircleArc2D;
import math.geom2d.domain.BoundaryPolyCurve2D;
import org.junit.Test;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;

import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class TestUtils {
    
    public class SVGWriter {
        private SVGGraphics2D svgGenerator = null;
        private String filename;
        public SVGWriter(String filename) {
            this.filename = filename;
            // Get a DOMImplementation
            DOMImplementation domImpl =
                GenericDOMImplementation.getDOMImplementation();
            String svgNamespaceURI = "http://www.w3.org/2000/svg";

            // Create an instance of org.w3c.dom.Document
            Document document = domImpl.createDocument(svgNamespaceURI, "svg", null);

            // Create an instance of the SVG Generator
             svgGenerator = new SVGGraphics2D(document);
        }

        public void writeSVG() {
            try {
                svgGenerator.stream(filename);
            } catch (Exception e) {
                // Do nothing
                e.printStackTrace();
                fail();
            }
        }

        public Graphics2D getGraphics() {
            return svgGenerator;
        }
    }
}
