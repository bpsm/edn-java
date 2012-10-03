package bpsm.edn.printer;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

public class StringPrinterTest {

	@Test
	public void TestBasicStringPrinter() {
		StringPrinter sp=new StringPrinter();
		
	   	ArrayList<Object> al=new ArrayList<Object>();
    	al.add(1);
    	al.add(2);
    	sp.printValue(al);
    	assertEquals("[1 2]", sp.toString());	
    	sp.close();
    	
    	assertEquals("[1 2]", StringPrinter.printString(al));	

    }
}
