# Frontend switch-over checklist

This is the exact checklist for turning the frontend from mocks to the real backend.

## 1) Environment variables
Set in the frontend project:

```env
VITE_USE_MOCKS=false
VITE_API_BASE_URL=http://localhost:8080/api
```

## 2) API contract
Ensure the frontend calls these endpoints:

- POST /api/users/identify
- POST /api/auth/request-otp
- POST /api/auth/verify-otp
- GET /api/crops
- GET /api/crops/{batchId}
- GET /api/crops/{batchId}/releases
- POST /api/reservations
- GET /api/reservations/{id}
- DELETE /api/reservations/{id}
- POST /api/payments/create-intent
- GET /api/payments/order/{id}
- POST /api/admin/batches/{batchId}/release/preview
- POST /api/admin/batches/{batchId}/release/confirm
- GET /api/admin/batches
- GET /api/admin/batches/{batchId}
- GET /api/admin/manifest
- GET /api/admin/audit

## 3) Auth flow
- User signs in by phone number
- Token returned in response
- Store it in memory/local storage
- Send it as: Authorization: Bearer <token>

## 4) Frontend route protection
- Customers should not access admin routes
- Admin-only routes should 403 or redirect if no admin role

## 5) Mock removal
After the backend is stable:
- set `VITE_USE_MOCKS=false`
- keep mock layer only as fallback for local demos
- remove the mock server from app boot if not needed

## 6) QA smoke flow
Run these end-to-end flows once the API is live:

1. Sign in as customer
2. View crop feed
3. Reserve a batch
4. View reservation in dashboard
5. Admin release preview
6. Confirm release
7. Create payment intent
8. Mark payment as settled
9. View manifest and audit log

## 7) Known backend assumptions
- Postgres must be running on localhost:5432 with database seedtoplate
- DB credentials are currently: postgres / postgres
- CORS is enabled for http://localhost:5173
- JWT secret is currently a dev placeholder
