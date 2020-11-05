package brave.example;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.web.SpringServletContainerInitializer;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/** Indirectly invoked by {@link SpringServletContainerInitializer} in a Servlet 3+ container */
public class Initializer extends AbstractAnnotationConfigDispatcherServletInitializer {
  static {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }

  @Override protected String[] getServletMappings() {
    return new String[] {"/"};
  }

  @Override protected Class<?>[] getRootConfigClasses() {
    return new Class[] {AppConfiguration.class};
  }

  @Override protected Class<?>[] getServletConfigClasses() {
    return new Class[] {Frontend.class, Backend.class};
  }
}
