# 🌿 Plantation Portal — Frontend (Angular 21 + Angular Material)

A complete frontend for the MCD Plantation backend
(`E:\Java 8\plantation-source-10072026\plantation`). It covers the **Admin**
dashboard, **Officer** dashboard, and **Citizen** portal, plus the public site
and the full set of authentication flows.

> **Security note:** this frontend contains **no secrets**. All credentials
> (database, JWT signing/encryption keys, payment keys, OAuth client secret)
> belong to the backend and must be provided through **backend environment
> variables** only.

---

## 1. Run it

```bash
cd plantation-frontend
npm install
npx ng serve          # → http://localhost:4200
```

Production build:

```bash
npx ng build          # output in dist/plantation-frontend
```

The API base URL lives in `src/environments/environment.ts`
(`apiUrl`, default `http://localhost:8080/plantation`).

---

## 2. What's included

### Public site (no login required)
| Route | Page |
|-------|------|
| `/home` | Landing page (hero, features, featured parks) |
| `/parks` | Browse parks, filter by zone, paginated |
| `/parks/:id` | Park detail → pick date → pick slot → pick trees → **book** |
| `/trees` | Tree species catalogue — search + category filter (the Category dropdown only offers categories that exist in the current search results, so every choice leads to at least one card) |
| `/verify` | Verify a certificate by number (`?cert=NUMBER` deep-link) |

### Authentication
| Route | Flow |
|-------|------|
| `/login` | Citizen **and** Official tabs, password login + **Google sign-in** (enabled only when the backend has `GOOGLE_CLIENT_ID`, otherwise an explained "not configured" state) |
| `/register` | Citizen registration (name, email, password, phone, Aadhaar-last-4, address) |
| `/forgot-password` | Email → **OTP** → new password (stepper) |
| `/otp-login` | Mobile number → **OTP** → sign in |

### Admin dashboard (`/admin`, role `R_HORTIC_ADM`)
`/admin/dashboard` renders the **complete** `GET /mcd/dashboard` snapshot (all
25 fields of `DashboardOverview`):

- KPIs: trees planted · total bookings · certificates issued · active parks ·
  upcoming slots (7 days) · low-stock alerts
- today / this-week (Mon–today) / this-month (1st–today) trees + bookings
- **all 7** booking statuses — pending payment, paid, scheduled, waiting,
  planted, completed, cancelled
- last-7-days planted-vs-booked trend, parks with pending work,
  zone breakdown (bars + table), top species, recent activity

Then · Parks (CRUD) · Zones · Officials · Assignments · Tree Species (CRUD —
the list comes from `GET /trees/all`, the only staff endpoint that returns
deactivated species too) · Occasions (CRUD) · Master Inventory (+history) ·
Citizens · Bookings

### Officer dashboard (`/officer`, roles `R_HORTIC_OFF` / `SUPERVISOR`)
`/officer/dashboard` is **individual / scoped** — the backend restricts every
call below to the logged-in official's `userSystemCode` joined against
`park_official_assignments`, so an officer only ever sees their own parks:

| Panel | Endpoint |
|-------|----------|
| My parks + zones covered | `GET /mcd/my-parks` |
| Pending work · trees to plant | `GET /mcd/plantation/pending` |
| Due today | `GET /mcd/plantation/pendingToday` |
| Plantations recorded (+ this month) | `GET /mcd/plantation/completed` |
| Low-stock alerts | `GET /mcd/inventory/low-stock` |

Each panel has an explicit empty state (e.g. "No parks are assigned to you yet"),
so a freshly created officer gets guidance instead of blank cards. Each call is
loaded independently (`catchError` fallback) — one failing endpoint cannot blank
the whole dashboard.

Then · Pending Plantations (record / mark-arrived) · Completed ·
Manage Slots · Park Inventory Matrix · Low-Stock Alerts

**Manage Slots · Trees dialog** — every slot row has a *Trees* button that opens
`slot-inventory-dialog`, the screen the backend always had but the UI never
exposed:

| Action | Endpoint |
|--------|----------|
| Add a species to a slot | `POST /mcd/inventory` |
| Change its stock qty | `PUT /mcd/inventory/{inventoryId}` |
| Remove it (2-step confirm) | `DELETE /mcd/inventory/{inventoryId}` |
| Reload the row after a change | `GET /slots/{slotId}` |

The species select is fed by `GET /trees` (species already on the slot are
hidden). Backend rules are shown verbatim in the snack bar, e.g. *"Gulmohar is
not allocated to this park. Add it via Park Inventory first."* or *"Adding 5
would exceed slot capacity (30)"* — the dialog never invents its own validation,
and the "N tree(s) can still be assigned here" hint mirrors the same rule.

