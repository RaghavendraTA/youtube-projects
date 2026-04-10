# Configuration App Backend - API Specification

## Base URL
```
http://localhost:8080
```

## Authentication

Most endpoints require JWT authentication via the `Authorization` header:
```
Authorization: Bearer <jwt-token>
```

The token is obtained from the login or refresh endpoints.

---

## Admin Endpoints

### 1. Register New Admin

**Endpoint:** `POST /api/v1/admin/register`  
**Authentication:** Not required  
**Status Code:** 200

**Request:**
```json
{
  "username": "admin@example.com",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "admin@example.com",
  "numberOfConfigurationsOwned": 0,
  "registeredOn": "2024-03-21T10:30:00",
  "active": true
}
```

---

### 2. Login

**Endpoint:** `POST /api/v1/admin/login`  
**Authentication:** Not required  
**Status Code:** 200

**Request:**
```json
{
  "username": "admin@example.com",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600000,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "admin@example.com",
    "numberOfConfigurationsOwned": 0,
    "registeredOn": "2024-03-21T10:30:00",
    "active": true
  }
}
```

---

### 3. Refresh Token

**Endpoint:** `POST /api/v1/admin/refresh`  
**Authentication:** Not required  
**Status Code:** 200

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600000,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "admin@example.com",
    "numberOfConfigurationsOwned": 0,
    "registeredOn": "2024-03-21T10:30:00",
    "active": true
  }
}
```

---

### 4. Get Admin By ID

**Endpoint:** `GET /api/v1/admin/{id}`  
**Authentication:** Required (JWT)  
**Status Code:** 200

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "admin@example.com",
  "numberOfConfigurationsOwned": 5,
  "registeredOn": "2024-03-21T10:30:00",
  "active": true
}
```

---

### 5. Update Admin Profile

**Endpoint:** `PATCH /api/v1/admin/{id}`  
**Authentication:** Required (JWT)  
**Status Code:** 200

**Request:**
```json
{
  "username": "newemail@example.com",
  "active": true
}
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "newemail@example.com",
  "numberOfConfigurationsOwned": 5,
  "registeredOn": "2024-03-21T10:30:00",
  "active": true
}
```

---

## Configuration Endpoints

### 1. Create Configuration

**Endpoint:** `POST /api/v1/configuration/create`  
**Authentication:** Required (JWT - OWNER)  
**Status Code:** 200

**Request:**
```json
{
  "name": "Production Settings",
  "description": "Main production configuration",
  "settings": [
    {
      "id": "setting-uuid-1",
      "key": "database.url",
      "value": "postgresql://prod.example.com:5432/db",
      "type": "string"
    },
    {
      "id": "setting-uuid-2",
      "key": "cache.ttl",
      "value": 3600,
      "type": "integer"
    }
  ],
  "additionalProperties": {
    "environment": "production",
    "region": "us-east-1"
  }
}
```

**Response:**
```json
{
  "id": "config-uuid-unique",
  "configurationId": "config-shared-uuid",
  "name": "Production Settings",
  "description": "Main production configuration",
  "version": 1,
  "active": true,
  "owner": "admin-uuid",
  "createdDate": "2024-03-21T10:30:00",
  "lastModifiedDate": "2024-03-21T10:30:00",
  "adminIds": [],
  "settings": [...],
  "additionalProperties": {...}
}
```

---

### 2. List Configurations

**Endpoint:** `GET /api/v1/configurations`  
**Authentication:** Required (JWT)  
**Status Code:** 200

**Response:**
```json
[
  {
    "id": "config-uuid-unique-1",
    "configurationId": "config-shared-uuid-1",
    "name": "Production Settings",
    "version": 2,
    "active": true,
    "owner": "admin-uuid",
    "adminIds": ["editor-uuid"],
    "createdDate": "2024-03-21T10:30:00",
    "lastModifiedDate": "2024-03-21T11:45:00",
    "settings": [...],
    "additionalProperties": {...}
  },
  {
    "id": "config-uuid-unique-2",
    "configurationId": "config-shared-uuid-2",
    "name": "Staging Settings",
    "version": 1,
    "active": true,
    "owner": "admin-uuid",
    "adminIds": [],
    "createdDate": "2024-03-20T09:15:00",
    "lastModifiedDate": "2024-03-20T09:15:00",
    "settings": [...],
    "additionalProperties": {...}
  }
]
```

---

### 3. Get Configuration History

**Endpoint:** `GET /api/v1/configurations/history/{configurationId}`  
**Authentication:** Required (JWT - OWNER or EDITOR)  
**Status Code:** 200

**Response:**
```json
[
  {
    "version": 1,
    "lastModifiedDate": "2024-03-21T10:30:00",
    "active": false
  },
  {
    "version": 2,
    "lastModifiedDate": "2024-03-21T11:45:00",
    "active": false
  },
  {
    "version": 3,
    "lastModifiedDate": "2024-03-21T14:20:00",
    "active": true
  }
]
```

---

### 4. Get Configuration Statistics

