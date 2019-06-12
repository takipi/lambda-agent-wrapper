package com.sparktale.bugtale.meta.lambda;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;
import com.sun.tools.attach.VirtualMachine;

public class LambdaWrapper {
	public static void main(String[] args) {
		new LambdaWrapper().execute(null, null);
	}
	
	public static class Request {
		public long requestId;
		public Object[] params;
	}
	
	public void execute(Request input, Context context) {
		String realLambda = System.getenv("REAL_LAMBDA");
		
		if (realLambda == null) {
			System.err.println("No lambda provided. Please set REAL_LAMBDDA environment variable");
			return;
		}
		
		System.out.println("[LW] Real lambda is: " + realLambda);

		init();

		String[] realLambdaPair = realLambda.split("::");

		try {
			String className = realLambdaPair[0];
			String methodName = realLambdaPair[1];

			Class<?> clazz = Class.forName(className);

			Method[] methods = clazz.getMethods();

			for (Method method : methods) {
				if (method.getName().equals(methodName)) {
					System.out.println("[LW] Lambda found");

					// Initializing new object
					Object o = clazz.newInstance();

					System.out.println("[LW] Invoking");

					Gson gson = new Gson();
					String json = gson.toJson(input);
					
					// Invoking the real lambda handler passing input and context
					method.invoke(o, json, context);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void init() {
		System.out.println("[LW] About to init agent");

		String agentPath 	= System.getProperty("javaagent.path");
		String agentOptions = System.getProperty("javaagent.options");

		if (agentPath != null) {
			System.out.println("About to load agent: " + agentPath);

			loadAgent(agentPath, agentOptions);
		}
	}

	private void loadAgent(String jarFilePath, String options) {
		try {
			String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
			String pid = nameOfRunningVM.substring(0, nameOfRunningVM.indexOf('@'));
			
			VirtualMachine vm = VirtualMachine.attach(pid);
			
			vm.loadAgent(jarFilePath, options != null ? options : "");
			
			vm.detach();
		} catch (Throwable e) {
			System.err.println("Problem loading " + jarFilePath);

			e.printStackTrace();
		}
	}
}
