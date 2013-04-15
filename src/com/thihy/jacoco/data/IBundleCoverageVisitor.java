package com.thihy.jacoco.data;

import org.jacoco.core.analysis.IBundleCoverage;

public interface IBundleCoverageVisitor {

	public void visitBundleCoverage(IBundleCoverage data);

}