package emas.vocabularies;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.rdf4j.RDF4J;

public class TD {

  private static final String PREFIX = "http://www.w3.org/ns/td#";
  private static RDF rdfImpl = new RDF4J();
  
  public static IRI createIRI(String fragment) {
    return rdfImpl.createIRI(PREFIX + fragment);
  }
  
  public static final IRI Thing         = createIRI("Thing");
  public static final IRI Action        = createIRI("Action");
  
  public static final IRI name          = createIRI("name");
  public static final IRI interaction   = createIRI("interaction");
}
