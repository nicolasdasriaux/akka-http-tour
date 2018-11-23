# Customer API

## Customer

`Customer` class

| Attribute   | Type     |
|-------------|----------|
| `id`        | `Int`    |
| `firstName` | `String` |
| `lastName`  | `String` |

## Customer Post

`CustomerPost` class

| Attribute   | Type     |
|-------------|----------|
| `firstName` | `String` |
| `lastName`  | `String` |

## Customer Service

`CustomerService` class

* `findAll: Seq[Customer]`
* `find(id: Int): Customer`
* `insert(post: CustomerPost): Customer`
* `update(customer: Customer): Customer

## Customer Update

`CustomerUpdate` class
``
| Attribute   | Type     |
|-------------|----------|
| `firstName` | `String` |
| `lastName`  | `String` |

## API

### Get Customers

**Request**

`GET /customers`

**Response**

```json
[
    { "id": 1, "firstName": "Linda", "lastName": "Stewart" },
    { "id": 2, "firstName": "Mary", "lastName": "Watson" },
    { "id": 3, "firstName": "Andrew", "lastName": "Carter" }
]
```

### Get Customer

**Request**

`GET /customers/1`

**Response**

```json
{ "id": 1, "firstName": "Linda", "lastName": "Stewart" },
```

### Post Customer

**Request**

`POST /customers`

```json
{ "firstName": "Paul", "lastName": "Simpson" }
```

**Response**

```json 
{ "id": 4, "firstName": "Paul", "lastName": "Simpson" }
```

### Update Customer

**Request**

POST /customers/4

```
{ "firstName": "Paul", "lastName": "Simon" }
```

**Response**

```
Updated 1 Customer
```

## Securing API

Protect API with HTTP Basic Authentication
