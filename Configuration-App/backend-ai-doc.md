# Configuration App — Backend Requirements Design Doc

## Overview

The Configuration App provides a centralized configuration management system for downstream services. Administrative users can create, modify, and manage configurations via a React UI. A separate lookup endpoint allows consumer services (non-human) to query configurations at runtime using setting GUIDs or keys.

---

## Users

| Type | Description |
|---|---|
| **Admin (OWNER)** | Creates configurations; has full CRUD access including delete and ownership transfer |
| **Admin (EDITOR)** | Can view and edit configurations; cannot delete or transfer ownership |
| **Consumer** | Non-human service; authenticated via JWT; read-only lookup access |

---

## Admin Roles

| Role | Assigned When | Permissions |
|---|---|---|
| **OWNER** | Automatically on configuration creation | Full CRUD, delete, transfer ownership |
| **EDITOR** | Assigned by OWNER | View + edit only; no delete, no ownership changes |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java |
| Framework | Spring Boot |
| API Style | REST — Non IO-bound using `@Async` and virtual thread ([reference](https://spring.io/guides/gs/async-method)) |
| Database | MongoDB (local endpoint — connection string to be added in code) |
| Auth | JWT (access token + refresh token) |
| Hosting | Docker Compose (local setup, Spring Boot app only) |

---

## API Endpoints

### Auth / Admin

| Method | Path | Description | Status |
|---|---|---|---|
| `POST` | `/api/v1/admin/register` | New user registration | 200 |
| `POST` | `/api/v1/admin/login` | Login; returns `{ token, refreshToken, expiresIn, user }` | 200 |
| `POST` | `/api/v1/admin/refresh` | Accepts `{ "refreshToken": "..." }`; returns same shape as login | 200 |
| `GET` | `/api/v1/admin/{id}` | Returns admin/user object by ID | 200 |
| `PATCH` | `/api/v1/admin/{id}` | Update user (only `username` and `active` fields allowed) | 200 |

### Configuration

| Method | Path | Description | Status |
|---|---|---|---|
| `POST` | `/api/v1/configuration/create` | Creates a new configuration | 200 |
| `GET` | `/api/v1/configuration/configurations` | Returns active configurations for logged-in user (OWNER or in `adminIds`) | 200 |
| `GET` | `/api/v1/configuration/history/{configurationId}` | Returns version history list: `[{ version, lastModifiedDate, active }]` | 200 |
| `GET` | `/api/v1/configuration/stats/{configurationId}` | Returns lookup stats for a configuration | 200 |
| `PATCH` | `/api/v1/configuration/{configurationId}` | Updates configuration — receives entire updated JSON; creates new version from active | 204 |
| `PUT` | `/api/v1/configuration/{version}/{configurationId}` | Sets a specific version as the active configuration | 202 |
| `DELETE` | `/api/v1/configuration/{configurationId}` | Deletes configuration — OWNER only | 204 |
| `PUT` | `/api/v1/configuration/transfer/{configurationId}` | Transfers ownership to another registered admin — OWNER only | 202 |

### Lookup (Consumer)

| Method | Path | Description | Status |
|---|---|---|---|
| `POST` | `/api/v1/lookup/{configurationId}` | Authenticated consumers lookup settings by GUID or key | 200 |

> **Note:** `configurationId` throughout refers to the document's `id` field (MongoDB `_id`).

---

## Request Payloads

### Transfer Ownership
```json
{
  "transfer-to": "<admin-guid>"
}
```
> Only executes if the logged-in user is the OWNER of the configuration.

### Lookup — By Setting GUID
```json
{
  "settings": [
    "<setting-guid>",
    "<setting-guid>"
  ]
}
```

### Lookup — By Key
```json
{
  "keys": [
    "<key-string>",
    "<key-string>"
  ]
}
```

---

## Data Schemas

### Configuration Document
```json
{
  "id": "<configuration-guid>",
  "configurationId": "<configuration-guid>",
  "name": "<string>",
  "description": "<string>",
  "version": "<number>",
  "active": "<boolean>",
  "owner": "<admin-guid>",
  "createdDate": "<date-time>",
  "lastModifiedDate": "<date-time>",
  "adminIds": ["<admin-guid>", "..."],
  "settings": [
    {
      "id": "<setting-guid>",
      "key": "<key-string>",
      "value": "<object>",
      "type": "<object>"
    }
  ],
  "additionalProperties": {
    "property-1": "<object>",
    "property-2": "<object>"
  }
}
```

| Field | Notes |
|---|---|
| `id` | MongoDB primary index (`_id`) |
| `configurationId` | Secondary index at DB level; shared across all versions of the same configuration |
| `version` | Auto-increments on every `PATCH` (1, 2, 3...); used to revert to older versions |
| `active` | Only one version per `configurationId` is active at a time |
| `owner` | Admin GUID; set on creation; only this user can delete or transfer |
| `adminIds` | List of admin GUIDs with edit access |

---

### Admin / User Document
```json
{
  "id": "<admin-guid>",
  "username": "<email>",
  "numberOfConfigurationsOwned": "<number>",
  "registeredOn": "<date-time>",
  "active": "<boolean>"
}
```

> `numberOfConfigurationsOwned` increments on every configuration creation regardless of configuration state. Does **not** decrement on DELETE (lifetime count).

---

### Stats Document
```json
{
  "configurationId": "<configuration-guid>",
  "totalLookups": "<number>",
  "lastLookupAt": "<date-time>"
}
```

> Stored in a separate MongoDB collection. Updated on every successful lookup call.

---

## Error Handling

- All exceptions return **HTTP 500**
- Exceptions must be **logged** (to support alerting on top of logs)
- Do not expose raw stack traces in API responses

---

## Non-Functional Requirements

- JWT authentication required on **all routes**
- Passwords must be hashed with **bcrypt**
- All IO-bound operations use Spring `@Async`
- MongoDB runs on **local endpoint** (connection string configured in code/application properties)
- Application containerized via **Docker Compose** (local only)
