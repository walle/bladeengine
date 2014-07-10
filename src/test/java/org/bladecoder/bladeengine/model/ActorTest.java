package org.bladecoder.bladeengine.model;

import org.junit.Test;
import static org.junit.Assert.*;

// TODO: Replace dummy test with real tests

public class ActorTest {
  @Test
  public void hasId() {
    Actor actor = new Actor();
    actor.setId("myId");
    assertEquals("myId", actor.getId());
  }
}
