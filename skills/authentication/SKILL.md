---
name: authentication
description: Token, password and login rules. Load before touching anything under iam auth.
---
# Authentication rules (ADR-003)

## The model
Access = JWT HS256, 15 min, claims `sub`/`roles`/`perms`/`jti`/`iss`/`iat`/`exp`, never stored.
Refresh = 256-bit random opaque, **SHA-256 hash only** in `refresh_tokens`, 30 days, single use,
rotated on every refresh (`replaced_by_id` chain).

## Non-negotiable
1. Never store a plaintext password or a plaintext refresh token. Never log either, or a JWT.
2. `PasswordEncoder` (BCrypt 12) is the only hashing path. Do not hand-roll.
3. Login returns exactly one error for every failure mode: `AUTH_INVALID_CREDENTIALS`.
   No "user not found", no "account locked", no "wrong password" to an unauthenticated caller.
   Always run the hash comparison (dummy hash for a missing user) so timing does not leak.
4. Presenting a revoked refresh token ⇒ revoke **the whole family** for that user +
   `TOKEN_REUSE_DETECTED` audit event. This is theft detection; do not "fix" it by returning
   a fresh token.
5. Check `UserStatus` on login **and** refresh. A suspended user must lose access within one
   access-token lifetime.
6. Failed-attempt counter + lockout (`AUTH_MAX_FAILED_ATTEMPTS`, `AUTH_LOCKOUT_DURATION`) and
   a per-IP rate limit on `/auth/*`. Reset the counter on success.
7. `JWT_SECRET` from env, ≥32 bytes, validated at startup (`JwtProperties` fails fast).
   A weak/missing secret must prevent boot, not warn.
8. Audit every auth event (`LOGIN_SUCCESS`, `LOGIN_FAILURE`, `ACCOUNT_LOCKED`, `TOKEN_REFRESH`,
   `TOKEN_REUSE_DETECTED`, `LOGOUT`, `PASSWORD_CHANGED`) with IP + user agent, never credentials.
9. Password change revokes all refresh tokens for that user.
10. Stateless: `SessionCreationPolicy.STATELESS`, CSRF disabled **because** the token is in a
    header. If anyone moves the token to a cookie, CSRF protection becomes mandatory — update
    ADR-003 in the same change.

## Files
`iam/app/auth/` — `AuthenticationService`, `TokenService`, `RefreshTokenService`,
`LoginAttemptService`. `iam/api/AuthController`. `common/security/` — `JwtAuthenticationFilter`,
`SecurityConfig`, `RateLimitFilter`.

## Dev credentials
`db/seed/` only, obviously fake (`Dev!Pass123`), documented as dev-only in the README.
Never a realistic-looking credential; never a credential in `db/migration/`.
