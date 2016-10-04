package us.bpsm.edn.protocols;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

import org.junit.Test;

import us.bpsm.edn.protocols.C3;

public class C3Test {

    @SuppressWarnings("unchecked")
	@Test
    public void testMroExample1() {
        assertEquals(Arrays.asList(X1.A.class, X1.B.class, X1.C.class,
                X1.D.class, X1.E.class, X1.F.class, X1.O.class),
                C3.methodResolutionOrder(X1.A.class));
    }

    interface X1 {
        interface O {
        }

        interface F extends O {
        }

        interface E extends O {
        }

        interface D extends O {
        }

        interface C extends D, F {
        }

        interface B extends D, E {
        }

        interface A extends B, C {
        }
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testMroExample2() {
        assertEquals(Arrays.asList(X2.A.class, X2.B.class, X2.E.class,
                X2.C.class, X2.D.class, X2.F.class, X2.O.class),
                C3.methodResolutionOrder(X2.A.class));
    }

    interface X2 {
        interface O {
        }

        interface F extends O {
        }

        interface E extends O {
        }

        interface D extends O {
        }

        interface C extends D, F {
        }

        // X2 has B extend "E, D", while X1 extends "D, E"
        interface B extends E, D {
        }

        interface A extends B, C {
        }
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testMroExample3() {
        assertEquals(Arrays.<Class<?>> asList(X3.A.class, X3.O.class),
                C3.methodResolutionOrder(X3.A.class));
        assertEquals(Arrays.<Class<?>> asList(X3.B.class, X3.O.class),
                C3.methodResolutionOrder(X3.B.class));
        assertEquals(Arrays.<Class<?>> asList(X3.C.class, X3.O.class),
                C3.methodResolutionOrder(X3.C.class));
        assertEquals(Arrays.<Class<?>> asList(X3.D.class, X3.O.class),
                C3.methodResolutionOrder(X3.D.class));
        assertEquals(Arrays.<Class<?>> asList(X3.E.class, X3.O.class),
                C3.methodResolutionOrder(X3.E.class));
        assertEquals(Arrays.<Class<?>> asList(X3.K1.class, X3.A.class,
                X3.B.class, X3.C.class, X3.O.class),
                C3.methodResolutionOrder(X3.K1.class));
        assertEquals(Arrays.<Class<?>> asList(X3.K2.class, X3.D.class,
                X3.B.class, X3.E.class, X3.O.class),
                C3.methodResolutionOrder(X3.K2.class));
        assertEquals(Arrays.<Class<?>> asList(X3.K3.class, X3.D.class,
                X3.A.class, X3.O.class), C3.methodResolutionOrder(X3.K3.class));
        assertEquals(Arrays.asList(X3.Z.class, X3.K1.class, X3.K2.class,
                X3.K3.class, X3.D.class, X3.A.class, X3.B.class, X3.C.class,
                X3.E.class, X3.O.class), C3.methodResolutionOrder(X3.Z.class));
    }

    interface X3 {
        interface O {
        }

        interface A extends O {
        }

        interface B extends O {
        }

        interface C extends O {
        }

        interface D extends O {
        }

        interface E extends O {
        }

        interface K1 extends A, B, C {
        }

        interface K2 extends D, B, E {
        }

        interface K3 extends D, A {
        }

        interface Z extends K1, K2, K3 {
        }
    }

    @Test
    public void testMroExample4OrderDisagreement() {
        try {
            C3.methodResolutionOrder(X4.Z.class);
            fail("Expected an exception");
        } catch (RuntimeException e) {
            assertEquals("Unable to compute a consistent method resolution"
                    + " order for us.bpsm.edn.protocols.C3Test$X4$Z.",
                    e.getMessage());
        }
        try {
            C3.methodResolutionOrder(X4.Z2.class);
            fail("Expected an exception");
        } catch (RuntimeException e) {
            assertEquals(
                    "Unable to compute a consistent method resolution "
                            + "order for us.bpsm.edn.protocols.C3Test$X4$Z2 because "
                            + "us.bpsm.edn.protocols.C3Test$X4$Z has no consistent "
                            + "method resolution order.", e.getMessage());
        }
    }

    /** order disagreement */
    interface X4 {
        interface O {
        }

        interface X extends O {
        }

        interface Y extends O {
        }

        interface A extends X, Y {
        }

        interface B extends Y, X {
        }

        interface Z extends A, B {
        }

        interface Z2 extends Z {
        }
    }

    /**
     * Java is not really multiple inheritance, unless we consider interfaces to
     * be classes, which we do for the purposes of C3. But, in this case, this
     * means that classes and interfaces don't share a common ultimate
     * super-type. (Object). That, in turn means that there can exist MROs where
     * interfaces in the ancestry are considered later Object. This isn't very
     * useful. Object should always be considered least specific.
     */
    @Test
    public void objectIsFinalInOrder() {
        assertEquals(
                Arrays.<Class<?>> asList(X5.K.class, X5.A.class, Object.class),
                C3.methodResolutionOrder(X5.K.class));
    }

    interface X5 {
        interface A {
        }

        class K implements A {
        }
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testArrayList() {
        assertEquals(Arrays.asList(ArrayList.class, AbstractList.class,
                AbstractCollection.class, List.class, Collection.class,
                Iterable.class, RandomAccess.class, Cloneable.class,
                Serializable.class, Object.class),
                C3.methodResolutionOrder(ArrayList.class));
    }
}
