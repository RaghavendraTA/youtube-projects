# Configuration App Requirements Design Doc
The Configuration App is all about providing configurations to downstream system, the consuming service admins can setup, create or modify configurations through User interface in a react UI and exposes seperate lookup endpoint which accepts lookup parameters and gives partial or full response based on the parameters.

## Users
- Adminstrative users who owns the downstream services for configuration app. (Special Json viewer - Tree view (create and edit supported))
- Consumers are actually non-human services.

## What does it solve
- This config app is way of centralizing all the configurations required by any services at any point in time given that modifiable and goes live with single click.

## Frontend Tech stack
- Frontend: React 18, Tailwind CSS, React Router
- State Management: Redux toolkit
- Backend details: .env
- token management: localStorage, refresh upon expiry
- Hosting: Docker compose (local setup only)

## MUST HAVE:
- User registration & login
- Dashboard showing user data
- CRUD for confitguration

## NICE TO HAVE:
- Dark mode
- Email notifications

## Page / Screen breakdown
/register                   → user registration page/section (Public route)
/login                      → Login form, redirect to configurations on success (Public route)
/dashboard                  → Show configuration consumption stats, recent configuration changes done by the user
/configuration/settings/:id → management page (owned configurations only else show a message) - should be able assign editors, transfer ownership, delete button
/configurations             → Shows List of configurations (owned or has edit access). [configuration name, id, lastModified by, history popup, active version]
/configuration/create       → create veiw of configuration
/configuration/:id          → CRUD on specific configuration (Tree view in UI)

### Note: 
- dashboard should have section to edit user/admin personal details, disable their account, and logout
- history popup will have button in the row to set active which does PUT - /api/v1/configuration/{version}/{configurationId}

# Admin Roles
OWNER → owns configuration (assigned when configuration gets created)
EDITOR → can view and edit configuration (cannot be deleted neither can change ownership)

## UI design notes
- Style: Clean, minimal, similar to Notion
- Colors: Blue primary (#3B82F6), white background
- Mobile-first responsive design
- Use shadcn/ui component library

## Tree View behavior
- supported types are (string, number, object)
View should look like this
Row-1:
    settings: name-1
    type: object (dropdown)
    value:
            // if the dropdown is selected as object it should dynamically create below section
            settingsId: name-2
            type: string (dropdown)
            value: xyz (input field)
Row-2:
    Settings: name-3 (Guid should be behind)
    type: number (dropdown)
    value: 2456.678 (input field)

- Seperate table for additional properties structure should be similar
- entire additional properties can be retrived with single api call

## Non-Functional requirements
- Auth required on all routes (except register and login)
- Input validation on all forms
- Error messages shown to user (not raw errors)

## Assumptions
- No payment integration needed yet
- Single language (English only)

## Messages
- success messages should be shown in single ribbon
- failed one's should be a popup