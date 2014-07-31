package com.nwgjb.xrmi;

import com.nwgjb.compiler.DynamicInvokeTarget;

class InternetInvoker implements DynamicInvokeTarget{
	
	RMIConnection conn;
	Object obj;
	
	public InternetInvoker(RMIConnection conn){
		this.conn=conn;
	}
	
	@Override
	public Object invoke(String method, Object... args) {
		return conn.invoke(obj, method, args);
	}
	
	
	
}
