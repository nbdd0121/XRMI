package com.nwgjb.compiler;

import java.util.HashMap;

class MemoryClassLoader extends ClassLoader{
	private HashMap<String, MemoryByteCode> byteCodes=new HashMap<String, MemoryByteCode>();
	 
    protected Class<?> findClass(String name) throws ClassNotFoundException {
    	String slashedName=name.replace(".","/");
        MemoryByteCode mbc=byteCodes.get(slashedName);       
        if (byteCodes==null){
            mbc=byteCodes.remove(slashedName);           
            if (mbc==null){               
                return super.findClass(name);           
            }       
        }
        return defineClass(name, mbc.getBytes(), 0, mbc.getBytes().length);
    }
 
    public void addClass(String name, MemoryByteCode mbc) {       
    	byteCodes.put(name, mbc);   
    }
}
