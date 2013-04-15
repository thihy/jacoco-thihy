package com.thihy.jacoco.data;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.internal.analysis.CounterImpl;

public class CoverageNode implements ICoverageNode {

	private final ElementType elementType;
	private final String name;

	/** Counter for branches. */
	private final ICounter branchCounter;

	/** Counter for instructions. */
	private final ICounter instructionCounter;

	/** Counter for lines */
	private final ICounter lineCounter;

	/** Counter for complexity. */
	private final ICounter complexityCounter;

	/** Counter for methods. */
	private final ICounter methodCounter;

	/** Counter for classes. */
	private final ICounter classCounter;

	public CoverageNode(ElementType elementType, String name,//
			ICounter branchCounter,//
			ICounter instructionCounter, //
			ICounter lineCounter, //
			ICounter complexityCounter,//
			ICounter methodCounter, //
			ICounter classCounter) {
		super();
		this.elementType = elementType;
		this.name = name;
		this.branchCounter = branchCounter;
		this.instructionCounter = instructionCounter;
		this.lineCounter = lineCounter;
		this.complexityCounter = complexityCounter;
		this.methodCounter = methodCounter;
		this.classCounter = classCounter;
	}

	@Override
	public ElementType getElementType() {
		return this.elementType;
	}

	public String getName() {
		return name;
	}

	public ICounter getBranchCounter() {
		return branchCounter;
	}

	public ICounter getInstructionCounter() {
		return instructionCounter;
	}

	public ICounter getLineCounter() {
		return lineCounter;
	}

	public ICounter getComplexityCounter() {
		return complexityCounter;
	}

	public ICounter getMethodCounter() {
		return methodCounter;
	}

	public ICounter getClassCounter() {
		return classCounter;
	}

	public ICounter getCounter(final CounterEntity entity) {
		switch (entity) {
		case INSTRUCTION:
			return getInstructionCounter();
		case BRANCH:
			return getBranchCounter();
		case LINE:
			return getLineCounter();
		case COMPLEXITY:
			return getComplexityCounter();
		case METHOD:
			return getMethodCounter();
		case CLASS:
			return getClassCounter();
		}
		throw new AssertionError(entity);
	}

	public ICoverageNode getPlainCopy() {
		return new CoverageNode(elementType, name,//
				CounterImpl.getInstance(branchCounter),//
				CounterImpl.getInstance(instructionCounter),//
				CounterImpl.getInstance(lineCounter),//
				CounterImpl.getInstance(complexityCounter),//
				CounterImpl.getInstance(methodCounter),//
				CounterImpl.getInstance(classCounter));
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(name).append(" [").append(elementType).append("]");
		return sb.toString();
	}
}
