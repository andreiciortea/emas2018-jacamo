/* Initial beliefs and rules */

environment_iri("http://localhost:8080/environments/env1").


/* Initial goals */

!start.


/* Plans for loading the environment */

+!start : environment_iri(EnvIRI) <-
  .print("hello world, today I'll explore the environment: ", EnvIRI);
  .wait(1000);
  .send(node_manager, achieve, environment_loaded(EnvIRI)).

+environment_loaded(EnvIRI, WorkspaceNames) : true <-
  .print("Environment loaded: ", EnvIRI).


/* Plans for discovering and using artifacts */

+artifact_available("emas.EventGeneratorArtifact", ArtifactName, WorkspaceName) : true <-
  .print("An event generator artifact is available in workspace: ", WorkspaceName);
  joinWorkspace(WorkspaceName, WorkspaceArtId);
  focusWhenAvailable(ArtifactName).

+artifact_available("emas.HueArtifact", ArtifactName, WorkspaceName) : true <-
  .print("A Philis Hue light bulb artifact is available in workspace: ", WorkspaceName);
  joinWorkspace(WorkspaceName, WorkspaceArtId);
  focusWhenAvailable(ArtifactName).


/* Plans for handling positive and negative events */

+event("positive") : artifact_available("emas.HueArtifact", ArtifactName, WorkspaceName) <-
  .print("There is a positive event and I can turn on a light!");
  joinWorkspace(WorkspaceName, WorkspaceArtId);
  !light_notification(ArtifactName).

+event("positive") : true <-
  .print("There is a positive event, but I cannot notify anyone.").


+event("negative") : artifact_available("emas.HueArtifact", ArtifactName, WorkspaceName) <-
  .print("There is a negative event and I can turn on a light!");
  joinWorkspace(WorkspaceName, WorkspaceArtId);
  !light_notification(ArtifactName).

+event("negative") : true <-
  .print("OMG, a negative event, but I cannot notify anyone!").

+!light_notification(ArtifactName) : true <-
  turnLightOn[artifact_name(ArtifactName)];
  .wait(2000);
  turnLightOff[artifact_name(ArtifactName)].


{ include("$jacamoJar/templates/common-cartago.asl") }
