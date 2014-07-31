package com.nwgjb.xrmi;

import java.io.Serializable;

class ObjectHandler implements Serializable{
	private static final long serialVersionUID = 9102869658627611884L;
	Class<?> clazz;
	int id;
	
	public ObjectHandler(Class<?> clazz, int id){
		this.clazz=clazz;
		this.id=id;
	}
	
	public int hashCode(){
		return id;
	}
	
	public boolean equals(Object h){
		if(h instanceof ObjectHandler){
			return id==((ObjectHandler)h).id;
		}
		return false;
	}
	
}
