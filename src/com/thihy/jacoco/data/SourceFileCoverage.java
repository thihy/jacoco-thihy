package com.thihy.jacoco.data;

import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.analysis.ISourceNode;

public class SourceFileCoverage extends SourceNodeDelegate implements ISourceFileCoverage {
	private final String packageName;

	public SourceFileCoverage(ISourceNode sourceNode, final String packageName) {
		super(sourceNode);
		this.packageName = packageName;
	}

	@Override
	public String getPackageName() {
		return this.packageName;
	}

}
