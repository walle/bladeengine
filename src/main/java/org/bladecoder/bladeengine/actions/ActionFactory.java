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

import org.bladecoder.bladeengine.util.EngineLogger;

import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public class ActionFactory {

	private static final HashMap<String, String> actions;

	static {
		actions = new HashMap<String, String>();
		
		actions.put("lookat",
				"org.bladecoder.bladeengine.actions.LookAtAction");
		actions.put("pickup",
				"org.bladecoder.bladeengine.actions.PickUpAction");
		actions.put("goto",
				"org.bladecoder.bladeengine.actions.GotoAction");
		actions.put("leave",
				"org.bladecoder.bladeengine.actions.LeaveAction");

		actions.put("set_state",
				"org.bladecoder.bladeengine.actions.SetStateAction");
		actions.put("set_active",
				"org.bladecoder.bladeengine.actions.SetActiveAction");
		actions.put("set_cutmode",
				"org.bladecoder.bladeengine.actions.SetCutmodeAction");
		actions.put("set_frame_animation",
				"org.bladecoder.bladeengine.actions.FrameAnimationAction");
		actions.put("pos_animation",
				"org.bladecoder.bladeengine.actions.PosAnimationAction");
		actions.put("remove_inventory_item",
				"org.bladecoder.bladeengine.actions.RemoveInventoryItemAction");
		actions.put("say",
				"org.bladecoder.bladeengine.actions.SayAction");
		actions.put("drop_item",
				"org.bladecoder.bladeengine.actions.DropItemAction");
		actions.put("wait",
				"org.bladecoder.bladeengine.actions.WaitAction");
		actions.put("talkto",
				"org.bladecoder.bladeengine.actions.TalktoAction");
		actions.put("dialog_option",
				"org.bladecoder.bladeengine.actions.DialogOptionAction");
		actions.put("say_dialog",
				"org.bladecoder.bladeengine.actions.SayDialogAction");
		actions.put("run_verb",
				"org.bladecoder.bladeengine.actions.RunVerbAction");
		actions.put("cancel_verb",
				"org.bladecoder.bladeengine.actions.CancelVerbAction");
		actions.put("sound",
				"org.bladecoder.bladeengine.actions.SoundAction");
		actions.put("music",
				"org.bladecoder.bladeengine.actions.MusicAction");
		actions.put("overlay",
				"org.bladecoder.bladeengine.actions.OverlayAction");
		actions.put("camera",
				"org.bladecoder.bladeengine.actions.CameraAction");
		actions.put("transition",
				"org.bladecoder.bladeengine.actions.TransitionAction");
	}
	
	public static String []getActionList() {
		return  actions.keySet().toArray(new String[actions.size()]);
	}

	public static Action create(String name,
			HashMap<String, String> params) {
		String className = actions.get(name);

		if (className == null) {
			EngineLogger.error( "Action with name '" + name
					+ "' not found.");

			return null;
		}

		return ActionFactory.createByClass(className, params);
	}	
	
	
	
	public static Action createByClass(String className,
			HashMap<String, String> params) {

		Action a = null;

		try {
			Class<?> c = ClassReflection.forName(className);
			a = (Action) ClassReflection.newInstance(c);
			
			if(params != null)
				a.setParams(params);
		} catch (ReflectionException e) {
			EngineLogger.error(e.getMessage());
		}

		return a;
	}
}
