# Configuration App — Frontend Requirements Design Doc

## Overview

The Configuration App frontend is a React 18 admin UI that allows administrators to create, manage, and configure settings for downstream services. It communicates with the Spring Boot backend via REST API. Consumers are non-human services and do not use this UI.

---

## Users

| Type | Description |
|---|---|
| **Admin (OWNER)** | Creates configurations; full access including delete and ownership transfer |
| **Admin (EDITOR)** | View and edit configurations only; no delete or ownership management |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | React 18 |
| Styling | Tailwind CSS |
| Routing | React Router v6 |
| State Management | Redux Toolkit |
| Component Library | shadcn/ui |
| API Config | Environment variables via `.env` |
| Token Management | localStorage; auto-refresh on expiry |
| Hosting | Docker Compose (local only) |

---

## Must Have Features

- User registration and login
- Dashboard with configuration stats and recent activity
- Full CRUD for configurations with tree view editor

## Nice to Have (Future)

- Dark mode
- Email notifications

---

## Pages & Routes

### Public Routes

| Route | Description |
|---|---|
| `/register` | User registration form |
| `/login` | Login form; redirects to `/configurations` on success |

### Protected Routes (JWT required; redirect to `/login` if unauthenticated)

| Route | Description |
|---|---|
| `/dashboard` | Configuration consumption stats + recent changes by user. Also contains: edit personal details, disable account, logout |
| `/configurations` | List of configurations the user owns or has edit access to |
| `/configuration/create` | Create new configuration form |
| `/configuration/:id` | View and edit specific configuration (tree view UI) |
| `/configuration/settings/:id` | Ownership management for a specific configuration (OWNER only; show message if not owner) |

---

## Page Details

### `/dashboard`
- **Stats cards:** Total configurations owned, total configurations editable, total lookup count (from stats API)
- **Recent activity table:** Last 5 configurations modified by the user (name, version, last modified date)
- **User section:** Edit username, disable account toggle, logout button

### `/configurations`
Table columns: `Configuration Name` | `ID` | `Last Modified By` | `Active Version` | `History` | `Actions`

- **History button** opens a popup showing version list from `GET /api/v1/configurations/history/{configurationId}`
  - Each row: `Version` | `Last Modified Date` | `Active (badge)`
  - Each row has a **"Set Active"** button → calls `PUT /api/v1/configuration/{version}/{configurationId}`
- **Actions:** Open (navigate to `/configuration/:id`), Settings (navigate to `/configuration/settings/:id`)

### `/configuration/create`
- Form fields: Name, Description, Partition Key
- Tree view editor (empty by default) to add initial settings and additional properties
- Submit → calls `POST /api/v1/configuration/create`

### `/configuration/:id`
- Displays the active version of the configuration
- Tree view editor for `settings` (see Tree View Behavior)
- Separate table for `additionalProperties` (same structure as tree view)
- Save → calls `PATCH /api/v1/configuration/{configurationId}` with full updated JSON
- Version history panel (collapsible) — same as history popup above

### `/configuration/settings/:id`
- **OWNER only page.** If the logged-in user is not the OWNER of this specific configuration, display a message: "You do not have ownership of this configuration."
- If OWNER:
  - Assign Editors: search registered admins by email, add/remove from `adminIds`
  - Transfer Ownership: input field for new owner's email/ID → calls `PUT /api/v1/configuration/transfer/{configurationId}`
  - Delete Configuration: confirmation dialog → calls `DELETE /api/v1/configuration/{configurationId}`

---

## Admin Roles (UI Behaviour)

| Role | Access |
|---|---|
| **OWNER** | Full access to all pages and actions |
| **EDITOR** | Can view and edit configurations; `/configuration/settings/:id` shows ownership message; no delete or transfer buttons visible |

---

## Tree View Behavior

Supports value types: `string`, `number`, `object`

### Visual Structure
```
Row 1:
  Setting Name : [text input]         (GUID stored internally, name shown)
  Type         : [dropdown: string | number | object]
  Value        : [input field]
                 ↳ if type = object, dynamically renders nested row:
                     Setting Name : [text input]
                     Type         : [dropdown]
                     Value        : [input field]

Row 2:
  Setting Name : [text input]
  Type         : [dropdown]
  Value        : [input field]
```

- Add Row button appends a new empty row
- Delete button on each row removes it
- Nested object rows are indented visually
- GUIDs are generated behind the scenes; only the key name is shown to the user
- `additionalProperties` uses the same tree view structure in a separate section/table on the same page

---

## UI Design

| Property | Value |
|---|---|
| Style | Clean, minimal — Notion-inspired |
| Primary Color | `#3B82F6` (blue) |
| Background | White |
| Layout | Mobile-first, responsive |
| Component Library | shadcn/ui |

---

## Notifications & Messages

| Type | Behaviour |
|---|---|
| **Success** | Single ribbon/banner (top of page, auto-dismisses) |
| **Error / Failure** | Modal popup with message and dismiss button |

---

## Non-Functional Requirements

- Auth required on all routes except `/login` and `/register`
- Unauthenticated users redirected to `/login`
- JWT token stored in `localStorage`; auto-refreshed on expiry via `POST /api/v1/admin/refresh`
- Input validation on all forms before submission
- User-friendly error messages only (no raw API errors or stack traces shown)
- All API base URLs configured via `.env` file

---

## Assumptions

- No payment integration
- English only (single language)
- Local development only (Docker Compose)
