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

	private String paragraphToHtml(String summery, String paragraph) {

		summery += "<tr><td colspan='2' align='left'>";
		for (String line : paragraph.split("[\n|\r]"))
			summery += "<p>" + line + "</p>";
		// summery += "<p>"+ description +"</p>";
		summery += "</td></tr>";
		return summery;
	}

	@Override
	protected String getRawTableRowsHtml() {
		String summery = "";

		Map cwlFile = configurationBean.getCwlConfigurations();
		String description = "";

		if (cwlFile.containsKey("description")) {
			description = (String) cwlFile.get("description");
			summery = paragraphToHtml(summery, description);

		}

		summery += "<tr><th colspan='2' align='left'>Inputs</th></tr>";

		HashMap<String, PortDetail> inputs = activity.getProcessedInputs();
		if (inputs != null)
			for (String id : inputs.keySet()) {
				PortDetail detail = inputs.get(id);
				summery += "<tr align='left'><td> ID: " + id + " </td><td>Depth: " + detail.getDepth() + "</td></tr>";

				if (detail.getDescription() != null) {

					summery = paragraphToHtml(summery, detail.getDescription());

				}
				summery += "<tr></tr>";
			}

		summery += "<tr><th colspan='2' align='left'>Outputs</th></tr>";

		HashMap<String, PortDetail> outPuts = activity.getProcessedOutputs();
		if (outPuts != null)
			for (String id : outPuts.keySet()) {
				PortDetail detail = outPuts.get(id);
				summery += "<tr align='left'><td> ID: " + id + " </td><td>Depth: " + detail.getDepth() + "</td></tr>";

				if (detail.getDescription() != null) {
					summery = paragraphToHtml(summery, detail.getDescription());
				}
				summery += "<tr></tr>";
			}

		return summery;
	}

}
