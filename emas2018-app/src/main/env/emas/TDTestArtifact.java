package emas;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import cartago.Artifact;
import cartago.OPERATION;
import emas.vocabularies.EVE;
import emas.vocabularies.TD;
import ro.andreiciortea.wot.td.ThingDescription;
import ro.andreiciortea.wot.td.TDParser;

public class TDTestArtifact extends Artifact {
	@OPERATION
	void getTD(String iri) {
	  RDF4J rdfImpl = new RDF4J();
	  
	  IRI artifactIRI = rdfImpl.createIRI(iri);
	  ThingDescription td = TDParser.parseFromIRI(artifactIRI);
	  
	  try {
        IRI isA = rdfImpl.createIRI(RDF.TYPE.stringValue());
        Graph tdGraph = td.getGraph();
        
        log("Retrieved TD, is artifact: " + tdGraph.contains(artifactIRI, isA, EVE.Artifact));
        
        List<IRI> types = tdGraph.stream(artifactIRI, isA, null)
          .map(t -> t.getObject())
          .filter(obj -> obj instanceof IRI)
          .map(obj -> (IRI) obj)
          .collect(Collectors.toList());
        
        log("Has types: " + types);
        
        List<BlankNodeOrIRI> actions = tdGraph.stream(artifactIRI, TD.interaction, null)
                  .filter(t -> t.getObject() instanceof BlankNodeOrIRI)
                  .map(t -> (BlankNodeOrIRI) t.getObject())
                  .filter(interaction -> tdGraph.contains(interaction, isA, TD.Action))
                  .collect(Collectors.toList());
        
        actions.forEach(action -> {
          List<IRI> actionTypes = tdGraph.stream(action, isA, null)
                                    .filter(triple -> (triple.getObject() instanceof IRI && !triple.getObject().equals(TD.Action)))
                                    .map(triple -> (IRI) triple.getObject())
                                    .collect(Collectors.toList());
          
          log("Found an action with types: " + actionTypes);
        });
        
        log("Has actions: " + actions);
        
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      }
	}
}
