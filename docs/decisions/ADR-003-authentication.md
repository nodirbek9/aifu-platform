# ADR-003 — Authentication: JWT access + rotating opaque refresh token

**Status:** Accepted

## Context
Login + password (brief §8). Future clients: browser SPA, possibly mobile. Need logout,
expiry, refresh, account status, brute-force resistance, audit.

## Options
1. **Server sessions + cookie.** Trivial revocation, needs sticky sessions or shared session
   store (⇒ Redis, which we are avoiding), and CSRF handling. Awkward for mobile.
2. **Long-lived JWT only.** Stateless but unrevocable. A stolen token is valid until expiry;
   "logout" becomes a lie. Rejected.
3. **Short JWT access + long opaque refresh token, stored hashed, rotated on use.**

## Decision
**Option 3.**
- **Access token**: JWT, HS256, TTL **15 min**, claims `sub` (user id), `roles`, `perms`,
  `jti`, `iss`, `iat`, `exp`. Sent as `Authorization: Bearer`. Never persisted server-side.
- **Refresh token**: 256-bit random opaque string, **only a SHA-256 hash is stored**
  (`refresh_tokens.token_hash`), TTL 30 days, **single use**. Refreshing revokes the old row
  and links `replaced_by_id`.
- **Reuse detection**: presenting an already-revoked refresh token revokes the **entire token
  family** for that user and writes an audit event. That turns token theft into a detectable,
  contained event.
- **Logout** = revoke the presented refresh token (`POST /auth/logout`);
  **logout-all** = revoke every active token for the user. Access tokens still die naturally
  within 15 min — accepted, and the reason the access TTL is short.
- **Account status** (`ACTIVE`/`PENDING`/`SUSPENDED`/`DISABLED`) is checked on login *and* on
  refresh, so suspending a user takes effect within one access-token lifetime.
- **Passwords**: BCrypt cost 12. `PasswordEncoder` is the only hashing entry point.
  Policy: ≥10 chars, not in a small common-password denylist, validated by
  `@StrongPassword`. Argon2id is a one-line swap if AIFU security policy demands it.
- **Brute force**: per-account counter + lockout (5 failures ⇒ 15 min, configurable) **and**
  a per-IP rate limit on `/auth/*` (token bucket, in-process). Both audited.
- **Secure errors**: login always answers `AUTH_INVALID_CREDENTIALS` — never "no such user"
  vs "wrong password", never "locked" to an unauthenticated caller. Timing is equalized by
  always running a hash comparison.
- **Audit**: `LOGIN_SUCCESS`, `LOGIN_FAILURE`, `ACCOUNT_LOCKED`, `TOKEN_REFRESH`,
  `TOKEN_REUSE_DETECTED`, `LOGOUT`, `PASSWORD_CHANGED` in `audit_log`.

CSRF is not applicable: tokens travel in an `Authorization` header, not a cookie, and the API
is `SessionCreationPolicy.STATELESS`. **If a future frontend switches to cookie storage, CSRF
protection becomes mandatory** — noted in `docs/security/README.md`.

## Consequences
Stateless request path (no session store), real revocation via the refresh table, theft
detection. Costs one DB write per refresh (~1/15min/user — nothing) and a scheduled purge of
expired rows. Access tokens remain valid up to 15 min after a ban.

## Revisit when
AIFU wants SSO/HEMIS federation (⇒ OIDC via Spring Authorization Server, additive) or the
frontend needs cookie-based auth.