**Endpoint:** `GET /api/v1/configuration/stats/{configurationId}`  
**Authentication:** Required (JWT - OWNER or EDITOR)  
**Status Code:** 200

**Response:**
```json
{
  "configurationId": "config-shared-uuid",
  "totalLookups": 1523,
  "lastLookupAt": "2024-03-21T14:55:30"
}
```

---

### 5. Update Configuration

**Endpoint:** `PATCH /api/v1/configuration/{configurationId}`  
**Authentication:** Required (JWT - OWNER or EDITOR)  
**Status Code:** 204 (No Content)

**Request:**
```json
{
  "name": "Updated Production Settings",
  "description": "Updated configuration",
  "settings": [
    {
      "id": "setting-uuid-1",
      "key": "database.url",
      "value": "postgresql://prod-new.example.com:5432/db",
      "type": "string"
    }
  ],
  "additionalProperties": {
    "environment": "production",
    "region": "us-west-2"
  }
}
```

---

### 6. Set Active Version

**Endpoint:** `PUT /api/v1/configuration/{version}/{configurationId}`  
**Authentication:** Required (JWT - OWNER or EDITOR)  
**Status Code:** 202 (Accepted)

**Example:**
```
PUT /api/v1/configuration/2/config-shared-uuid
```

---

### 7. Delete Configuration

**Endpoint:** `DELETE /api/v1/configuration/{configurationId}`  
**Authentication:** Required (JWT - OWNER only)  
**Status Code:** 204 (No Content)

---

### 8. Transfer Ownership

**Endpoint:** `PUT /api/v1/configuration/transfer/{configurationId}`  
**Authentication:** Required (JWT - OWNER only)  
**Status Code:** 202 (Accepted)

**Request:**
```json
{
  "transfer-to": "new-owner-admin-uuid"
}
```

---

## Lookup Endpoints (Consumer)

### 1. Lookup Configurations by Setting GUID

**Endpoint:** `POST /api/v1/lookup/{configurationId}`  
**Authentication:** Required (JWT)  
**Status Code:** 200

**Request:**
```json
{
  "settings": [
    "setting-uuid-1",
    "setting-uuid-2"
  ]
}
```

**Response:**
```json
{
  "settings": [
    {
      "id": "setting-uuid-1",
      "key": "database.url",
      "value": "postgresql://prod.example.com:5432/db",
      "type": "string"
    },
    {
      "id": "setting-uuid-2",
      "key": "cache.ttl",
      "value": 3600,
      "type": "integer"
    }
  ]
}
```

---

### 2. Lookup Configurations by Key

**Endpoint:** `POST /api/v1/lookup/{configurationId}`  
**Authentication:** Required (JWT)  
**Status Code:** 200

**Request:**
```json
{
  "keys": [
    "database.url",
    "cache.ttl"
  ]
}
```

**Response:**
```json
{
  "settings": [
    {
      "id": "setting-uuid-1",
      "key": "database.url",
      "value": "postgresql://prod.example.com:5432/db",
      "type": "string"
    },
    {
      "id": "setting-uuid-2",
      "key": "cache.ttl",
      "value": 3600,
      "type": "integer"
    }
  ]
}
```

---

## Error Responses

All error responses follow this format:

```json
{
  "timestamp": "2024-03-21T15:30:45",
  "status": 400,
  "error": "Invalid request"
}
```

### Common Error Codes

| Status | Scenario |
|--------|----------|
| 400 | Invalid request, validation error, or bad credentials |
| 401 | Unauthorized - missing or invalid token |
| 403 | Forbidden - insufficient permissions |
| 404 | Resource not found |
| 500 | Internal server error |

---

## Access Control

### Admin Routes
- Registration, Login, Refresh: Public (no auth required)
- Get/Update Admin: User can only modify own profile
- Other routes: Require OWNER or EDITOR role

### Configuration Routes
- Create: OWNER (automatic assignment)
- Read: OWNER or EDITOR
- Update: OWNER or EDITOR
- Delete: OWNER only
- Transfer: OWNER only

### Lookup Routes
- Requires JWT authentication (any valid admin or consumer token)

---

## Examples with cURL

### Register Admin
```bash
curl -X POST http://localhost:8080/api/v1/admin/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin@example.com",
    "password": "password123"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/v1/admin/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin@example.com",
    "password": "password123"
  }'
```

### Create Configuration
```bash
curl -X POST http://localhost:8080/api/v1/configuration/create \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Config",
    "description": "Test configuration",
    "settings": []
  }'
```

### List Configurations
```bash
curl -X GET http://localhost:8080/api/v1/configurations \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Lookup Settings
```bash
curl -X POST http://localhost:8080/api/v1/lookup/config-id \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "keys": ["app.name", "app.version"]
  }'
```

---

## Notes

1. All timestamps are in ISO 8601 format (UTC)
2. UUIDs are used for all entity identifiers
3. Async operations may not complete immediately; use appropriate polling or callbacks
4. Virtual threads enable efficient handling of concurrent requests
5. All passwords are hashed with bcrypt before storage
