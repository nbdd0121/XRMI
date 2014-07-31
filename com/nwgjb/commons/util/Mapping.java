package com.nwgjb.commons.util;

import java.util.Hashtable;

public class Mapping<T1, T2> {
	
	Hashtable<T1, T2> t12t2=new Hashtable<T1, T2>();
	Hashtable<T2, T1> t22t1=new Hashtable<T2, T1>();
	
	public synchronized T2 getT2(T1 t){
		return t12t2.get(t);
	}
	
	public synchronized T1 getT1(T2 t){
		return t22t1.get(t);
	}
	
	public synchronized void put(T1 t1, T2 t2){
		t12t2.put(t1, t2);
		t22t1.put(t2, t1);
	}
	
	public synchronized void removeT1(T1 t1){
		T2 t2=t12t2.remove(t1);
		t22t1.remove(t2);
	}
	
	public synchronized void removeT2(T2 t2){
		T1 t1=t22t1.remove(t2);
		t12t2.remove(t1);
	}

	public synchronized boolean containsT1(T1 t1) {
		return t12t2.containsKey(t1);
	}
	
	public synchronized boolean containsT2(T2 t2) {
		return t22t1.containsKey(t2);
	}
}
