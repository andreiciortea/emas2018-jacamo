package emas;

import cartago.Artifact;
import cartago.LINK;
import cartago.OPERATION;
import emas.infra.Notification;

public class EventGeneratorArtifact extends Artifact {

  @OPERATION
  void init() {
  }
  
  @LINK
  void onNotification(Notification notification) {
    signal("event", notification.getMessage());
  }
  
}
