package com.example.exampletool.ui.serviceprovider;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import com.example.exampletool.ExampleActivity;
import com.example.exampletool.ExampleActivityConfigurationBean;

public class ExampleServiceDesc extends ServiceDescription<ExampleActivityConfigurationBean> {
	private static final String DESCRIPTION = "description";

	/**
	 * The subclass of Activity which should be instantiated when adding a
	 * service for this description
	 */
	@Override
	public Class<? extends Activity<ExampleActivityConfigurationBean>> getActivityClass() {
		return ExampleActivity.class;
	}

	private Map cwlConfiguration;

	public void setCwlConfiguration(Map cwlConfiguration) {
		this.cwlConfiguration = cwlConfiguration;
	}

	@Override
	public String getDescription() {
		String description = (String) cwlConfiguration.get(DESCRIPTION);
		
		if (description==null||description.length()>40) return "";
		else return description;
	}

	/**
	 * The configuration bean which is to be used for configuring the
	 * instantiated activity. Making this bean will typically require some of
	 * the fields set on this service description, like an endpoint URL or
	 * method name.
	 * 
	 */
	@Override
	public ExampleActivityConfigurationBean getActivityConfiguration() {
		ExampleActivityConfigurationBean bean = new ExampleActivityConfigurationBean();
		bean.setExampleString(exampleString);
		bean.setExampleUri(exampleUri);
		bean.setCwlConfigurations(cwlConfiguration);
		return bean;
	}

	/**
	 * An icon to represent this service description in the service palette.
	 */
	@Override
	public Icon getIcon() {
		return ExampleServiceIcon.getIcon();
	}

	/**
	 * The display name that will be shown in service palette and will be used
	 * as a template for processor name when added to workflow.
	 */
	@Override
	public String getName() {
		return exampleString;
	}

	/**
	 * The path to this service description in the service palette. Folders will
	 * be created for each element of the returned path.
	 */
	@Override
	public List<String> getPath() {
		// For deeper paths you may return several strings
		return Arrays.asList("CWL Services " + exampleUri);
	}

	/**
	 * Return a list of data values uniquely identifying this service
	 * description (to avoid duplicates). Include only primary key like fields,
	 * ie. ignore descriptions, icons, etc.
	 */
	@Override
	protected List<? extends Object> getIdentifyingData() {
		// FIXME: Use your fields instead of example fields
		return Arrays.<Object> asList(exampleString, exampleUri);
	}

	// FIXME: Replace example fields and getters/setters with any required
	// and optional fields. (All fields are searchable in the Service palette,
	// for instance try a search for exampleString:3)
	private String exampleString;
	private URI exampleUri;

	public String getExampleString() {
		return exampleString;
	}

	public URI getExampleUri() {
		return exampleUri;
	}

	public void setExampleString(String exampleString) {
		this.exampleString = exampleString;
	}

	public void setExampleUri(URI exampleUri) {
		this.exampleUri = exampleUri;
	}

}
