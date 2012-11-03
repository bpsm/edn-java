package us.bpsm.edn.protocols.c3;

import java.util.ArrayList;
import java.util.List;

import us.bpsm.edn.EdnException;

class C3 {

    static List<Class<?>> methodResolutionOrder(Class<?> c) {
        try {
            List<Class<?>> result = mro(c);
            if (c.getSuperclass() != null) {
                result.add(Object.class);
            }
            return result;
        } catch (InconsistentHierarchy e) {
            StringBuilder b = new StringBuilder()
                 .append("Unable to compute a consistent method resolution order for ")
                 .append(c.getName());

            if (c.equals(e.problematicClass)) {
                 b.append(".");
            } else {
                b.append(" because ")
                 .append(e.problematicClass.getName())
                 .append(" has no consistent method resolution order.");
            }
            throw new EdnException(b.toString());
        }
    }

    private static List<Class<?>> mro(Class<?> c) throws InconsistentHierarchy {
        List<List<Class<?>>> seqsToMerge = new ArrayList<List<Class<?>>>();
        seqsToMerge.add(asList(c));
        List<Class<?>> supers = supers(c);
        for (Class<?> s: supers) {
            seqsToMerge.add(mro(s));
        }
        seqsToMerge.add(supers);
        try {
            return merge(seqsToMerge);
        } catch (InconsistentHierarchy e) {
            throw new InconsistentHierarchy(c);
        }
    }

    private static List<Class<?>> asList(Class<?> c) {
        List<Class<?>> result = new ArrayList<Class<?>>(1);
        result.add(c);
        return result;
    }

    private static List<Class<?>> supers(Class<?> c) {
        Class<?> sc = c.getSuperclass();
        Class<?>[] interfaces = c.getInterfaces();
        List<Class<?>> result = new ArrayList<Class<?>>();
        if (sc != null && sc != Object.class) {
            result.add(sc);
        }
        for (Class<?> i: interfaces) {
            result.add(i);
        }
        return result;
    }

    private static List<Class<?>> merge(List<List<Class<?>>> seqsToMerge)
    throws InconsistentHierarchy {
        List<Class<?>> result = new ArrayList<Class<?>>();
        while (!allAreEmpty(seqsToMerge)) {
            Class<?> candidate = findCandidate(seqsToMerge);
            if (candidate == null) {
                throw new InconsistentHierarchy();
            }
            result.add(candidate);
            removeCandidate(seqsToMerge, candidate);
        }
        return result;
    }

    private static boolean allAreEmpty(List<List<Class<?>>> lists) {
        for (List<?> l: lists) {
            if (!l.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> findCandidate(List<List<Class<?>>> seqsToMerge) {
        for (List<Class<?>> seq: seqsToMerge) {
            if (!seq.isEmpty() && !occursInSomeTail(seqsToMerge, seq.get(0))) {
                return seq.get(0);
            }
        }
        return null;
    }

    private static boolean occursInSomeTail(List<List<Class<?>>> seqsToMerge,
        Object c) {
        for (List<?> seq: seqsToMerge) {
            for (int i = 1; i < seq.size(); i++) {
                if (c.equals(seq.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void removeCandidate(List<List<Class<?>>> seqsToMerge,
        Class<?> candidate) {
        for (List<Class<?>> seq: seqsToMerge) {
            if (!seq.isEmpty() && candidate.equals(seq.get(0))) {
                seq.remove(0);
            }
        }
    }

    static class InconsistentHierarchy extends Exception {
        private static final long serialVersionUID = 1L;
        Class<?> problematicClass;
        InconsistentHierarchy(Class<?> problematicClass) {
            super();
            this.problematicClass = problematicClass;
        }
        InconsistentHierarchy() {
            super();
        }
    }

}
