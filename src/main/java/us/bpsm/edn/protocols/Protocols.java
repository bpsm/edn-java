// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.protocols;

import java.util.HashMap;
import java.util.Map;

import us.bpsm.edn.EdnException;


@SuppressWarnings("rawtypes")
public class Protocols {

    static final String SINGLE_USE_MSG =
            "This builder can only be used to build a single Protocol.";

    static final String NO_MODIFY_MSG =
            "This builder is single-use and may not be modified after the Protocol has been built.";

    public static Protocol.Builder builder(final String name) {
        return new Protocol.Builder() {
            Function nullFn = null;
            Map<Class, Function> m = new HashMap<Class, Function>();
            boolean usedUp = false;

            public Protocol.Builder put(Class selfClass, Function fn) {
                if (usedUp) {
                    throw new IllegalStateException(NO_MODIFY_MSG);
                }
                if (selfClass == null) {
                    nullFn = fn;
                } else {
                    m.put(selfClass, fn);
                }
                return this;
            }

            public Protocol build() {
                if (usedUp) {
                    throw new IllegalStateException(SINGLE_USE_MSG);
                }
                usedUp = true;
                return new Protocol() {
                    // populate cache with user-specified bindings.
                    final Map<Class, Function> cache = new HashMap<Class, Function>(m);

                    @SuppressWarnings("unchecked")
                    public synchronized Function lookup(Class selfClass) {
                        if (selfClass == null) {
                            return nullFn;
                        } else {
                            Function fn;

                            // fast: derived binding found in cache
                            fn = cache.get(selfClass);
                            if (fn != null) {
                                return fn;
                            }

                            // slow: search for derived binding, then cache it.
                            Class k = null;
                            for (Class c: m.keySet()) {
                                if (c.isAssignableFrom(selfClass)) {
                                    if (k == null) {
                                        k = c;
                                    } else {
                                        throw new EdnException(ambiguity(
                                                this, selfClass, k, c));
                                    }
                                }
                            }

                            // This puns on the fact that m never has a null
                            // key, so m.get(null) will return null.
                            fn = m.get(k);
                            cache.put(selfClass, fn);
                            return fn;
                        }
                    }

                    @Override
                    public String toString() {
                        return "Protocol '" + name;
                    }

                    public String name() {
                        return name;
                    }
                };
            }

        };
    }

    static String ambiguity(Protocol p, Class selfClass, Class k, Class c) {
        return p +
                " can't decide on an implementation for " +
                selfClass + " both " + k +
                " and " + c + " seem to match. " +
                "Bind an implementation to " +
                selfClass + ", specifically, " +
                " to resolve this ambiguity.";
    }

}
