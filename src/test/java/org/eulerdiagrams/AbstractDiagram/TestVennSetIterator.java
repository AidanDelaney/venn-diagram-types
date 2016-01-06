package org.eulerdiagrams.AbstractDiagram;

import static org.junit.Assert.*;

import org.eulerdiagrams.AbstractDiagram.AbstractContour;
import org.eulerdiagrams.AbstractDiagram.VennSetIterator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.HashSet;

public class TestVennSetIterator {
    private final static Logger logger = LoggerFactory.getLogger(TestVennSetIterator.class);

    @Test
    public void testBasicIterator() {
        VennSetIterator it = new VennSetIterator(Arrays.asList());

        assertThat(it.hasNext(), is(false));
    }

    @Test
    public void testIterator() {
        AbstractContour a = new AbstractContour("A");
        AbstractContour b = new AbstractContour("B");
        AbstractContour c = new AbstractContour("C");

        int i=0;
        for(AbstractZone z: new VennSetIterator(Arrays.asList(a, b, c))) { // testing for-each here
            logger.info(z.toString());
            i++;
        }
        assertThat(i, is((int) Math.pow(2,3) - 1));
    }
}
