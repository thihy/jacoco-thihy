/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package com.thihy.jacoco.data;

import static java.lang.String.format;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.internal.data.CompactDataInput;

/**
 * Deserialization of execution data from binary streams.
 */
public class BundleCoverageDataReader {

	/** Underlying data input */
	protected final CompactDataInput in;

	private ISessionInfoVisitor sessionInfoVisitor = null;

	private IExecutionDataVisitor executionDataVisitor = null;

	private IBundleCoverageVisitor bundleCoverageVisitor = null;

	private boolean firstBlock = true;

	/**
	 * Creates a new reader based on the given input stream input. Depending on
	 * the nature of the underlying stream input should be buffered as most data
	 * is read in single bytes.
	 * 
	 * @param input
	 *            input stream to read execution data from
	 */
	public BundleCoverageDataReader(final InputStream input) {
		this.in = new CompactDataInput(input);
	}

	/**
	 * Sets an listener for session information.
	 * 
	 * @param visitor
	 *            visitor to retrieve session info events
	 */
	public void setSessionInfoVisitor(final ISessionInfoVisitor visitor) {
		this.sessionInfoVisitor = visitor;
	}

	/**
	 * Sets an listener for execution data.
	 * 
	 * @param visitor
	 *            visitor to retrieve execution data events
	 */
	public void setExecutionDataVisitor(final IExecutionDataVisitor visitor) {
		this.executionDataVisitor = visitor;
	}

	public void setBundleCoverageVisitor(IBundleCoverageVisitor bundleCoverageVisitor) {
		this.bundleCoverageVisitor = bundleCoverageVisitor;
	}

	/**
	 * Reads all data and reports it to the corresponding visitors. The stream
	 * is read until its end or a command confirmation has been sent.
	 * 
	 * @return <code>true</code> if additional data can be expected after a
	 *         command has been executed. <code>false</code> if the end of the
	 *         stream has been reached.
	 * @throws IOException
	 *             might be thrown by the underlying input stream
	 */
	public boolean read() throws IOException {
		try {
			byte type;
			do {
				type = in.readByte();
				if (firstBlock && type != ExecutionDataWriter.BLOCK_HEADER) {
					throw new IOException("Invalid execution data file.");
				}
				firstBlock = false;
			} while (readBlock(type));
			return true;
		} catch (final EOFException e) {
			return false;
		}
	}

	/**
	 * Reads a block of data identified by the given id. Subclasses may
	 * overwrite this method to support additional block types.
	 * 
	 * @param blocktype
	 *            block type
	 * @return <code>true</code> if there are more blocks to read
	 * @throws IOException
	 *             might be thrown by the underlying input stream
	 */
	protected boolean readBlock(final byte blocktype) throws IOException {
		switch (blocktype) {
		case BundleCoverageDataWriter.BLOCK_HEADER:
			readHeader();
			return true;
		case BundleCoverageDataWriter.BLOCK_SESSIONINFO:
			readSessionInfo();
			return true;
		case BundleCoverageDataWriter.BLOCK_EXECUTIONDATA:
			readExecutionData();
			return true;
		case BundleCoverageDataWriter.BLOCK_BUNDLE_COVERAGE_DATA:
			readBundleCoverage();
			return true;
		default:
			throw new IOException(format("Unknown block type %x.", Byte.valueOf(blocktype)));
		}
	}

	private void readHeader() throws IOException {
		if (in.readChar() != ExecutionDataWriter.MAGIC_NUMBER) {
			throw new IOException("Invalid execution data file.");
		}
		final char version = in.readChar();
		if (version != ExecutionDataWriter.FORMAT_VERSION) {
			throw new IOException(format("Incompatible version %x.", Integer.valueOf(version)));
		}
	}

	private void readSessionInfo() throws IOException {
		if (sessionInfoVisitor == null) {
			throw new IOException("No session info visitor.");
		}
		final String id = in.readUTF();
		final long start = in.readLong();
		final long dump = in.readLong();
		if (sessionInfoVisitor != null) {
			sessionInfoVisitor.visitSessionInfo(new SessionInfo(id, start, dump));
		}
	}

	private void readExecutionData() throws IOException {
		if (executionDataVisitor == null) {
			throw new IOException("No execution data visitor.");
		}
		final long id = in.readLong();
		final String name = in.readUTF();
		final boolean[] probes = in.readBooleanArray();
		if (executionDataVisitor != null) {
			executionDataVisitor.visitClassExecution(new ExecutionData(id, name, probes));
		}
	}

