# Blood Bank (Spring Boot + PostgreSQL)
1. Create the database:  `createdb bloodbank`  (edit credentials in src/main/resources/application.properties)
2. Run:  `mvn spring-boot:run`  (needs Java 17+ and Maven), then open http://localhost:8080
3. Open http://localhost:8080 — stock tanks update live over WebSocket (/ws).
4. Make an admin: register normally, then `UPDATE users SET role='admin' WHERE email='you@example.com';` and sign in again.
5. Change `app.jwt.secret` before deploying anywhere real.
