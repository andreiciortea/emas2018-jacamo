package ro.andreiciortea.wot.td;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import ro.andreiciortea.wot.vocabularies.TDVocab;

public class ThingDescription {
  
  private final IRI isA = (new RDF4J()).createIRI(RDF.TYPE.stringValue());
  
  private IRI thingIRI;
  private Graph tdGraph;
  
  private Optional<String> thingName;
  private Optional<IRI> baseIRI;
  
  private Map<IRI,Action> actions;
  
  public ThingDescription(IRI thingIRI, Graph tdGraph) {
    this.thingIRI = thingIRI;
    this.tdGraph = tdGraph;
    
    Optional<Literal> name = getFirstObjectAsLiteral(TDVocab.name);
    this.thingName = (name.isPresent()) ? Optional.of(name.get().getLexicalForm()) : Optional.empty();
    
    this.baseIRI = getFirstObjectAsIRI(TDVocab.base);
  }
  
  public Optional<String> getName() {
    return thingName;
  }
  
  public Optional<IRI> getBaseIRI() {
    return baseIRI;
  }
  
  public List<IRI> getActionTypes() {
    List<BlankNodeOrIRI> actions = tdGraph.stream(thingIRI, TDVocab.interaction, null)
        .filter(t -> t.getObject() instanceof BlankNodeOrIRI)
        .map(t -> (BlankNodeOrIRI) t.getObject())
        .filter(interaction -> tdGraph.contains(interaction, isA, TDVocab.Action))
        .collect(Collectors.toList());
    
    List<IRI> actionTypes = new ArrayList<IRI>();
    
    actions.forEach(action -> {
      List<IRI> actionTypeIRIs = tdGraph.stream(action, isA, null)
                                .filter(triple -> (triple.getObject() instanceof IRI && !triple.getObject().equals(TDVocab.Action)))
                                .map(triple -> (IRI) triple.getObject())
                                .collect(Collectors.toList());
      
      if (!actionTypeIRIs.isEmpty()) {
        // TODO: handle case with multiple action type IRIs
        actionTypes.add(actionTypeIRIs.get(0));
      }
    });
    
    return actionTypes;
  }
  
  public HTTPForm getForm(IRI actionTypeIRI) {
    
    
    return null;
  }
  
  public Graph getGraph() {
    return tdGraph;
  }
  
  private Optional<RDFTerm> getFirstObject(IRI propertyIRI) {
    if (!tdGraph.contains(thingIRI, propertyIRI, null)) {
      return Optional.empty();
    }
    
    RDFTerm object = tdGraph.stream(thingIRI, TDVocab.name, null).findFirst().get().getObject();
    
    return Optional.of(object);
  }
  
  private Optional<Literal> getFirstObjectAsLiteral(IRI propertyIRI) {
    Optional<RDFTerm> term = getFirstObject(propertyIRI);
    
    if (term.isPresent() && term.get() instanceof Literal) {
      return Optional.of((Literal) term.get());
    }
    
    return Optional.empty();
  }
  
  private Optional<IRI> getFirstObjectAsIRI(IRI propertyIRI) {
    Optional<RDFTerm> term = getFirstObject(propertyIRI);
    
    if (term.isPresent() && term.get() instanceof IRI) {
      return Optional.of((IRI) term.get());
    }
    
    return Optional.empty();
  }
}
