package us.bpsm.edn.protocols.c3;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class C3Test {

    @Test
    public void testMroExample1() {
        assertEquals(
                Arrays.asList(X1.A.class, X1.B.class, X1.C.class, X1.D.class, X1.E.class, X1.F.class, X1.O.class),
                Cons.toList(C3.mro(X1.A.class)));
    }

    interface X1 {
        interface O {}
        interface F extends O {}
        interface E extends O {}
        interface D extends O {}
        interface C extends D, F {}
        interface B extends D, E {}
        interface A extends B, C {}
    }

}
