# RDC - Real DE4A Connector

This is the implementation of the DE4A Connector based on the TOOP Connector.

The thing is just called "Connector" in the rest of the documentation.

The content of this repository is licensed under the Apache 2.0 license.

## Structure

RDC is structured in the following sub-modules:

* `rdc-api` - contains all generic interfaces for the message exchange etc. This project may be included as a dependency when programming against RDC.
* `rdc-core` - contains the main implementation logic, the configuration, the phase4 AS4 integration, the SMP lookup etc.
* `rdc-web-api` - contains the web integration of the core components (REST APIs), but only as a solution to be integrated (library) and not self-contained
* `rdc-webapp` - is a standalone web application (WAR) to be deployed in a Java JEE application server like Tomcat or Jetty. It may also serve as the basis for Docker images.

## Running RDC

To be described

### Integration

There are different ways how to integrate the Connector into your environment.
The two main ways to do it is, to
1. run the Connector solution "as is" - this could mean you take the binary WAR file or the Docker image and run it in the JEE application service
1. integrate the Connector into your Java application that uses Servlet technology (required for AS4). Depending on your application needs you can either 
    * Integrate `rdc-core` if you are building an application that does not require an external HTTP interface
    * Integrate `rdc-web-api` if you are building a Servlet-based JEE application and you want to use the external HTTP interfaces but don't want to use the pre-build web application

## Developing RDC

### Prerequisites

* Java 1.8 or higher
* Apache Maven 3.6 or higher as build tool
