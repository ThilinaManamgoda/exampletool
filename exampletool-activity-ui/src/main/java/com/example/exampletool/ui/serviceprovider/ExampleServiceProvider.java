package com.example.exampletool.ui.serviceprovider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.swing.Icon;

import org.yaml.snakeyaml.Yaml;

import net.sf.taverna.t2.servicedescriptions.AbstractConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;

public class ExampleServiceProvider extends AbstractConfigurableServiceProvider<ExampleServiceProviderConfig>
		implements ConfigurableServiceProvider<ExampleServiceProviderConfig> {
	public ExampleServiceProvider() {
		super(new ExampleServiceProviderConfig());
	}

	private File cwlFilesLocation;
	private static final URI providerId = URI.create("http://cwl.com/2016/service-provider/cwlcommandlinetools");

	/**
	 * Do the actual search for services. Return using the callBack parameter.
	 */
	@SuppressWarnings("unchecked")
	public void findServiceDescriptionsAsync(final FindServiceDescriptionsCallBack callBack) {
		// Use callback.status() for long-running searches
		// callBack.status("Resolving example services");
	//	System.out.println(getConfiguration().getPath().get(0));
		// cwlFilesLocation = new File(getConfiguration().getPath());//here give
		// the location of cwl tools.
		cwlFilesLocation = new File("/home/maanadev/cwlTools");
		
		// cwlFilesLocation =new
		// File("/home/maanadev/Developer/GSOC/common-workflow-language/draft-3/draft-3");
		// This is holding the CWL configuration beans
		final List<ExampleServiceDesc> result = new ArrayList<ExampleServiceDesc>();

		// File[] cwlFiles = getCwlFiles();
		//
		// // Load the CWL file using SnakeYaml lib
		// Yaml cwlReader = new Yaml();
		// int i=0;
		// for (File file : cwlFiles) {
		// Map cwlFile = null;
		//
		// try {
		// cwlFile = (Map) cwlReader.load(new FileInputStream(file));
		// } catch (FileNotFoundException e) {
		// System.out.println(e.getMessage());
		// }
		// if (cwlFile != null) {
		// // Creating CWl service Description
		// ExampleServiceDesc cwlServiceDesc = new ExampleServiceDesc();
		// cwlServiceDesc.setCwlConfiguration(cwlFile);
		// cwlServiceDesc.setExampleString(file.getName().split("\\.")[0]);
		// cwlServiceDesc.setExampleUri(URI.create("http://localhost:8192/service"));
		//
		// // Optional: set description
		// // cwlServiceDesc.setDescription("Service example number " + i);
		// i++;
		// // add to the result
		// result.add(cwlServiceDesc);
		// // return the service description
		// callBack.partialResults(result);
		// }
		//
		// }
		Path path1 = Paths.get("/home/maanadev/cwlTools");
		Path path2 = path1.normalize();
		DirectoryStream<Path> stream = null;
		try {
			stream = Files.newDirectoryStream(path2, "*.cwl");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Stream<Path> paralleStream = StreamSupport.stream(stream.spliterator(), true);
		paralleStream.forEach(path->{
			Yaml reader =getYamlReader();
			try {
				Map map=(Map) reader.load(new FileInputStream(path.toFile()));
				ExampleServiceDesc cwlServiceDesc = new ExampleServiceDesc();
				 cwlServiceDesc.setCwlConfiguration(map);
				 cwlServiceDesc.setExampleString(path.getFileName().toString().split("\\.")[0]);
				 cwlServiceDesc.setExampleUri(URI.create("http://localhost:8192/service"));
				result.add(cwlServiceDesc);
				callBack.partialResults(result);
				//emptying the list
				result.clear();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}); 
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
		return "CWL Services";
	}

	@Override
	public String toString() {
		return "CWL services";
	}

	public String getId() {
		return providerId.toASCIIString();
	}

	@Override
	protected List<? extends Object> getIdentifyingData() {
		// TODO Auto-generated method stub
		return Arrays.asList(getConfiguration().getPath());
	}

	public Yaml getYamlReader() {
		Yaml reader = new Yaml();
		return reader;
	}
}
