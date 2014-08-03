package com.nwgjb.compiler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class BindHelper {

	static boolean SOURCE_COMPRESS=true;
	static String METHOD_TEMPLATE="public {r} {n}({a}){return({r})i.invoke(\"{f}\"{w});}";
	static String METHOD_VOID_TEMPLATE="public void {n}({a}){i.invoke(\"{f}\"{w});}";
	static String CLASS_TEMPLATE="package com.nwjgb.compiler.dynamicbind;import com.nwgjb.compiler.*;"
			+ "public class {s}DynamicTarget implements {f}{DynamicInvokeTarget i;public {s}DynamicTarget(DynamicInvokeTarget a){i=a;}{m}}";
	static String METHOD_TEMPLATE_REV="case\"{f}\":return i.{n}({a});";
	static String METHOD_VOID_TEMPLATE_REV="case\"{f}\":i.{n}({a});return null;";
	static String CLASS_TEMPLATE_REV="package com.nwjgb.compiler.forwardbind;import com.nwgjb.compiler.*;"
			+ "public class {s}ForwardingBind implements DynamicInvokeTarget{{f} i;public {s}ForwardingBind({f} a){i=a;}"
			+ "public Object invoke(String n, Object... _){try{switch(n){{m}default:throw new UnsupportedOperationException();}}catch(RuntimeException e){throw e;}catch(Throwable e){throw new RuntimeException(e);}}}";
	
	static Hashtable<Class<?>, Class<?>> polyfill=new Hashtable<>();
	static Hashtable<Class<?>, Class<?>> forawrdBind=new Hashtable<>();
	
	static final Map<Class<?>, String> nameMap;
	static{
		HashMap<Class<?>, String> nm=new HashMap<>();
		nm.put(void.class, "V");
		nm.put(boolean.class, "Z");
		nm.put(byte.class, "B");
		nm.put(short.class, "S");
		nm.put(char.class, "C");
		nm.put(int.class, "I");
		nm.put(long.class, "J");
		nm.put(double.class, "D");
		nm.put(float.class, "F");
		
		nm.put(Object.class, "o");
		nm.put(String.class, "s");
		nm.put(Integer.class, "i");
		nm.put(Double.class, "d");
		nameMap=Collections.unmodifiableMap(nm);
	}
	
	private static String buildArgumentListWithType(Method m){
		/* Uncompressed
		StringBuilder sb=new StringBuilder();
		 
		int i=0;
		for(Class<?> t:m.getParameterTypes()){
			if(i>0){
				sb.append(", ");
			}
			sb.append(t.getCanonicalName()).append("arg").append(i++);
		}
		return sb.toString();
		*/
		
		StringBuilder sb=new StringBuilder();
		int i=0;
		for(Class<?> t:m.getParameterTypes()){
			if(i>0){
				sb.append(",");
			}
			sb.append(t.getCanonicalName()).append(" _").append(i++);
		}
		return sb.toString();
	}
	
	private static String buildArgumentListWithoutType(Method m){
		/* Uncompressed
		StringBuilder sb=new StringBuilder();
		int len=m.getParameterTypes().length;
		for (int i=0;i<len;i++) {
			sb.append(", arg").append(i);
		}
		return sb.toString();
		*/
		StringBuilder sb=new StringBuilder();
		int len=m.getParameterTypes().length;
		for (int i=0;i<len;i++) {
			sb.append(",_").append(i);
		}
		return sb.toString();
	}
	

	private static String buildArgumentListFromVarargs(Method m) {
		StringBuilder sb=new StringBuilder();
		int i=0;
		for(Class<?> t:m.getParameterTypes()){
			if(i>0){
				sb.append(", ");
			}
			sb.append('(').append(t.getCanonicalName()).append(")_[").append(i++).append(']');
		}
		return sb.toString();
	}
	
	private static String getTypeSignature(Class<?> t){
		if(nameMap.containsKey(t)){
			return nameMap.get(t);
		}else{
			return t.getCanonicalName();
		}
	}
	
	private static String generateSignature(Method m){
		StringBuilder b=new StringBuilder(m.getName());
		b.append('(');
		for(Class<?> t:m.getParameterTypes()){
			b.append(getTypeSignature(t));
		}
		b.append(')');
		b.append(getTypeSignature(m.getReturnType()));
		return b.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Class<? extends T> createDynamicBind(Class<T> intf){
		if(polyfill.containsKey(intf)){
			return (Class<? extends T>) polyfill.get(intf);
		}
		StringBuilder mpf=new StringBuilder();
		for(Method m:intf.getDeclaredMethods()){
			if(m.getReturnType()==Void.TYPE){
				mpf.append(METHOD_VOID_TEMPLATE
						.replaceAll("\\{n\\}", m.getName())
						.replaceAll("\\{f\\}", generateSignature(m))
						.replaceAll("\\{a\\}", buildArgumentListWithType(m))
						.replaceAll("\\{w\\}", buildArgumentListWithoutType(m))
						);
			}else{
				mpf.append(METHOD_TEMPLATE
						.replaceAll("\\{r\\}", m.getReturnType().getCanonicalName())
						.replaceAll("\\{n\\}", m.getName())
						.replaceAll("\\{f\\}", generateSignature(m))
						.replaceAll("\\{a\\}", buildArgumentListWithType(m))
						.replaceAll("\\{w\\}", buildArgumentListWithoutType(m))
						);
			}
		}
		String gen=CLASS_TEMPLATE
				.replaceAll("\\{s\\}", intf.getSimpleName())
				.replaceAll("\\{f\\}", intf.getCanonicalName())
				.replaceAll("\\{m\\}", mpf.toString());
		//System.out.println(gen);
		Class<?> c=MemoryCompiler.getClass("com.nwjgb.compiler.dynamicbind."+intf.getSimpleName()+"DynamicTarget", gen);
		polyfill.put(intf, c);
		return (Class<? extends T>) c;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Class<? extends DynamicInvokeTarget> createForwardingInvocationBinder(Class<T> intf){
		if(forawrdBind.containsKey(intf)){
			return (Class<? extends DynamicInvokeTarget>) forawrdBind.get(intf);
		}
		StringBuilder mpf=new StringBuilder();
		for(Method m:intf.getDeclaredMethods()){
			if(m.getReturnType()==Void.TYPE){
				mpf.append(METHOD_VOID_TEMPLATE_REV
						.replaceAll("\\{n\\}", m.getName())
						.replaceAll("\\{f\\}", generateSignature(m))
						.replaceAll("\\{a\\}", buildArgumentListFromVarargs(m))
						);
			}else{
				mpf.append(METHOD_TEMPLATE_REV
						.replaceAll("\\{n\\}", m.getName())
						.replaceAll("\\{f\\}", generateSignature(m))
						.replaceAll("\\{a\\}", buildArgumentListFromVarargs(m))
						);
			}
		}
		String gen=CLASS_TEMPLATE_REV
				.replaceAll("\\{s\\}", intf.getSimpleName())
				.replaceAll("\\{f\\}", intf.getCanonicalName())
				.replaceAll("\\{m\\}", mpf.toString());
		//System.out.println(gen);
		Class<?> c=MemoryCompiler.getClass("com.nwjgb.compiler.forwardbind."+intf.getSimpleName()+"ForwardingBind", gen);
		forawrdBind.put(intf, c);
		return (Class<? extends DynamicInvokeTarget>) c;
	}
	

	public static <T> T createDynamicBind(Class<T> intf, DynamicInvokeTarget impl){
		Class<? extends T> cl=createDynamicBind(intf);
		try {
			return cl.getConstructor(DynamicInvokeTarget.class).newInstance(impl);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> DynamicInvokeTarget createForwardingInvocationBinder(Class<T> intf, T impl){
		Class<? extends DynamicInvokeTarget> cl=createForwardingInvocationBinder(intf);
		try {
			return cl.getConstructor(intf).newInstance(impl);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static DynamicInvokeTarget createForwardingInvocationBinder(Object impl){
		return createForwardingInvocationBinder((Class<Object>)getBindingInterface(impl.getClass()), impl);
	}
	
	public static Class<?> getBindingInterface(Class<?> sub){
		Class<?>[] c=(Class<?>[]) sub.getInterfaces();
		if(c.length==0){
			if(sub.getSuperclass()!=null){
				return getBindingInterface(sub.getSuperclass());
			}else{
				throw new RuntimeException("No suitable bind interface for "+sub);
			}
		}
		return c[0];
	}
}
