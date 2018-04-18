/* Initial beliefs and rules */

environment_iri("http://localhost:8080/environments/env1").

positive_color(0.409, 0.518).
negative_color(0.167, 0.04).

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

+thing_artifact_available(ArtifactIRI, ArtifactName, WorkspaceName) : true <-
  .print("A thing artifact is available: " , ArtifactIRI);
  joinWorkspace(WorkspaceName, WorkspaceArtId);
  focusWhenAvailable(ArtifactName).

/* Plans for handling positive and negative events */

+event("positive") 
  : thing_artifact_available(_, ArtifactName, WorkspaceName) &
    hasAction(_,"http://iotschema.org/SwitchOn")[artifact_name(_, ArtifactName)] 
    & hasAction(_,"http://iotschema.org/SwitchOff")[artifact_name(_, ArtifactName)]
    & hasAction(_,"http://iotschema.org/SetColor")[artifact_name(_, ArtifactName)]
  <-
  .print("There is a positive event and I can turn on a green light via a thing: ", ArtifactName);
  joinWorkspace(WorkspaceName, WorkspaceArtId);
  ?positive_color(CIEx, CIEy);
  !thing_colored_light_notification(ArtifactName, CIEx, CIEy).

+event("positive") 
  : thing_artifact_available(_, ArtifactName, WorkspaceName) &
    hasAction(_,"http://iotschema.org/SwitchOn")[artifact_name(_, ArtifactName)] 
    & hasAction(_,"http://iotschema.org/SwitchOff")[artifact_name(_, ArtifactName)] 
  <-
  .print("There is a positive event and I can turn on a light via a thing: ", ArtifactName);
  joinWorkspace(WorkspaceName, WorkspaceArtId);
  !thing_light_notification(ArtifactName).

+event("positive") : artifact_available("emas.HueArtifact", ArtifactName, WorkspaceName) <-
  .print("There is a positive event and I can turn on a light!");
  joinWorkspace(WorkspaceName, WorkspaceArtId);
  !light_notification(ArtifactName).

+event("positive") : true <-
  .print("There is a positive event, but I cannot notify anyone.").


+event("negative")
  : thing_artifact_available(_, ArtifactName, WorkspaceName) &
    hasAction(_,"http://iotschema.org/SwitchOn")[artifact_name(_, ArtifactName)] 
    & hasAction(_,"http://iotschema.org/SwitchOff")[artifact_name(_, ArtifactName)]
    & hasAction(_,"http://iotschema.org/SetColor")[artifact_name(_, ArtifactName)] 
  <-
  .print("There is a negative event and I can turn on a blue light via a thing: ", ArtifactName);
  joinWorkspace(WorkspaceName, WorkspaceArtId);
  ?negative_color(CIEx, CIEy);
  !thing_colored_light_notification(ArtifactName, CIEx, CIEy).

+event("negative")
  : thing_artifact_available(_, ArtifactName, WorkspaceName) &
    hasAction(_,"http://iotschema.org/SwitchOn")[artifact_name(_, ArtifactName)] 
    & hasAction(_,"http://iotschema.org/SwitchOff")[artifact_name(_, ArtifactName)] 
  <-
  .print("There is a negative event and I can turn on a light via a thing: ", ArtifactName);
  joinWorkspace(WorkspaceName, WorkspaceArtId);
  !thing_light_notification(ArtifactName).

+event("negative") : artifact_available("emas.HueArtifact", ArtifactName, WorkspaceName) <-
  .print("There is a negative event and I can turn on a light!");
  joinWorkspace(WorkspaceName, WorkspaceArtId);
  !light_notification(ArtifactName).

+event("negative") : true <-
  .print("OMG, a negative event, but I cannot notify anyone!").


+!thing_colored_light_notification(ArtifactName, CIEx, CIEy) : true <-
  act("http://iotschema.org/SetColor", [
          ["http://iotschema.org/CIExData", CIEx], 
          ["http://iotschema.org/CIEyData", CIEy]
        ])[artifact_name(ArtifactName)];
  .wait(2000);
  act("http://iotschema.org/SwitchOff", [])[artifact_name(ArtifactName)].

+!thing_light_notification(ArtifactName) : true <-
  act("http://iotschema.org/SwitchOn", [])[artifact_name(ArtifactName)];
  .wait(2000);
  act("http://iotschema.org/SwitchOff", [])[artifact_name(ArtifactName)].

+!light_notification(ArtifactName) : true <-
  turnLightOn[artifact_name(ArtifactName)];
  .wait(2000);
  turnLightOff[artifact_name(ArtifactName)].
