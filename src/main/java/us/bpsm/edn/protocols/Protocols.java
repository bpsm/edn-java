// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.protocols;

import static us.bpsm.edn.protocols.C3.methodResolutionOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressWarnings("rawtypes")
public class Protocols {

    static final String SINGLE_USE_MSG =
            "This builder can only be used to build a single Protocol.";

    static final String NO_MODIFY_MSG =
            "This builder is single-use and may not be modified after the Protocol has been built.";

    public static <F> Protocol.Builder<F> builder(final String name) {
        return new ProtocolImpl<F>(name);
    }

    static class ProtocolImpl<F> implements Protocol.Builder<F>, Protocol<F> {
        final String name;
        F nullFn = null;
        final Map<Class<?>, F> m = new HashMap<Class<?>, F>();
        boolean built = false;

        ProtocolImpl(String name) { this.name = name; }

        public Protocol.Builder<F> put(Class selfClass, F fn) {
            if (built) {
                throw new IllegalStateException(NO_MODIFY_MSG);
            }
            if (selfClass == null) {
                nullFn = fn;
            } else {
                m.put(selfClass, fn);
            }
            return this;
        }

        public Protocol<F> build() {
            if (built) {
                throw new IllegalStateException(SINGLE_USE_MSG);
            }
            built = true;
            return this;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public F lookup(Class selfClass) {
            if (selfClass == null) {
                return nullFn;
            }
            synchronized (m) {
                F fn;
                fn = m.get(selfClass);
                if (fn != null) {
                    return fn;
                }
                for (Class<?> c: butfirst(methodResolutionOrder(selfClass))) {
                    fn = m.get(c);
                    if (fn != null) {
                        m.put(selfClass, fn);
                        return fn;
                    }
                }
                return null;
            }
        }
    }

    static <E> List<E> butfirst(List<E> es) {
        return es.isEmpty() ? es : es.subList(1, es.size());
    }

}
