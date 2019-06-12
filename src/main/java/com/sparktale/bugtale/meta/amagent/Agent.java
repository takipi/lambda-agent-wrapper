package com.sparktale.bugtale.meta.amagent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;

import com.sun.tools.attach.VirtualMachine;

public class Agent {
	
	// Agents loaded from the command line -javaagent:agent.jar
	public static void premain(String agentArgs, Instrumentation inst) {
		try {
			internalPremain(agentArgs, inst);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Agents loaded dynamically in runtime
	public static void agentmain(String agentArgs, Instrumentation inst) {
		try {
			internalPremain(agentArgs, inst);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void internalPremain(String agentArgs, Instrumentation inst) throws IOException {
		System.out.println("[DA] Takipi allocation monitor agent loaded. Args:" + agentArgs);

		String optionsStr = null;

		if ((agentArgs == null) || (agentArgs.length() == 0) || (agentArgs.equals("null"))) {
			optionsStr = System.getProperty("debugagent.args");
		}

		if (optionsStr == null) {
			optionsStr = "c=java.util.ArrayList";
		}

		Options options = Options.parse(optionsStr);

		String targetClassName = options.getTargetClassName();
		String outputFilePrefix = options.getOutputFilePrefix();

		System.out.println("[DA]  Target class name: " + targetClassName);
		String outputFileName = null;

		if (outputFilePrefix != null) {
			outputFileName = outputFilePrefix + "." + Long.toString(System.currentTimeMillis());
			System.out.println("[DA]  Output file name:  " + outputFileName);
		} else {
			System.out.println("[DA]  Output file null, default to System.out  ");
		}

		Transformer transformer = new Transformer(targetClassName);
		Recorder recorder = new Recorder(outputFileName);

		Monitor.init(recorder);

		inst.addTransformer(transformer, true);

		try {
			Class<?> targetCls = Class.forName(targetClassName);

			inst.retransformClasses(targetCls);
		} catch (Throwable e) {
			System.err.print("[DA] Problem retransforming " + targetClassName);

			e.printStackTrace();
		}
	}

	public void loadAgent(String jarFilePath, String options) {
		try {
			String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
			String pid = nameOfRunningVM.substring(0, nameOfRunningVM.indexOf('@'));
			VirtualMachine vm = VirtualMachine.attach(pid);
			vm.loadAgent(jarFilePath, options != null ? options : "");
			vm.detach();
		} catch (Throwable e) {
			System.err.print("[DA] Problem loading agent " + jarFilePath);
			e.printStackTrace();
		}
	}
}
