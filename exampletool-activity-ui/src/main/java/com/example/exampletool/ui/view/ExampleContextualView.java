package com.example.exampletool.ui.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.example.cwl.CwlContextualUtil;
import com.example.cwl.PortDetail;
import com.example.cwl.Utility;
import com.example.exampletool.ExampleActivity;
import com.example.exampletool.ExampleActivityConfigurationBean;

import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;

@SuppressWarnings("serial")
public class ExampleContextualView extends HTMLBasedActivityContextualView<ExampleActivityConfigurationBean> {
	private static final String LABEL = "label";

	private static final String TABLE_COLOR = "59A9CB";// this is color in RGB
														// hex value
	private static final String TABLE_BORDER = "2";
	private static final String TABLE_WIDTH = "100%";
	private static final String TABLE_CELL_PADDING = "5%";
	private static final String DESCRIPTION = "description";
	private final ExampleActivity activity;
	private JLabel description = new JLabel("ads");
	private final ExampleActivityConfigurationBean configurationBean;
	private CwlContextualUtil utility;
	Map cwl;
	public ExampleContextualView(ExampleActivity activity) {
		super(activity);
		this.activity = activity;
		this.configurationBean = activity.getConfiguration();
		cwl=configurationBean.getCwlConfigurations();
		utility = new CwlContextualUtil(configurationBean.getCwlConfigurations());
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
		paragraph=formatParagraph(paragraph);
		for (String line : paragraph.split("[\n|\r]"))
			summery += "<p>" + line + "</p>";
		// summery += "<p>"+ description +"</p>";
		summery += "</td></tr>";
		return summery;
	}

	@Override
	protected String getRawTableRowsHtml() {
//		String summery = "<table border=\"" + TABLE_BORDER + "\" style=\"width:" + TABLE_WIDTH + "\" bgcolor=\""
//				+ TABLE_COLOR + "\" cellpadding=\"" + TABLE_CELL_PADDING + "\" >";
//
//		Map cwlFile = configurationBean.getCwlConfigurations();
//		String description = "";
//
//		if (cwlFile.containsKey("description")) {
//			description = (String) cwlFile.get("description");
//			summery = paragraphToHtml(summery, description);
//
//		}
//		if (cwlFile.containsKey(LABEL)) {
//			summery += "<tr><th colspan='2' align='left'>Label</th></tr>";
//			summery += "<tr><td colspan='2' align='left'>" + (String) cwlFile.get(LABEL) + "</td></tr>";
//		}
//		summery += "<tr><th colspan='2' align='left'>Inputs</th></tr>";
//
//		HashMap<String, PortDetail> inputs = utility.processInputDetails();
//		HashMap<String, Integer> inputDepths = utility.processInputDepths();
//		if ((inputs != null && !inputs.isEmpty())&&(inputDepths != null && !inputDepths.isEmpty()))
//			for (String id : inputs.keySet()) {
//				PortDetail detail = inputs.get(id);
//				if(inputDepths.containsKey(id))
//				summery = extractSummery(summery, id, detail,inputDepths.get(id));
//			}
//
//		summery += "<tr><th colspan='2' align='left'>Outputs</th></tr>";
//
//		HashMap<String, PortDetail> outPuts = utility.processOutputDetails();
//		HashMap<String, Integer> outputDepths = utility.processOutputDepths();
//		if ((outPuts != null && !outPuts.isEmpty())&&(outputDepths != null && !outputDepths.isEmpty()))
//			for (String id : outPuts.keySet()) {
//				PortDetail detail = outPuts.get(id);
//				if(outputDepths.containsKey(id))
//					summery = extractSummery(summery, id, detail,outputDepths.get(id));
//			}
//		summery += "</table>";
//		return summery;
		String summary = "<table border=\"" + TABLE_BORDER + "\" style=\"width:" + TABLE_WIDTH + "\" cellpadding=\""
				+ TABLE_CELL_PADDING + "\" >";

		
		// Get the CWL tool Description
		if (cwl.containsKey(DESCRIPTION)) {
			String description = cwl.get(DESCRIPTION).toString();
			summary = utility.paragraphToHtml(summary, description);

		}
		// Get the CWL tool Label
		if (cwl.containsKey(LABEL)) {
			summary += "<tr><th colspan='2' align='left'>Label</th></tr>";
			summary += "<tr><td colspan='2' align='left'>" + cwl.get(LABEL) + "</td></tr>";
		}
		summary += "<tr><th colspan='2' align='left'>Inputs</th></tr>";

		summary = utility.setUpInputDetails(summary);

		summary += "<tr><th colspan='2' align='left'>Outputs</th></tr>";

		summary = utility.setUpOutputDetails(summary);
		summary += "</table>";
		return summary;
	}

	private String extractSummery(String summery, String id, PortDetail detail,int depth) {
		summery += "<tr align='left'><td> ID: " + id + " </td><td>Depth: " + depth+ "</td></tr>";
		if (detail.getLabel() != null) {
			summery += "<tr><td  align ='left' colspan ='2'>Label: " + detail.getLabel() + "</td></tr>";
		}
		if (detail.getDescription() != null) {

			summery = paragraphToHtml(summery, detail.getDescription());

		}
		if (detail.getFormat() != null) {
			summery += "<tr><td  align ='left' colspan ='2'>Format: ";
			ArrayList<String> formats = detail.getFormat();

			int Size = formats.size();

			if (Size == 1) {
				summery += formats.get(0);
			} else {
				for (int i = 0; i < (Size - 1); i++) {
					summery += formats.get(i) + ", ";
				}
				summery += formats.get(Size - 1);
			}
			summery += "</td></tr>";
		}
		summery += "<tr></tr>";
		return summery;
	}
	private String formatParagraph(String paragraph) {
		String result = "";
		for(String s: paragraph.split("\n")){
			
			while(s.length()>80){
				int lastSpaceIndex = s.lastIndexOf(" ",80);
				String firstHalf =s.substring(0, lastSpaceIndex)+"\n";
				s=s.substring(lastSpaceIndex+1);
				result+=(firstHalf);
			}
			result+=(s+"\n");
			
		}
			return result;
	}
}
