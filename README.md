# How to run


```
./gradlew bootRun
```
> Application requires mongo (>=v3.6) running on port **27017**

> Requires JDK 8 to compile and run

Alternatively application can be run on docker together with mongodb:

- Build image to local Docker daemon
```
./gradlew jibDockerBuild
```
- Start containers 
```
docker-compose up
```
You can load fixtures (films and customers) into running mongodb by using command:
```
./load.sh
```
in _fixtures_ directory.

# How to use

> I suggest importing [postman collection](Video Rental Store.postman_collection.json) to test API.

## Films

####Create new film
```javascript
POST /api/films
{
	"title": "Pulp Fiction",
	"type": "OLD|REGULAR|NEW_RELEASE"
}
```

####Find all films
```javascript
GET /api/films
```

####Find film by id
```javascript
GET /api/films/{id}
```

## Customers
####Create new customer
```javascript
POST /api/customers
{
	"name": "John"
}
```

####Find all customers
```javascript
GET /api/customers
```

####Find customer by id
*Request:*
```javascript
GET /api/customer/{id}
```
*Response:*
```javascript
{
    "id": "7b49e3cc-baa6-1094-03a3-23e30a11af90",
    "name": "John",
    "bonusPoints": 6
}
```

## Rentals

####Rent film(s)
*Request:*
```javascript
POST /api/rentals
{
    "customerId": "7b49e3cc-baa6-1094-03a3-23e30a11af90",
    "films": [
        {
            "film": "a4457073-cadf-ccd0-ab79-3917c485d894",
            "days": 3
        },
        {
            "film": "d1483e1c-0f8c-9fad-ef8c-e7994ce8009b",
            "days": 2
        }
    ]
}
```

*Response:*
```javascript
{
    "id": "4061299e-f4d2-4264-95a7-ac337abfc1c6",
    "customerId": "7b49e3cc-baa6-1094-03a3-23e30a11af90",
    "rentedAt": "2018-12-28T14:15:37.474213",
    "regularPrice": 150,
    "surcharge": 0,
    "items": [
        {
            "id": "654cff8a-9fc7-7fa7-a6d0-da0a7bf110aa",
            "filmId": "a4457073-cadf-ccd0-ab79-3917c485d894",
            "days": 3,
            "regularPrice": 120,
            "surcharge": 0,
            "returnedAt": null
        },
        {
            "id": "4b4d8d58-e8f2-e079-6269-1709244a13a1",
            "filmId": "d1483e1c-0f8c-9fad-ef8c-e7994ce8009b",
            "days": 2,
            "regularPrice": 30,
            "surcharge": 0,
            "returnedAt": null
        }
    ]
}
```

####Return film(s)
*Request:*
```javascript
POST /api/rentals/4061299e-f4d2-4264-95a7-ac337abfc1c6
{
    "items": ["4b4d8d58-e8f2-e079-6269-1709244a13a1"]
}
```

*Response:*
```javascript
{
    "id": "4061299e-f4d2-4264-95a7-ac337abfc1c6",
    "customerId": "7b49e3cc-baa6-1094-03a3-23e30a11af90",
    "rentedAt": "2018-12-28T14:15:37.474213",
    "regularPrice": 150,
    "surcharge": 0,
    "items": [
        {
            "id": "654cff8a-9fc7-7fa7-a6d0-da0a7bf110aa",
            "filmId": "a4457073-cadf-ccd0-ab79-3917c485d894",
            "days": 3,
            "regularPrice": 120,
            "surcharge": 0,
            "returnedAt": null
        },
        {
            "id": "4b4d8d58-e8f2-e079-6269-1709244a13a1",
            "filmId": "d1483e1c-0f8c-9fad-ef8c-e7994ce8009b",
            "days": 2,
            "regularPrice": 30,
            "surcharge": 0,
            "returnedAt": "2018-12-29T16:15:37.474213"
        }
    ]
}
```
