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

import org.bladecoder.bladeengine.model.DialogOption;
import org.bladecoder.bladeengine.model.SpriteActor;
import org.bladecoder.bladeengine.model.Text;
import org.bladecoder.bladeengine.model.World;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class SayDialogAction extends BaseCallbackAction implements Action {

	public static final String INFO = 
			"Says the selected option from the current dialog. This action does the next steps:\n" +
			"- Sets the player 'talk' animation and say the player text" +
			"- Restore the previous player animation and set the target actor 'talk' animation and say the response text" + 
			"- Restore the target actor animation";
	public static final Param[] PARAMS = {
		};

	private boolean characterTurn = false;
	private String characterName;
	private String responseText;
	
	private String previousFA;

	@Override
	public void setParams(HashMap<String, String> params) {
	}

	@Override
	public void run() {
		World w = World.getInstance();
		DialogOption o = World.getInstance().getCurrentDialog().getCurrentOption();
		String playerText = o.getText();
		responseText = o.getResponseText();
		characterName = w.getCurrentDialog().getActor();
		
		characterTurn = true;
		previousFA = null;
		
		// If the player or the character is talking restore to 'stand' pose
		restoreStandPose(w.getCurrentScene().getPlayer());
		restoreStandPose((SpriteActor)w.getCurrentScene().getActor(characterName));

		if (playerText != null) {
			SpriteActor player = World.getInstance().getCurrentScene().getPlayer();

//			WorldCamera c = World.getInstance().getCamera();
//			Vector3 p = c.scene2screen(pos.x, pos.y + player.getHeight());

			World.getInstance().getTextManager()
					.addSubtitle(playerText, player.getX(), player.getY() + player.getHeight(), false, Text.Type.TALK, Color.BLACK, this);

			previousFA = player.getRenderer().getCurrentFrameAnimationId(); 
			player.startFrameAnimation(getTalkFA(previousFA), null);

		} else {
			onEvent();
		}
	}

	@Override
	public void onEvent() {

		World w = World.getInstance();
		SpriteActor actor = (SpriteActor)w.getCurrentScene().getActor(characterName);
		
		if (characterTurn) {
			characterTurn = false;
			
			if(previousFA!= null){
				SpriteActor player = World.getInstance().getCurrentScene().getPlayer();
				player.startFrameAnimation(previousFA, null);
			}

			if (responseText != null) {

//				WorldCamera c = World.getInstance().getCamera();
//				Vector3 p = c.scene2screen(pos.x, pos.y + actor.getHeight());

				World.getInstance()
						.getTextManager()
						.addSubtitle(responseText, actor.getX(), actor.getY() + actor.getHeight() , false, Text.Type.TALK,
								Color.BLACK, this);

				previousFA = actor.getRenderer().getCurrentFrameAnimationId(); 
				actor.startFrameAnimation(getTalkFA(previousFA), null);
			} else {
				super.onEvent();
			}
		} else {			
			actor.startFrameAnimation(previousFA, null);
			super.onEvent();			
		}
	}
	
	private void restoreStandPose(SpriteActor a) {
		if(a == null) return;
		
		String fa = a.getRenderer().getCurrentFrameAnimationId();
		
		if(fa.startsWith("talk.")){ // If the actor was already talking we restore the actor to the 'stand' pose
			int idx = fa.indexOf('.');
			String prevFA = "stand" + fa.substring(idx);
			a.startFrameAnimation(prevFA, null);
		}
	}
	
	private String getTalkFA(String prevFA) {
		if(prevFA.endsWith("left")) return "talk.left";
		else if(prevFA.endsWith("right")) return "talk.right";
		
		return "talk";
	}

	@Override
	public void write(Json json) {
		json.writeValue("previousFA", previousFA);
		json.writeValue("responseText", responseText);
		json.writeValue("characterTurn", characterTurn);
		json.writeValue("characterName", characterName);
		super.write(json);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		previousFA = json.readValue("previousFA", String.class, jsonData);
		responseText = json.readValue("responseText", String.class, jsonData);
		characterTurn = json.readValue("characterTurn", Boolean.class, jsonData);
		characterName = json.readValue("characterName", String.class, jsonData);
		super.read(json, jsonData);
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
