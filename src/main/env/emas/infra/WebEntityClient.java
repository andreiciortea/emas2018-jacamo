package emas.infra;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;
import java.util.logging.Logger;

import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.RDFSyntax;
import org.apache.commons.rdf.rdf4j.RDF4J;
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

public class WebEntityClient {
  
  private final static Logger LOGGER = Logger.getLogger(WebEntityClient.class.getName());
  private HttpClient client;
  
  public WebEntityClient() {
    client = HttpClientBuilder.create().build();
  }
  
  public Optional<Graph> fetchEntity(String iri) {
    try {
      HttpResponse response = client.execute(new HttpGet(iri));
      int statusCode = response.getStatusLine().getStatusCode();
      
      if (statusCode == HttpStatus.SC_OK) {
        String payload = EntityUtils.toString(response.getEntity());
        
        if (payload != null && !payload.isEmpty()) {
          return Optional.of(stringToGraph(payload, iri, RDFSyntax.TURTLE));
        }
      } else {
        LOGGER.severe("Retrieving entity failed (status code " + statusCode + "): " + iri);
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
  
  private Graph stringToGraph(String graphString, String baseIRI, RDFSyntax syntax) throws IllegalArgumentException, IOException {
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

