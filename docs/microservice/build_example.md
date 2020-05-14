We want to use microservices that are **small**, **light** and **robust**.

On our quest to achieve this, we compiled this reference documentation for building CEPTA microservices:

### What language do I use?
Go.

### I love java, why should I use Go?
Go is becoming a very popular language for microservices due to it's simple nature, speed and lightweight deployment. Last but not least, properly learning Go might even get you a (cool) job ;)

### How to run the microservices?
We use [bazel]() as our build tool. Build with `bazel build <something>` and run with `bazel run <something>`, where `<something>` references a location and a target:
```bash
# // is the repositories root
# location is aux/producers/traindataproducer
# target is traindataproducer
bazel run //aux/producers/traindataproducer:traindataproducer
``` 
A location always holds a `BUILD` file (more on that in the next question), which can define multiple targets.
If the target and location is equal you can omit specifying the target, which simplifies the above to just:
```bash
bazel run //aux/producers/traindataproducer
``` 

### How do I build my new microservice?
To help you build your new microservice, we must understand how bazel works. There is one `WORKSPACE` file in our repositories root, which holds some archives, rules but most importantly the definitions of our dependencies for all of our languages. Then there is a `BUILD` file for each microservice's directory that configures the target and references the dependencies from the `WORKSPACE` file.

If you want to build your new Go service, just have a look at the other existing services and their `BUILD` files. Here is a rough tutorial:
1. Create your services directory with some go files
2. Copy a `BUILD` file from another  

### Can I start building my JSON REST API yet?
We decided against using RESTful JSON for microservices in 2020! Please properly define a schema of your service using `protofuf` and use generated `grpc` stubs for a quick start. Our infrastructure supports `grpc` all the way from the frontend.

Here is a rough outline:
- Have a look at any of `models/grpc/*.proto` and use it as a template for your own service definition.
- Add an entry to `models/grpc/BUILD` that compiles your service `.proto` schema
- Add the target to your go services `BUILD` file as a dependency.
- Have a look at `aux/producers/traindataproducer/BUILD` and `aux/producers/traindataproducer/server.go` as a reference

### Command line options?
Please make your service configurable using environment variables and cli options. And please use [github.com/urfave/cli/v2](https://github.com/urfave/cli/blob/master/docs/v2/manual.md#getting-started). Please make sure your service does allow configuration of at least the following parameters via cli and environment variables:
- log level (always default to `Info`)
- service port (always default to 80)
- if applicable: database connection parameters (host, port, user, password, dbname...)

**Tip:** When you run your service with the default cli options, you will likely receive a permission error when your service tries to bind to port 80. In this case, just make use of your new cli parameter like in this example:
```bash
bazel run //aux/producers/traindataproducer -- --port 8080
```

### Logging?
We do love useful logging! Please use [github.com/sirupsen/logrus](). Using it is just as using the standard `log` library when you import aliased as log:
```go
import log "github.com/sirupsen/logrus"
```

### Can we do better than logging?
Yes we can! You can use [github.com/uber/jaeger-client-go]() to allow debugging and production monitoring using the distributed `opentracing` system. We might soon add a `jaeger` tracing collector to our infrastructure that will collect your logs.

### Testing?
Oh hell yeah! At least we love a robust and relatively safe codebase. That's why we write tests.
Testing is rather challenging in a microservice environment because you have to mock all the interfaces to have a standalone services that is fully tested. Don't worry though, Unit-Test should be as easy as you are used to it at least. 

[Intro to testing a microservice](https://www.testingexcellence.com/testing-microservices-beginners-guide/)

#### **Unit Testing:**
**Isn' very intuitively and in most cases not necessary**

Look [here](https://github.com/bptlab/cepta/wiki/Unit-Testing--Microservices) and analyse the necessity of unit tests in your microservice.

Test source files are identified by naming conventions, example.go file would have the corresponding example_test.go file in the same directory.

**Integrated Go Library for testing (automated testing of Go packages)**
```go
import "testing"
``` 

It is intended to be used in connection with the “go test” command, which automates execution of any function of the following form: 

```go
func TestXxx(*testing.T)
```

We also want to use another popular library for testing in Go because it gives us the ability of assertions and generates better outputs and we can create mocks for further testing.
```go
import "github.com/stretchr/testify/assert"
import "github.com/stretchr/testify/mock"
```

A handy tool for generating mocks for golang interfaces is:
```go
import "github.com/vektra/mockery"
```
See the [example](https://github.com/vektra/mockery) on the GitHub page for further usage.

**Microservice/s need to be testet through:**

_Component Tests:_ Test the microservice itself in isolation, mocking of other used microservices is required.
Use following link as a good [reference](https://tutorialedge.net/golang/improving-your-tests-with-testify-go/) and see the associated branch for the tested notification branch.

_Integration Tests:_ for inter-service communication tests.

_End-To-End Tests:_ google if you are interested.

Which type of test is suitable? Unit Test? Marc started a great discussion on whether microservices should even be unit tested [here](https://github.com/bptlab/cepta/wiki/Unit-Testing--Microservices).
