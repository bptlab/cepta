# Integration Tests with Java
This document aims to give an overview how to do integration tests in Java with JUnit4. For all integration tests we want to use [testcontainers](https://www.testcontainers.org/) so they can run isolated from each other with their own small docker container.

## Testcontainers
see https://www.testcontainers.org/quickstart/junit_4_quickstart/ for a quickstart.
For some usecases and/or container types there already are some containers and/or tutorials provided.

## with PostgreSQL database
### set up container
You can create a Postgresql container in each test with:
```java
public void testMethod() throws IOException {
  try(PostgreSQLContainer postgres = newPostgreSQLContainer()) {
    postgres.start();
    [...]
  }
}
```
To establish a connection with [JDBC](https://www.tutorialspoint.com/jdbc/jdbc-select-database.htm) the following snipped is needed:
```java
public void initDatabaseStuff(PostgreSQLContainer container) {
  // JDBC driver name and database URL
  String db_url = container.getJdbcUrl();
  String user = container.getUsername();
  String password = container.getPassword();

  Connection conn = null;
  Statement stmt = null; // later used for sending queries
  try{
    // Register JDBC driver
    Class.forName("org.postgresql.Driver");

    // Open a connection
    System.out.println("Connecting to a database...");
    conn = DriverManager.getConnection(db_url, user, password);
    System.out.println("Connected database successfully...");

    }catch(Exception e){
      e.printStackTrace();
    }finally{
      //finally block used to close resources
      try{
        if(stmt!=null)
          conn.close();
      }catch(SQLException se){
      }// do nothing
      try{
          if(conn!=null)
            conn.close();
      }catch(SQLException se){
          se.printStackTrace();
      }//end finally try
    }//end try
    System.out.println("Goodbye!");
  }
```
Here we fetch the container's connection information and pass it to a JDBC connection.

To execute queries `stmt.executeQuery("select * from tablename")` suffices. 

If you need to access query results, use something like `ResultSet result = stmt.executeQuery("select * from tablename")`. You can iterate over a [ResultSet](https://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html) similar to an iterator with `result.next()`:

