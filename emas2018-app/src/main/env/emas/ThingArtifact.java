package emas;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.eclipse.jetty.http.HttpMethod;

import cartago.Artifact;
import cartago.LINK;
import cartago.OPERATION;
import emas.infra.Notification;
import ro.andreiciortea.wot.td.HTTPForm;
import ro.andreiciortea.wot.td.TDParser;
import ro.andreiciortea.wot.td.ThingDescription;

public class ThingArtifact extends Artifact {
  
  private ThingDescription thing;
  
  private String artifactIRI;
  private Optional<String> artifactName;
  
  private Set<String> actions;
  
  private RDF rdfImpl = new RDF4J();
  
  void init(String artifactIRI) {
    this.artifactIRI = artifactIRI;
    defineObsProperty("hasIRI", artifactIRI);

    this.actions = new HashSet<String>();
    
    ThingDescription thing = TDParser.parseFromIRI(rdfImpl.createIRI(artifactIRI));
    updateDescription(thing);
  }
  
  @LINK
  void onNotification(Notification notification) {
    ThingDescription thing = TDParser.parseFromString(rdfImpl.createIRI(artifactIRI), notification.getMessage());
    updateDescription(thing);
    log("Updated hue thing artifact with new state!");
  }
  
  @OPERATION
  void act(String actionTypeIRI, Object[] params) {
    // TODO
    
    log("Performing operation: " + actionTypeIRI + ", params: " + Arrays.asList(params));
    
//    HttpRequest request = buildHTTPRequest(thing.getForm(rdfImpl.createIRI(actionTypeIRI)), params);
  }
  
  private void updateDescription(ThingDescription thing) {
    this.thing = thing;
    this.artifactName = thing.getName();
    
//  String[] actions = thing.getActionTypes().stream()
//                                .map(iri -> iri.getIRIString())
//                                .toArray(String[]::new);
//  defineObsProperty("hasActions", artifactIRI, (Object[]) actions);
  
  Set<String> newActions = thing.getActionTypes().stream()
                                .map(iri -> iri.getIRIString())
                                .collect(Collectors.toSet());
  
  // Clear out deleted actions
  actions.removeAll(newActions);
  actions.forEach(action -> {
    removeObsPropertyByTemplate("hasAction", artifactIRI, action);
  });
  
  newActions.forEach(action -> {
    if (getObsPropertyByTemplate("hasAction", artifactIRI, action) == null) {
      defineObsProperty("hasAction", artifactIRI, action);
    }
  });
  
  this.actions = newActions;
  
  // TODO: add params for all actions
  // TODO: implement agent rule to test if an action can be exec
  }
  
  private HttpRequest buildHTTPRequest(HTTPForm form, Object[] params) {
    HttpMethod method = HttpMethod.valueOf(form.getMethodName().toUpperCase());
    
    URI requestIRI = buildRequestIRI(form);

    if (requestIRI == null) {
      throw new IllegalArgumentException("Invalid request IRI");
    }
    
    HttpRequest request = null;
    
    switch (method) {
      // TODO: support other methods
      case PUT: request = buildPutRequest(requestIRI); break;
      default: break;
    }
    
    return request;
  }
  
  private HttpRequest buildPutRequest(URI requestIRI) {
    HttpPut request = new HttpPut(requestIRI);
    
    try {
      request.setEntity(new StringEntity("{ \"on\": true }"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    };
    
    return request;
  }
  
  private URI buildRequestIRI(HTTPForm form) {
    URI requestURI;
    
    try {
      Optional<IRI> baseIRI = thing.getBaseIRI();
      
      if (baseIRI.isPresent()) {
          requestURI = new URI(baseIRI.get().getIRIString());
          
          return requestURI.resolve(form.getHref().getIRIString());
      } else {
        return new URI(form.getHref().getIRIString());
      }
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    
    return null;
  }
  
  private void performHTTPRequest() {
    // TODO
  }
}
