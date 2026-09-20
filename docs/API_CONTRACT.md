# Seed & Plate API Contract

This contract is aligned to the frontend product logic and mock API behavior.

Base URL:
- /api

Authentication:
- JWT bearer token for authenticated routes
- Header: Authorization: Bearer <token>
- Public routes: auth + crop + identify

## 1) User identity and auth

### POST /api/users/identify
Request:
```json
{
  "phone": "9000000001",
  "fullName": "Priya Shah"
}
```

Response:
```json
{
  "id": "uuid",
  "phone": "9000000001",
  "fullName": "Priya Shah",
  "role": "CUSTOMER",
  "isAdmin": false,
  "token": "jwt-token"
}
```

### POST /api/auth/request-otp
Request:
```json
{
  "phone": "9000000001",
  "fullName": "Priya Shah"
}
```

Response:
```json
{
  "userId": "uuid",
  "phone": "9000000001",
  "fullName": "Priya Shah",
  "role": "CUSTOMER",
  "isAdmin": false,
  "token": "jwt-token"
}
```

### POST /api/auth/verify-otp
Request:
```json
{
  "phone": "9000000001",
  "otpCode": "123456"
}
```

Response:
```json
{
  "userId": "uuid",
  "phone": "9000000001",
  "fullName": "Priya Shah",
  "role": "CUSTOMER",
  "isAdmin": false,
  "token": "jwt-token"
}
```

## 2) Crop feed and detail

### GET /api/crops
Response:
```json
[
  {
    "id": "uuid",
    "cropName": "Rajapuri Turmeric",
    "farmName": "Wai Farm",
    "currentStage": "STORED_CURING",
    "description": "Cured and kept in the farm store.",
    "variety": "Rajapuri",
    "isChemicalFree": true,
    "actualYieldKg": 212,
    "availableYieldKg": 212,
    "releasedKg": 0,
    "estimatedPriceLowPerKg": 205,
    "estimatedPriceHighPerKg": 250,
    "finalRetailPricePerKg": null
  }
]
```

### GET /api/crops/{batchId}
Same shape as one item in the feed.

### GET /api/crops/{batchId}/releases
Response:
```json
[
  {
    "id": "uuid",
    "batchId": "uuid",
    "releasedKg": 150,
    "pricePerKg": 92,
    "releasedAt": "2026-09-18T10:00:00Z",
    "note": "Second tranche"
  }
]
```

## 3) Reservations

### POST /api/reservations
Request:
```json
{
  "batchId": "uuid",
  "reservedKg": 10,
  "expectedPriceLowPerKg": 205,
  "expectedPriceHighPerKg": 250
}
```

Response:
```json
{
  "id": "uuid",
  "batchId": "uuid",
  "userId": "uuid",
  "reservedKg": 10,
  "status": "PENDING_RELEASE",
  "pricePerKg": null,
  "expectedPriceLowPerKg": 205,
  "expectedPriceHighPerKg": 250,
  "cancelReason": null,
  "createdAt": "2026-09-18T10:00:00Z"
}
```

### GET /api/reservations/{id}
Same shape as above.

### DELETE /api/reservations/{id}
Response:
- 204 No Content

## 4) Payments

### POST /api/payments/create-intent
Request:
```json
{
  "reservationId": "uuid"
}
```

Response:
```json
{
  "id": "uuid",
  "reservationId": "uuid",
  "amount": 2500,
  "status": "PENDING",
  "providerRef": "mock-provider-uuid",
  "paymentLink": "/checkout/uuid/return?orderId=uuid"
}
```

### GET /api/payments/order/{orderId}
Response:
```json
{
  "id": "uuid",
  "reservationId": "uuid",
  "amount": 2500,
  "status": "PAID",
  "providerRef": "mock-provider-uuid",
  "paymentLink": "/checkout/uuid/return?orderId=uuid"
}
```

