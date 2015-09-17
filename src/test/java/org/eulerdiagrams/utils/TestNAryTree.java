package org.eulerdiagrams.utils;

import java.util.Arrays;
import java.util.List;

import org.eulerdiagrams.utils.NAryTree;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TestNAryTree {

    @Test
    public void testTrivial () {
        List<Integer> is1 = Arrays.asList(1)
                , is2 = Arrays.asList(1);

        NAryTree<Integer> t1 = new NAryTree<>(0)
                          , t2 = new NAryTree<>(0);

        for(Integer i : is1) {
            t1.insert(i);
        }

        for(Integer i : is2) {
            t2.insert(i);
        }

        assertThat(t1, is(equalTo(t2)));
    }

    @Test
    public void testThree () {
        List<Integer> is1 = Arrays.asList(1,2)
                , is2 = Arrays.asList(0,1);

        NAryTree<Integer> t1 = new NAryTree<>(0)
                          , t2 = new NAryTree<>(2);

        for(Integer i : is1) {
            t1.insert(i);
        }

        for(Integer i : is2) {
            t2.insert(i);
        }

        assertThat(t1, is(equalTo(t2)));
    }

    @Test
    public void testSimpleTree () {
        List<Integer> is1 = Arrays.asList(1,2,3,3,4,5)
                      , is2 = Arrays.asList(5,4,3,3,2,1);

        NAryTree<Integer> t1 = new NAryTree<>(0)
                          , t2 = new NAryTree<>(0);

        for(Integer i : is1) {
            t1.insert(i);
        }

        for(Integer i : is2) {
            t2.insert(i);
        }

        assertThat(t1, is(equalTo(t2)));
    }
}
