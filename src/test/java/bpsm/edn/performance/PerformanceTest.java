package bpsm.edn.performance;


/**
 * This is not exactly a full benchmarking suite
 * 
 * But enough to test efficiency of various operations
 * 
 * @author Mike
 *
 */

public class PerformanceTest {
	private static final int REPEATS=10;
	private static final int BURN_IN=10;
	
	public static void main(String[] args) {
		runBenchmark(testA);
	}
	
	private static void runBenchmark(Benchmark b) {
		System.out.println("Running benchmark: "+b);
		double d=b.benchmark();
		System.out.println ("Time per execution: " + d + " ns");
	}
	
	private static abstract class Benchmark implements Runnable {
		private double benchmark() {
			// burn-in
			for (int i=0; i<BURN_IN; i++) {
				run();
			}

			long time=System.nanoTime();
			for (int i=0; i<REPEATS; i++) {
				run();
			}
			
			double ns= (System.nanoTime()-time)/(1.0*getIterations()*REPEATS);	
			return ns;
		}		
		
		public int getIterations() {
			return 1;
		}
	}
	
	public static Benchmark testA = new Benchmark() {
		public void run() {
			// empty
		}
	};
	
}
