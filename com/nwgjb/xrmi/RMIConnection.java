package com.nwgjb.xrmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.nwgjb.commons.util.Mapping;
import com.nwgjb.compiler.BindHelper;
import com.nwgjb.compiler.DynamicInvokeTarget;

public class RMIConnection {
	
	Socket socket;
	ObjectInputStream in;
	ObjectOutputStream out;
	
	ExecutorService pool=Executors.newCachedThreadPool();
	LinkedList<Integer> freeId=new LinkedList<>();
	
	Mapping<Object, DynamicInvokeTarget> targets=new Mapping<>();
	Mapping<Object, ObjectHandler> handlers=new Mapping<>();
	
	Object bind;
	
	int lastFreeId=16;
	{
		for(int i=0;i<16;i++){
			freeId.add(i);
		}
	}
	Hashtable<Integer, Object[]> waitList=new Hashtable<>(); 
	int handlerId;
	
	private synchronized int getFreeID(){
		if(freeId.isEmpty()){
			for(int i=0;i<16;i++){
				freeId.add(i+lastFreeId);
			}
			lastFreeId+=16;
		}
		int i=freeId.removeFirst();
		return i;
	}
	
	private synchronized void returnID(int id){
		freeId.addFirst(id);
	}
	
	private ObjectHandler getTarget(Object o){
		if(targets.containsT1(o)){
			return handlers.getT2(o);
		}else{
			DynamicInvokeTarget dit=BindHelper.createForwardingInvocationBinder(o);
			ObjectHandler handler=new ObjectHandler(BindHelper.getBindingInterface(o.getClass()), handlerId++);
			targets.put(o, dit);
			handlers.put(o, handler);
			return handler;
		}
	}
	
	private Object getObject(ObjectHandler h){
		if(handlers.containsT2(h)){
			return handlers.getT1(h);
		}else{
			InternetInvoker t=new InternetInvoker(RMIConnection.this);
			Object simulated=BindHelper.createDynamicBind(h.clazz, t);
			t.obj=simulated;
			targets.put(simulated, t);
			handlers.put(simulated, h);
			return simulated;
		}
	}
	
	/**
	 * Create a RMIConnection instance without a bind
	 * @param socket Socket to build connections on
	 * @throws IOException Thrown if IO exception occurs during networking
	 */
	public RMIConnection(Socket socket) throws IOException{
		this(socket, null);
	}
	
	/**
	 * Create a RMIConnection instance with a bind
	 * @param socket Socket to build connections on
	 * @param bind Bind a value, so the other terminal can call getBind() to get that
	 * @throws IOException Thrown if IO exception occurs during networking
	 */
	public RMIConnection(Socket socket, Object bind) throws IOException{
		this.socket=socket;
		out=new ObjectOutputStream(socket.getOutputStream()){
			{
				this.enableReplaceObject(true);
			}
			public Object replaceObject(Object o){
				if(o instanceof Serializable){
					return o;
				}
				return getTarget(o);
			}
		};
		out.flush();
		in=new ObjectInputStream(socket.getInputStream()){
			{
				this.enableResolveObject(true);
			}
			public Object resolveObject(Object o){
				if(o instanceof ObjectHandler){
					return getObject((ObjectHandler)o);
				}else{
					return o;
				}
			}
		};
		if(bind!=null){
			handlerId=Integer.MIN_VALUE;
			getTarget(bind);
			this.bind=bind;
		}
		new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					receivingPackage();
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	private void receivingPackage() throws ClassNotFoundException, IOException {
		while(true){
			Object p=in.readObject();
			if(p instanceof MethodInvocationPackage){
				final MethodInvocationPackage pack=(MethodInvocationPackage)p;
				if(pack.object==null){
					send(new ReturnPackage(pack.packId, bind));
				}else{
					pool.submit(new Runnable(){
						@Override
						public void run(){
							Package rp;
							try {
								DynamicInvokeTarget target=targets.getT2(pack.object);
								rp=new ReturnPackage(pack.packId, target.invoke(pack.descriptor, pack.args));
							} catch (RuntimeException e) {
								rp=new ThrowPackage(pack.packId, e);
							}
							try {
								send(rp);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
				}
			}else if(p instanceof ReturnPackage){
				final ReturnPackage returnPack=(ReturnPackage)p;
				Object[] ret=waitList.get(returnPack.id);
				ret[0]=returnPack.ret;
				waitList.remove(returnPack.id);
				returnID(returnPack.id);
				synchronized(ret){
					ret.notifyAll();
				}
			}else{
				final ThrowPackage throwPack=(ThrowPackage)p;
				Object[] ret=waitList.get(throwPack.id);
				ret[1]=throwPack.ret;
				waitList.remove(throwPack.id);
				returnID(throwPack.id);
				synchronized(ret){
					ret.notifyAll();
				}
			}
		}
	}

	private synchronized void send(Package pack) throws IOException{
		out.writeObject(pack);
		out.flush();
		out.reset();
	}
	
	Object invoke(Object obj, String str, Object... args){
		try {
			Object[] ret=new Object[2];
			int id=getFreeID();
			synchronized(ret){
				waitList.put(id, ret);
				send(new MethodInvocationPackage(id, obj, str, args));
				ret.wait();
			}
			if(ret[1]!=null){
				throw (RuntimeException)ret[1];
			}
			return ret[0];
		}catch(RuntimeException e){
			throw e;
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Get the binded object from the other terminal
	 * @return Bind on the other terminal
	 */
	public Object getBind(){
		return invoke(null, null);
	}
	
}
