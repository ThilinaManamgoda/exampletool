package com.example.exampletool.ui.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;

import com.example.exampletool.ExampleActivity;
import com.example.exampletool.ExampleActivityConfigurationBean;
import com.example.exampletool.PortDetail;
import com.example.exampletool.ui.config.ExampleConfigureAction;

@SuppressWarnings("serial")
public class ExampleContextualView extends HTMLBasedActivityContextualView<ExampleActivityConfigurationBean> {

	private static final String INPUTS = "inputs";
	private static final String OUTPUTS = "outputs";
	private static final String ID = "id";
	private static final String TYPE = "type";
	private static final String ARRAY = "array";
	private static final String DESCRIPTION = "description";

	private static final int DEPTH_0 = 0;
	private static final int DEPTH_1 = 1;
	private static final int DEPTH_2 = 2;

	private final ExampleActivity activity;
	private JLabel description = new JLabel("ads");
	private final ExampleActivityConfigurationBean configurationBean;

	public ExampleContextualView(ExampleActivity activity) {
		super(activity);
		this.activity = activity;
		this.configurationBean = activity.getConfiguration();
		super.initView();
	}

	@Override
	public void initView() {
	}

	@Override
	public JComponent getMainFrame() {
		final JComponent mainFrame = super.getMainFrame();
		JPanel flowPanel = new JPanel(new FlowLayout());

		mainFrame.add(flowPanel, BorderLayout.SOUTH);
		return mainFrame;
	}

	@Override
	public String getViewTitle() {
		return configurationBean.getExampleString();
	}

	/**
	 * Typically called when the activity configuration has changed.
	 */
	@Override
	public void refreshView() {
	}

	/**
	 * View position hint
	 */
	@Override
	public int getPreferredPosition() {
		return 100;
	}

	@Override
	public Action getConfigureAction(final Frame owner) {
		return null;
	}

	@Override
	protected String getRawTableRowsHtml() {
		String summery = "";

		Map cwlFile = configurationBean.getCwlConfigurations();
		String description = "";

		if (cwlFile.containsKey("description")) {
			description = (String) cwlFile.get("description");
			summery += "<tr><td colspan='2' align='left'>";
			for (String line : description.split("[\n|\r]"))
				summery += "<p>" + line + "</p>";
			// summery += "<p>"+ description +"</p>";
			summery += "</td></tr>";

		}
		if (cwlFile.containsKey(INPUTS)) {
			summery += "<tr><th colspan='2' align='left'>Inputs</th></tr>";

			HashMap<String, PortDetail> inputs = process(cwlFile.get(INPUTS));
			for (String id : inputs.keySet()) {
				PortDetail detail = inputs.get(id);
				summery += "<tr align='left'><td> ID: " + id + " </td><td>Depth: " + detail.getDepth() + "</td></tr>";

				if (detail.getDescription() != null) {
					summery += "<tr><td colspan='2' align='left'>";
					for (String line : detail.getDescription().split("[\n|\r]"))
						summery += "<p>" + line + "</p>";

					summery += "</td></tr>";
				}
				summery += "<tr></tr>";
			}
		}
		if (cwlFile.containsKey(OUTPUTS)) {
			summery += "<tr><th colspan='2' align='left'>Outputs</th></tr>";

			HashMap<String, PortDetail> inputs = process(cwlFile.get(OUTPUTS));
			for (String id : inputs.keySet()) {
				PortDetail detail = inputs.get(id);
				summery += "<tr align='left'><td> ID: " + id + " </td><td>Depth: " + detail.getDepth() + "</td></tr>";

				if (detail.getDescription() != null) {
					summery += "<tr><td colspan='2' align='left'>";
					for (String line : detail.getDescription().split("[\n|\r]"))
						summery += "<p>" + line + "</p>";

					summery += "</td></tr>";
				}
				summery += "<tr></tr>";
			}
		}
		return summery;
	}

	private HashMap<String, PortDetail> process(Object inputs) {

		HashMap<String, PortDetail> result = new HashMap<>();

		if (inputs.getClass() == ArrayList.class) {
			PortDetail detail = new PortDetail();
			for (Map input : (ArrayList<Map>) inputs) {
				String currentInputId = (String) input.get(ID);
				Object typeConfigurations;
				if (input.containsKey(DESCRIPTION)) {
					detail.setDescription((String) input.get(DESCRIPTION));
				} else {
					detail.setDescription(null);
				}
				try {

					typeConfigurations = input.get(TYPE);
					// if type :single argument
					if (typeConfigurations.getClass() == String.class) {
						detail.setDepth(DEPTH_0);

						result.put(currentInputId, detail);
						// type : defined as another map which contains type:
					} else if (typeConfigurations.getClass() == LinkedHashMap.class) {
						String inputType = (String) ((Map) typeConfigurations).get(TYPE);
						if (inputType.equals(ARRAY)) {
							detail.setDepth(DEPTH_1);
							result.put(currentInputId, detail);
						}
					}

				} catch (ClassCastException e) {

					System.out.println("Class cast exception !!!");
				}

			}
		} else if (inputs.getClass() == LinkedHashMap.class) {
			for (Object parameter : ((Map) inputs).keySet()) {
				if (parameter.toString().startsWith("$"))
					System.out.println("Exception");
			}
		}
		return result;
	}

}
