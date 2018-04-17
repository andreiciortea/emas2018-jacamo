package ro.andreiciortea.wot.td;

import java.util.List;

import org.apache.commons.rdf.api.IRI;

public class HTTPForm {
  private String methodName;
  private IRI href;
  private String mediaType;
  private List<String> rel;
  
  public HTTPForm(String methodName, IRI href, String mediaType, List<String> rel) {
    this.methodName = methodName;
    this.href = href;
    this.mediaType = mediaType;
    this.rel = rel;
  }

  public String getMethodName() {
    return methodName;
  }

  public IRI getHref() {
    return href;
  }

  public String getMediaType() {
    return mediaType;
  }

  public List<String> getRel() {
    return rel;
  }
}
