// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.protocols;

import static org.junit.Assert.*;

import org.junit.Test;

import bpsm.edn.EdnException;

public class ProtocolsTest {

    @Test
    public void testSupers() {
        final Function fnObject = newFn();
            final Function fnA0 = newFn();
            final Function fnB0a = newFn();
            final Function fnB0b = newFn();
            final Function fnB0 = newFn();

        Protocol p = Protocols.builder("p")
        .put(Object.class, fnObject)
        .build();

        assertEquals(fnObject, p.lookup(Object.class));
        assertEquals(fnObject, p.lookup(A.class));

        p = Protocols.builder("p")
                .put(A0.class, fnA0)
                .build();

        assertEquals(null, p.lookup(Object.class));
        assertEquals(fnA0, p.lookup(A.class));
        assertEquals(fnA0, p.lookup(B.class));
        assertEquals(fnA0, p.lookup(C.class));

        p = Protocols.builder("p")
                .put(B0a.class, fnB0a)
                .put(B0b.class, fnB0b)
                .build();
        try {
            p.lookup(B.class);
            fail();
        } catch (EdnException _) {
            ; // pass
        }

        p = Protocols.builder("p")
                .put(B0a.class, fnB0a)
                .put(B0b.class, fnB0b)
                .put(B.class, fnB0)
                .build();

        assertEquals(fnB0, p.lookup(B.class));

    }
    private Function newFn() {
        return new Function() {
            public Object eval(Object self, Object argument) {
                return argument;
            }};
    }


    interface A0 {}
    interface B0a {}
    interface B0b {}
    interface B0 extends B0a, B0b {}
    interface C0 {}
    interface C1 extends B0b {}
    static class A implements A0 {}
    static class B extends A implements B0 {}
    static class C extends B implements C0, C1 {}


}
