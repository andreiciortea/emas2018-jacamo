package ro.andreiciortea.wot.td;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TDHttpClient {

  private final static Logger LOGGER = LoggerFactory.getLogger(TDHttpClient.class.getName());
  
  private ThingDescription td;
  
  public TDHttpClient(ThingDescription td) {
    this.td = td;
  }
  
  public HttpResponse performActionByType(IRI actionTypeIRI, Map<IRI, Object> input) {
    Optional<Action> action = td.getAction(actionTypeIRI);
    
    if (action.isPresent()) {
      List<HTTPForm> forms = action.get().getForms();
      Optional<Schema> inputSchema = action.get().getInputSchema();
      
      if (!forms.isEmpty()) {
        // TODO: select form
        HTTPForm form = forms.get(0);
        
        try {
          HttpUriRequest request = buildHttpRequest(actionTypeIRI, form, inputSchema, input);
          
//          System.out.println("Ready to execute request for: " + request.getRequestLine());
          
          HttpClient client = HttpClientBuilder.create().build();
          HttpResponse response = client.execute(request);
          
//          System.out.println("Status code: " + response.getStatusLine().getStatusCode());
          
          return response;
        } catch (Exception e) {
          LOGGER.error(e.getMessage());
        }
      }
    }
    
    return null;
  }
  
  private HttpUriRequest buildHttpRequest(IRI actionTypeIRI, HTTPForm form, Optional<Schema> inputSchema, Map<IRI, Object> input) {
    HttpUriRequest request = null;
    URI requestIRI = buildRequestIRI(form);
    
    switch (form.getMethodName()) {
      // TODO: support other methods
      case HttpPut.METHOD_NAME: request = buildPutRequest(actionTypeIRI, requestIRI, inputSchema, input); break;
      default: break;
    }
    
    return request;
  }
  
  private URI buildRequestIRI(HTTPForm form) {
    URI requestURI;
    
    try {
      Optional<IRI> baseIRI = td.getBaseIRI();
      
      if (baseIRI.isPresent()) {
        requestURI = new URI(baseIRI.get().getIRIString());
        return requestURI.resolve(form.getHref());
      } else {
        return new URI(form.getHref());
      }
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    
    return null;
  }
  
  private HttpPut buildPutRequest(IRI actionTypeIRI, URI requestIRI, Optional<Schema> inputSchema, Map<IRI, Object> input) {
    HttpPut request = new HttpPut(requestIRI);
    
    try {
      String payload = buildPayload(actionTypeIRI, inputSchema, input);
      
//      System.out.println("Payload: " + payload);
      
      request.setEntity(new StringEntity(payload));
    } catch (UnsupportedEncodingException e) {
      LOGGER.error(e.getMessage());
    }
    
    return request;
  }
  
  private String buildPayload(IRI actionTypeIRI, Optional<Schema> schema, Map<IRI, Object> input) {
    // TODO
    
    RDF4J rdfImpl = new RDF4J();
    
    IRI CIExIRI = rdfImpl.createIRI("http://iotschema.org/CIExData");
    IRI CIEyIRI = rdfImpl.createIRI("http://iotschema.org/CIEyData");
    
    if (input.containsKey(CIExIRI) && input.containsKey(CIEyIRI)) {
      return "{ \"on\" : true, \"xy\" : [" + (double) input.get(CIExIRI) + ", " + (double) input.get(CIEyIRI) + "] }";
    } else if (actionTypeIRI.getIRIString().equals("http://iotschema.org/SwitchOn")) {
      return "{ \"on\" : true }";
    } else if (actionTypeIRI.getIRIString().equals("http://iotschema.org/SwitchOff")) {
      return "{ \"on\" : false }";
    }
    
    return "";
  }
}
