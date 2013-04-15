package com.thihy.jacoco.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.analysis.IBundleCoverage;

public class BundleCoverageStore implements IBundleCoverageVisitor {

	private final Map<String, IBundleCoverage> coverages = new HashMap<String, IBundleCoverage>();

	@Override
	public void visitBundleCoverage(IBundleCoverage data) {
		coverages.put(data.getName(), data);
	}

	public IBundleCoverage getBundleCoverage(String bundleName) {
		return coverages.get(bundleName);
	}

	public Collection<IBundleCoverage> getBundleCoverages() {
		return this.coverages.values();
	}
}
