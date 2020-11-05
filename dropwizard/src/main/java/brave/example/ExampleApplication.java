package brave.example;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

abstract class ExampleApplication<C extends Configuration> extends Application<C> {
  /** Fake /health endpoint that allows us to ensure our HEALTHCHECK doesn't start traces. */
  @Path("/")
  public static class HealthCheck {
    @GET
    @Path("/health")
    public String health() {
      return "ok";
    }
  }

  final String name;

  ExampleApplication(String name, int port) {
    this.name = name;
    System.setProperty("dw.server.applicationConnectors[0].port", String.valueOf(port));
  }

  @Override public final String getName() {
    return name;
  }

  @Override public void run(C configuration, Environment environment) {
    // Our example is single port per app. Also, it is easiest when all health URLs are the same
    // HTTP path (/health). As these aren't configurable, add our own /health directly
    environment.jersey().register(new HealthCheck());
  }

  /** Centralized config for both applications go into one yaml file. */
  void run() throws Exception {
    run("server", "example.yml");
  }

  @Override public final void initialize(Bootstrap<C> bootstrap) {
    bootstrap.setConfigurationSourceProvider(new ResourceConfigurationSourceProvider());
  }
}
