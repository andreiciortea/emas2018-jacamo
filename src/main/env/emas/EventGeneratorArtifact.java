package emas;

import cartago.Artifact;
import cartago.LINK;
import cartago.OPERATION;

public class EventGeneratorArtifact extends Artifact {

  @OPERATION
  void init() {
  }
  
  @LINK
  void onNotification(String notification) {
    signal("event", notification);
  }
  
}
