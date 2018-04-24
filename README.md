Offer service project
=====================

Requirement
-----------
- sbt (build tools)
- jdk 1.8 (There is a regression with jdk 9 and 10: https://scala-lang.org/news/2.12.5)

Libraries or frameworks
-----------------------

- H2: The database
- Akka HTTP: A HTTP library 
- Slick : A persistence framework
- Macwire: A lightweight Scala dependency injection library 
- Swagger: As way to document the http service
- Scalatest: The testing framework

Design
------

The application has three layers in three different packages:

### com.github.leifh.offer.rest
It implements the interactions between the application and a client through
an http service (using REST). 

### com.github.leifh.offer.service
It implements some business logics and add an abstraction above the persistence.

### com.github.leifh.offer.persistence
It implements the low level code for interacting with the database.

The three layers are wired with the macwire library using dependency injection.



Testing
-------

The class com.github.leifh.offer.OfferServiceSpec is testing the service through
the http layer from a customer point of view.  
 
An improvement will be to use the Akka HTTP testkit for the tests.
  

Deployment
----------
The SBT native packager plugin is included in the project.
It can create a zip file with the application, a docker imager, etc...

Service URLs
------------

### The base URL for the Offer service
http://localhost:8080/offers

### The endpoint for the swagger schema
http://localhost:8080/api-docs/swagger.json

### The swagger ui application
http://localhost:8080/swagger


