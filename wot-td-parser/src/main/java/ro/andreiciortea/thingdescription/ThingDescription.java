package ro.andreiciortea.thingdescription;

public class ThingDescription {

  private String thingName;
  private String baseUri;
  
  
  public ThingDescription(String thingName, String baseUri) {
      this.thingName = thingName;
      this.baseUri = baseUri;
  }
  
  public String getThingName() {
      return this.thingName;
  }
  
  public String getBaseUri() {
      return this.baseUri;
  }
}
