# sample.consulservicediscovery

## Using Consul for Service Discovery in Liberty

Service discovery is an important element of any microservices architecture. A number of technologies exist in this space, including Eureka, Apache Zookeeper + Curator, Kubernetes, etcd, and Consul. [Consul by Hashicorp](https://www.consul.io) is growing in popularity and has a number of attractive features, including broad platform support and a DNS interface. 

In general, registration of a service with a service discovery engine is a manual process. This sample shows how WebSphere Liberty Profile's extensibility can be used to automatically register all REST endpoints as Consul services. It provides a user feature which hooks into Liberty's annotation scanning function and publishes jax-rs endpoints with Consul.  

### Why not do this with reflection? 

It's possible to achieve something similar without the Liberty profile, using reflection. See, for example, [Dale Lane's blog post](http://dalelane.co.uk/blog/?p=1871) describing how to use reflection to produce a web page documenting endpoints. The limitation of this approach is that reflection will only see classes which have already been loaded. In a simple application, this would probably include the whole class space, but in an optimised application server, applications may only be loaded when they're first called. This makes reflectively scanning the class space unsuitable for service discovery.  

### Layout 

There are two user features, one which provides the basic consul api, and one which hooks annotation scanning to automatically register endpoints. 

The consul project wraps the consul jars and dependencies into an OSGi bundle, and the discovery.annotation project is the source for the annotation scanning. 

### Example server configuration 

To use and configure the feature, add the following to the server.xml of your Liberty server: 

	<!-- Enable features -->
	<featureManager>
		<feature>usr:consulDiscovery</feature>
	</featureManager>

	<consul.annotation.scanner server="my.consul.hostname" />

### Configuring the location of the Consul server

If you’re running in an environment like Bluemix, configuring services via environment variables is preferable to setting them up in the server.xml. If the CONSUL_SERVER environment variable is set, it will be preferred instead of the configured server. CONSUL_SERVER should be set to a host name or IP address. 

### Known limitations

#### Build 

At the moment, there is no command-line build. Consul libraries need to be copied to the lib folder manually.
At the moment, you’ll need to copy com.ibm.ws.compat jar from your liberty installation into the lib folder. 

#### Host and port detection 

If a server is listening on multiple ports (for example, http and https), the registered ports may not be accurate. Similarly, if a host computer has multiple network interfaces, and the server is listening on all of them, the registered endpoint may not be the preferred one. One workaround is to extend the consul configuration to allow a preferred port and host to be specified. 

#### XML endpoint registration 

This feature only scans annotations, rather than parsing web.xml and other files. Therefore, only REST endpoints exposed by annotation will be available for discovery. 


## Outstanding work 

As well as the known limitations, there are some pieces of work which we still want to do. 

* Gradle build
* Exposure of consul host configuration to client code




