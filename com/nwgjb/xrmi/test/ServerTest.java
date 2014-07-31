package com.nwgjb.xrmi.test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

import com.nwgjb.xrmi.RMIConnection;

public class ServerTest {
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		ChatTerminal func=new ChatTerminal() {
			@Override
			public void println(String str) {
				System.out.println(str);
				throw new Error("Gary sucks");
			}

			@Override
			public void set(final ChatTerminal t) {
				new Thread(new Runnable(){
					@Override
					public void run() {
						Scanner s=new Scanner(System.in);
						while(true){
							t.println(s.nextLine());
						}
					}
				}).start();
			}
		};
		new RMIConnection(new ServerSocket(1000).accept(), func);
	}

}
