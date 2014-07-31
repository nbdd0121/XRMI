package com.nwgjb.compiler;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;

import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

public class MemoryCompiler {

	static final JavaCompiler compiler=new EclipseCompiler();
	static StandardJavaFileManager stdfileMan=compiler.getStandardFileManager(null, null, null);
	static MemoryClassLoader loader=new MemoryClassLoader();
	static MemoryFileManager fileManager = new MemoryFileManager(stdfileMan, loader); 
	static ByteArrayOutputStream err=new ByteArrayOutputStream();
	static Writer errWriter=new OutputStreamWriter(err);
	
	public static Class<?> getClass(String className, String source){
		try{
	        JavaCompiler.CompilationTask compile=compiler.getTask(errWriter, fileManager, null, null, null, Arrays.asList(new MemoryFileObject(className, source)));           
	        boolean res=compile.call();    
	        if(res){ 
	            Class<?> c=loader.loadClass(className);
	            return c;
	        }else{
	        	System.err.println(err.toString());
	        	throw new RuntimeException("Compilation Failed");
	        }
		}catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}finally{
			err.reset();
		}
	}

	
}

