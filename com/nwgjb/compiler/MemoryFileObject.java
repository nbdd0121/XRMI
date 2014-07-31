package com.nwgjb.compiler;

import java.net.URI;

import javax.tools.SimpleJavaFileObject;

class MemoryFileObject extends SimpleJavaFileObject{

	String code;
	
	public MemoryFileObject(String name, String code) {
		super(URI.create("file:///"+name.replace('.', '/')+Kind.SOURCE.extension), Kind.SOURCE);
		this.code=code;
	}

	@Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors){
        return code;
    }
	
}
