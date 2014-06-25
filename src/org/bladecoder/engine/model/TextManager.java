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
package org.bladecoder.engine.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import org.bladecoder.engine.actions.ActionCallback;
import org.bladecoder.engine.i18n.I18N;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.JsonValue;

/**
 * TextManager mantains a fifo for the character subtitles.
 * 
 * For now, only one subtitle is displayed in the screen.
 * 
 * A subtitle is cut in pieces and quee. Each piece has is own time in screen.
 * 
 * 
 * @author rgarcia
 * 
 */
public class TextManager implements Serializable {
	public static final float POS_CENTER = -1f;
	public static final float POS_SUBTITLE = -2f;
	public static final float RECT_MARGIN = 18f;
	public static final float RECT_BORDER = 2f;

	private float inScreenTime;
	private Text currentSubtitle = null;

	private Queue<Text> fifo;

	public TextManager() {
		fifo = new LinkedList<Text>();
	}

	public void addSubtitle(String str, float x, float y, boolean quee, Text.Type type,
			Color color, ActionCallback cb) {
		
		if(str.charAt(0) == '@')
			str = I18N.getString(str.substring(1));
		
		String s = str.replace("\\n", "\n");
		String[] text = s.split("\n\n");

		if (!quee)
			clear();

		for (int i = 0; i < text.length; i++) {
			String cutStr = text[i];

			// search for embedded duration in the string ex:
			// "2#two seconds subtitle"
			float duration = 0;
			String finalStr = cutStr;

			int idx = cutStr.indexOf('#');
			if (idx != -1) {
				duration = Float.parseFloat(cutStr.substring(0, idx));
				finalStr = cutStr.substring(idx + 1);
			}

			Text sub;

			if (i != text.length - 1) {
				sub = new Text(finalStr, x, y, duration, type, color, null);
			} else {
				sub = new Text(finalStr, x, y, duration, type, color, cb);
			}

			fifo.add(sub);
		}

		if (!quee || currentSubtitle == null) {
			if (currentSubtitle != null) {
				next();
			} else {
				setCurrentSubtitle(fifo.poll());
			}
		}

	}
	
	public Text getCurrentSubtitle() {
		return currentSubtitle;
	}

	private void setCurrentSubtitle(Text sub) {
		inScreenTime = 0f;
		currentSubtitle = sub;
	}

	public void update(float delta) {

		if (currentSubtitle == null) {
			return;
		}

		inScreenTime += delta;

		if (inScreenTime > currentSubtitle.time) {
			next();
		}
	}

	public void next() {
		if (currentSubtitle != null) {

			Text prev = currentSubtitle;

			setCurrentSubtitle(fifo.poll());

			// call cb after putting next subtitle because 'cb' can put new
			// subtitles too
			prev.callCb();
		}
	}

	public void clear() {
		fifo.clear();
		inScreenTime = 0;

		if (currentSubtitle != null) {
			currentSubtitle = null;
		}
	}

	/**
	 * Put manager in the init state. Use it when changing current scene
	 */
	public void reset() {
		clear();
	}

	@Override
	public void write(Json json) {
		json.writeValue("inScreenTime", inScreenTime);
		json.writeValue("currentSubtitle", currentSubtitle);
		json.writeValue("fifo", new ArrayList<Text>(fifo), ArrayList.class, Text.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read (Json json, JsonValue jsonData) {
		inScreenTime = json.readValue("inScreenTime", Float.class, jsonData);
		currentSubtitle = json.readValue("currentSubtitle", Text.class, jsonData);
		fifo = new LinkedList<Text>(json.readValue("fifo", ArrayList.class, Text.class, jsonData));
	}
}
