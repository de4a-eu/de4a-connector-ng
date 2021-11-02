# DCNG - DE4A Connector NG

This is the implementation of the DE4A Connector based on the TOOP Connector.
It includes [phase4](https://github.com/phax/phase4) as the AS4 gateway for sending and receiving messages but can also be extended to use other AS4 Gateways.

The thing is just called "Connector" in the rest of the documentation.

The content of this repository is dually licensed under the Apache 2.0 license and the EUPL 1.2.

## Goals and non-goals

The goals of the Connector are as follows:
* Encapsulate the calls of common interfaces
* Be able to send and receive any kind of payload, for any kind of transmission pattern (IM, USI, ...)
* Provide a solution that can be used in DE4A and later in the context of SDG as well
* Be technically integrable in as many ways as possible
* Be technically compatible with the WP5 DE4A Connector (where necessary)
* Provide support for alternative AS4 gateways

The following are non-goals of the Connector:
* Provide a one-size fits all solution for all DE4A pilots - the specific workflow orchestrations are out of scope of this project

## Structure

The Connector is structured in the following sub-modules:

* `dcng-api` - contains all generic interfaces for the message exchange etc. This project may be included as a dependency when programming against the Connector.
* `dcng-core` - contains the main implementation logic, the configuration, the SMP lookup etc. This module can be integrated into other applications to have the full functionality in a Java API. It does not contain the AS4 gateway.
* `dcng-phase4` - contains the phase4 AS4 Gateway
* `dcng-web-api` - contains the web integration of the core components (REST APIs), but only as a solution to be integrated (library) and not self-contained. This may be used to provide alternative implementations in another web application. Compared to `dcng-core` it offers an HTTP API.
* `dcng-webapp-phase4-it1-im` - is a standalone **demo** web application (WAR) to be deployed in a Java JEE application server like Tomcat or Jetty, based on the `dcng-web-api` and `dcng-phase4` projects. It may also serve as the demo for Docker images. This is the specific solution for Iteration 1 for the intermediation pattern (IM) using the phase4 AS4 Gateway.

## Running the Connector

To be described

### Integration

There are different ways how to integrate the Connector into your environment.
The main way to do it is, to integrate the Connector into your Java application that uses Servlet technology (required for AS4). Depending on your application needs you can either 
* Integrate `dcng-core` if you are building an application that does not require an external HTTP interface
* Integrate `dcng-web-api` if you are building a Servlet-based JEE application and you want to use the external HTTP interfaces but don't want to use the pre-build web application
No matter what you choose, you need to pick an AS4 Gateway implementation. Without the AS4 Gateway, the Connector will not work.

Alternatively you may choose one of the prebuilt web applications (WARs) as your implementation. But this limits your choice of the supported patterns as well as of the AS4 Gateway.

## Developing the Connector

### Prerequisites

* Java 1.8 or higher
* Apache Maven 3.6 or higher as build tool

### Dependencies

This lists the major dependencies for the Connector specific dependent libraries.

* [phax/peppol-commons](https://github.com/phax/peppol-commons) is used for the consistent identifier handling as well as for the SMP client, required for the dynamic discovery
* [phax/ph-regrep](https://github.com/phax/ph-regrep) is used to create the RegRep representation
* [phax/phase4](https://github.com/phax/phase4) is used as the built-in AS4 gateway for sending and receiving messages, based on the [CEF AS4](https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/eDelivery+AS4+-+1.15) profile
* [de4a-wp5/de4a-commons](https://github.com/de4a-wp5/de4a-commons) is used for the data model and the Kafka integration
