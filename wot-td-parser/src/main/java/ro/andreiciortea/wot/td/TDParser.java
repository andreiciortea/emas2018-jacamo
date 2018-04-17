package ro.andreiciortea.wot.td;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFSyntax;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.apache.commons.rdf.rdf4j.RDF4JGraph;
import org.apache.commons.rdf.rdf4j.RDF4JTriple;
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
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

public class TDParser {
  
  private static final Logger LOGGER = Logger.getLogger(TDParser.class.getName());
  
  // TODO: what about CoAP IRIs?
  public static ThingDescription parseFromIRI(IRI thingIRI) {
    try {
      HttpClient client = HttpClientBuilder.create().build();
      HttpResponse response = client.execute(new HttpGet(thingIRI.getIRIString()));
      int statusCode = response.getStatusLine().getStatusCode();
      
      if (statusCode == HttpStatus.SC_OK) {
        String payload = EntityUtils.toString(response.getEntity());
        
        if (payload != null && !payload.isEmpty()) {
          Graph graph = stringToGraph(payload, thingIRI.getIRIString(), RDFSyntax.TURTLE);
          
          return new ThingDescription(thingIRI, graph);
        }
      } else {
        LOGGER.severe("Retrieving entity failed (status code " + statusCode + "): " + thingIRI.getIRIString());
      }
    }
    catch (ClientProtocolException e) {
      LOGGER.severe(e.getMessage());
    }
    catch (IOException e) {
      LOGGER.severe(e.getMessage());
    }
    
    return null;
  }
  
  public static ThingDescription parseFromString(IRI thingIRI, String data) {
    try {
      Graph graph = stringToGraph(data, thingIRI.getIRIString(), RDFSyntax.TURTLE);
      return new ThingDescription(thingIRI, graph);
    } catch (IllegalArgumentException e) {
      LOGGER.severe(e.getMessage());
    } catch (IOException e) {
      LOGGER.severe(e.getMessage());
    }
    
    return null;
  }
  
  private Interaction parseInteraction(Graph graph, BlankNodeOrIRI ref) {
    
    
    return null;
  }
  
  public static Graph stringToGraph(String graphString, String baseIRI, RDFSyntax syntax) throws IllegalArgumentException, IOException {
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
  
  public static String graphToString(Graph graph, RDFSyntax syntax) throws IllegalArgumentException, IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    // TODO: don't hardcode the RDF format
    RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, out);
    
    if (graph instanceof RDF4JGraph) {
      try {
        writer.startRDF();
        try (Stream<RDF4JTriple> stream = ((RDF4JGraph) graph).stream()) {
          stream.forEach(triple -> {
            writer.handleStatement(triple.asStatement());
          });
        }
        writer.endRDF();
      }
      catch (RDFHandlerException e) {
        throw new IOException("RDF handler exception: " + e.getMessage());
      }
      catch (UnsupportedRDFormatException e) {
        throw new IllegalArgumentException("Unsupported RDF syntax: " + e.getMessage()); 
      }
      finally {
        out.close();
      }
    } else {
      throw new IllegalArgumentException("Unsupported RDF graph implementation");
    }
    
    return out.toString();
  }
}
