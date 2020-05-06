## This Page documents our findings about unit testing our microservices

Unit testing seems to be easy for microservices at first glance.
The main problem in unit testing microservices is, that the tests should run independently. So whenever we use any protocol or connection to an outside entity we need to mock this.

In some microservices, most (or nearly all) of the functionality is to provide a connection between components. The authentication microservice e.g. takes POST and GET requests, makes a database query and an appropriate response.
So the question is if a unit test with mocking the request and the database just to test the correct forwarding of the request is necessary or even useful.

The same result could be reached with an integration test and would eliminate the need for many mockups overhead.
In the research we found, that unit testing microservice is a controversial topic.

> Unit testing your service’s implementation details isn’t very important; you can achieve more effective coverage by focusing on component testing the http api.

(https://blog.gopheracademy.com/advent-2014/testing-microservices-in-go/)

> A microservice may be smaller by definition, but with unit testing, you can go even more granular. A unit test focuses on the smallest part of a testable software to ascertain whether that component works as it should. Renowned software engineer, author, and international speaker Martin Fowler breaks unit testing down into two categories:

> Sociable unit testing: This unit testing method tests the behavior of modules by observing changes in their state.

> Solitary unit testing: This method focuses on the interactions and collaborations between an object and its dependencies, which are replaced by test doubles.

(https://www.freecodecamp.org/news/these-are-the-most-effective-microservice-testing-strategies-according-to-the-experts-6fb584f2edde/)

## Example
You can see example tests for the user management microservice, currently located in osiris.

There, a mock connection is established, so the service server feels like in an actual environment.

```go
func SetUpServerConnection() {
	lis = bufconn.Listen(bufSize)
	s := grpc.NewServer()
	pb.RegisterUserManagementServer(s, &server{db: ldb})
	go func() {
		if err := s.Serve(lis); err != nil {
			fmt.Printf("Server exited with error: %v", err)
		}
	}()
}
```

For sending requests/grpc calls we need to create a client:
```go
ctx := context.Background()
conn, err := grpc.DialContext(ctx, "bufnet", grpc.WithDialer(bufDialer), grpc.WithInsecure())
if err != nil {
	t.Fatalf("Failed to dial bufnet: %v", err)
}
defer conn.Close()
client := pb.NewUserManagementClient(conn)
```
Now we can assemble a request and send it to the service (e.g. a request to get all user data by sending the user's id):
```go
request := &pb.UserId{ Value: 1,}
response, err := client.GetUser(context.Background(), request)
```

# Unit tests in GO

Let's take a look at two example tests from the authentication microservice tests.

All tests have the filename as a base and then an _test.go as an appendix.

First we look at the file accounts_test.go. 

```go
func SetupTests() {
	mocket.Catcher.Register()
	db, err := gorm.Open(mocket.DriverName, "connection_string") // Can be any connection string
	if err != nil {
		print(err)
	}
	DB = db
}
```

Here we have our SetUp function for our tests in this file. Because the accounts model manages database actions, we need a mocked database. For this we use 'mocket'. In the first we register the mocket catcher, which listens for sql querys from our funcions. After this we initialize a grom db instance. 

If we want to log which queries come to our db, we can enable logging of that by adding `DB.LogMode(true)`. The Catcher has logging, too. We can enable it by adding `mocket.Catcher.Logging = true`.
We can enable logging exclusively for single tests by adding those lines not to the setup, but to the tests we want to log.

With this setup our tests are good to go! 

An example test look like this: 

```go
func TestCorrectLogin(t *testing.T) {
	SetupTests()
	hashedPassword, _ := bcrypt.GenerateFromPassword([]byte("password"), bcrypt.DefaultCost)
	testPassword := string(hashedPassword)
	commonReply := []map[string]interface{}{{"email": "email", "password": testPassword}}
	mocket.Catcher.Reset().NewMock().WithQuery(`SELECT * FROM "accounts"  WHERE "accounts"."deleted_at" IS NULL AND ((email = email)) ORDER BY "accounts"."id" ASC LIMIT 1`).WithReply(commonReply)
	result := Login("email", "password")
	if (result["message"]) != "Logged In" {
		t.Errorf("Not Logged In correctly! Got '%v'", result["message"])
	}
}
```

As you can see from the test name, all tests start with a "Test" and then the what is about to be tested. As a parameter we have "t *testing", which every test needs. Then we call our setup and create a testPassword for our function which is going to be tested. We then create a commonReply, which is the answer our mocked database will provide if our catcher catches a matching sql query. This can be seen in the next line. If you want the mock db to return an array, there should be no spaces between the array's entries.

After this we declare our result and call our function which we wanna test: Login
Internally the Login function calls the database with the exact sql statement we declared in our mocket catcher. 
We then check if our result has the expected values. If not we call an t.Errorf, which declares the test as failed and print our error message. 

Secondly, let's take a look at controllers_test.go

```go
func TestCreateAccount(t *testing.T) {
	var jsonStr = []byte(`{"email": "email@email","password":"password"}`)

	req, err := http.NewRequest("POST", "/api/user/new", bytes.NewBuffer(jsonStr))
	if err != nil {
		t.Fatal(err)
	}
	req.Header.Set("Content-Type", "application/json")
	rr := httptest.NewRecorder()
	handler := http.HandlerFunc(CreateAccount)
	handler.ServeHTTP(rr, req)
	if status := rr.Code; status != http.StatusOK {
		t.Errorf("handler returned wrong status code: got %v want %v",
			status, http.StatusOK)
	}
}
```

Here we create an jsonStr and an mocked http request for our function. After this we directly call the function with an http handler and once again check, if our result match the expected value. With t.Errorf we call if the test has failed and provide an message what went wrong. 







