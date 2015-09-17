package org.eulerdiagrams.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * This N-ary tree is specialised with the assumption that if an element is a
 * child of something, it can't be a child of something else.
 *
 *
 */
public class NAryTree<T extends Comparable<T>> {
    T root;
    Optional<List<NAryTree<T>>> payload;

    public NAryTree(T root) {
        this.root = root;
        payload = Optional.empty();
    }

    // FIXME: This is horribly hacked together!
    public void insert(T element) {
        if(-1 == root.compareTo(element)) {
            if(!payload.isPresent()) {
                // Use Vector as removeAll is definied on it.
                Vector<NAryTree<T>> v = new Vector<>();
                v.add(new NAryTree<T>(element));
                payload = Optional.of(v);
                return;
            } else if (payload.get().stream().allMatch(t -> 0 == t.root.compareTo(element))) {
                // all elements are at the same level
                payload.get().add(new NAryTree<T>(element));
                return;
            } else if(payload.get().stream().anyMatch(t -> 1 == t.root.compareTo(element))) {
                // some elements are "lower" than element
                List<NAryTree<T>> lower = payload.get().stream().filter(t -> 1 == t.root.compareTo(element)).collect(Collectors.toList());
                payload.get().removeAll(lower);
         
                NAryTree<T> te = new NAryTree<>(element);
                te.payload = Optional.of(lower);
                payload.get().add(te);
                return;
            }

            // find the one element "higher" than it
            Optional<NAryTree<T>> higher = Optional.empty();
            for(NAryTree<T> t : payload.get()) {
                if(-1 == t.root.compareTo(element)) {
                    higher = Optional.of(t);
                }
            }
            if(higher.isPresent()) {
                higher.get().insert(element);
            }
        } else if(1 == root.compareTo(element)) {
            NAryTree<T> copy = new NAryTree<>(root);
            copy.payload = payload.isPresent()?Optional.of(new Vector<NAryTree<T>>(payload.get())):payload;

            this.root = element;
            this.payload = Optional.of(new Vector<>());
            this.payload.get().add(copy);
        }
    }

    public Optional<NAryTree<T>> find(T element) {
        // TODO
        return Optional.empty();
    }

    public boolean isLeaf() {
        return !payload.isPresent();
    }

    public boolean equals(Object o) {
        if(null == o) return false;

        if(! (o instanceof NAryTree<?>)) return false;

        NAryTree<T> other = (NAryTree<T>) o;
        if(!this.root.equals(other.root)) return false;

        return this.payload.equals(other.payload);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        sb.append(root.toString());
        sb.append(":");
        if(payload.isPresent()) {
            for(NAryTree<T> t: payload.get()) {
                sb.append(t.toString());
            }
        } else {
            sb.append("leaf");
        }
        sb.append("]");
        return sb.toString();
    }
}
