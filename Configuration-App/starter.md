# Configuration System (Problem Statement)

Configuration System is a centralized configuration management system designed to solve the critical challenge of managing and distributing configurations across multiple downstream services in a scalable, version-controlled manner.

### Core Problem
Organizations need a unified way to:
- Maintain configurations for various services without scattering them across multiple systems
- Modify configurations dynamically with immediate deployment capability (single-click activation)
- Track configuration changes and maintain version history for rollback capability
- Control access through role-based permissions (owners and editors)
- Enable downstream consumers (non-human services) to query relevant parts of configurations via a lookup API

### Key Challenges
- **Configuration Fragmentation**: Configurations scattered across services with no central source of truth
- **Version Control Gap**: No built-in versioning or rollback mechanism for configuration changes
- **Access Control**: Need granular permissions—some users own configurations, others need edit access
- **Dynamic Updates**: Changes must go live instantly without service redeployment
- **Consumption Pattern**: Consuming services need efficient lookup APIs to fetch only relevant configuration sections

### Solution Overview
A full-stack application with:
- Secure admin portal to manage configurations with tree-view UI
- REST API backend with proper authentication and authorization
- DB for persistence with automatic versioning on each configuration update
- Role-based access control (OWNER, EDITOR)
- Lookup endpoint for consuming services to fetch configurations by setting GUID or key

  ![Configuaration System](<problem statement.png>)
  