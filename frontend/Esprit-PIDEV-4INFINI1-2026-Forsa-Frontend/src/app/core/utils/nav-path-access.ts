/**
 * Whether a nav link is allowed by the paths returned from the API.
 *
 * - Each allowed path may match **exactly** or as a **prefix** for deeper routes
 *   (e.g. {@code /dashboard/insurance} allows {@code /dashboard/insurance/products}).
 * - {@code /dashboard} alone means **only** the dashboard home, not every child route
 *   (otherwise it would incorrectly allow Feedback, AI, etc.).
 */
export function isNavPathAllowed(href: string, allowedPaths: string[] | null | undefined): boolean {
  if (!allowedPaths || allowedPaths.length === 0) {
    return true;
  }
  const path = normalizePath(href.split('?')[0] ?? href);
  for (const raw of allowedPaths) {
    const base = normalizePath(raw);
    if (!base) {
      continue;
    }
    if (base === '/dashboard') {
      if (path === '/dashboard') {
        return true;
      }
      continue;
    }
    if (path === base || path.startsWith(`${base}/`)) {
      return true;
    }
  }
  return false;
}

function normalizePath(p: string): string {
  if (!p) {
    return p;
  }
  return p.length > 1 && p.endsWith('/') ? p.slice(0, -1) : p;
}
