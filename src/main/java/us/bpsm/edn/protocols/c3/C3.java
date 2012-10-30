package us.bpsm.edn.protocols.c3;

import static us.bpsm.edn.protocols.c3.Cons.cons;
import static us.bpsm.edn.protocols.c3.Cons.first;
import static us.bpsm.edn.protocols.c3.Cons.rest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class C3 {

    static int b2i(boolean b) {
        return b ? 1 : 0;
    }

    static Cons<Class<?>> supers(Class<?> c) {
        Class<?> sc = c.getSuperclass();
        Class<?>[] interfaces = c.getInterfaces();
        Cons<Class<?>> result = null;
        for (int i = interfaces.length - 1; i >= 0; i--) {
            result = cons(interfaces[i], result);
        }
        if (sc != null) {
            result = cons(sc, result);
        }
        return result;
    }

    static List<Class<?>> methodResolutionOrder(Class<?> c) {
        return Cons.toList(mro(c));
    }

    static Cons<Class<?>> mro(Class<?> c) {
        Cons<Class<?>> supers = supers(c);
        List<Cons<Class<?>>> supermros = new ArrayList<Cons<Class<?>>>();
        for (Cons<Class<?>> ss = supers; ss != null; ss = rest(ss)) {
            supermros.add(mro(first(ss)));
        }
        return cons(c, merge(supermros));
    }

    private static Cons<Class<?>> merge(List<Cons<Class<?>>> supermros) {
        List<Class<?>> merged = new ArrayList<Class<?>>();
        while (true) {
            List<Class<?>> heads = heads(supermros);
            if (heads.size() == 0) {
                return Cons.toCons(merged);
            }
            List<Cons<Class<?>>> tails = tails(supermros);
            Class<?> h = findGoodHead(heads, tails);
            if (h != null) {
                removeMatchingHeadFromAll(h, supermros);
                merged.add(h);
            } else {
                throw new RuntimeException("Inconsistently ordered hierarchy");
            }
        }
    }

    private static void removeMatchingHeadFromAll(Class<?> h,
            List<Cons<Class<?>>> supermros) {
        for (int i = 0; i < supermros.size(); i++) {
            if (Cons.first(supermros.get(i)) == h) {
                supermros.set(i, Cons.rest(supermros.get(i)));
            }
        }
        for(Iterator<Cons<Class<?>>> it = supermros.iterator(); it.hasNext();) {
            if (it.next() == null) {
                it.remove();
            }
        }
    }

    private static Class<?> findGoodHead(List<Class<?>> heads,
            List<Cons<Class<?>>> tails) {
        for (Class<?> h: heads) {
            if (headIsGood(h, tails)) {
                return h;
            }
        }
        return null;
    }


    private static boolean headIsGood(Class<?> h, List<Cons<Class<?>>> tails) {
        for (Cons<Class<?>> t: tails) {
            if (Cons.contains(t, h)) {
                return false;
            }
        }
        return true;
    }

    private static List<Cons<Class<?>>> tails(List<Cons<Class<?>>> supermros) {
        List<Cons<Class<?>>> tails = new ArrayList<Cons<Class<?>>>();
        for (Cons<Class<?>> c: supermros) {
            if (c != null && c.rest != null) {
                tails.add(c.rest);
            }
        }
        return tails;
    }

    private static List<Class<?>> heads(List<Cons<Class<?>>> supermros) {
        List<Class<?>> heads = new ArrayList<Class<?>>();
        for (Cons<Class<?>> c: supermros) {
            if (c != null) {
                heads.add(c.first);
            }
        }
        return heads;
    }

}
