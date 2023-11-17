# pricing-api

## Assignment

**Requirements**

You’re implementing a part of a shopping platform. Design and implement a service that will provide a REST API 
for **calculating a given product's price based on the number of products ordered**. Products in the system are 
identified by UUID. There should be the possibility of applying discounts based on two 
policies – count based (the more pieces of the product are ordered, the bigger the discount is) and 
percentage based. Policies should be configurable.

**Non-functional requirements**

- Use Java >= 8 and frameworks of your choice
- The project should be containerized and easy to build and run via Gradle or Maven.
- Please provide a README file with instructions on how to launch it
- There's no need for full test coverage. Implement only the most essential (in your opinion) tests
- Use the git repository for developing the project and after you’re done, send us a link to it
- Make sure we can run the project easily, without any unnecessary local dependencies (e.g. Don’t use OS-specific code)

## Assumptions
The focus is on the markdown apply logic and markdown management. I choose to add the unit price for a product in the 
input request for calculating the final price. It's a common way of manage this use cases during the _checkout funnel_.

Persistence is _in-memory_. See `MarkdownGatewayImpl` class for more details.

## Behaviours
The service exposes three groups of API

- Calculate the full price
  - `GET /v1/pricing/finalprice`
- Simple CRUD for managing markdowns
  - `GET /v1/pricing/markdowns`
  - `POST /v1/pricing/markdowns`
  - `GET /v1/pricing/markdowns/{id}`
  - `PATCH /v1/pricing/markdowns/{id}`
  - `DELETE /v1/pricing/markdowns/{id}`
- Markdown to product association
  - `POST /v1/pricing/markdowns/{id}/associations`
  - `DELETE /v1/pricing/markdowns/{id}/associations`

If a product is not associated to a markdown a _default_ policy will be applied. The default policy will 
calculate the full price without applying any discount.

### OpenAPI specs
Once the service is up and running  
Swagger UI can be found at [http://localhost:8080/swagger-ui](http://localhost:8080/swagger-ui)  
OpenAPI specs can be found at [http://localhost:8080/swagger](http://localhost:8080/swagger)

## Tech specs
The assignment is based on the following technologies:
- Quarkus.io
- RESTEasy
- Hibernate Validators
- OpenApi and Swagger UI
- RestAssured (test)
- Mockito (test)

### Further developments
For sake of time the following major topics are not covered even if neessary in a production environment:
- logging
- metrics
- api authentication
- database persistence
- tracing

## Build, test and run
**Requirements**
- Java 17
- Apache Maven 3.9.0 

**Test**
```shell script
cd /path-to-src/
mvn clean test
```

**Package and run**
```shell script
cd /path-to-src/
mvn clean package -Dquarkus.package.type=uber-jar
java -jar target/pricing-api-1.0.0-SNAPSHOT-runner.jar
```
**Package and run in docker**
```shell script
cd /path-to-src/
mvn clean package
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/pricing-api-jvm .
docker run -i --rm -p 8080:8080 quarkus/pricing-api-jvm
```