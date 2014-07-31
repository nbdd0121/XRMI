package com.nwgjb.xrmi.test;


public interface ChatTerminal{
	void println(String str);
	void set(ChatTerminal t) throws Throwable;
}
