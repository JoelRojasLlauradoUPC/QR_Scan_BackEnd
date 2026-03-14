# QR Event Access Backend API

Backend Java (Jersey + Grizzly + MariaDB) para validar entradas de evento mediante QR.

## Endpoints

- `GET /api/event/verify/{hash}`
  - Consulta una entrada sin consumirla.
  - `200` si existe, `404` si no existe.

- `GET /api/event/enter/{hash}`
  - Valida acceso y marca la entrada como consumida.
  - `200` si valida, `404` si no existe, `409` si ya fue usada.

- `GET /api/event/tickets`
  - Lista todas las entradas.

- `GET /api/event/tickets/email/{email}`
  - Lista todas las entradas de un usuario segun su correo electronico.

- `GET /api/event/tickets/used`
  - Lista todas las entradas usadas.

- `GET /api/event/tickets/unused`
  - Lista todas las entradas no usadas.

### Respuesta de entrada

```json
{
  "nombre": "Leonardo",
  "apellido": "Andreoli",
  "correo_electronico": "delegacio.eetac@upc.edu",
  "tipo": 4,
  "pmr": false,
  "hash": "HnmdIOCoTKusuKcCSyQCugr9v30fcQ5J",
  "numero_local": 2,
  "consumed": false
}
```

## Base de datos

- Script de esquema: `src/main/resources/db/schema.sql`
- Script de datos de prueba: `src/main/resources/db/seed.sql`
- JSON fuente de entradas: `src/main/resources/db/input/tickets.raw.json`
- Generador de seed desde JSON: `scripts/generate-seed-from-json.ps1`

### Cargar entradas desde JSON

1. Pega o actualiza el JSON en `src/main/resources/db/input/tickets.raw.json`.
2. Regenera `seed.sql` con:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\generate-seed-from-json.ps1
```

3. Ejecuta esquema y seed sobre MariaDB:

```powershell
mysql --default-character-set=utf8mb4 -u root -p < src/main/resources/db/schema.sql
mysql --default-character-set=utf8mb4 -u root -p < src/main/resources/db/seed.sql
```

El backend ya no ejecuta `schema.sql` ni `seed.sql` automaticamente al arrancar.

El `seed.sql` se genera con `ON DUPLICATE KEY UPDATE` usando `hash` unico, por lo que es idempotente.

Comprobacion rapida de acentos/ñ:

```powershell
mysql --default-character-set=utf8mb4 -u root -p -D qr_app -e "SELECT nombre, apellido FROM event_tickets WHERE apellido LIKE '%ñ%' OR apellido LIKE '%á%' LIMIT 10;"
```

Variables de conexion soportadas (env o `-D` JVM):

- `DB_HOST` (default `127.0.0.1`)
- `DB_PORT` (default `3306`)
- `DB_NAME` (default `qr_app`)
- `DB_USER` (default `root`)
- `DB_PASS` (default `root`)

## Estructura nueva

- `src/main/java/edu/upc/dsa/event/services/EventService.java`
- `src/main/java/edu/upc/dsa/event/service/EventManager.java`
- `src/main/java/edu/upc/dsa/event/repository/JdbcTicketRepository.java`
- `src/main/java/edu/upc/dsa/event/model/Ticket.java`

## Tests

- `src/test/java/edu/upc/dsa/event/service/EventManagerTest.java`

## Deploy en Render (Docker)

Si Render solo te deja elegir entre `docker`, `node`, `python`, etc., crea el servicio como **Docker**.

Archivos ya preparados en el repo:

- `Dockerfile`
- `.dockerignore`

Variables de entorno recomendadas en Render:

- `DB_HOST`
- `DB_PORT` (normalmente `3306`)
- `DB_USER`
- `DB_PASS`

`PORT` y `HOST` los gestiona Render (tu `Main` ya los lee por entorno).

Tambien puedes usar Blueprint con `render.yaml` para crear el servicio automaticamente desde el repo.

### MongoDB Atlas (gratis)

Si quieres desplegar sin MariaDB, activa Mongo con estas variables:

- `DB_PROVIDER=mongo`
- `MONGO_URI` (cadena de conexion de Atlas)
- `MONGO_DB=qr_app`
- `MONGO_COLLECTION=event_tickets`

Con `DB_PROVIDER=mongo`, el backend deja de inicializar scripts SQL y usa MongoDB como almacenamiento principal.

