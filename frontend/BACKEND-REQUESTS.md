# Backend requests

What the frontend needs that the API does not offer today. Rule 2 of `../AGENTS.md`: never
fake the missing data — build the rest of the screen and show an honest empty state for the
missing part. Entry format:

```
## <date> — <screen, spec id>
Needs: <what data or action>
Today: <what the API offers and why it is not enough>
Proposal: <METHOD /path — request → response>
Meanwhile: <what the screen shows>
```

## 2026-10-02 — Notifications (S-09), the bell of the top bar

Needs: a readable line per notification — what happened and to what ("Grade published:
Dasturlash, JN-1"), and where to go on click.
Today: `NotificationView` carries `type`, `createdAt`, `readAt` and `payloadJson` (a JSON string
whose keys differ by type and are not in the contract). The list can only say "Grade published".
Proposal: add to `NotificationView` the fields `title`, `body` (already localized by the
`locale` parameter) and a typed `target {kind, id, courseId?}`; or document the keys of
`payloadJson` per type in the contract.
Meanwhile: the bell shows a text per notification type and the time; rows do not link anywhere.
