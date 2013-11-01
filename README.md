# brave-resteasy-example #

Example RESTEasy service implementation that shows the use of the client and server side interceptors from the
`brave-resteasy-spring` module and the usage of [brave](https://github.com/kristofa/brave) in general.

## What is it about? ##

On one hand there is the service resource which can be found in following class: 
`com.github.kristofa.brave.resteasyexample.RestEasyExampleResourceImpl`

Next to that there is an integration test: `com.github.kristofa.brave.resteasyexample.ITRestEasyExample` that sets up
an embedded Jetty server at port `8080` which deploys our service resource at context path http://localhost:8080/RestEasyTest.

The resource (RestEasyExampleResource) makes available 2 URI's

*   GET http://localhost:8080/RestEasyTest/brave-resteasy-example/a
*   GET http://localhost:8080/RestEasyTest/brave-resteasy-example/b


The test code (ITRestEasyExample) sets up our EndPoint, does a http GET request to http://localhost:8080/RestEasyTest/brave-resteasy-example/a
The code that is triggered through
this URI will make a new call to the other URI: http://localhost:8080/RestEasyTest/brave-resteasy-example/b.  

For both requests our client and server side interceptors that use the Brave api are executed.  This results in 2 spans being logged.
The test uses `LoggingSpanCollectorImpl` which simply logs the spans through slf4j:

    14:57:42,730 INFO  [qtp948887574-13] brave.LoggingSpanCollectorImpl (LoggingSpanCollectorImpl.java:24) - Span(trace_id:-5137944522864564053, name:/brave-resteasy-example/b, id:-6201587421931759351, parent_id:-5137944522864564053, annotations:[Annotation(timestamp:1372856262381000, value:sr, host:Endpoint(ipv4:2130706433, port:8080, service_name:RestEasyTest)), Annotation(timestamp:1372856262730000, value:ss, host:Endpoint(ipv4:2130706433, port:8080, service_name:RestEasyTest))], binary_annotations:null)
    14:57:42,744 INFO  [qtp948887574-18] brave.LoggingSpanCollectorImpl (LoggingSpanCollectorImpl.java:24) - Span(trace_id:-5137944522864564053, name:brave-resteasy-example/b, id:-6201587421931759351, parent_id:-5137944522864564053, annotations:[Annotation(timestamp:1372856262366000, value:cs, host:Endpoint(ipv4:2130706433, port:8080, service_name:RestEasyTest)), Annotation(timestamp:1372856262743000, value:cr, host:Endpoint(ipv4:2130706433, port:8080, service_name:RestEasyTest))], binary_annotations:[BinaryAnnotation(key:http.responsecode, value:32 30 30, annotation_type:STRING, host:Endpoint(ipv4:2130706433, port:8080, service_name:RestEasyTest))])
    14:57:42,745 INFO  [qtp948887574-18] brave.LoggingSpanCollectorImpl (LoggingSpanCollectorImpl.java:24) - Span(trace_id:-5137944522864564053, name:/brave-resteasy-example/a, id:-5137944522864564053, annotations:[Annotation(timestamp:1372856261792000, value:sr, host:Endpoint(ipv4:2130706433, port:8080, service_name:RestEasyTest)), Annotation(timestamp:1372856262745000, value:ss, host:Endpoint(ipv4:2130706433, port:8080, service_name:RestEasyTest))], binary_annotations:null)
    14:57:42,746 INFO  [main] brave.LoggingSpanCollectorImpl (LoggingSpanCollectorImpl.java:24) - Span(trace_id:-5137944522864564053, name:brave-resteasy-example/a, id:-5137944522864564053, annotations:[Annotation(timestamp:1372856261537000, value:cs, host:Endpoint(ipv4:2130706433, port:8080, service_name:RestEasyTest)), Annotation(timestamp:1372856262746000, value:cr, host:Endpoint(ipv4:2130706433, port:8080, service_name:RestEasyTest))], binary_annotations:[BinaryAnnotation(key:http.responsecode, value:32 30 30, annotation_type:STRING, host:Endpoint(ipv4:2130706433, port:8080, service_name:RestEasyTest))])

The spans are logged in reverse order:

1.  The last log line logs the client part of the first span the is initiated in the test code. 
    You notice that is has no parent spanid and it logs cs (client send), cr (client received) annotations as well as the http code return code as binary annotation.
2.  The 3rd log line logs the server part of the first span. So it has the same trace id, span id and null parent span id. 
    It logs sr (server received) and ss (server send) annotations.
3.  The 2nd log line logs the client request from URI brave-resteasy-example/a to brave-resteasy-example/b. 
    It has the same trace id but different span id and refers to the our first span as parent span id. 
    As it is a client request it again logs cs, cr and http return code annotations.
4.  The 1st log line logs the server side part of brave-resteasy-example/b. 
    So it has sr and ss annotations and same trace information as previous span log.

So the 2 server side logs are generated by having `BravePreProcessInterceptor` and `BravePostProcessInterceptor` available.
The client side logs are generated by the `BraveClientExecutionInterceptor`.

## How is it all hooked together? ##

### src/main/webapp/WEB-INF/web.xml ###

We use Spring to wire everything together and use the `brave-impl-spring` module. Spring is set up in the web.xml

We use the Spring DispatcherServlet and choose to use annotation based configuration (AnnotationConfigWebApplicationContext) instead of
XML configuration.

We set up our context by scanning only 1 package (com.github.kristofa.brave). Spring will scan this package AND sub packages (!)
for bean definitions, Configuration classes. As a result following classes will be detected and picked up by Spring:

*   com.github.kristofa.brave : Because we have `brave-impl-spring` as dependency the Configuration classes for EndPointSubmitter, ClientTracer, ServerTracer will be picked up.
*   com.github.kristofa.brave.resteasy : This package contains our Brave client interceptor and server pre/post process interceptors. 
    As they are annotated with the Spring @Component annotation they will be picked up by Spring.
*   com.github.kristofa.brave.resteasyexample : This package contains our application resource but also dependency injection configuration 
    classes for:
  
    Resteasy : Loads springmvc-resteasy.xml which integrates resteasy with Spring.
    
    TraceFilters : Sets up TraceFilter that will trace all requests. The TraceFilters dependency is required for the ClientTracer Configuration part of brave-impl-spring.
    
    SpanCollector : Used and shared by both ClientTracer and ServerTracer that are set up by Configuration classes in brave-impl-spring.
    
### Jetty ###

Jetty is embedded and started in the setup method of our test (ITRestEasyExample) and stopped in the tearDown method.

It is configured to use src/main/webapp/WEB-INF/web.xml so that is how Spring / Resteasy are being set up.

## Running it yourself ##

The project depends on Brave 2.1.0-SNAPSHOT which is not available through Maven Central so you
should do: 
        
    
    # Check out brave
    git clone https://github.com/kristofa/brave.git
    cd brave
    mvn install
    
    # Next, check out and build brave-resteasy-example which relies on brave 2.1.0-SNAPSHOT dependencies.    
    git clone https://github.com/kristofa/brave-resteasy-example.git
    # In brave-resteasy-example directory execute:
    mvn verify # This executes unit and integration tests and will execute ITRestEasyExample.

## Adapt test to submit spans to zipkin collector ##

By default we use a SpanCollector implementation that simply logs the received spans through log4j.
We don't want to use the ZipkinSpanCollector by default because we can't assume that everybody who
checks out the code and runs the test has the Zipkin Collector service running at a fixed port.

However with few adaptations you can change this test to make it submit spans to Zipkin Collector.

### Add brave-zipkin-spancollector dependency to pom.xml ###

    <dependency>
        <groupId>com.github.kristofa</groupId>
        <artifactId>brave-zipkin-spancollector</artifactId>
        <version>${brave.version}</version>
    </dependency>

First you have to add the brave-zipkin-spancollector dependency to your pom.xml

### Update SpanCollectorConfiguration ###

Update com.github.kristofa.brave.resteasyexample.SpanCollectorConfiguration class to instantiate ZipkinSpanCollector instead
of the LoggingSpanCollector configured by default.


    @Configuration
    public class SpanCollectorConfiguration {

        @Bean
        @Scope(value = "singleton")
        public SpanCollector spanCollector() {

            return new ZipkinSpanCollector("localhost", 9410);            
        }
    }

Before you run the test you should make sure the Zipkin collector is running at port
9410, and in the example case on localhost. If you execute the test now you should 
see the spans in zipkin-web if services are running and properly configured.
    