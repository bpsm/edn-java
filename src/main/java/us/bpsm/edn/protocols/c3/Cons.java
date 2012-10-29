package us.bpsm.edn.protocols.c3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import us.bpsm.edn.protocols.c3.C3Test.X1.E;

class Cons<E> {
    final E first;
    final Cons<E> rest;
    Cons(E first, Cons<E> rest) {
        this.first = first; this.rest = rest;
    }
    static <E> Cons<E> cons(E first, Cons<E> rest) {
        return new Cons<E>(first, rest);
    }
    static <E> E first(Cons<E> cons) {
        return cons == null ? null : cons.first;
    }
    static <E> Cons<E> rest(Cons<E> cons) {
        return cons == null ? null : cons.rest;
    }
    static boolean isEmpty(Cons<?> cons) {
        return cons == null;
    }
    static <E> List<E> toList(Cons<E> cons) {
        if (cons == null) {
            return Collections.emptyList();
        } else {
            List<E> result = new ArrayList<E>();
            for(Cons<E> c = cons; c != null; c = c.rest) {
                result.add(c.first);
            }
            return result;
        }
    }
    static boolean contains(Cons<?> cons, Object value) {
        for (Cons<?> c = cons; c != null; c = c.rest) {
            if (c.first == value ||
                    c.first != null && value != null && c.first.equals(value)) {
                return true;
            }
        }
        return false;
    }
    static <E> Cons<E> toCons(List<E> xs) {
        Cons<E> c = null;
        int n = xs.size();
        for (int i = n-1; i >= 0; i--) {
            c = cons(xs.get(i), c);
        }
        return c;
    }
}