### Citizen portal (`/citizen`, role `CITIZEN`)
`/citizen/dashboard` is the landing page, derived only from the caller's own
data: `GET /bookings/mine` plus a per-booking `GET /certificates/{bookingId}`
fan-out (capped at 30 most-recent planted bookings).

- KPIs: my bookings · upcoming slots · trees planted · certificates earned
- trees reserved / completed plantations / cancelled counts
- full status breakdown, upcoming list, recent list, "no bookings yet" empty state

#### `/citizen/book` — the 6-step booking wizard
Material-only stepper (`mat-stepper`, linear — a step cannot be left until it
is satisfied). The component ships **no stylesheet**: every control is an
Angular Material component, so it inherits the app theme.

| Step | What happens | Ends when |
|------|--------------|-----------|
| 1 · OCCASION | occasion **cards** (emoji avatar, description, Select → Selected); picking one loads `GET /occasions/{id}/species` so suitable stocked species are marked "⭐ Recommended" later; "Any occasion" card skips | always |
| 2 · PARK | info banner + search box + **zone chips** (`mat-chip-option`) + park **cards** over `GET /parks/all` showing the free-slots chip and zone · address | a park is chosen |
| 3 · DATE & SLOT | two columns: datepicker (today →) + `GET /parks/{id}/slots?date=` on the left (full slots disabled), and a **"Pick a slot to see trees"** panel on the right that previews that slot's `inventory[]` (name, price, available qty) — empty state until a slot is picked | a slot is chosen |
| 4 · TREE | that slot's stock with price/available chips, +/− counters, occasion banner (or an honest "recommended trees aren't stocked in this slot" note) | ≥ 1 tree chosen |
| 5 · CONFIRM | summary + trees subtotal → `POST /bookings` (server adds GST + processing fee) | booking created |
| 6 · PAYMENT | server's `paymentOrder` breakdown (subtotal / GST / fee / total) → `POST /bookings/{id}/payment/init` → redirect to the MCD gateway | citizen pays |

Entry points: the citizen sidenav ("Book a slot"), the dashboard buttons and
"My Bookings → New booking".

> GST and the processing fee are **not** duplicated in the frontend: the confirm
> step shows the trees subtotal, and the payment step shows the authoritative
> figures the server returns with the booking.

#### `/citizen/payment/result` — gateway return route
`PG_RETURN_URL` points the gateway back here with `?encryptedResponse=…`; the
page posts that payload to `POST /bookings/payment/verify` and renders the
status the server reports (never an optimistic "success"). Without a payload it
explains where to look instead.

#### `/citizen/payment/test` — local fake gateway (TEST MODE, off by default)
Only reachable while the backend runs with `PG_TEST_MODE=true`: `payment/init`
then hands back this page instead of the real MCD gateway URL. It shows the
booking and offers **Simulate success** / **Simulate failure**, which call
`POST /bookings/{id}/payment/test-payload` to mint a clearly-labelled fake
gateway response (`transactionNumber` / `txnResponseCode` `000`|`102`) that the
**real** `/bookings/payment/verify` endpoint consumes — so success and failure
both exercise the production verify path and the page always shows the server's
verdict. With the flag off the endpoint answers 404 and the page says so.

Then · My Bookings · Booking Detail (pay, presence mode, cancel) · My Certificates

---

## 3. Architecture

```
src/
  environments/environment.ts        API base URL only (no secrets, no client id)
  app/
    core/
      models/index.ts               Typed interfaces for every backend DTO/enum
      auth/token.store.ts           JWT + user session (sessionStorage)
      auth/auth.service.ts          login/register/forgot/OTP/OAuth2 calls
      auth/auth.guard.ts            authGuard + roleGuard
      http/auth.interceptor.ts      attaches Bearer token, logs out on 401
      http/api.service.ts           1:1 gateway to every backend controller
    features/
      auth/       login, register, forgot-password, otp-login, unauthorized
      public/     public layout, home, parks, park-detail, trees, verify
      citizen/    layout, dashboard, my-bookings, booking-detail, certificates
      officer/    layout, dashboard, pending, completed, slots, inventory, low-stock
      admin/      layout, dashboard, parks, zones, officials, assignments,
                  trees, occasions, master-inventory, citizens, bookings
    app.routes.ts                   lazy-loaded, role-guarded routes
```

---

## 4. Backend prerequisites (important)

