package edu.mayo.cimi.rdf.auxiliary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class FileUtils
{
	public static String getOsName() 
	{
		  String os = "";
		  if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) 
		  {
		    os = "windows";
		  } 
		  else if (System.getProperty("os.name").toLowerCase().indexOf("linux") > -1) 
		  {
			  os = "linux";
		  } 
		  else if (System.getProperty("os.name").toLowerCase().indexOf("mac") > -1) 
		  {
		    os = "mac";
		  }
		 
		  return os;
	}
	
	public static void setContents(File aFile, String aContents, boolean append)
									throws FileNotFoundException, IOException 
	{
		if (aFile == null) 
		{
			throw new IllegalArgumentException("File should not be null.");
		}
		if (!aFile.exists()) 
		{
			throw new FileNotFoundException ("File does not exist: " + aFile);
		}
		if (!aFile.isFile()) 
		{
			throw new IllegalArgumentException("Should not be a directory: " + aFile);
		}
		
		if (!aFile.canWrite()) 
		{
			throw new IllegalArgumentException("File cannot be written: " + aFile);
		}
		
		//use buffering
		Writer output = new BufferedWriter(new FileWriter(aFile));
		try 
		{
			//FileWriter always assumes default encoding is OK!
			if (append)
				output.append(aContents);
			else
				output.write( aContents );
		
			output.flush();
		}
		finally 
		{
			output.close();
		}
	}
	
	public static File createFileWithContents(String fileName, String aContents) throws IOException
	{
		if (fileName == null) 
		{
			throw new IllegalArgumentException("File should not be null.");
		}

		File temp = new File(fileName);
		
		if (!temp.exists())
			temp.createNewFile();
		
		// use buffering
		Writer output = new BufferedWriter(new FileWriter(temp));
		
		try 
		{
			output.write(aContents);
		} 
		finally 
		{
			output.close();
		}
		
		return temp;
	}
	
	public static String getContents(File aFile) 
	{
		// ...checks on aFile are elided
		StringBuilder contents = new StringBuilder();

		try 
		{
			// use buffering, reading one line at a time
			// FileReader always assumes default encoding is OK!
			BufferedReader input = new BufferedReader(new FileReader(aFile));
			try 
			{
				String line = null; // not declared within while loop
				/*
				 * readLine is a bit quirky : it returns the content of a line
				 * MINUS the newline. it returns null only for the END of the
				 * stream. it returns an empty String if two newlines appear in
				 * a row.
				 */
				while ((line = input.readLine()) != null) 
				{
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
			} 
			finally 
			{
				input.close();
			}
		} 
		catch (IOException ex) 
		{
			ex.printStackTrace();
		}

		return contents.toString();
	}
}