	private void readBundleCoverage() {
		try {
			ICoverageNode coverageNode = readCoverageNode();
			int packageCoveragesSize = in.readVarInt();
			ArrayList<IPackageCoverage> packageCoverages = new ArrayList<IPackageCoverage>(packageCoveragesSize);
			for (int i = 0; i < packageCoveragesSize; ++i) {
				packageCoverages.add(readPackageCoverage());
			}
			if (bundleCoverageVisitor != null) {
				bundleCoverageVisitor.visitBundleCoverage(new BundleCoverage(coverageNode, packageCoverages));
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private IPackageCoverage readPackageCoverage() {
		try {
//			byte blocktype;
//			if ((blocktype = in.readByte()) != BundleCoverageDataWriter.BLOCK_PACKAGE_COVERAGE_DATA) {
//				throw new IOException(format("Unknown block type %x.", Byte.valueOf(blocktype)));
//			}
			ICoverageNode coverageNode = readCoverageNode();
			int classCoveragesSize = in.readVarInt();
			Collection<IClassCoverage> classCoverages = new ArrayList<IClassCoverage>(classCoveragesSize);
			for (int i = 0; i < classCoveragesSize; ++i) {
				classCoverages.add(readClassCoverage(coverageNode.getName()));
			}
			int sourceFileCoveragesSize = in.readVarInt();
			Collection<ISourceFileCoverage> sourceFileCoverages = new ArrayList<ISourceFileCoverage>(sourceFileCoveragesSize);
			for (int i = 0; i < sourceFileCoveragesSize; ++i) {
				sourceFileCoverages.add(readSourceFileCoverage(coverageNode.getName()));
			}
			return new PackageCoverage(coverageNode, classCoverages, sourceFileCoverages);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private IClassCoverage readClassCoverage(String packageName) {
		try {
//			byte blocktype;
//			if ((blocktype = in.readByte()) != BundleCoverageDataWriter.BLOCK_CLASS_COVERAGE_DATA) {
//				throw new IOException(format("Unknown block type %x.", Byte.valueOf(blocktype)));
//			}
			ISourceNode sourceNode = readSourceNode();
			long id = in.readLong();
			String signature = StringUtils.trimToNull(in.readUTF());
			String superName = StringUtils.trimToNull(in.readUTF());
			int interfaceNamesLength = in.readVarInt();
			String[] interfaceNames = new String[interfaceNamesLength];
			for (int i = 0; i < interfaceNamesLength; ++i) {
				interfaceNames[i] = in.readUTF();
			}
			if (false) {
				// String packageName = in.readUTF();
			}
			String sourceFileName = in.readUTF();
			int methodCoveragesSize = in.readVarInt();
			Collection<IMethodCoverage> methodCoverages = new ArrayList<IMethodCoverage>(methodCoveragesSize);
			for (int i = 0; i < methodCoveragesSize; ++i) {
				methodCoverages.add(readMethodCoverage());
			}
			return new ClassCoverage(sourceNode, packageName, id, signature, superName, interfaceNames, methodCoverages, sourceFileName);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private IMethodCoverage readMethodCoverage() {
		try {
//			byte blocktype;
//			if ((blocktype = in.readByte()) != BundleCoverageDataWriter.BLOCK_METHOD_COVERAGE_DATA) {
//				throw new IOException(format("Unknown block type %x.", Byte.valueOf(blocktype)));
//			}
			ISourceNode sourceNode = readSourceNode();
			String signature = StringUtils.trimToNull(in.readUTF());
			String desc = in.readUTF();
			return new MethodCoverage(sourceNode, signature, desc);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private ISourceFileCoverage readSourceFileCoverage(String packageName) {
		try {
//			byte blocktype;
//			if ((blocktype = in.readByte()) != BundleCoverageDataWriter.BLOCK_SOURCE_FILE_COVERAGE_DATA) {
//				throw new IOException(format("Unknown block type %x.", Byte.valueOf(blocktype)));
//			}
			ISourceNode sourceNode = readSourceNode();
			return new SourceFileCoverage(sourceNode, packageName);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private ISourceNode readSourceNode() {
		try {
			ICoverageNode coverageNode = readCoverageNode();
			boolean hasLine = in.readBoolean();
			if (!hasLine) {
				return new SourceNode(coverageNode, ISourceNode.UNKNOWN_LINE, ISourceNode.UNKNOWN_LINE);
			}
			int firstLine = in.readVarInt();
			int lastLine = in.readVarInt() + firstLine;
			SourceNode result = new SourceNode(coverageNode, firstLine, lastLine);
			for (int line = firstLine; line <= lastLine; ++line) {
				int status = in.readByte();
				ICounter branchCounter = readCounter();
				ICounter instructionCounter = readCounter();
				ILine lineCounter = new Line(status, branchCounter, instructionCounter);
				result.setLine(line, lineCounter);
			}
			return result;
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private ICoverageNode readCoverageNode() {
		try {
			int elementTypeId = in.readByte();
			ElementType elementType = ElementType.values()[elementTypeId];
			String name = in.readUTF();
			final ICounter tBranchCounter = readCounter();
			final ICounter tInstructionCounter = readCounter();
			final ICounter tLineCounter = readCounter();
			final ICounter tComplexityCounter = readCounter();
			final ICounter tMethodCounter = readCounter();
			final ICounter tClassCounter = readCounter();
			return new CoverageNode(elementType, name,//
					tBranchCounter,//
					tInstructionCounter,//
					tLineCounter,//
					tComplexityCounter,//
					tMethodCounter,//
					tClassCounter);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private ICounter readCounter() {
		try {
			int coveredCount = in.readVarInt();
			int missedCount = in.readVarInt();
			return CounterImpl.getInstance(missedCount, coveredCount);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}
