# Hdi Sla3tak Pro

Mobile-first Spring Boot web app for repair shops in Morocco. It helps technicians register customer items, attach proof photos, generate pickup codes, update repair status, verify ownership, and confirm delivery.

## Deployment model
- This app is designed for one repair shop per deployment.
- Each shop should get its own app instance, its own database, its own uploads volume, its own users, and its own shop name.
- The repo does not ship a default admin account or seeded customer/shop data.
- On a fresh database, `/login` redirects to `/setup`, where the first owner account and shop name are created.

## Main features
- Mobile-first frontend inspired by the approved Stitch direction
- Spring Boot MVC + Thymeleaf frontend and backend in one project
- Bilingual UI: French and Arabic with RTL support for Arabic
- First-run owner setup: no default admin account shipped in the app
- Customer management
- Repair queue with search and filters
- Photo upload and pickup code generation
- Delivery verification using phone + photo + pickup code
- Admin user management: create, edit, delete, change password
- Integration-ready WhatsApp messaging service

## Tech stack
- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Thymeleaf
- H2 by default
- PostgreSQL-ready profile still included in the project

## What already makes single-shop deployments safe
- `app_users` is local to each deployment's database and the first owner is created only through the setup flow.
- `shop_settings` stores one shop-wide business name per deployment.
- Customer, repair item, history, and upload data all live inside that deployment's own database and volume.
- `data/`, `uploads/`, and `target/` are gitignored, so shop data is not meant to live in the repo.

## Run in IntelliJ
1. Open the project root that contains `pom.xml`.
2. Set Project SDK to Java 21.
3. Reload the Maven project.
4. Run `HdiSla3takApplication.java`.
5. Open `http://localhost:8080/login`.
6. On the first run, you will be redirected to `/setup` to create the owner account.

## Run from terminal
If your machine has Maven installed:

```bash
mvn spring-boot:run
```

Then open:

```text
http://localhost:8080/login
```

## Free deployment
The easiest free hosting path for this app is an Oracle Cloud Always Free VM. That gives you a persistent Linux machine and free block storage, which fits this app better than ephemeral free app-hosting tiers because the app stores uploads and the H2 database on disk.

Oracle's free tier documentation:
- [Always Free overview](https://docs.oracle.com/en-us/iaas/Content/FreeTier/freetier.htm)
- [Block Volume service](https://docs.oracle.com/en-us/iaas/Content/Block/Concepts/overview.htm)

### Recommended setup
1. Create an Oracle Cloud Free Tier account and pick a home region you will keep using.
2. Launch an Always Free Linux VM.
3. Open inbound TCP ports `22` and `8080` in the Oracle Cloud network rules.
4. Install Docker and Docker Compose on the VM.
5. Clone this repository on the VM.
6. Start the app with:

```bash
docker compose up -d --build
```

7. Open `http://<your-public-ip>:8080/login`.
8. On first launch, go to `/setup` and create the owner account and business name.

The included `docker-compose.yml` already mounts persistent storage for:
- the H2 database files at `/app/data`
- uploaded item photos at `/app/uploads`

## Railway per-shop deployment
Use this repo as the reusable template, and create one new Railway project per shop.

### Recommended repeatable workflow
1. Create a brand-new Railway project from this GitHub repo for each shop.
2. Attach one fresh volume for uploads and mount it at `/app/uploads`.
3. Provision one fresh PostgreSQL database for that same shop only, or connect one external PostgreSQL database dedicated to that shop.
4. Set the environment variables from `.env.example`.
5. Open the app after the first deploy and complete `/setup` to create the owner account and shop name.
6. Never point two shop deployments at the same database or the same volume.

### Minimum Railway variables
```text
SPRING_PROFILES_ACTIVE=postgres
DB_HOST=<shop-specific-db-host>
DB_PORT=5432
DB_NAME=<shop-specific-db-name>
DB_USERNAME=<shop-specific-db-user>
DB_PASSWORD=<shop-specific-db-password>
APP_STORAGE_UPLOAD_DIR=/app/uploads/items
APP_SMS_BASE_URL=https://${{RAILWAY_PUBLIC_DOMAIN}}
```

### Railway config included in this repo
- `railway.json` sets Dockerfile builds, a stable `/healthz` deployment healthcheck, and a conservative restart policy.
- The health endpoint is public and returns HTTP 200 without depending on whether the shop has finished owner setup yet.

### Best template strategy for Railway
- Safest: keep one clean GitHub repo as the golden template and create a new Railway project from it for each shop.
- Avoid duplicating an already-live shop environment, because that can copy service wiring or secrets you did not intend to reuse.
- Pick a Railway region close to the shop's database region to reduce request latency.

## Default database
The app uses a file-based H2 database by default:
- path: `./data/hdisla3takdb`

## Uploads
Uploaded item photos are stored outside the classpath in:
- `uploads/items`

Override with:
- `APP_STORAGE_UPLOAD_DIR`

## Language switch
Use the FR / AR buttons in the interface. The app stores the selection in a cookie.

## SMS compose
The app prepares short customer SMS messages and opens the phone's default messaging app with the phone number, pickup code, and tracking link filled in.

No paid SMS provider is needed for the current flow. The server still records history entries so staff can see that an SMS compose action was prepared.

When a new repair item is created, the app prepares an SMS compose action.
When the item moves to **Ready for pickup**, the app prepares another SMS compose action.

## Notes
- The message content is bilingual-friendly and can be refined later.
- The app is designed as a strong version 1 foundation and can later be extended with QR codes, billing, advanced analytics, or a separate frontend.
- For production-style deployments, PostgreSQL + a dedicated uploads volume is the intended setup.
