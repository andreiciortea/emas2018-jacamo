package ro.andreiciortea.wot.vocabularies;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.rdf4j.RDF4J;

public class TDVocab {

  private static final String PREFIX = "http://www.w3.org/ns/td#";
  private static RDF rdfImpl = new RDF4J();
  
  public static IRI createIRI(String fragment) {
    return rdfImpl.createIRI(PREFIX + fragment);
  }
  
  public static final IRI Thing         = createIRI("Thing");
  public static final IRI Action        = createIRI("Action");
  
  public static final IRI name          = createIRI("name");
  public static final IRI base          = createIRI("base");
  public static final IRI interaction   = createIRI("interaction");
  
  public static final IRI form          = createIRI("form");
  public static final IRI methodName    = createIRI("methodName");
  public static final IRI href          = createIRI("href");
  public static final IRI mediaType     = createIRI("mediaType");
  public static final IRI rel           = createIRI("rel");
  
  public static final IRI inputSchema   = createIRI("inputSchema");
  public static final IRI outputSchema  = createIRI("outputSchema");
}
