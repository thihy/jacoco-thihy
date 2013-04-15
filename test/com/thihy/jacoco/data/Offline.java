package com.thihy.jacoco.data;


public class Offline {

	public static final MemoryRuntimeData data;

	static {
		data = new MemoryRuntimeData();
	}

	private Offline() {
		// no instances
	}

	/**
	 * API for offline instrumented classes.
	 * 
	 * @param classid
	 *            class identifier
	 * @param classname
	 *            VM class name
	 * @param probecount
	 *            probe count for this class
	 * @return probe array instance for this class
	 */
	public static boolean[] getProbes(final long classid, final String classname, final int probecount) {
		return data.getExecutionData(Long.valueOf(classid), classname, probecount).getProbes();
	}

}
