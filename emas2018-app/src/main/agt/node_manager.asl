/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

+!start : true <- 
  .print("hello world.");
  makeArtifact("notification-server", "emas.infra.NotificationServerArtifact", [8081], _);
  start.


+!environment_loaded(EnvIRI)[source(Ag)] : environment_loaded(EnvIRI) <-
  .send(Ag, tell, environment_loaded(EnvIRI)).

+!environment_loaded(EnvIRI)[source(Ag)] : true <-
  .print("Received request from ", Ag, " to load enviornment: ", EnvIRI);
  makeArtifact("envar", "emas.infra.WebEnvironmentArtifact", [EnvIRI], WebEnvArtID);
  focusWhenAvailable("envar");
  +web_environment_artifact_id(WebEnvArtID);
  getWorkspaceIRIs(WorkspaceIRIs);
  .print("Available workspaces: ", WorkspaceIRIs);
  !buildWorkspaces(WorkspaceIRIs);
  .print("Done, notifying agent ", Ag, " of created workspaces!");
  getWorkspaceNames(WorkspaceNames);
  .send(Ag, tell, environment_loaded(EnvIRI, WorkspaceNames)).


+!buildWorkspaces([]) : true .

+!buildWorkspaces([WorkspaceIRI | T]) : web_environment_artifact_id(WebEnvArtID) <-
  .print("Creating workspace ", WorkspaceIRI);
  getWorkspaceDetails(WorkspaceIRI, WorkspaceName, WorkspaceWebSubHubIRI, WorkspaceArtifactIRIs);
  .print("[Workspace: ", WorkspaceIRI, "] Name: ", WorkspaceName, ", available artifacts: ", WorkspaceArtifactIRIs);
  createWorkspace(WorkspaceName);
  joinWorkspace(WorkspaceName, WorkspaceArtId);
  +artifact_details(WorkspaceIRI, WorkspaceName, WorkspaceArtifactIRIs, WorkspaceArtId);
  registerArtifactForNotifications(WorkspaceIRI, WebEnvArtID, WorkspaceWebSubHubIRI);
  !buildArtifacts(WorkspaceName, WorkspaceArtifactIRIs);
  !buildWorkspaces(T).


+!buildArtifacts(WorkspaceName, []) : true .

+!buildArtifacts(WorkspaceName, [ArtifactIRI | T]) : true <-
  getArtifactDetails(ArtifactIRI, ArtifactName, IsThing, ArtifactClassName, InitParams, WebSubHubIRI);
  .print("[Artifact: ", ArtifactIRI, "] Name: ", ArtifactName, ", class name: ", ArtifactClassName, ", init params: ", InitParams, ", web sub hub IRI: ", WebSubHubIRI);
  !makeArtifact(WorkspaceName, ArtifactIRI, ArtifactName, IsThing, ArtifactClassName, InitParams, WebSubHubIRI);
  !buildArtifacts(WorkspaceName, T).


+artifact_created(WorkspaceName, ArtifactIRI) : true <-
  .print("New artifact created in workspace ", WorkspaceName, ": ", ArtifactIRI);
  getArtifactDetails(ArtifactIRI, ArtifactName, IsThing, ArtifactClassName, InitParams, WebSubHubIRI);
  .print("[Artifact: ", ArtifactIRI, "] Name: ", ArtifactName, ", class name: ", ArtifactClassName, ", init params: ", InitParams, ", web sub hub IRI: ", WebSubHubIRI);
  !makeArtifact(WorkspaceName, ArtifactIRI, ArtifactName, IsThing, ArtifactClassName, InitParams, WebSubHubIRI).


+!makeArtifact(WorkspaceName, ArtifactIRI, ArtifactName, true, _, _, WebSubHubIRI)
  : .ground([WorkspaceName, ArtifactIRI, ArtifactName, WebSubHubIRI])
  <-
  .print("Got a thing artifact with a WebSubIRI!");
  !createThingArtifact(WorkspaceName, ArtifactName, ArtifactIRI, ArtID);
  registerArtifactForNotifications(ArtifactIRI, ArtID, WebSubHubIRI);
  .print("Subscribed artifact ", ArtifactName, " for notifications!").

+!makeArtifact(WorkspaceName, ArtifactIRI, ArtifactName, true, _, _, WebSubHubIRI)
  : .ground([WorkspaceName, ArtifactIRI, ArtifactName])
  <-
  .print("Got a thing artifact without a WebSubIRI!");
  !createThingArtifact(WorkspaceName, ArtifactName, ArtifactIRI, ArtID).

+!makeArtifact(WorkspaceName, ArtifactIRI, ArtifactName, false, ArtifactClassName, InitParams, WebSubHubIRI)
  : .ground([WorkspaceName, ArtifactIRI, ArtifactName, ArtifactClassName, InitParams, WebSubHubIRI])
  <-
  .print("Got an artifact with a WebSubIRI!");
  !createCartagoArtifact(WorkspaceName, ArtifactName, ArtifactClassName, InitParams, ArtID);
  registerArtifactForNotifications(ArtifactIRI, ArtID, WebSubHubIRI);
  .print("Subscribed artifact ", ArtifactName, " for notifications!").

+!makeArtifact(WorkspaceName, ArtifactIRI, ArtifactName, false, ArtifactClassName, InitParams, WebSubHubIRI)
  : .ground([WorkspaceName, ArtifactIRI, ArtifactName, ArtifactClassName, InitParams])
  <-
  !createCartagoArtifact(WorkspaceName, ArtifactName, ArtifactClassName, InitParams, ArtID).

+!makeArtifact(WorkspaceName, ArtifactIRI, ArtifactName, IsThing, ArtifactClassName, InitParams, WebSubHubIRI) : true <-
  .print("Discovered an artifact I cannot create: ", ArtifactIRI).


+!createThingArtifact(WorkspaceName, ArtifactName, ArtifactIRI, ArtID) : true <-
  makeArtifact(ArtifactName, "emas.ThingArtifact", [ArtifactIRI], ArtID);
  +artifact_details(ArtifactIRI, ArtifactName, ArtID);
  .broadcast(tell, thing_artifact_available(ArtifactIRI, ArtifactName, WorkspaceName)).

+!createCartagoArtifact(WorkspaceName, ArtifactName, ArtifactClassName, InitParams, ArtID) : true <-
  makeArtifact(ArtifactName, ArtifactClassName, InitParams, ArtID);
  +artifact_details(ArtifactIRI, ArtifactName, ArtifactClassName, ArtID);
  .broadcast(tell, artifact_available(ArtifactClassName, ArtifactName, WorkspaceName)).
