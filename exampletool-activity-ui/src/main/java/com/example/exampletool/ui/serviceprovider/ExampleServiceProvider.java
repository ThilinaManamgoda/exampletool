package com.example.exampletool.ui.serviceprovider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import org.yaml.snakeyaml.Yaml;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

public class ExampleServiceProvider implements ServiceDescriptionProvider {
	private File cwlFilesLocation = new File("/home/maanadev/1");//here give the location of cwl tools.
	private static final URI providerId = URI
		.create("http://example.com/2011/service-provider/exampletool");
	
	/**
	 * Do the actual search for services. Return using the callBack parameter.
	 */
	@SuppressWarnings("unchecked")
	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
		// Use callback.status() for long-running searches
		// callBack.status("Resolving example services");

	

		// This is holding the CWL configuration beans
		List<ExampleServiceDesc> result = new ArrayList<ExampleServiceDesc>();

		File[] cwlFiles = getCwlFiles();
		
		// Load the CWL file using SnakeYaml lib
		Yaml cwlReader = new Yaml();
		int i=0;
		for (File file : cwlFiles) {
			Map cwlFile = null;

			try {
				cwlFile = (Map) cwlReader.load(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			if (cwlFile != null) {
				// Creating CWl service Description
				ExampleServiceDesc cwlServiceDesc = new ExampleServiceDesc();
				cwlServiceDesc.setCwlConfiguration(cwlFile);
				cwlServiceDesc.setExampleString("Example " + i);
				cwlServiceDesc.setExampleUri(URI.create("http://localhost:8192/service"));

				// Optional: set description
				cwlServiceDesc.setDescription("Service example number " + i);
				i++;
				// add to the result
				result.add(cwlServiceDesc);
				// return the service description
				callBack.partialResults(result);
			}

		}
		callBack.finished();
	}
	private File[] getCwlFiles() {
		// Get the .cwl files in the directory using the FileName Filter
		FilenameFilter fileNameFilter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (name.lastIndexOf('.') > 0) {
					// get last index for '.' char
					int lastIndex = name.lastIndexOf('.');

					// get extension
					String str = name.substring(lastIndex);

					// match path name extension
					if (str.equals(".cwl")) {
						return true;
					}
				}
				return false;
			}
		};

		return cwlFilesLocation.listFiles(fileNameFilter);
	}
	/**
	 * Icon for service provider
	 */
	public Icon getIcon() {
		return ExampleServiceIcon.getIcon();
	}

	/**
	 * Name of service provider, appears in right click for 'Remove service
	 * provider'
	 */
	public String getName() {
		return "My example service";
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public String getId() {
		return providerId.toASCIIString();
	}

}
