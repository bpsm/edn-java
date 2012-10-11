package bpsm.edn.performance;

import bpsm.edn.parser.IOUtil;
import bpsm.edn.parser.Parser;
import bpsm.edn.parser.Parsers;


/**
 * This is not exactly a full benchmarking suite
 * 
 * But enough to test efficiency of various operations
 * 
 * @author Mike
 *
 */
public class PerformanceTest {
	private static final int REPEATS=5;
	private static final int BURN_IN=100;
	
	public static void main(String[] args) {
		runBenchmark(testA);
	}
	
	private static void runBenchmark(Benchmark b) {
		for (int i=0; i<BURN_IN; i++) {
			b.benchmark();
		}
		double total=0.0;
		System.out.println("Running benchmark: "+b);
		for (int i=0; i<REPEATS; i++) {
			double d=b.benchmark();
			total+=d;
			System.out.println ("Time per iteration: " + (long)d + " ns");
		}
		System.out.println ("AVERAGE time per iteration: " + (long)(total/REPEATS) + " ns");
	}
	
	private static abstract class Benchmark implements Runnable {
		private double benchmark() {
			long time=System.nanoTime();
			run();	
			double ns= (System.nanoTime()-time)/(1.0*getIterations()*REPEATS);	
			return ns;
		}		
		
		public final void run() {
			int max=getIterations();
			for (int i=0; i<max; i++) {
				runIteration();
			}
		}
		
		public abstract void runIteration();

		public int getIterations() {
			return 1;
		}
	}
	
	static final String ednString = IOUtil.stringFromResource("bpsm/edn/edn-sample.txt");
	
	public static Benchmark testA = new Benchmark() {
		public String toString() {return "Parse edn from String";}
		public int getIterations() {return 100;}
		
		@SuppressWarnings("unused")
		public void runIteration() {
			Parser p=Parsers.newParser(Parsers.defaultConfiguration(), ednString);
		}
	};
	
}
