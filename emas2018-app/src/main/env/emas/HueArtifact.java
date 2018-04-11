package emas;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import cartago.Artifact;
import cartago.OPERATION;


public class HueArtifact extends Artifact {
    
    private final int UNDEFINED = -1;
    
    String lightbulbUri;
    
    
	void init(String uri) {
	    this.lightbulbUri = uri;
	}
	
	private int setState(String state) {
	    HttpPut request = new HttpPut(lightbulbUri);
        
        try {
            
            request.setEntity(new StringEntity(state));
            
            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse response = client.execute(request);
            
            return response.getStatusLine().getStatusCode();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return UNDEFINED;
	}
	
	@OPERATION
	void turnLightOn() {
	    log("Turning light on!");
//	    setState("{ \"on\" : true }");
	}
	
	@OPERATION
    void turnLightOn(double x, double y) {
	      log("Turning light on with colors: " + x + " " + y);
        setState("{ \"on\" : true, \"xy\" : [" + x + ", " + y + "] }");
    }
	
	@OPERATION
	void turnLightOff() {
	    log("Turning light off!");
//	    setState("{ \"on\" : false }");
	}
}

