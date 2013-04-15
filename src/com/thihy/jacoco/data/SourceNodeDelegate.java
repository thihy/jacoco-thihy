package com.thihy.jacoco.data;

import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceNode;

public class SourceNodeDelegate extends CoverageNodeDelegate implements ISourceNode {
	private final ISourceNode sourceNode;

	public SourceNodeDelegate(ISourceNode sourceNode) {
		super(sourceNode);
		this.sourceNode = sourceNode;
	}

	@Override
	public int getFirstLine() {
		return sourceNode.getFirstLine();
	}

	@Override
	public int getLastLine() {
		return sourceNode.getLastLine();
	}

	@Override
	public ILine getLine(int nr) {
		return sourceNode.getLine(nr);
	}

}
