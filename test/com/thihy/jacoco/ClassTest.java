package com.thihy.jacoco;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;

import org.junit.Test;

public class ClassTest {
	@Test
	public void test() throws IOException, URISyntaxException {
		String path ="/"+ this.getClass().getName().replace('.', '/')+".classs";
		System.out.println(path);
		Enumeration<URL> urls = getClass().getClassLoader().getResources(path);
		while (urls.hasMoreElements()) {
			File file = new File(urls.nextElement().toURI());
			if (file.isDirectory()) {
				File[] childFiles = file.listFiles();
				for (File childFile : childFiles) {
					System.out.println(childFile);
				}
			}
		}
	}
}
