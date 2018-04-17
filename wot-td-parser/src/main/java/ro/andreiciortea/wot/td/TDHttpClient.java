package ro.andreiciortea.wot.td;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.apache.commons.rdf.api.IRI;

public class TDHttpClient {

  private ThingDescription td;
  
  public TDHttpClient(ThingDescription td) {
    this.td = td;
  }
  
  public void performActionByType(IRI actionTypeIRI) {
    
  }
  
  private URI buildRequestIRI(HTTPForm form) {
    URI requestURI;
    
    try {
      Optional<IRI> baseIRI = td.getBaseIRI();
      
      if (baseIRI.isPresent()) {
          requestURI = new URI(baseIRI.get().getIRIString());
          
          return requestURI.resolve(form.getHref().getIRIString());
      } else {
        return new URI(form.getHref().getIRIString());
      }
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    
    return null;
  }
}
