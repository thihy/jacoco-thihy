package com.thihy.jacoco.data;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;

public abstract class CoverageNodeDelegate implements ICoverageNode {

	private final ICoverageNode coverageNode;

	public CoverageNodeDelegate(ICoverageNode coverageNode) {
		super();
		this.coverageNode = coverageNode;
	}

	public ElementType getElementType() {
		return coverageNode.getElementType();
	}

	public String getName() {
		return coverageNode.getName();
	}

	public ICounter getBranchCounter() {
		return coverageNode.getBranchCounter();
	}

	public ICounter getInstructionCounter() {
		return coverageNode.getInstructionCounter();
	}

	public ICounter getLineCounter() {
		return coverageNode.getLineCounter();
	}

	public ICounter getComplexityCounter() {
		return coverageNode.getComplexityCounter();
	}

	public ICounter getMethodCounter() {
		return coverageNode.getMethodCounter();
	}

	public ICounter getClassCounter() {
		return coverageNode.getClassCounter();
	}

	public ICounter getCounter(CounterEntity entity) {
		return coverageNode.getCounter(entity);
	}

	public ICoverageNode getPlainCopy() {
		return coverageNode.getPlainCopy();
	}

}
