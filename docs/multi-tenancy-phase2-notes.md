# Multi-Tenancy Phase 2 Notes

Phase 1 adds the `Shop` tenant root plus nullable `shop_id` references on tenant-owned tables and backfills legacy single-shop data into one default shop.

The following areas still need Phase 2 work before the app can safely operate as a true shared multi-tenant deployment:

## Authentication and Login
- Replace global username lookup with `shop + username` authentication.
- Resolve tenant context before authentication, likely by shop slug or subdomain.
- Replace global `/setup` semantics with shop onboarding instead of deployment-wide first-user setup.

## Repository Filtering
- Add tenant-aware repository methods such as `findByIdAndShopId(...)`.
- Stop using global `findAll()`, `findById()`, and global count/search methods for tenant-owned tables.
- Add service-layer safeguards so linked records always belong to the same shop.

## Uploads
- Separate uploaded files by shop path or object key.
- Replace the current shared public `/uploads/**` assumption with tenant-safe access rules.

## Public Tracking
- Bind public tracking responses to the correct shop context and branding.
- Revisit whether tracking URLs should carry both shop slug and token.

## Settings and Shop Context
- Migrate from the current global `ShopSettings` singleton behavior to tenant-scoped settings.
- Update global model attributes and branding lookup to read from the active shop instead of one global row.
