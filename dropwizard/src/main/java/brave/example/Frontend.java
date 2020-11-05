package brave.example;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

/** Example conventions include a self-contained main class. */
public class Frontend extends ExampleApplication<FrontendConfiguration> {
  @Path("/")
  public static class Resource {
    final Client client;
    final String backendEndpoint;

    Resource(Client client, String backendEndpoint) {
      this.client = client;
      this.backendEndpoint = backendEndpoint;
    }

    @GET
    @Path("/") public Response callBackend() {
      return client.target(backendEndpoint).request().get();
    }
  }

  Frontend() {
    super("frontend", 8081);
  }

  @Override public void run(FrontendConfiguration configuration, Environment environment) {
    super.run(configuration, environment);

    final Client client = new JerseyClientBuilder(environment).using(configuration.getBackend().getJerseyClientConfiguration())
        .build(getName());
    Resource resource = new Resource(client, configuration.getBackend().getEndpoint());

    environment.jersey().register(resource);
  }

  public static void main(String[] args) throws Exception {
    new Frontend().run();
  }
}
