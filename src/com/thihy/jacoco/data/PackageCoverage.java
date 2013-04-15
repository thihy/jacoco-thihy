package com.thihy.jacoco.data;

import java.util.Collection;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;

public class PackageCoverage extends CoverageNodeDelegate implements IPackageCoverage {

	private final Collection<IClassCoverage> classCoverages;
	private final Collection<ISourceFileCoverage> sourceFileCoverages;

	public PackageCoverage(ICoverageNode coverageNode, Collection<IClassCoverage> classCoverages, Collection<ISourceFileCoverage> sourceFileCoverages) {
		super(coverageNode);
		this.classCoverages = classCoverages;
		this.sourceFileCoverages = sourceFileCoverages;
	}

	@Override
	public Collection<IClassCoverage> getClasses() {
		return this.classCoverages;
	}

	@Override
	public Collection<ISourceFileCoverage> getSourceFiles() {
		return this.sourceFileCoverages;
	}

}
