package emas.infra;

import java.io.IOException;
import java.util.AbstractQueue;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import cartago.Artifact;
import cartago.ArtifactId;
import cartago.INTERNAL_OPERATION;
import cartago.OPERATION;

public class NotificationServerArtifact extends Artifact {
  
  private Map<String,ArtifactId> artifactRegistry;
  private Map<String,AbstractQueue<String>> notificationQueues;
  
  private Server server;
  private boolean httpServerRunning;
  
  public static final int NOTIFICATION_DELIVERY_DELAY = 100;
  
  void init(int port) {
    server = new Server(port);
    server.setHandler(new NotificationHandler());
    
    artifactRegistry = new Hashtable<String,ArtifactId>();
    notificationQueues = new Hashtable<String,AbstractQueue<String>>();
  }
  
  @OPERATION
  void registerArtifact(String artifactIRI, ArtifactId artifactId) {
    artifactRegistry.put(artifactIRI, artifactId);
    notificationQueues.put(artifactIRI, new ConcurrentLinkedQueue<String>());
  }
  
  @OPERATION
  void start() {
    try {
      httpServerRunning = true;
      subscribeForNotifications();
      
      execInternalOp("deliverNotifications");
      
      server.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  @OPERATION
  void stop() {
    try {
      server.stop();
      httpServerRunning = false;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  @INTERNAL_OPERATION
  void deliverNotifications() {
    while (httpServerRunning) {
      for (String artifactIRI : notificationQueues.keySet()) {
        AbstractQueue<String> notifications = notificationQueues.get(artifactIRI);
        ArtifactId artifactId = artifactRegistry.get(artifactIRI);
        
        while (!notifications.isEmpty()) {
          String n = notifications.poll();
          
          try {
            execLinkedOp(artifactId, "onNotification", n);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
      
      await_time(NOTIFICATION_DELIVERY_DELAY);
    }
  }
  
  private void subscribeForNotifications() {
    HttpClient client = new HttpClient();
    try {
      client.start();
      
      ContentResponse response = client.POST("http://localhost:8080/hub/")
          .content(new StringContentProvider("{"
              + "\"hub.mode\" : \"subscribe\","
              + "\"hub.topic\" : \"http://localhost:8080/artifacts/event-gen\","
              + "\"hub.callback\" : \"http://localhost:8081/notifications/\""
              + "}"), "application/json")
          .send();
      
      if (response.getStatus() != HttpStatus.SC_OK) {
        log("Request failed: " + response.getStatus());
      }
      
      client.stop();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  class NotificationHandler extends AbstractHandler {
    
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, 
        HttpServletResponse response) throws IOException, ServletException {

      String artifactIRI = null;
      Enumeration<String> linkHeadersEnum = baseRequest.getHeaders("Link");
      
      while (linkHeadersEnum.hasMoreElements()) {
        String value = linkHeadersEnum.nextElement();
        
        if (value.endsWith("rel=\"self\"")) {
          artifactIRI = value.substring(1, value.indexOf('>'));
        }
      }
      
      if (artifactIRI == null) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/plain");
        response.getWriter().println("Link headers are missing! See the W3C WebSub Recommendation for details.");
      } else {
          /** Note: the following code (commented out) will occasionally throw an IllegalMonitorStateException, 
          hence the need for an intermediary buffer. **/
          
//        ArtifactId artifactId = artifactRegistry.get(artifactIRI);
//        
//        if (artifactId != null) {
//          String notification = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
//          
//          try {
//            execLinkedOp(artifactId, "onNotification", notification);
//          } catch (OperationException e) {
//            log(e.getMessage());
//            e.printStackTrace();
//          }
//          
//          response.setStatus(HttpServletResponse.SC_OK);
//        } else {
//          response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//        }
        
        AbstractQueue<String> queue = notificationQueues.get(artifactIRI);
        
        if (queue != null) {
          String payload = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
          
          queue.add(payload);
          notificationQueues.put(artifactIRI, queue);
          
          response.setStatus(HttpServletResponse.SC_OK);
        } else {
          response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
      }
      
      baseRequest.setHandled(true);
    }
  }
	
}

