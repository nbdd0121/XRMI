package com.nwgjb.compiler;

public interface DynamicInvokeTarget {
	Object invoke(String method, Object... args);
}