The auth subsystem that used to be commented out in the backend is **restored
and verified** (43/43 automated assertions). What is left for you is only
configuration:

### Starting the backend (local dev)

`application.yml` contains **no secrets** — five values have no default and
must be supplied or the app refuses to start with
`Could not resolve placeholder '<KEY>'`:

| Variable | Purpose |
|----------|---------|
| `JWT_SIGN_KEY` | JWT signing key (64-char hex) — **required, no default** |
| `JWT_ENCRYPT_KEY` | JWT encryption key (64-char hex) — **required, no default** |
| `PG_AES_KEY` | Payment-gateway payload encryption key (16/24/32 chars) — **required, no default** |
| `DB_USER`, `DB_PASSWORD` | Database credentials — **required, no default** |

They are resolved from, **highest precedence first**:

1. **Real environment variables** — use these in CI / deployed environments.
2. **`%USERPROFILE%\.plantation\backend.properties`** — the local-dev secrets
   file. It sits *outside* the source tree, so it can never be committed.
   On this machine it has already been created with the database
   credentials, the payment AES key and freshly rotated JWT keys.
3. **`./.env.properties`** next to the directory you start from (optional).

If you start from the IDE (Run ▸ Run on `PlantationPortalApplication`) it
picks option 2 automatically — no run-configuration changes needed. To use
option 1 instead, add the five variables under
*Run ▸ Edit Configurations ▸ Modify options ▸ Environment variables*.

Optional extras: `CORS_ALLOWED_ORIGINS`, `JWT_ACCESS_EXPIRY_MS`,
`JWT_REFRESH_EXPIRY_MS`, `RAZORPAY_KEY_ID` / `RAZORPAY_KEY_SECRET`,
`MAIL_HOST` / `MAIL_PORT` / `MAIL_USER` / `MAIL_PASS`.

### What the backend now provides

1. **CORS** — already enabled by this work in
   `security/SecurityConfig.java` (allowed origin from the
   `CORS_ALLOWED_ORIGINS` env var, default `http://localhost:4200`).
   Rebuild/restart the backend to activate it.

2. **Auth endpoints — RESTORED and verified.** All four exist and were tested
   end-to-end against the running backend:
   - `POST /auth/citizen/register` → 201, role `CITIZEN`
   - `POST /auth/citizen/login` → 200, role `CITIZEN`
   - `POST /auth/official/login` → 200, role `R_HORTIC_ADM` (Horticulture Admin) /
     `R_HORTIC_OFF` (Horticulture Officer) / `SUPERVISOR`
   - `POST /auth/refresh` → 200 (a refresh token cannot be replayed as a
     bearer token, and vice versa)

   Restored backend files: `entity/Citizen.java`, `entity/McdOfficial.java`,
   `repository/CitizenRepository.java`, `repository/McdOfficialRepository.java`,
   `security/CompositeUserDetailsService.java`, `service/impl/AuthService.java`,
   the `AuthController` in `controller/Controllers.java`, and the
   `PasswordEncoder` / `AuthenticationManager` beans in `SecurityConfig`.

   **How the three roles are bound:** the login writes `roleCode` into the
   JWT → `AuthTokenFilter` grants `ROLE_USER` + `ROLE_<roleCode>` →
   `SecurityConfig` allows `/mcd/**` only for the three staff roles.
   A valid token with the wrong role gets **403** (JSON, via
   `AuthAccessDeniedHandler`); a missing/invalid token gets **401**.
   The admin-only checks inside the controllers (e.g. `/mcd/citizens`,
   `/mcd/officials`) return 401 for non-admin staff.

3. **Forgot-password / OTP** — the routes exist and answer **501** with an
   explicit message naming exactly what has to be configured (`MAIL_HOST` /
   `MAIL_PORT` / `MAIL_USER` / `MAIL_PASS` for e-mail, an SMS gateway for OTP).
   Nothing is faked: no OTP or reset link is ever generated without the real
   channel, and no external API is called by the frontend itself.

   **Google sign-in is different — it is implemented end to end** (see §6).

4. **Tree species reads — ADDED.** `SecurityConfig` permitted `/trees` and
   `/trees/**` but **no controller served them**, so the public Tree Species
   page, the admin Tree Species page and the admin Master Inventory page all
   received 404 (and the officer slot dialog silently showed an empty species
   list). `controller/TreeSpeciesController.java` now provides:
   - `GET /trees` → active species only (anonymous, powers `/trees`,
     master inventory and the officer slot dialog)
   - `GET /trees/all` → every species incl. inactive, **MCD staff only**
     (powers the admin Tree Species screen). `/trees/**` is publicly permitted
     at the URL level, so this endpoint enforces the staff rule itself and
     answers **401** for anonymous callers.

