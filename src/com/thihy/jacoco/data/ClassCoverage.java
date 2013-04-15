package com.thihy.jacoco.data;

import java.util.Collection;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.ISourceNode;

public class ClassCoverage extends SourceNodeDelegate implements IClassCoverage {
	private final String packageName;
	private final long id;
	private final String signature;
	private final String superName;
	private final String[] interfaces;
	private final Collection<IMethodCoverage> methods;
	private String sourceFileName;

	public ClassCoverage(ISourceNode sourceNode, String packageName, long id, String signature, String superName, String[] interfaces, Collection<IMethodCoverage> methods, String sourceFileName) {
		super(sourceNode);
		this.packageName = packageName;
		this.id = id;
		this.signature = signature;
		this.superName = superName;
		this.interfaces = interfaces;
		this.methods = methods;
		this.sourceFileName = sourceFileName;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	public void setSourceFileName(String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}

	public long getId() {
		return id;
	}

	public String getSignature() {
		return signature;
	}

	public String getSuperName() {
		return superName;
	}

	public Collection<IMethodCoverage> getMethods() {
		return methods;
	}

	@Override
	public String[] getInterfaceNames() {
		return this.interfaces;
	}

	@Override
	public String getPackageName() {
		return this.packageName;
	}

}
