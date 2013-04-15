package com.thihy.jacoco.data;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.html.HTMLFormatter;
import org.junit.Test;

public class BundleCoverageDataReaderWriterTest {

	@Test
	public void test() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException {
		MemoryRuntimeData runtimeData = Offline.data;
		OfflineInstrumentationAccessGenerator runtime = new OfflineInstrumentationAccessGenerator() {
		};
		Instrumenter instrumenter = new Instrumenter(runtime);
		String className = KMP.class.getName();
		byte[] classBytes = instrumenter.instrument(getTargetClass(className));
		MemoryClassLoader memoryClassLoader = new MemoryClassLoader();
		memoryClassLoader.addDefinition(className, classBytes);
		Class<?> clazz = memoryClassLoader.loadClass(className);
		Object kmp = clazz.newInstance();
		int pos = (Integer) kmp.getClass().getMethod("find", char[].class, char[].class).invoke(kmp, "12345678abcdefg".toCharArray(), "abc".toCharArray());
		assertEquals(8, pos);
		OutputStream output = new ByteArrayOutputStream();
		final ExecutionDataWriter writer = new ExecutionDataWriter(output);
		runtimeData.collect(writer, writer, false);
		IBundleCoverage bundleCoverage = analyzeStructure(runtimeData.getStore());
		boolean zip = true;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream zipOutputStream = new GZIPOutputStream(baos);
		OutputStream bundleCoverageOutputStream = zip ? zipOutputStream : baos;
		BundleCoverageDataWriter bundleCoverageDataWriter = new BundleCoverageDataWriter(bundleCoverageOutputStream);
		runtimeData.collect(bundleCoverageDataWriter, bundleCoverageDataWriter, false);
		bundleCoverageDataWriter.visitBundleCoverage(bundleCoverage);
		bundleCoverageDataWriter.flush();
		bundleCoverageOutputStream.close();

		byte[] byteArray = baos.toByteArray();
		System.out.println(byteArray.length);
		ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
		GZIPInputStream zipInputStream = new GZIPInputStream(bais);
		InputStream bundleCoverageInputStream = zip ? zipInputStream : bais;
		BundleCoverageDataReader bundleCoverageDataReader = new BundleCoverageDataReader(bundleCoverageInputStream);
		BundleCoverageStore bundleCoverageStore = new BundleCoverageStore();
		SessionInfoStore sessionInfoStore = new SessionInfoStore();
		ExecutionDataStore executionDataStore = new ExecutionDataStore();
		bundleCoverageDataReader.setSessionInfoVisitor(sessionInfoStore);
		bundleCoverageDataReader.setExecutionDataVisitor(executionDataStore);
		bundleCoverageDataReader.setBundleCoverageVisitor(bundleCoverageStore);
		bundleCoverageDataReader.read();
		// Report
		// XMLFormatter formatter = new XMLFormatter();
		// IReportVisitor formatterVisitor = formatter.createVisitor(new
		// FileOutputStream("a.xml"));
		HTMLFormatter formatter = new HTMLFormatter();
		IReportVisitor formatterVisitor = formatter.createVisitor(new FileMultiReportOutput(new File("report")));
		formatterVisitor.visitInfo(sessionInfoStore.getInfos(), executionDataStore.getContents());
		ISourceFileLocator locator = new DirectorySourceFileLocator(new File("test"), "utf-8", 4);
		for (IBundleCoverage coverage : bundleCoverageStore.getBundleCoverages()) {
			formatterVisitor.visitBundle(coverage, locator);
		}
		formatterVisitor.visitEnd();
	}

	private InputStream getTargetClass(final String name) {
		final String resource = '/' + name.replace('.', '/') + ".class";
		return getClass().getResourceAsStream(resource);
	}

	/**
	 * A class loader that loads classes from in-memory data.
	 */
	public static class MemoryClassLoader extends ClassLoader {

		private final Map<String, byte[]> definitions = new HashMap<String, byte[]>();

		/**
		 * Add a in-memory representation of a class.
		 * 
		 * @param name
		 *            name of the class
		 * @param bytes
		 *            class definition
		 */
		public void addDefinition(final String name, final byte[] bytes) {
			definitions.put(name, bytes);
		}

		@Override
		protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
			final byte[] bytes = definitions.get(name);
			if (bytes != null) {
				return defineClass(name, bytes, 0, bytes.length);
			}
			return super.loadClass(name, resolve);
		}

	}

	class ExecutionRuntimeData {
		public final ExecutionDataStore executionDataStore;
		public final SessionInfoStore sessionInfoStore;

		public ExecutionRuntimeData(ExecutionDataStore executionDataStore, SessionInfoStore sessionInfoStore) {
			super();
			this.executionDataStore = executionDataStore;
			this.sessionInfoStore = sessionInfoStore;
		}

	}

	private ExecutionRuntimeData loadExecutionData(InputStream is) throws IOException {
		final ExecutionDataReader executionDataReader = new ExecutionDataReader(is);
		ExecutionDataStore executionDataStore = new ExecutionDataStore();
		SessionInfoStore sessionInfoStore = new SessionInfoStore();

		executionDataReader.setExecutionDataVisitor(executionDataStore);
		executionDataReader.setSessionInfoVisitor(sessionInfoStore);

		while (executionDataReader.read()) {
		}

		is.close();
		return new ExecutionRuntimeData(executionDataStore, sessionInfoStore);
	}

	private IBundleCoverage analyzeStructure(ExecutionDataStore executionDataStore) throws IOException {
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(executionDataStore, coverageBuilder);
		Collection<ExecutionData> executionDatas = executionDataStore.getContents();
		for (ExecutionData executionData : executionDatas) {
			InputStream classBytes = getTargetClass(executionData.getName());
			analyzer.analyzeAll(classBytes);
		}
		return coverageBuilder.getBundle("test");
	}

}
