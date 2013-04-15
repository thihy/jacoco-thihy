package com.thihy.jacoco.data;

import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.runtime.RuntimeData;

public class MemoryRuntimeData extends RuntimeData {
	ExecutionDataStore getStore() {
		return this.store;
	}
}
