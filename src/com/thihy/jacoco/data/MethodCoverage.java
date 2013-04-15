package com.thihy.jacoco.data;

import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.ISourceNode;

public class MethodCoverage extends SourceNodeDelegate implements IMethodCoverage {
	private final String signature;
	private final String desc;

	public MethodCoverage(ISourceNode sourceNode, String signature, String desc) {
		super(sourceNode);
		this.signature = signature;
		this.desc = desc;
	}

	public String getSignature() {
		return signature;
	}

	public String getDesc() {
		return desc;
	}

}
