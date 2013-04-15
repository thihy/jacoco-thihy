package com.thihy.jacoco.data;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;

public class Line implements ILine {
	private final int status;
	private final ICounter branchCounter;
	private final ICounter instructionCounter;

	public Line(int status, ICounter branchCounter, ICounter instructionCounter) {
		super();
		this.status = status;
		this.branchCounter = branchCounter;
		this.instructionCounter = instructionCounter;
	}

	public int getStatus() {
		return status;
	}

	public ICounter getBranchCounter() {
		return branchCounter;
	}

	public ICounter getInstructionCounter() {
		return instructionCounter;
	}

}
