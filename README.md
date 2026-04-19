docker run -d --name activemq -p 61616:61616 -p 8161:8161 apache/activemq-classic

Maven      = build tool, manages dependencies via pom.xml
Spring Boot = auto-configured Java web framework
IoC        = Spring manages object creation (beans)
DI         = Spring injects dependencies automatically
@Service   = business logic bean
@RestController = HTTP request handler
ActiveMQ   = message broker (post office between services)
Queue      = messages wait here until consumed
Docker     = runs software in isolated containers
Port 8080  = Spring Boot app
Port 61616 = ActiveMQ messaging
Port 8161  = ActiveMQ web console

docker ps 

 http://localhost:8161 in your browser

model Data classes (POJOs) — what your data looks like
config Spring configuration beans — AMQ queue definitions
service Business logic — API calls, calculations, scheduling
producer AMQ message senders 
controller REST API endpoints — what the frontend calls 
static Frontend files served directly by Spring Boot

Java Object → Serialized (bytes) → AMQ Queue → Deserialized → Java Object
  Producer                                                      Consumer

When you send a Java object over ActiveMQ it needs to be converted to bytes first. Serializable tells Java "this object can be converted to bytes and back":


CodeMeaning
200 OK — success
400 Bad request — wrong parameters
403 Forbidden — wrong API key
429 Too many requests — rate limited 
503 Service unavailable
