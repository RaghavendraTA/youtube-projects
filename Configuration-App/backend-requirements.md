# Configuration App Requirements Design Doc
The Configuration App is all about providing configurations to downstream system, the consuming service admins can setup, create or modify configurations through User interface in a react UI and exposes seperate lookup endpoint which accepts lookup parameters and gives partial or full response based on the parameters.

## Users
- Adminstrative users who owns the downstream services for configuration app. (Special Json viewer - Tree view (create and edit supported))
- Consumers are actually non-human services.

## What does it solve
- This config app is way of centralizing all the configurations required by any services at any point in time given that modifiable and goes live with single click.

# Admin Roles
OWNER → owns configuration (assigned when configuration gets created)
EDITOR → can view and edit configuration (cannot be deleted neither can change ownership)

## Backend Tech stack
- Java version: 21+
- Backend: Java, Sprint-Boot 3.5, REST API, for IO bound APIs use virtual threads (like Project Loom)
- Database: MongoDB (Server runs in local endpoint needs to be added in code)
- Auth: JWT
- Hosting: Docker compose (local setup for spring boot app only)

## API endpoints
POST - /api/v1/admin/register                           → new user registration (HTTP Status 200)
GET - /api/v1/admin/{id}                                → returns admin/user object (HTTP status 200)
PATCH - /api/v1/admin/{id}                              → update user details (can only update username and active flag) (HTTP Status 200)
POST - /api/v1/admin/login                              → returns `{ token, refreshToken, expiresIn, user }` (HTTP status 200)
POST - /api/v1/admin/refresh                            → token refresh (accepts { "refreshToken": "..." }) returns as same as login
POST - /api/v1/configuration/create                     → creates a new configuration (HTTP Status 200)
GET - /api/v1/configuration/configurations              → returns list of configurations (active) for the logged-in user (both owns and adminIds) (HTTP Status 200)
GET - /api/v1/configuration/history/{configurationId}  → returns specific configuration with all versions [{ version, lastModifiedDate, active }] (HTTP Status 200)
GET - /api/v1/configuration/stats/{configurationId}     → returns configuration usage stats (HTTP Status 200)
PATCH - /api/v1/configuration/{configurationId}         → updates configuration (creates new version from the active) (HTTP Status 204)
PUT - /api/v1/configuration/{version}/{configurationId} → enables speicifc version as active configuration (HTTP Status 202)
DELETE - /api/v1/configuration/{configurationId}        → Delete a configuration (only owners can do this) (HTTP Status 204)
PUT - /api/v1/configuration/transfer/{configurationId}  → transfer the ownership to another registered admin (HTTP Status 202)
POST - /api/v1/lookup/{configurationId}                 → consumers with JWT token can do a lookup within configuration (HTTP Status 200)

Note: 
- configurationId here is the db id
- configuration patch will receive entire updated configuration json

## Transfer request payload
```
{
    "transfer-to": "<admin-guid>"
}
```
- it should only work if the logged-in user is the owner of selected configuration

## Errors and Exception
- Throw 500 for any errors and log exceptions. (so alerts can be written on top of that)

## Non-Functional requirements
- Auth required on all routes
- Passwords must be hashed (bcrypt)

## configuration schema
```
{
    "id": "<configuration-guid>",
    "configurationId": "<configuration-guid>",
    "name": "<string>",
    "description": "<string>",
    "version": "<number>",
    "active": "<boolean>",
    "owner": "<string>",
    "createdDate": "<date-time>",
    "lastModifiedDate": "<date-time>",
    "adminIds": ["<adminis-guid>"...],
    "settings": {
        "key-1": {
            "id": "<setting-guid>",
            "value": "<object>",
            "type": "<object>"
        },
        "key-2": {
            "id": "<setting-guid>",
            "value": "<string>",
            "type": "<string>"
        },
        ...
    },
    "additionalProperties": {
        "property-1": "<object>",
        "property-2": "<object>"
    }
}
```
owner           → admin guid defaulted when configuration gets created, and has delete access. (can also transfer the ownership to another registered user)
adminIds        → this is a list of user guid's who has the edit access to the configuration
version         → Autoincrement On configuration update (1, 2, 3...) it will be easy to switch to the older version.
id              → DB indexed
configurationId → additional indexing will be added at db level


## Lookup payload
- Using just <setting-guid>
POST - /api/v1/lookup/{configurationId}
```
{
    "settings": [
        "<setting-guid>"
        "<setting-guid>"...
    ]
}
```

- Using just keys
POST - /api/v1/lookup/{configurationId}
```
{
    "keys": [
        "<key-string>"
        "<key-string>"...
    ]
}
```

- Lookup call should also needs to put some stats in the seperate table/container so it can be fetched to the dashboard
- for now total number of lookups should be good enough

## User/admin Schema
```
{
    "id": "<admin-guid>",
    "username": "<emailid>",
    "numberOfConfigurationsOwned": "<number>",
    "registeredOn": "<Datetime>",
    "active": "<boolean>"
}
```
numberOfConfigurationsOwned - should gets incremented for every configuration that the user creates, doesn't decrement on DELETE.

## Stats Schema
```
{ configurationId, totalLookups, lastLookupAt }
```