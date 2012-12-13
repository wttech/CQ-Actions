package com.cognifide.actions.api;

import com.day.cq.wcm.api.Page;

public interface Action {

	void perform(Page page) throws Exception;

	String getType();

}
