package com.nwgjb.compiler;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

class MemoryByteCode extends SimpleJavaFileObject {   
    private ByteArrayOutputStream content=new ByteArrayOutputStream(); 
    
    public MemoryByteCode(String name) { 
        super(URI.create("memory:///"+name+".class"), Kind.CLASS);   
    }
    
    public OutputStream openOutputStream() {       
        return content;   
    }

    public byte[] getBytes() {       
        return content.toByteArray();   
    }
}