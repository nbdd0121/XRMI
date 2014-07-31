package com.nwgjb.xrmi;


class ThrowPackage implements Package {

	private static final long serialVersionUID = 3871045503810631175L;
	
	int id;
	RuntimeException ret;
	
	public ThrowPackage(int id, RuntimeException ret){
		this.id=id;
		this.ret=ret;
	}
	
}
