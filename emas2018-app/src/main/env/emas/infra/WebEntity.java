package emas.infra;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.api.RDFSyntax;
import org.apache.commons.rdf.api.Triple;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import emas.vocabularies.EVE;
import emas.vocabularies.TD;

public class WebEntity {
  private IRI entityIRI;
  private Graph entityGraph;
  private Optional<IRI> subscriptionIRI;
  
  private static final Logger LOGGER = Logger.getLogger(WebEntity.class.getName());
  
  public WebEntity(IRI entityIRI, Graph entityGraph, Optional<IRI> webSubHubIRI) {
    this.entityIRI = entityIRI;
    this.entityGraph = entityGraph;
    this.subscriptionIRI = webSubHubIRI;
  }
  
  public IRI getIRI() {
    return entityIRI;
  }
  
  public Graph getGraph() {
    return entityGraph;
  }
  
  public IRI getSubscriptionIRI() {
    return subscriptionIRI.get();
  }
  
  public boolean isMutable() {
    return subscriptionIRI.isPresent();
  }
  
  public boolean isThing() {
    IRI isA = (new RDF4J()).createIRI(org.eclipse.rdf4j.model.vocabulary.RDF.TYPE.stringValue());
    return entityGraph.stream(entityIRI, isA, TD.Thing).findAny().isPresent();
  }
  
  public Optional<String> getName() {
    Optional<String> eveName = getStringObject(entityIRI, EVE.hasName);
    
    if (eveName.isPresent()) {
      return eveName;
    }
    
    return getStringObject(entityIRI, TD.name);
  }
  
  public List<IRI> getMembers() {
    return entityGraph.stream(entityIRI, EVE.contains, null)
                        .map(Triple::getObject)
                        .filter(obj -> obj instanceof IRI)
                        .map(obj -> (IRI) obj)
                        .collect(Collectors.toList());
  }
  
  public Optional<IRI> getIRIObject(IRI subject, IRI predicate) {
    return entityGraph.stream(subject, predicate, null)
                        .map(Triple::getObject)
                        .filter(obj -> obj instanceof IRI)
                        .map(obj -> (IRI) obj)
                        .findFirst();
  }
  
  public Optional<IRI> getIRIObject(IRI predicate) {
    return getIRIObject(entityIRI, predicate);
  }
  
  public Optional<String> getStringObject(IRI subject, IRI predicate) {
    return entityGraph.stream(subject, predicate, null)
                        .findAny().map(Triple::getObject)
                        .filter(obj -> obj instanceof Literal)
                        .map(literalName -> ((Literal)literalName).getLexicalForm());
  }
  
  public Optional<String> getStringObject(IRI predicate) {
    return getStringObject(entityIRI, predicate);
  }
  
  public static Optional<WebEntity> buildFromString(String entityIRI, String entityGraph, String hubIRI) throws IllegalArgumentException, IOException {
      RDF rdf4j = new RDF4J();
      
      IRI iri = rdf4j.createIRI(entityIRI);
      Graph graph = stringToGraph(entityGraph, entityIRI, RDFSyntax.TURTLE);
      Optional<IRI> hub = (hubIRI == null) ? Optional.empty() : Optional.of(rdf4j.createIRI(hubIRI));
      
      return Optional.of(new WebEntity(iri, graph, hub));
  }
  
  public static Optional<WebEntity> fetchEntity(String entityIRI) {
    RDF rdfImpl = new RDF4J();
    
    try {
      HttpClient client = HttpClientBuilder.create().build();
      HttpResponse response = client.execute(new HttpGet(entityIRI));
      int statusCode = response.getStatusLine().getStatusCode();
      
      if (statusCode == HttpStatus.SC_OK) {
        String payload = EntityUtils.toString(response.getEntity());
        
        if (payload != null && !payload.isEmpty()) {
          Graph graph = stringToGraph(payload, entityIRI, RDFSyntax.TURTLE);
          Optional<IRI> webSubHubIRI = Optional.empty();
          Header[] linkHeaders = response.getHeaders("Link");
          
          for (Header h : linkHeaders) {
            if (h.getValue().endsWith("rel=\"hub\"")) {
              String hubIRIStr = h.getValue().substring(1, h.getValue().indexOf('>'));
              webSubHubIRI = Optional.of(rdfImpl.createIRI(hubIRIStr));
            }
          }
          
          return Optional.of(new WebEntity((new RDF4J()).createIRI(entityIRI), graph, webSubHubIRI));
        }
      } else {
        LOGGER.severe("Retrieving entity failed (status code " + statusCode + "): " + entityIRI);
      }
    }
    catch (ClientProtocolException e) {
      LOGGER.severe(e.getMessage());
    }
    catch (IOException e) {
      LOGGER.severe(e.getMessage());
    }
    
    return Optional.empty();
  }
  
  private static Graph stringToGraph(String graphString, String baseIRI, RDFSyntax syntax) throws IllegalArgumentException, IOException {
    StringReader stringReader = new StringReader(graphString);
    
    // TODO: don't hardcode the RDF format
    RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
    Model model = new LinkedHashModel();
    rdfParser.setRDFHandler(new StatementCollector(model));
    
    try {
      rdfParser.parse(stringReader, baseIRI);
    }
    catch (RDFParseException e) {
      throw new IllegalArgumentException("RDF parse error: " + e.getMessage());
    }
    catch (RDFHandlerException e) {
      throw new IOException("RDF handler exception: " + e.getMessage());
    }
    finally {
      stringReader.close();
    }
    
    return (new RDF4J()).asGraph(model);
  }
}
