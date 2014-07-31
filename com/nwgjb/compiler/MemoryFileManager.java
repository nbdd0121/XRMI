package com.nwgjb.compiler;

import java.io.IOException;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

class MemoryFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

	MemoryClassLoader loader;
	
	public MemoryFileManager(StandardJavaFileManager fileManager, MemoryClassLoader loader) {
		super(fileManager);
		this.loader=loader;
	}

	public JavaFileObject getJavaFileForOutput(Location location, String name, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        MemoryByteCode mbc=new MemoryByteCode(name);       
        loader.addClass(name, mbc);       
        return mbc;   
    }
 
    public ClassLoader getClassLoader(Location location) {       
        return loader;   
    }
   
}  