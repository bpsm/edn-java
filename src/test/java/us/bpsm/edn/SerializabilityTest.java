package us.bpsm.edn;

import org.junit.Test;
import us.bpsm.edn.parser.IOUtil;
import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;

import java.io.*;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


public class SerializabilityTest {

    private static byte[] serialize(Object o) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ObjectOutputStream objectsOut = new ObjectOutputStream(bytesOut);
        objectsOut.writeObject(o);
        objectsOut.close();
        return bytesOut.toByteArray();
    }

    private static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        ObjectInputStream objectsIn = new ObjectInputStream(bytesIn);
        return objectsIn.readObject();
    }

    @Test
    public void testSerializability() throws IOException, ClassNotFoundException {
        Parseable pbr = Parsers.newParseable(IOUtil.stringFromResource(
          "us/bpsm/edn/serializability.edn"));
        Parser parser = Parsers.newParser(Parsers.defaultConfiguration());
        Object expected = parser.nextValue(pbr);
        assertNotEquals(Parser.END_OF_INPUT, expected);
        List<Object> result = (List<Object>) deserialize(serialize(expected));
        assertEquals(expected, result);
    }

}
