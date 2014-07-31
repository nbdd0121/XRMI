package com.nwgjb.xrmi.test;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import com.nwgjb.xrmi.RMIConnection;

public class Test {
	
	public static void main(String[] args) throws IOException {
		RMIConnection pc=new RMIConnection(new Socket("127.0.0.1", 1000));
		ChatTerminal func=new ChatTerminal() {
			@Override
			public void println(String str) {
				System.out.println(str);
			}

			@Override
			public void set(final ChatTerminal t) {
				new Thread(new Runnable(){
					@Override
					public void run() {
						@SuppressWarnings("resource")
						Scanner s=new Scanner(System.in);
						while(true){
							t.println(s.nextLine());
						}
					}
				}).start();
			}
		};
		ChatTerminal server=(ChatTerminal)pc.getBind();
		try {
			func.set(server);
			server.set(func);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
	}

}
