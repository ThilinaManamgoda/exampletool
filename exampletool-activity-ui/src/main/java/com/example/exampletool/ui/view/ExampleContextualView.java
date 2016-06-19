package com.example.exampletool.ui.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.example.exampletool.ExampleActivity;
import com.example.exampletool.ExampleActivityConfigurationBean;
import com.example.exampletool.PortDetail;

import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;

@SuppressWarnings("serial")
public class ExampleContextualView extends HTMLBasedActivityContextualView<ExampleActivityConfigurationBean> {
	private static final String LABEL = "label";

	
	private static final String TABLE_COLOR = "59A9CB";//this is color in RGB hex value
	private static final String TABLE_BORDER="2";
	private static final String TABLE_WIDTH="100%";
	private static final String TABLE_CELL_PADDING = "5%";
	
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
		String summery = "<table border=\""+TABLE_BORDER+"\" style=\"width:"+TABLE_WIDTH+"\" bgcolor=\""+TABLE_COLOR+"\" cellpadding=\""+TABLE_CELL_PADDING+"\" >";

		Map cwlFile = configurationBean.getCwlConfigurations();
		String description = "";

		if (cwlFile.containsKey("description")) {
			description = (String) cwlFile.get("description");
			summery = paragraphToHtml(summery, description);

		}
		if(cwlFile.containsKey(LABEL)){
			summery += "<tr><th colspan='3' align='left'>Label</th></tr>";
			summery += "<tr><td colspan='2' align='left'>"+ (String) cwlFile.get(LABEL)+"</td></tr>";
		}
		summery += "<tr><th colspan='2' align='left'>Inputs</th></tr>";

		HashMap<String, PortDetail> inputs = activity.getProcessedInputs();
		if (inputs != null && !inputs.isEmpty())
			for (String id : inputs.keySet()) {
				PortDetail detail = inputs.get(id);
				summery += "<tr align='left'><td> ID: " + id + " </td><td>Depth: " + detail.getDepth() + "</td></tr>";
				if(detail.getLabel()!=null){
					System.out.println("got a label");
					summery+="<tr><td  align ='left' colspan ='2'>Label: "+detail.getLabel()+"</td></tr>";
				}
				if (detail.getDescription() != null) {

					summery = paragraphToHtml(summery, detail.getDescription());

				}
				if(detail.getFormat()!=null){
					summery+="<tr><td  align ='left' colspan ='2'>Format: "+detail.getFormat()+"</td></tr>";
				}
				summery += "<tr></tr>";
			}

		summery += "<tr><th colspan='2' align='left'>Outputs</th></tr>";

		HashMap<String, PortDetail> outPuts = activity.getProcessedOutputs();
		if (outPuts != null && !outPuts.isEmpty())
			for (String id : outPuts.keySet()) {
				PortDetail detail = outPuts.get(id);
				summery += "<tr align='left'><td> ID: " + id + " </td><td>Depth: " + detail.getDepth() + "</td></tr>";
				if(detail.getLabel()!=null){
					summery+="<tr><td  align ='left' colspan ='2'>Label: "+detail.getLabel()+"</td></tr>";
				}
				if (detail.getDescription() != null) {
					summery = paragraphToHtml(summery, detail.getDescription());
				}
				summery += "<tr></tr>";
			}
		summery+="</table>";
		return summery;
	}

}
