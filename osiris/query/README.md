## Query microservice

This microservice provides a GraphQL interface for 

#### Running

To run the service locally with an interactive query executor, run:
```bash
bazel run //osiris/query -- --port 8080 --debug
```
Then have a look at [locahost:8080](http://localhost:8080).

#### Useful Query
```graphql
{
  queryGetUser(in: {uid: 100 }) {
    name
		id
    transports {
      operator
      planned {
        location {
          position {
            lat
            lon
          }
          country
          name
        }
        arrival
        departure
      }
      etas {
        location {
          position {
            lat
            lon
          }
          country
          name
        }
        arrival
        reason
        departure
      }
    }
  }
}
```

This query then yields the follwing (mocked) data:
```json
{
  "data": {
    "queryGetUser": {
      "name": "Uwe",
      "id": {
        "Value": 100
      },
      "transports": [
        {
          "operator": "DB",
          "planned": [
            {
              "location": {
                "position": {
                  "lat": 12.453,
                  "lon": 134.42
                },
                "country": "DE",
                "name": "UBahn"
              },
              "arrival": "Today",
              "departure": ""
            },
            {
              "location": {
                "position": {
                  "lat": 12.453,
                  "lon": 134.42
                },
                "country": "DE",
                "name": "UBahn"
              },
              "arrival": "Tomorrow",
              "departure": ""
            }
          ],
          "etas": [
            {
              "location": {
                "position": {
                  "lat": 12.453,
                  "lon": 134.42
                },
                "country": "DE",
                "name": "UBahn"
              },
              "arrival": "Today",
              "reason": "Heavy Storm",
              "departure": ""
            },
            {
              "location": {
                "position": {
                  "lat": 12.453,
                  "lon": 134.42
                },
                "country": "DE",
                "name": "UBahn"
              },
              "arrival": "Tomorrow",
              "reason": "Heavy Storm",
              "departure": ""
            }
          ]
        },
        {
          "operator": "Hapag",
          "planned": [
            {
              "location": {
                "position": {
                  "lat": 12.453,
                  "lon": 134.42
                },
                "country": "DE",
                "name": "UBahn"
              },
              "arrival": "Today",
              "departure": ""
            },
            {
              "location": {
                "position": {
                  "lat": 12.453,
                  "lon": 134.42
                },
                "country": "DE",
                "name": "UBahn"
              },
              "arrival": "Tomorrow",
              "departure": ""
            }
          ],
          "etas": [
            {
              "location": {
                "position": {
                  "lat": 12.453,
                  "lon": 134.42
                },
                "country": "DE",
                "name": "UBahn"
              },
              "arrival": "Today",
              "reason": "Heavy Storm",
              "departure": ""
            },
            {
              "location": {
                "position": {
                  "lat": 12.453,
                  "lon": 134.42
                },
                "country": "DE",
                "name": "UBahn"
              },
              "arrival": "Tomorrow",
              "reason": "Heavy Storm",
              "departure": ""
            }
          ]
        }
      ]
    }
  }
}
```