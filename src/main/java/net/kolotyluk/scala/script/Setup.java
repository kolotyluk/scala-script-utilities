/*  Copyright © 2014 by Eric Kolotyluk <eric@kolotyluk.net>

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package net.kolotyluk.scala.script;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;


/**
 * @author Eric
 *
 */
public class Setup {
	
	public enum OperatingSystem {WINDOWS, UNIX}
	
	static OperatingSystem operatingSystem;
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.out.println("Setup");
		
		Set<Entry<Object, Object>> entrySet = System.getProperties().entrySet();

		for (Entry<?, ?> entry : entrySet)
		{
			System.out.println(entry.getKey() + " = " + entry.getValue());
		
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			
			if (key.contains("os.name"))
			{
				if (value.toUpperCase().contains("WINDOWS"))
					operatingSystem = OperatingSystem.WINDOWS;
				else
					operatingSystem = OperatingSystem.UNIX;
			}
		}
		
		System.out.println("Operating System = " + operatingSystem);
		System.out.println("isThereScala() = " + scalaVersion());


	}
	
	public static boolean isThereScala() 
	{
		
		Set<Entry<String, String>> entrySet = System.getenv().entrySet();
		
		for(Entry<?, ?> entry : entrySet)
		{
			String key = (String) entry.getKey();
			if (key.equalsIgnoreCase("PATH"))
			{
				String value = (String) entry.getValue();
				System.out.println("PATH = " + value);
				String [] path = value.split(";");
				for (String pathItem : path)
				{
					if (pathItem.contains("scala"))
					{
						System.out.println("Scala = " + pathItem);
						
						if (pathItem.contains("bin"))
						{							
							System.out.println("scalaVersion = " + scalaVersion());
						}
					}
				}
			}
		}
		
		return false;
	}
	
	public static boolean scalaVersion()
	{
		
		ArrayList<String> processArguments = new ArrayList<String>();
		
		switch (operatingSystem)
		{
		case WINDOWS:
			processArguments.add("cmd");
			processArguments.add("/c");
			processArguments.add("scala");
			processArguments.add("-e");
			processArguments.add("\"println(scala.util.Properties.versionString)\"");
			break;
		case UNIX:
			System.out.println("Error " + operatingSystem);
			break;
		default:
			System.out.println("Error" + operatingSystem);
			break;
		}
		
		System.out.print("Process arguments: ");
		for (String processArgument : processArguments) System.out.print(" " + processArgument);
		System.out.println();

		ProcessBuilder processBuilder = new ProcessBuilder(processArguments.toArray(new String[processArguments.size()]));

		Process process;

			try {
				process = processBuilder.start();
				InputStream processOutput = process.getInputStream();
				InputStream processErrors = process.getErrorStream();

				// We need to log the process output via other threads because process.waitFor() blocks

				Logger outputLogger = new Logger(processOutput, System.out);
				Logger errorsLogger = new Logger(processErrors, System.err);
				
				outputLogger.start();
				errorsLogger.start();

				int exitValue = process.waitFor();	// BLOCKING Operation

				System.out.println("exitValue = " + exitValue);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

			return false;

	}

	static class Logger extends Thread
	{
		InputStream inputStream;
		PrintStream printStream;
		
		Logger(InputStream inputStream, PrintStream printStream)
		{
			this.inputStream = inputStream;
			this.printStream = printStream;
			this.setDaemon(true);
		}
		
		@Override
		public void run()
		{
			StringBuilder stringBuilder = new StringBuilder();
			try
			{
				for (int inputByte = inputStream.read(); inputByte > -1; inputByte = inputStream.read())
				{
					stringBuilder.append((char) inputByte);
					if (inputByte == Character.getNumericValue('\n'))
					{
						System.out.println(stringBuilder.toString());
						stringBuilder.setLength(0);
					}
				}
				
				if (stringBuilder.length() > 0) printStream.println(stringBuilder);
			}
			catch (SocketException e)
			{
				e.printStackTrace();

			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					inputStream.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

}
