package com.example.exampletool.ui.serviceprovider;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.activityicons.ActivityIconSPI;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import com.example.exampletool.ExampleActivity;

public class ExampleServiceIcon implements ActivityIconSPI {

	private static Icon icon;

	public int canProvideIconScore(Activity<?> activity) {
		if (activity instanceof ExampleActivity) {
			return DEFAULT_ICON;
		}
		return NO_ICON;
	}

	public Icon getIcon(Activity<?> activity) {
		return getIcon();
	}
	
	public static Icon getIcon() {
		if (icon == null) {
			icon = new ImageIcon("/home/maanadev/Developer/GSOC/CWL/cwl-logo-header.png");
		}
		return icon;
	}

}
