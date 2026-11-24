# BloodBank — Live Blood Stock & Donation Management

A Spring Boot web application that connects donors, hospitals and blood bank administrators in one secure, role-based platform.

## Problem statement

Blood donation coordination is fragmented. Donors, hospitals and administrators often rely on separate tools, phone calls and manual records, so available blood stock is unclear, requests move slowly, appointments are hard to track, and administrators lack one reliable dashboard.

BloodBank solves this by providing a single platform where live stock, blood drives, donor appointments, hospital requests and admin management work together.

## Live links

- **Web app:** https://bloodbank-pme5.vercel.app
- **Backend API:** https://soon-strap-munich-lake.trycloudflare.com
- **Source:** https://github.com/mtnamisi-web/BLOODBANK

> The backend API link above is a temporary Cloudflare quick tunnel. It changes when the tunnel or host machine restarts. A permanent Render + Neon deployment is planned.

## Features

- Live blood stock summary and search by blood group
- Blood drives with venue, date, time and contact details
- Donor registration, Sign in and appointment booking
- Hospital blood requests with urgency and units needed
- Admin console for inventory, drives, requests and appointments
- Donor eligibility calculation using the 56-day donation interval
- Forgot-password flow using a configurable reset word (`happy` in the demo)
- WebSocket support for live updates

## Technology stack

- Java 17, Spring Boot 3.3.5, Maven
- Spring Web, Spring Data JPA, Hibernate
- Spring Security, JWT (JJWT), BCrypt
- PostgreSQL
- WebSocket
- Static HTML/CSS/JavaScript frontend
- Vercel frontend hosting
- GitHub for source control

## User roles

| Role | Main actions |
|---|---|
| Donor | View drives and stock, book appointments, check eligibility |
| Hospital | Search stock, raise and track blood requests |
| Admin | Manage inventory, drives, requests and appointments |

## Run locally

```bash
cd bloodbank
mvn spring-boot:run
```

Then open http://localhost:8080

## Configuration

The application reads configuration from environment variables with local defaults:

| Variable | Purpose |
|---|---|
| `DATABASE_URL` | JDBC PostgreSQL connection URL |
| `DB_USERNAME` | Database user |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | Secret used to sign JWT tokens |
| `PASSWORD_RESET_WORD` | Demo password reset word |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed frontend origins |
| `PORT` | Server port (default `8080`) |

## Deployment

The repository includes `render.yaml` for deploying the backend as a free Render web service. Pair it with a free Neon PostgreSQL database for a permanent public deployment.
