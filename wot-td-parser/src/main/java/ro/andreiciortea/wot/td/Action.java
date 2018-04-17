package ro.andreiciortea.wot.td;

import java.util.List;
import java.util.Optional;

import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;

public class Action extends Interaction {
  
  private Optional<Schema> inputSchema;
  
  public Action(BlankNodeOrIRI iri, Optional<String> name, List<IRI> types, 
      List<HTTPForm> forms, Optional<Schema> inputSchema) {
    super(iri, name, types, forms);
    
    this.inputSchema = inputSchema;
  }
  
  public Optional<Schema> getInputSchema() {
    return inputSchema;
  }
}
