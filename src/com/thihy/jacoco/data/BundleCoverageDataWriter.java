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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
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
import org.jacoco.core.internal.data.CompactDataOutput;

/**
 * Serialization of execution data into binary streams.
 */
public class BundleCoverageDataWriter implements ISessionInfoVisitor, IExecutionDataVisitor {

	/** File format version, will be incremented for each incompatible change. */
	public static final char FORMAT_VERSION = 0x1006;

	/** Magic number in header for file format identification. */
	public static final char MAGIC_NUMBER = 0xC0C0;

	/** Block identifier for file headers. */
	public static final byte BLOCK_HEADER = 0x01;

	/** Block identifier for session information. */
	public static final byte BLOCK_SESSIONINFO = 0x10;

	/** Block identifier for execution data of a single class. */
	public static final byte BLOCK_EXECUTIONDATA = 0x11;

	/** Block identifier for bundle coverage data. */
	public static final byte BLOCK_BUNDLE_COVERAGE_DATA = 0x1f;

	/** Underlying data output */
	protected final CompactDataOutput out;

	/**
	 * Creates a new writer based on the given output stream. Depending on the
	 * nature of the underlying stream output should be buffered as most data is
	 * written in single bytes.
	 * 
	 * @param output
	 *            binary stream to write execution data to
	 * @throws IOException
	 *             if the header can't be written
	 */
	public BundleCoverageDataWriter(final OutputStream output) throws IOException {
		this.out = new CompactDataOutput(output);
		writeHeader();
	}

	/**
	 * Writes an file header to identify the stream and its protocol version.
	 * 
	 * @throws IOException
	 *             if the header can't be written
	 */
	private void writeHeader() throws IOException {
		out.writeByte(BLOCK_HEADER);
		out.writeChar(MAGIC_NUMBER);
		out.writeChar(FORMAT_VERSION);
	}

	/**
	 * Flushes the underlying stream.
	 * 
	 * @throws IOException
	 *             if the underlying stream can't be flushed
	 */
	public void flush() throws IOException {
		out.flush();
	}

