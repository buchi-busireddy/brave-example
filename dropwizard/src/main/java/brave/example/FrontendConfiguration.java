package brave.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class FrontendConfiguration extends Configuration {
  final BackendConfiguration backend = new BackendConfiguration();

  @JsonProperty public BackendConfiguration getBackend() {
    return backend;
  }
}