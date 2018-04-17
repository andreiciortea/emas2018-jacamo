package ro.andreiciortea.wot.td;

import java.util.List;
import java.util.Optional;

import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;

public class Action extends Interaction {
  
  public Action(BlankNodeOrIRI iri, Optional<String> name, List<IRI> types, List<HTTPForm> forms) {
    super(iri, name, types, forms);
  }
}
