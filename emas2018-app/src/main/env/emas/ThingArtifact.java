package emas;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.api.RDFSyntax;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;

import cartago.Artifact;
import cartago.LINK;
import cartago.OPERATION;
import emas.infra.Notification;
import ro.andreiciortea.wot.td.TDHttpClient;
import ro.andreiciortea.wot.td.TDParser;
import ro.andreiciortea.wot.td.ThingDescription;

public class ThingArtifact extends Artifact {
  
  private String artifactIRI;
  private ThingDescription thing;
  
  private Set<String> exposedActions;
  
  private RDF rdfImpl = new RDF4J();
  
  void init(String artifactIRI) {
    this.artifactIRI = artifactIRI;
    defineObsProperty("hasIRI", artifactIRI);

    this.exposedActions = new HashSet<String>();
    
    try {
      ThingDescription thing = TDParser.parseFromHttpIRI(rdfImpl.createIRI(artifactIRI));
      updateDescription(thing);
    } catch (ParseException | IllegalArgumentException | IOException e) {
      e.printStackTrace();
    }
  }
  
  @LINK
  void onNotification(Notification notification) {
    try {
      ThingDescription thing = TDParser.parseFromString(rdfImpl.createIRI(artifactIRI), 
                                  notification.getMessage(), RDFSyntax.TURTLE);
      updateDescription(thing);
    } catch (IllegalArgumentException | IOException e) {
      e.printStackTrace();
    }
    log("Updated hue thing artifact with new state!");
  }
  
  @OPERATION
  void act(String actionTypeIRI, Object[] params) {
    TDHttpClient client = new TDHttpClient(thing);
    
    Map<IRI, Object> paramMap = buildParamMap(params);
    
    HttpResponse response = client.performActionByType(rdfImpl.createIRI(actionTypeIRI), paramMap);
    
    if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
      // TODO: process output
      log("Performed operation: " + actionTypeIRI + ", params: " + buildParamMap(params));
    } else {
      log("Could not perform operation: " + actionTypeIRI + ", params: " + buildParamMap(params));
    }
  }
  
  private Map<IRI, Object> buildParamMap(Object[] params) {
    Map<IRI, Object> paramMap = new HashMap<IRI, Object>();
    
    for (int i = 0; i < params.length; ++ i) {
      Object[] pair = (Object[]) params[i];
      paramMap.put(rdfImpl.createIRI((String) pair[0]), pair[1]);
    }
    
    return paramMap;
  }
  
  private void updateDescription(ThingDescription thing) {
    this.thing = thing;
    
    Set<String> newActions = thing.getSupportedActionTypes().stream()
                                    .map(iri -> iri.getIRIString())
                                    .collect(Collectors.toSet());
    
    // Clear out stale actions
    exposedActions.removeAll(newActions);
    exposedActions.forEach(action -> {
      removeObsPropertyByTemplate("hasAction", artifactIRI, action);
    });
    
    newActions.forEach(action -> {
      if (getObsPropertyByTemplate("hasAction", artifactIRI, action) == null) {
        defineObsProperty("hasAction", artifactIRI, action);
      }
    });
    
    this.exposedActions = newActions;
    
    // TODO: add params for all actions
    // TODO: implement agent rule to test if an action can be exec
  }
}
