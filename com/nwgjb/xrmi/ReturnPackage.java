package com.nwgjb.xrmi;


class ReturnPackage implements Package {

	private static final long serialVersionUID = -3967706283083708008L;

	int id;
	Object ret;
	
	public ReturnPackage(int id, Object ret){
		this.id=id;
		this.ret=ret;
	}
	
}
