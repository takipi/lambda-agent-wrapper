package com.sparktale.bugtale.meta.amagent;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.sparktale.bugtale.meta.amagent.data.Record;
import com.sparktale.bugtale.meta.amagent.data.StackTrace;

public class Recorder
{
	private final DataOutputStream dos;
	private final Set<Integer> hashes;
	
	public Recorder(String outputFileName) throws IOException
	{
		BufferedOutputStream bos;
		
		if (outputFileName != null)
		{
			FileOutputStream fos = new FileOutputStream(outputFileName);
		 	bos = new BufferedOutputStream(fos);
		}
		else
		{
			bos = new BufferedOutputStream(System.out);
		}
		
		this.dos = new DataOutputStream(bos);
		this.hashes = new HashSet<Integer>();
	}
	
	public void record(Record record) throws IOException
	{
		StackTrace stackTrace = record.getStackTrace();
		int stackHash = stackTrace.getStackHash();
		
		if (hashes.add(stackHash))
		{
			StackTraceElement[] stackElements = stackTrace.getElements();
			
			for (StackTraceElement stackElement : stackElements)
			{
				String className = stackElement.getClassName();
				String methodName = stackElement.getMethodName();
				dos.writeUTF(className);
				dos.writeUTF("::");
				dos.writeUTF(methodName);
				dos.writeUTF(" >> ");
			}
		}
		
		dos.writeUTF("\n");
		
		dos.flush();
	}
}
