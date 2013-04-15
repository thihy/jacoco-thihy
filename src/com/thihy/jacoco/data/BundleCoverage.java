package com.thihy.jacoco.data;

import java.util.Collection;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.IPackageCoverage;

public class BundleCoverage extends CoverageNodeDelegate implements IBundleCoverage {

	private final Collection<IPackageCoverage> packages;

	public BundleCoverage(ICoverageNode coverageNode, Collection<IPackageCoverage> packages) {
		super(coverageNode);
		this.packages = packages;
	}

	@Override
	public Collection<IPackageCoverage> getPackages() {
		return this.packages;
	}
}
