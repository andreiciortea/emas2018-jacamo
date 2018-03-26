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
  makeArtifact("envar", "emas.infra.WebEnvironmentArtifact", [EnvIRI], _);
  getWorkspaceIRIs(WorkspaceIRIs);
  .print(WorkspaceIRIs);
  !buildWorkspaces(WorkspaceIRIs);
  ?artifact_details(ArtifactIRI, ArtifactName, "emas.EventGeneratorArtifact", ArtID);
  registerArtifact(ArtifactIRI, ArtID);
  .print("Done, notifying agent ", Ag);
  getWorkspaceNames(WorkspaceNames);
  .send(Ag, tell, environment_loaded(EnvIRI, WorkspaceNames)).


+!buildWorkspaces([]) : true .
  
+!buildWorkspaces([WorkspaceIRI | T]) : true <-
  .print("Creating workspace ", WorkspaceIRI);
  getWorkspaceDetails(WorkspaceIRI, WorkspaceName, WorkspaceArtifactIRIs);
  .print("[Workspace: ", WorkspaceIRI, "] Name: ", WorkspaceName, ", available artifacts: ", WorkspaceArtifactIRIs);
  createWorkspace(WorkspaceName);
  joinWorkspace(WorkspaceName, WorkspaceArtId);
  !buildArtifacts(WorkspaceName, WorkspaceArtifactIRIs);
  !buildWorkspaces(T).


+!buildArtifacts(WorkspaceName, []) : true .

+!buildArtifacts(WorkspaceName, [ArtifactIRI | T]) : true <-
  getArtifactDetails(ArtifactIRI, ArtifactName, ArtifactClassName, InitParams);
  .print("[Artifact: ", ArtifactIRI, "] Name: ", ArtifactName, ", class name: ", ArtifactClassName, ", init params: ", InitParams);
  makeArtifact(ArtifactName, ArtifactClassName, InitParams, ArtID);
  focusWhenAvailable(ArtifactName);
  .print("Artifact created and focused!");
  +artifact_details(ArtifactIRI, ArtifactName, ArtifactClassName, ArtID);
  // Just a test
  lookupArtifactByType(ArtifactClassName, ArtID2);
  .print("Found artifact of type ", ArtifactClassName, " with ID ", ArtID2);
  .broadcast(tell, artifact_available(ArtifactClassName, ArtifactName, WorkspaceName));
  !buildArtifacts(WorkspaceName, T).


{ include("$jacamoJar/templates/common-cartago.asl") }