	public void visitSessionInfo(final SessionInfo info) {
		try {
			out.writeByte(BLOCK_SESSIONINFO);
			out.writeUTF(info.getId());
			out.writeLong(info.getStartTimeStamp());
			out.writeLong(info.getDumpTimeStamp());
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void visitClassExecution(final ExecutionData data) {
		try {
			out.writeByte(BLOCK_EXECUTIONDATA);
			out.writeLong(data.getId());
			out.writeUTF(data.getName());
			out.writeBooleanArray(data.getProbes());
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void visitBundleCoverage(final IBundleCoverage bundleCoverage) {
		try {
			out.writeByte(BLOCK_BUNDLE_COVERAGE_DATA);
			writeCoverageNode(bundleCoverage);
			Collection<IPackageCoverage> packageCoverages = bundleCoverage.getPackages();
			if (packageCoverages == null || packageCoverages.isEmpty()) {
				out.writeVarInt(0);
			} else {
				out.writeVarInt(packageCoverages.size());
				for (IPackageCoverage packageCoverage : packageCoverages) {
					writePackageCoverage(packageCoverage);
				}
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writePackageCoverage(IPackageCoverage packageCoverage) {
		try {
			//out.writeByte(BLOCK_PACKAGE_COVERAGE_DATA);
			writeCoverageNode(packageCoverage);
			Collection<IClassCoverage> classCoverages = packageCoverage.getClasses();
			if (classCoverages == null || classCoverages.isEmpty()) {
				out.writeVarInt(0);
			} else {
				out.writeVarInt(classCoverages.size());
				for (IClassCoverage classCoverage : classCoverages) {
					writeClassCoverage(classCoverage);
				}
			}
			Collection<ISourceFileCoverage> sourceFileCoverages = packageCoverage.getSourceFiles();
			if (sourceFileCoverages == null || sourceFileCoverages.isEmpty()) {
				out.writeVarInt(0);
			} else {
				out.writeVarInt(sourceFileCoverages.size());
				for (ISourceFileCoverage sourceFileCoverage : sourceFileCoverages) {
					writeSourceFileCoverage(sourceFileCoverage);
				}
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeClassCoverage(IClassCoverage classCoverage) {
		try {
			//out.writeByte(BLOCK_CLASS_COVERAGE_DATA);
			writeSourceNode(classCoverage);
			out.writeLong(classCoverage.getId());
			out.writeUTF(StringUtils.trimToEmpty(classCoverage.getSignature()));
			out.writeUTF(StringUtils.trimToEmpty(classCoverage.getSuperName()));
			String[] interfaceNames = classCoverage.getInterfaceNames();
			if (interfaceNames == null) {
				out.writeVarInt(0);
			} else {
				out.writeVarInt(interfaceNames.length);
				for (int i = 0; i < interfaceNames.length; i++) {
					out.writeUTF(interfaceNames[i]);
				}
			}
			if (false) {
				out.writeUTF(classCoverage.getPackageName());
			}
			out.writeUTF(classCoverage.getSourceFileName());
			Collection<IMethodCoverage> methodCoverages = classCoverage.getMethods();
			if (methodCoverages == null || methodCoverages.isEmpty()) {
				out.writeVarInt(0);
			} else {
				out.writeVarInt(methodCoverages.size());
				for (IMethodCoverage methodCoverage : methodCoverages) {
					writeMethodCoverage(methodCoverage);
				}
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeMethodCoverage(IMethodCoverage methodCoverage) {
		try {
			//out.writeByte(BLOCK_METHOD_COVERAGE_DATA);
			writeSourceNode(methodCoverage);
			out.writeUTF(StringUtils.trimToEmpty(methodCoverage.getSignature()));
			out.writeUTF(methodCoverage.getDesc());
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeSourceFileCoverage(ISourceFileCoverage sourceFileCoverage) {
		try {
			//out.writeByte(BLOCK_SOURCE_FILE_COVERAGE_DATA);
			writeSourceNode(sourceFileCoverage);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void writeSourceNode(ISourceNode sourceNode) {
		try {
			writeCoverageNode(sourceNode);
			int firstLine = sourceNode.getFirstLine();
			int lastLine = sourceNode.getLastLine();
			if (firstLine == ISourceNode.UNKNOWN_LINE) {
				out.writeBoolean(false);
				return;
			}
			out.writeBoolean(true);
			out.writeVarInt(firstLine);
			out.writeVarInt(lastLine - firstLine);// length -1
			for (int line = firstLine; line <= lastLine; ++line) {
				ILine lineCounter = sourceNode.getLine(line);
				out.writeByte(lineCounter.getStatus());
				writeCounter(lineCounter.getInstructionCounter());
				writeCounter(lineCounter.getBranchCounter());
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeCoverageNode(ICoverageNode coverage) {
		try {
			out.writeByte(coverage.getElementType().ordinal());
			out.writeUTF(coverage.getName());
			writeCounter(coverage.getBranchCounter());
			writeCounter(coverage.getInstructionCounter());
			writeCounter(coverage.getLineCounter());
			writeCounter(coverage.getComplexityCounter());
			writeCounter(coverage.getMethodCounter());
			writeCounter(coverage.getClassCounter());
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeCounter(ICounter counter) {
		try {
			out.writeVarInt(counter.getCoveredCount());
			out.writeVarInt(counter.getMissedCount());
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the first bytes of a file that represents a valid execution data
	 * file. In any case every execution data file starts with the three bytes
	 * <code>0x01 0xC0 0xC0</code>.
	 * 
	 * @return first bytes of a execution data file
	 */
	public static final byte[] getFileHeader() {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			new ExecutionDataWriter(buffer);
		} catch (final IOException e) {
			// Must not happen with ByteArrayOutputStream
			throw new AssertionError(e);
		}
		return buffer.toByteArray();
	}

}
