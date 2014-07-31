package com.nwgjb.xrmi;



class MethodInvocationPackage implements Package{

	private static final long serialVersionUID = 5109158807947436035L;
	
	int packId;
	Object object;
	String descriptor;
	Object[] args;
	
	public MethodInvocationPackage(int id, Object object, String descriptor, Object... args){
		this.packId=id;
		this.object=object;
		this.descriptor=descriptor;
		this.args=args;
	}
	
}
