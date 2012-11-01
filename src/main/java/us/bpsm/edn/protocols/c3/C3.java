package us.bpsm.edn.protocols.c3;

import java.util.ArrayList;
import java.util.List;

class C3 {

    static List<Class<?>> methodResolutionOrder(Class<?> c) {
        List<List<Class<?>>> seqsToMerge = new ArrayList<List<Class<?>>>();
        seqsToMerge.add(asList(c));
        List<Class<?>> supers = supers(c);
        for (Class<?> s: supers) {
            seqsToMerge.add(methodResolutionOrder(s));
        }
        seqsToMerge.add(supers);
        return merge(seqsToMerge);
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
        if (sc != null) {
            result.add(sc);
        }
        for (Class<?> i: interfaces) {
            result.add(i);
        }
        return result;
    }

    private static List<Class<?>> merge(List<List<Class<?>>> seqsToMerge) {
        List<Class<?>> result = new ArrayList<Class<?>>();
        while (!allAreEmpty(seqsToMerge)) {
            Class<?> candidate = findCandidate(seqsToMerge);
            if (candidate == null) {
                throw new RuntimeException("Inconsistent hierarchy");
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

}
