# Hdi Sla3tak Pro

Mobile-first Spring Boot web app for repair shops in Morocco. It helps technicians register customer items, attach proof photos, generate pickup codes, update repair status, verify ownership, and confirm delivery.

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
