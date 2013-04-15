package com.thihy.jacoco.data;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.core.internal.analysis.LineImpl;

public class SourceNode extends CoverageNodeDelegate implements ISourceNode {
	private final int firstLine;
	private final int lastLine;
	private final ILine[] lines;

	public SourceNode(ICoverageNode coverageNode, int firstLine, int lastLine) {
		super(coverageNode);
		this.firstLine = firstLine;
		this.lastLine = lastLine;
		this.lines = new ILine[lastLine - firstLine + 1];
	}

	@Override
	public int getFirstLine() {
		return this.firstLine;
	}

	@Override
	public int getLastLine() {
		return this.lastLine;
	}

	public void setLine(int nr, ILine line) {
		this.lines[nr - firstLine] = line;
	}

	@Override
	public ILine getLine(int nr) {
		if (nr < this.firstLine || nr > this.lastLine) {
			return LineImpl.EMPTY;
		}
		return this.lines[nr - firstLine];
	}

}
