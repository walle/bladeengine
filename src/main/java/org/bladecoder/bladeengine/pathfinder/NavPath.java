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

package org.bladecoder.bladeengine.pathfinder;

/** A navigation path.
 * @author hneuer */
public interface NavPath<N extends NavNode> {
	/** Fills the navigation path between the start and target node.
	 * <p>
	 * Note that current implementations have to follow the path backward from the targetNode to the startNode (following the
	 * parent relation).
	 * <p> */
	public void fill (N startNode, N targetNode);

	/** Returns the length of the path, i.e. the number of reached nodes. */
	public int getLength ();

	/** Clear the path. */
	public void clear ();
}
