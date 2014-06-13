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
package org.bladecoder.engine.actions;

import java.util.HashMap;

import org.bladecoder.engine.actions.Param.Type;
import org.bladecoder.engine.model.SpriteActor;
import org.bladecoder.engine.model.World;

public class TalktoAction implements Action {
	public static final String INFO = "Sets the dialog mode";
	public static final Param[] PARAMS = {
		new Param("dialog", "The 'dialogId' to show", Type.STRING, true)
		};		
	
	String actorId;
	String dialog;
	
	@Override
	public void setParams(HashMap<String, String> params) {
		actorId = params.get("actor");
		dialog = params.get("dialog");
	}

	@Override
	public void run() {
		
		SpriteActor actor = (SpriteActor)World.getInstance().getCurrentScene().getActor(actorId);
		
		World.getInstance().setCurrentDialog(actor.getDialog(dialog));
	}


	@Override
	public String getInfo() {
		return INFO;
	}

	@Override
	public Param[] getParams() {
		return PARAMS;
	}
}