5. **Dates sent to the API are local, never UTC.** `core/util/date.util.ts`
   (`toLocalDate`, `todayLocalDate`, `startOfToday`, `parseLocalDate`) must be
   used for every `YYYY-MM-DD` value. `Date.prototype.toISOString()` converts
   to UTC, which on an IST clock sent the *previous* day — the park-detail page
   asked for slots one day before the chosen date, the officer slot form saved
   a slot one day early and plantation records were dated a day behind.

6. **Google sign-in — IMPLEMENTED, opt-in via `GOOGLE_CLIENT_ID`.**
   The old button navigated the browser to
   `GET /auth/oauth2/authorization/google`, a URL **no backend endpoint
   served** → a raw Spring **404** page (that is what "OAuth2 is not working"
   looked like). It is now a real flow:

   | Step | Where |
   |------|-------|
   | `GET /auth/oauth2/google/config` (anonymous) → `{ enabled, clientId }` | `AuthController`, backed by the `oauth2.google-client-id` property |
   | UI renders the official Google button **only** when `enabled && clientId`, otherwise it shows "not configured" + what to set (never a dead link) | `core/auth/google-sign-in.service.ts`, `login.component.ts` |
   | Google Identity Services script is injected **only** in the enabled case — nothing external loads otherwise | `GoogleSignInService.loadScript()` |
   | `POST /auth/oauth2/google { credential }` → verify the ID token (Google's signature/expiry + **audience = our client id** + verified e-mail), then issue the same JWT a password login gets | `GoogleTokenVerifier`, `AuthService.loginWithGoogle` |
   | New Google account → created as a citizen with a random, unusable local password hash; a staff e-mail is rejected (staff accounts are provisioned, not self-served) | `AuthService` |

   Enable it by creating an OAuth2 **"Web application"** client in the Google
   Cloud console (authorised JavaScript origin = the Angular origin), then
   `export GOOGLE_CLIENT_ID=<your public client id>` and restart the backend.
   No value is hardcoded anywhere: unset → `POST` answers **501** with that
   exact instruction and the UI shows it. The ID token is a bearer credential,
   so it is never stored, never logged and never echoed into an error message
   (only HTTP status codes are logged for a rejected token — Google puts the
   token in the request URI).

### Required backend environment variables
| Variable | Purpose |
|----------|---------|
| `JWT_SIGN_KEY` | JWT signing key (64-char hex) — **required, no default** |
| `JWT_ENCRYPT_KEY` | JWT encryption key (64-char hex) — **required, no default** |
| `PG_AES_KEY` | Payment-gateway payload encryption key (16/24/32 chars) — **required, no default** |
| `DB_USER`, `DB_PASSWORD` | Database credentials — **required, no default** |
| `CORS_ALLOWED_ORIGINS` | Optional, comma-separated origins (defaults to the Angular dev server) |
| `JWT_ACCESS_EXPIRY_MS` | Optional, access-token lifetime (default 1 hour) |
| `JWT_REFRESH_EXPIRY_MS` | Optional, refresh-token lifetime (default 7 days) |
| `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET` | Payment keys (when payments enabled) |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USER`, `MAIL_PASS` | SMTP — enables the forgot-password e-mail flow |
| `GOOGLE_CLIENT_ID` | Public OAuth2 client id — **enables Google sign-in** (unset ⇒ 501 + explained UI) |
| `PG_RETURN_URL` | Optional — where the gateway sends the citizen back after paying (e.g. `http://localhost:4200/citizen/payment/result`); unset ⇒ the existing MCD staging callback is used |
| `PG_CANCEL_URL` | Optional — same, for the gateway's cancel path |
| `PG_TEST_MODE` | Optional, default `false` — **TEST ONLY, never enable in production**: sends `payment/init` to the local `/citizen/payment/test` page and enables `POST /bookings/{id}/payment/test-payload` (404 otherwise) |
| `PG_FRONTEND_BASE_URL` | Optional, default `http://localhost:4200` — where the test-gateway page lives, used only when `PG_TEST_MODE=true` |

> Hardcoded secrets (JWT keys, a payment-gateway AES key, an email
> app-password and `${DB_*:postgres}` fallbacks) were found in
> `application.yml` and **removed**; every value above now comes from the
> environment only. Delete stale build copies that still contain the old
> literals (`build/classes/application.yml`, `target/**/application.yml`) —
> they are regenerated from `src/main/resources/application.yml` on the next
> build.
