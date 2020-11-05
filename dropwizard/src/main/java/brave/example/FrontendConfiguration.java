package brave.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class FrontendConfiguration extends Configuration {
  static class BackendConfiguration extends Configuration {
    @NotNull String endpoint = "http://127.0.0.1:9000/api";

    @Valid
    @NotNull
    private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

    @JsonProperty public String getEndpoint() {
      return endpoint;
    }

    @JsonProperty public void setEndpoint(String endpoint) {
      this.endpoint = endpoint;
    }

    @JsonProperty("jerseyClient")
    public JerseyClientConfiguration getJerseyClientConfiguration() {
      return jerseyClient;
    }

    @JsonProperty("jerseyClient")
    public void setJerseyClientConfiguration(JerseyClientConfiguration jerseyClient) {
      this.jerseyClient = jerseyClient;
    }
  }

  final BackendConfiguration backend = new BackendConfiguration();

  @JsonProperty public BackendConfiguration getBackend() {
    return backend;
  }
}