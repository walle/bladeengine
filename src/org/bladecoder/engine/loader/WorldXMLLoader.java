package org.bladecoder.engine.loader;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.bladecoder.engine.actions.Action;
import org.bladecoder.engine.actions.ActionFactory;
import org.bladecoder.engine.assets.EngineAssetManager;
import org.bladecoder.engine.i18n.I18N;
import org.bladecoder.engine.model.Actor;
import org.bladecoder.engine.model.Scene;
import org.bladecoder.engine.model.Verb;
import org.bladecoder.engine.model.World;
import org.bladecoder.engine.util.EngineLogger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class WorldXMLLoader extends DefaultHandler {
	private World world;

	private String initScene;
	private Verb currentVerb;

	private float scale;

	private Locator locator;

	private String chapter;
	private String currentChapter;

	private List<Scene> scenes;
	
	public static void load(String filename, World world, String chapter) throws ParserConfigurationException, SAXException, IOException  {
		SAXParserFactory spf = SAXParserFactory.newInstance();
	    spf.setNamespaceAware(true);
	    SAXParser saxParser = spf.newSAXParser();
	    
	    WorldXMLLoader parser = new WorldXMLLoader(world, chapter);
	    XMLReader xmlReader = saxParser.getXMLReader();
	    xmlReader.setContentHandler(parser);
	    xmlReader.parse(new InputSource(EngineAssetManager.getInstance().getModelFile(filename).read()));
	}

	public WorldXMLLoader(World world, String chapter) {
		this.world = world;
		this.chapter = chapter;
	}

	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {

		if (currentVerb != null) {
			String actionName = localName;
			Action action = null;
			HashMap<String, String> params = new HashMap<String, String>();
			String actionClass = null;

			for (int i = 0; i < atts.getLength(); i++) {
				String attName = atts.getLocalName(i);

				if (attName.equals("class")) {
					actionClass = atts.getValue(attName);
				} else {
					String value = atts.getValue(attName);

					params.put(attName, value);
				}
			}

			if (actionClass != null) {
				action = ActionFactory.createByClass(actionClass, params);
			} else {
				action = ActionFactory.create(actionName, params);
			}

			if (action != null) {
				currentVerb.add(action);
			} else {
				EngineLogger.error("Action '" + actionName + "' not found.");
			}

		} else if (localName.equals("world")) {
			int width, height;

			try {
				width = Integer.parseInt(atts.getValue("width"));
				height = Integer.parseInt(atts.getValue("height"));

				// When we know the world width, we can put the scale
				EngineAssetManager.getInstance().setScale(width);
				scale = EngineAssetManager.getInstance().getScale();

				width = (int) (width * scale);
				height = (int) (height * scale);

			} catch (NumberFormatException e) {
				SAXParseException e2 = new SAXParseException(
						"World 'width' or 'height' missing or incorrect in XML.",
						locator);
				error(e2);
				throw e2;
			}

			world.setWidth(width);
			world.setHeight(height);

			if (chapter == null)
				chapter = atts.getValue("init_chapter");

		} else if (localName.equals("chapter")) {
			currentChapter = atts.getValue("id");

			if (chapter == null)
				chapter = currentChapter;

			// LOAD THE SELECTED OR THE INIT CHAPTER ONLY
			if (chapter.equals(currentChapter)) {
				
				try {
					loadChapter(currentChapter);
					
					for(Scene s:scenes) {
						s.resetCamera(world.getWidth(), world.getHeight());

						world.addScene(s);
					}
					
				} catch (Exception e) {
					SAXParseException e2 = new SAXParseException(
							"Error loading chapter '" + currentChapter + "'", locator,
							e);
					error(e2);
					throw e2;
				}
			}
		} else if (localName.equals("verb")) {
			String id = atts.getValue("id");

			currentVerb = new Verb(id);

			Actor.addDefaultVerb(id, currentVerb);
		}
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {

		if (localName.equals("world")) {
			if(initScene != null)
				world.setCurrentScene(initScene);
		} else if (localName.equals("verb")) {
			currentVerb = null;
		}
	}

	@Override
	public void setDocumentLocator(Locator l) {
		locator = l;
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		EngineLogger.error(MessageFormat.format(
				"{0} in 'world.xml' Line: {1} Column: {2}", e.getMessage(),
				e.getLineNumber(), e.getColumnNumber()));
	}
	
	private void loadChapter(String id) throws ParserConfigurationException, SAXException, IOException  {	
		SAXParserFactory spf = SAXParserFactory.newInstance();
	    spf.setNamespaceAware(true);
	    SAXParser saxParser = spf.newSAXParser();
	    
	    ChapterXMLLoader parser = new ChapterXMLLoader();
	    XMLReader xmlReader = saxParser.getXMLReader();
	    xmlReader.setContentHandler(parser);
	    xmlReader.parse(new InputSource(EngineAssetManager.getInstance().getModelFile(id + ".chapter").read()));
	    
	    I18N.load(EngineAssetManager.MODEL_DIR + "world", EngineAssetManager.MODEL_DIR + id);
	    
	    scenes = parser.getScenes();
	    initScene = parser.getInitScene();
	    
	    if(initScene == null && scenes.size() > 0)
	    	initScene = scenes.get(0).getId();
	}	
}