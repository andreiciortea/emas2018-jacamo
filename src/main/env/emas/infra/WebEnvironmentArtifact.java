package emas.infra;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.api.Triple;
import org.apache.commons.rdf.rdf4j.RDF4J;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;
import emas.vocabularies.EVE;

public class WebEnvironmentArtifact extends Artifact {
  
  private WebEntityClient client;
  
  private String environmentIRI;
  private RDF rdfImpl = new RDF4J();
  
  void init(String environmentIRI) {
    this.environmentIRI = environmentIRI;
    client = new WebEntityClient();
  }
  
  @OPERATION
  void getWorkspaceIRIs(OpFeedbackParam<String[]> workspaceIRIs) {
    Set<IRI> workspaceIRIsSet = getMemberIRIs(environmentIRI);
    workspaceIRIs.set(transformIRISetToArray(workspaceIRIsSet));
  }
  
  @OPERATION
  void getWorkspaceDetails(String workspaceIRIStr, OpFeedbackParam<String> name, OpFeedbackParam<String[]> artifactIRIs) {
    Optional<Graph> graphOpt = client.fetchEntity(workspaceIRIStr);
    
    if (graphOpt.isPresent()) {
      Graph workspaceGraph = graphOpt.get();
      IRI workspaceIRI = rdfImpl.createIRI(workspaceIRIStr);
      
      Optional<String> workspaceName = workspaceGraph
                                          .stream(workspaceIRI, EVE.hasName, null)
                                          .findAny().map(Triple::getObject)
                                          .filter(obj -> obj instanceof Literal)
                                          .map(literalName -> ((Literal)literalName).getLexicalForm());
      
      if (workspaceName.isPresent()) {
        name.set(workspaceName.get());
      }
      
      String[] artifactIRIArray = workspaceGraph
                                  .stream(workspaceIRI, EVE.contains, null)
                                  .map(Triple::getObject)
                                  .filter(obj -> obj instanceof IRI)
                                  .map(iri -> ((IRI) iri).getIRIString())
                                  .toArray(String[]::new);
          
      artifactIRIs.set(artifactIRIArray);
    }
  }
  
  @OPERATION
  void getWorkspaceNames(OpFeedbackParam<String[]> workspaceNames) {
    Set<String> workspaceNamesSet = new HashSet<String>();
    Set<IRI> workspaceIRIs = getMemberIRIs(environmentIRI);
    
    for (IRI workspaceIRI : workspaceIRIs) {
      Optional<Graph> graphOpt = client.fetchEntity(workspaceIRI.getIRIString());
      
      if (graphOpt.isPresent()) {
        Graph workspaceGraph = graphOpt.get();
        
        Optional<String> workspaceName = workspaceGraph
                                            .stream(workspaceIRI, EVE.hasName, null)
                                            .findAny().map(Triple::getObject)
                                            .filter(obj -> obj instanceof Literal)
                                            .map(literalName -> ((Literal)literalName).getLexicalForm());
        
        if (workspaceName.isPresent()) {
          workspaceNamesSet.add(workspaceName.get());
        }
      }
    }
    
    workspaceNames.set(workspaceNamesSet.stream().toArray(String[]::new));
  }
  
  @OPERATION
  void getArtifactIRIs(String workspaceIRI, OpFeedbackParam<String[]> artifactIRIs) {
    Set<IRI> artifactIRIsSet = getMemberIRIs(workspaceIRI);
    artifactIRIs.set(transformIRISetToArray(artifactIRIsSet));
  }
  
  @OPERATION
  void getArtifactDetails(String artifactIRIStr, OpFeedbackParam<String> name, 
      OpFeedbackParam<String> className, OpFeedbackParam<String[]> params) {
    
    Optional<Graph> graphOpt = client.fetchEntity(artifactIRIStr);
    
    if (graphOpt.isPresent()) {
      Graph graph = graphOpt.get();
      IRI artifactIRI = rdfImpl.createIRI(artifactIRIStr);
      
      Optional<String> artfiactName =
          graph.stream(artifactIRI, EVE.hasName, null)
                  .findAny().map(Triple::getObject)
                  .filter(obj -> obj instanceof Literal)
                  .map(literalName -> ((Literal)literalName).getLexicalForm());
      
      if (artfiactName.isPresent()) {
        name.set(artfiactName.get());
      }
      
      Optional<String> artfiactClassName =
          graph.stream(artifactIRI, EVE.hasCartagoArtifact, null)
                  .findAny().map(Triple::getObject)
                  .filter(obj -> obj instanceof Literal)
                  .map(literalName -> ((Literal)literalName).getLexicalForm());
      
      if (artfiactClassName.isPresent()) {
        className.set(artfiactClassName.get());
      }
      
      String[] artfiactInitParams =
          graph.stream(artifactIRI, EVE.hasInitParam, null)
                  .map(Triple::getObject)
                  .map(obj -> obj.toString())
                  .toArray(String[]::new);
      
      if (artfiactInitParams.length > 0) {
        params.set(artfiactInitParams);
      } else {
        params.set(new String[0]);
      }
    }
  }
  
  private String[] transformIRISetToArray(Set<IRI> iriSet) {
    return iriSet.stream().map(iri -> iri.getIRIString()).toArray(String[]::new);
  }
  
  // TODO: validate member type
  private Set<IRI> getMemberIRIs(String containerIRI) {
    Set<IRI> memberIRIs = new HashSet<IRI>();
    
    Optional<Graph> graphOpt = client.fetchEntity(containerIRI);
    
    if (graphOpt.isPresent()) {
      Graph environmentGraph = graphOpt.get();
      
      for (Triple triple : environmentGraph.iterate(rdfImpl.createIRI(containerIRI), EVE.contains, null)) {
        if (triple.getObject() instanceof IRI) { 
          memberIRIs.add((IRI) triple.getObject());
        }
      }
    }
    
    return memberIRIs;
  }
}
