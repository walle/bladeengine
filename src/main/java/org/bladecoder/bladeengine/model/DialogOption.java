/*******************************************************************************
 * Copyright 2014 Rafael Garcia Moreno.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.bladecoder.bladeengine.model;

import java.util.ArrayList;

public class DialogOption {
	ArrayList<DialogOption> options = new ArrayList<DialogOption>();
	
	transient private DialogOption parent;
		
	private String text;
	private String responseText;
	private String verbId;
	private String next;
	private boolean visible = true;
	
	
	public void addOption(DialogOption o) {
		options.add(o);
	}


	public boolean isVisible() {
		return visible;
	}


	public void setVisible(boolean visible) {
		this.visible = visible;
	}


	public String getVerbId() {
		return verbId;
	}


	public void setVerbId(String verbId) {
		this.verbId = verbId;
	}


	public String getText() {
		return text;
	}


	public void setText(String text) {
		this.text = text;
	}


	public DialogOption getParent() {
		return parent;
	}


	public void setParent(DialogOption parent) {
		this.parent = parent;
	}


	public ArrayList<DialogOption> getOptions() {
		return options;
	}


	public void setResponseText(String responseText) {
		this.responseText = responseText;
	}
	
	public String getResponseText() {
		return responseText;
	}


	public String getNext() {
		return next;
	}


	public void setNext(String next) {
		this.next = next;
	}

}
