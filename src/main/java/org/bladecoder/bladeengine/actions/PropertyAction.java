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
package org.bladecoder.bladeengine.actions;

import java.util.HashMap;

import org.bladecoder.bladeengine.actions.Param.Type;
import org.bladecoder.bladeengine.model.Actor;
import org.bladecoder.bladeengine.model.Scene;
import org.bladecoder.bladeengine.model.World;

public class PropertyAction implements Action {
	public static final String INFO = "Set/Remove a global property of the game";
	public static final Param[] PARAMS = {
		new Param("type", "Property type", Type.STRING, true, "actor", new String[] {"world", "scene", "actor"}),
		new Param("prop", "Property name", Type.STRING, true), 
		new Param("value", "Property value", Type.STRING, true),
		};		
	
	String actorId;
	String op;
	String prop;
	String value;
	String type;
	
	@Override
	public void setParams(HashMap<String, String> params) {
		actorId = params.get("actor");
		
		prop = params.get("prop");
		value = params.get("value");
		type = params.get("type");
	}

	@Override
	public void run() {
		
		if(type.equals("world")) {
			World.getInstance().setCustomProperty(prop, value);
		} else if(type.equals("scene")) {
			Scene s = World.getInstance().getCurrentScene();
			s.setCustomProperty(prop, value);
		} else {
			Actor actor = World.getInstance().getCurrentScene().getActor(actorId);
			actor.setCustomProperty(prop, value);
		}
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
