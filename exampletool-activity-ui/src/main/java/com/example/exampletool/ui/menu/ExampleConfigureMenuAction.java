package com.example.exampletool.ui.menu;

import javax.swing.Action;

import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;
import com.example.exampletool.ExampleActivity;
import com.example.exampletool.ui.config.ExampleConfigureAction;

public class ExampleConfigureMenuAction extends
		AbstractConfigureActivityMenuAction<ExampleActivity> {

	public ExampleConfigureMenuAction() {
		super(ExampleActivity.class);
	}

	@Override
	protected Action createAction() {
		ExampleActivity a = findActivity();
		Action result = null;
		result = new ExampleConfigureAction(findActivity(),
				getParentFrame());
		result.putValue(Action.NAME, "Configure example service");
		addMenuDots(result);
		return result;
	}

}