### POST /api/payments/{paymentId}/settle
Response:
```json
{
  "id": "uuid",
  "reservationId": "uuid",
  "amount": 2500,
  "status": "PAID",
  "providerRef": "mock-provider-uuid",
  "paymentLink": "/checkout/uuid/return?orderId=uuid"
}
```

## 5) Admin release preview and confirm

### GET /api/admin/batches
Response:
```json
[
  {
    "id": "uuid",
    "cropName": "Farm Onion",
    "farmName": "Wai Farm",
    "currentStage": "BATCH_RELEASED",
    "actualYieldKg": 1200,
    "availableYieldKg": 198,
    "releasedKg": 500,
    "estimatedPriceLowPerKg": 88,
    "estimatedPriceHighPerKg": 96,
    "queueDepth": 3
  }
]
```

### POST /api/admin/batches/{batchId}/release/preview
Request:
```json
{
  "mandiRate": 70,
  "releasedKg": 150,
  "pricePerKg": 96,
  "note": "Second release"
}
```

Response:
```json
{
  "batchId": "uuid",
  "availableKg": 198,
  "releasedKg": 150,
  "pricePerKg": 96,
  "marginPercent": 37.14,
  "reservationIds": ["uuid", "uuid"],
  "bandBreach": false,
  "message": "Preview looks valid and remains inside the band."
}
```

### POST /api/admin/batches/{batchId}/release/confirm
Same request as preview. Same response shape as preview.

Behavior:
- If pricePerKg > bandHigh: affected reservations are auto-cancelled and kg is restored.
- Otherwise: create BatchRelease, set batch releasedKg, decrement availableYieldKg, update reservations in FIFO slice to PAID_READY_FOR_PICKUP.

## 6) Manifest and dispatch

### GET /api/admin/manifest
Response:
```json
[
  {
    "id": "uuid",
    "batchId": "uuid",
    "zone": "Pune",
    "route": "Wai -> Pune",
    "loadKg": 150,
    "handoffStatus": "PLANNED",
    "createdAt": "2026-09-18T10:00:00Z"
  }
]
```

### POST /api/admin/manifest
Request:
```json
{
  "batchId": "uuid",
  "zone": "Pune",
  "route": "Wai -> Pune",
  "loadKg": 150
}
```

Response:
```json
{
  "id": "uuid",
  "batchId": "uuid",
  "zone": "Pune",
  "route": "Wai -> Pune",
  "loadKg": 150,
  "handoffStatus": "PLANNED",
  "createdAt": "2026-09-18T10:00:00Z"
}
```

### PUT /api/admin/manifest/{manifestId}/status/{status}
Allowed values:
- PLANNED
- LOADED
- DISPATCHED
- DELIVERED

## 7) Audit trail

### GET /api/admin/audit
Response:
```json
[
  {
    "id": "uuid",
    "entityType": "RESERVATION",
    "entityId": "uuid",
    "eventType": "RELEASE_CONFIRMED",
    "message": "Reservation moved to PAID_READY_FOR_PICKUP",
    "createdAt": "2026-09-18T10:00:00Z"
  }
]
```

## 8) Status enums

ReservationStatus:
- PENDING_RELEASE
- PAID_READY_FOR_PICKUP
- CANCELLED
- FULFILLED

CropStage:
- GROWING
- HARVESTED
- STORED_CURING
- BATCH_RELEASED
- DISPATCHED
- COMPLETE

ManifestStatus:
- PLANNED
- LOADED
- DISPATCHED
- DELIVERED

## 9) Business rules to enforce
- FIFO allocation is strict on release preview and confirm.
- Price band is shown to the customer and enforced on release.
- If price exceeds the band high, affected reservations are auto-cancelled.
- Price is set per tranche on the day it leaves the farm store.
- Delivery is baked into the price and is not a charged line item.
- Admin release and manifest endpoints are JWT-protected and require ROLE_ADMIN.
