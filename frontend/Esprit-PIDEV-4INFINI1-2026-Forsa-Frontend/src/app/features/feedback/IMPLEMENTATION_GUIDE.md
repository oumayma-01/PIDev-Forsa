# Feedback & Complaint Management System - Frontend Documentation

## Overview
This document describes the complete implementation of the Complaint/Feedback/Response/Chatbot Management Module for the Forsa fintech Angular 18 frontend.

## Architecture

### Services Layer (`src/app/core/data/`)

#### 1. **ComplaintService** (`complaint.service.ts`)
Handles all complaint-related API operations:

```typescript
// GET endpoints
getAll(): Observable<ComplaintBackend[]>           // /retrieve-all-complaints
getById(id: number): Observable<ComplaintBackend>  // /retrieve-complaint/{id}

// POST endpoints
add(complaint): Observable<ComplaintBackend>       // /add-complaint
addWithAI(complaint): Observable<ComplaintBackend> // /add-complaint-ai (auto category+priority)

// PUT endpoints
update(complaint): Observable<ComplaintBackend>    // /modify-complaint

// DELETE endpoints
delete(id: number): Observable<void>               // /remove-complaint/{id}
close(id: number): Observable<void>                // /{id}/close

// AI & Analytics endpoints
getAIResponse(id: number): Observable<{ response: string }>    // /{id}/ai-response
getSummaryReport(): Observable<any>                // /summary-report
getTrendsLastMonths(months): Observable<any>      // /trends-last-months?months=6
getStatsByCategory(): Observable<any>              // /stats-by-category
getStatsByPriority(): Observable<any>              // /stats-by-priority
addResponse(...): Observable<any>                  // /{id}/responses
```

**Key Features:**
- Type-safe Observable responses
- Full CRUD operations
- AI integration for auto-categorization
- Statistical aggregation methods

#### 2. **FeedbackService** (`feedback.service.ts`)
Manages user feedback submissions and analysis:

```typescript
// GET endpoints
getAll(): Observable<Feedback[]>              // /retrieve-all-feedbacks
getById(id: number): Observable<Feedback>    // /retrieve-feedback/{id}

// POST endpoints
add(feedback): Observable<Feedback>           // /add-feedback
addWithAI(feedback): Observable<Feedback>    // /add-feedback-ai (auto satisfactionLevel)

// PUT endpoints
update(feedback): Observable<Feedback>        // /modify-feedback

// DELETE endpoints
delete(id: number): Observable<void>          // /remove-feedback/{id}

// Analytics endpoints
getSummaryReport(): Observable<any>           // /report/summary
getTrendsReport(months): Observable<any>     // /report/trends?months=6
getAvgRatingByCategory(): Observable<any>    // /report/avg-rating-by-category
```

**Key Features:**
- Star rating system (1-5)
- Anonymous submission support
- AI-powered satisfaction level detection
- Trend analysis

#### 3. **ResponseService** (`response.service.ts`)
Manages complaint responses from ADMIN/AGENT:

```typescript
// GET endpoints
getAll(): Observable<ComplaintResponse[]>     // /retrieve-all-responses
getById(id: number): Observable<ComplaintResponse>  // /retrieve-response/{id}

// POST endpoints
add(response): Observable<ComplaintResponse>  // /add-response

// PUT endpoints
update(response): Observable<ComplaintResponse> // /modify-response
improveWithAI(id: number): Observable<ComplaintResponse>  // /improve-response-ai/{id}

// DELETE endpoints
delete(id: number): Observable<void>          // /remove-response/{id}
```

**Key Features:**
- Response tracking with status (PENDING, PROCESSED, SENT, FAILED)
- AI enhancement button for responses
- Responder role & name tracking

#### 4. **ChatbotService** (`chatbot.service.ts`)
Real-time chatbot interaction:

```typescript
ask(message: string): Observable<ChatbotResponse>  // /ask
// Returns: { answer: string }
```

**Key Features:**
- Single endpoint design for simplicity
- Request/response model

---

## Components Layer (`src/app/features/feedback/`)

### 1. **feedback-list** (Main Dashboard)
**Route:** `/dashboard/feedback`

**Features:**
- Lists all complaints
- Shows complaint status with color-coded icons:
  - OPEN (blue) → `alert-circle`
  - IN_PROGRESS (amber) → `clock`
  - RESOLVED (green) → `check-circle-2`
  - CLOSED (red) → `x-circle`
  - REJECTED (gray) → `ban`
- Priority badges with tones
- Category and creation date display
- Action buttons:
  - **Edit** (visible to creator/ADMIN/AGENT)
  - **Close** (ADMIN/AGENT only)
  - **Delete** (ADMIN only)
- Navigation tabs to other modules (Feedback, Responses, Chatbot)
- Empty state handling
- Loading indicator
- Error handling

**Role-Based Access:**
```typescript
isClient: boolean     // ROLE_CLIENT
isAdmin: boolean      // ROLE_ADMIN
isAgent: boolean      // ROLE_AGENT
isAdminOrAgent: boolean
```

---

### 2. **complaint-form** (Create/Edit Complaints)
**Routes:** 
- `/dashboard/feedback/complaint/add` (Create)
- `/dashboard/feedback/complaint/:id` (Edit)

**Features:**
- Subject input (min 5 chars, max 200)
- Description textarea (min 10 chars, max 1000)
- Category dropdown (with AI auto-detection option)
- Priority dropdown (ADMIN/AGENT only in create mode)
- Status dropdown (edit mode only, ADMIN/AGENT only)
- **AI Toggle** - Auto-detects category & priority from text
- Form validation with error display
- Loading state during submission
- Cancel button returns to `/dashboard/feedback`

**Form States:**
- **Create Mode**: All fields editable, AI toggle visible, category/priority auto-filled if AI enabled
- **Edit Mode**: Status field visible for ADMIN/AGENT, AI toggle hidden, all fields editable

---

### 3. **feedback-form** (Submit Feedback)
**Routes:**
- `/dashboard/feedback/feedback` (Create)
- `/dashboard/feedback/feedback/:id` (Edit)

**Features:**
- **Star Rating System**: Interactive 1-5 star rating with visual feedback
- Comment textarea (max 500 chars) with character counter
- Satisfaction Level dropdown (hidden if AI enabled):
  - VERY_SATISFIED
  - SATISFIED
  - NEUTRAL
  - DISSATISFIED
  - VERY_DISSATISFIED
- Anonymous checkbox option
- **AI Toggle** - Auto-determines satisfaction level from comment
- Rating display (e.g., "3 / 5")
- Responsive design for mobile
- Character limit enforcement

**Visual Feedback:**
- Stars fill with gold color (#fbbf24) on selection
- Empty stars show in gray (#d1d5db)
- Hover effect (scale 1.15x)

---

### 4. **response-list** (View Responses)
**Route:** `/dashboard/feedback/responses`

**Features:**
- Lists all complaint responses
- Status indicators:
  - PENDING (amber clock icon)
  - PROCESSED (blue check icon)
  - SENT (green check-circle icon)
  - FAILED (red alert icon)
- Shows:
  - Responder name
  - Responder role
  - Full response message
  - Response date/time
  - Status badge
- Action buttons (ADMIN/AGENT only):
  - **Improve with AI** - AI enhancement button
  - **Edit** - Modify response
  - **Delete** - Remove response
- Add New Response button (ADMIN/AGENT only)
- Empty state handling
- Loading indicator

---

### 5. **response-form** (Create/Edit Responses)
**Routes:**
- `/dashboard/feedback/response/add` (Create)
- `/dashboard/feedback/response/:id` (Edit)

**Features:**
- Responder Name input (auto-filled with current user)
- Responder Role input (auto-filled with user's first role)
- Response Message textarea (min 10 chars, max 1000)
- Status dropdown (edit mode only)
- Auto-populated from current user data
- Form validation
- ADMIN/AGENT permission check
- Cancel button returns to `/dashboard/feedback/responses`

**Permission Guard:**
- Submit button disabled if user is not ADMIN/AGENT

---

### 6. **chatbot** (AI Chat Interface)
**Route:** `/dashboard/feedback/chatbot`

**Features:**
- Real-time chat interface
- Message display with timestamps
- User messages (right-aligned, blue background)
- Bot messages (left-aligned, gray background)
- **Typing Indicator**: Animated dots when bot is responding
- Auto-scroll to latest message
- Input field with send button
- Disabled state during loading
- Clear chat button (with confirmation)
- Empty message prevention
- Responsive design

**UI Components:**
- Message bubbles with rounded corners
- Time display (HH:mm format)
- Disabled input during loading
- Send button (icon-only with accessible label)
- Typing animation using CSS keyframes

**Auto-Features:**
- Enter key sends message
- Auto-focus on input
- Message history preservation
- Loading state management

---

### 7. **feedback-stats** (Statistics Dashboard)
**Route:** `/dashboard/feedback/stats` (ADMIN only)

**Complaint Metrics Cards:**
```
┌─────────────────────────────┐
│  Total Complaints  │  50     │
├─────────────────────────────┤
│  Open Complaints   │  15     │
├─────────────────────────────┤
│  Resolved Complaints│ 30     │
└─────────────────────────────┘
```

**Feedback Metrics Cards:**
```
┌─────────────────────────────┐
│  Total Feedback    │  120    │
├─────────────────────────────┤
│  Average Rating    │  4.2/5  │
└─────────────────────────────┘
```

**Statistical Tables:**

1. **Complaints by Category** - Progress bars showing distribution
   ```
   TECHNICAL    [████████░░░░░░░░░]  18
   FINANCE      [███████░░░░░░░░░░░]  14
   SUPPORT      [██████░░░░░░░░░░░░]  12
   ...
   ```

2. **Complaints by Priority** - Color-coded distribution
   ```
   CRITICAL     [█████░░░░░░░░░░░░░]  10
   HIGH         [████░░░░░░░░░░░░░░]  8
   MEDIUM       [██████░░░░░░░░░░░░]  18
   LOW          [████░░░░░░░░░░░░░░]  14
   ```

3. **Average Rating by Category** - Grid showing category-based ratings
   ```
   TECHNICAL: 4.3/5
   FINANCE:   3.8/5
   SUPPORT:   4.5/5
   ...
   ```

**Features:**
- Refresh button to reload data
- Real-time calculation of metrics
- Permission-based access (ADMIN only)
- Error messages for failed data loads
- Loading state during data fetch
- Responsive grid layout

---

## Statistical Methods Implementation

### Complaint Statistics

#### 1. **Summary Report** (`getSummaryReport()`)
**Data Structure:**
```javascript
{
  totalComplaints: number,       // Total count
  openComplaints: number,        // OPEN status count
  resolvedComplaints: number,    // RESOLVED status count
  closedComplaints: number,      // CLOSED status count
  rejectedComplaints: number,    // REJECTED status count
  avgResolutionTime: number,     // Days to resolve
  satisfactionScore: number      // Avg feedback rating for complaints
}
```

**Frontend Calculation:**
```typescript
getTotalComplaints(): number {
  return this.summaryReport?.totalComplaints ?? 0;
}

getOpenComplaints(): number {
  return this.summaryReport?.openComplaints ?? 0;
}

getResolvedComplaints(): number {
  return this.summaryReport?.resolvedComplaints ?? 0;
}
```

#### 2. **Trends Analysis** (`getTrendsLastMonths(months: number)`)
**Data Structure:**
```javascript
{
  month: string,          // "2026-01", "2026-02", etc.
  complaintsCount: number,
  resolvedCount: number,
  avgResolutionDays: number,
  satisfactionScore: number
}[]
```

**Time Series Processing:**
- Returned as array of monthly data points
- Used for trend visualization (last 6 months by default)
- Supports custom date ranges via `months` parameter

#### 3. **Category Statistics** (`getStatsByCategory()`)
**Data Structure:**
```javascript
{
  TECHNICAL: number,
  FINANCE: number,
  SUPPORT: number,
  FRAUD: number,
  ACCOUNT: number,
  CREDIT: number,
  OTHER: number
}
```

**Frontend Rendering:**
```typescript
getCategoryKeys(): string[] {
  return Object.keys(this.statsByCategory);
}

getCategoryValue(category: string): number {
  return this.statsByCategory?.[category] ?? 0;
}
```

**Visual Representation:**
- Progress bar width = (categoryCount / totalComplaints) * 100
- Color: Primary color (#3b82f6)

#### 4. **Priority Statistics** (`getStatsByPriority()`)
**Data Structure:**
```javascript
{
  CRITICAL: number,
  HIGH: number,
  MEDIUM: number,
  LOW: number
}
```

**Similar processing to Category Stats**

### Feedback Statistics

#### 1. **Feedback Summary** (`getSummaryReport()`)
**Data Structure:**
```javascript
{
  totalCount: number,        // Total feedback submissions
  averageRating: number,     // Mean rating (1-5)
  ratingDistribution: {      // Count by rating
    1: number,
    2: number,
    3: number,
    4: number,
    5: number
  },
  satisfactionDistribution: {
    VERY_SATISFIED: number,
    SATISFIED: number,
    NEUTRAL: number,
    DISSATISFIED: number,
    VERY_DISSATISFIED: number
  },
  anonymousCount: number     // Anonymous submissions
}
```

#### 2. **Feedback Trends** (`getTrendsReport(months: number)`)
**Data Structure:**
```javascript
{
  month: string,
  feedbackCount: number,
  averageRating: number,
  averageSatisfaction: string
}[]
```

#### 3. **Category-Based Ratings** (`getAvgRatingByCategory()`)
**Data Structure:**
```javascript
{
  TECHNICAL: 4.2,
  FINANCE: 3.8,
  SUPPORT: 4.5,
  FRAUD: 2.1,
  ACCOUNT: 4.0,
  CREDIT: 3.9,
  OTHER: 3.5
}
```

**Frontend Processing:**
```typescript
avgRatingByCategory[category] // Direct decimal value
// Format with pipe: | number: '1.1-1'  // e.g., 4.2
```

---

## Data Flow & State Management

### Signal-Based State (AuthService)
```typescript
currentUser = signal<CurrentUser | null>(null)
  .roles: string[]  // ['ROLE_ADMIN'], ['ROLE_CLIENT'], etc.
```

### Component State Patterns

**List Components:**
```typescript
items: ComplaintBackend[] = []
loading: boolean = false
error: string = ''

loadComplaints(): void {
  this.loading = true;
  this.complaintService.getAll().subscribe({
    next: (data) => { this.items = data; this.loading = false; },
    error: () => { this.error = '...'; this.loading = false; }
  });
}
```

**Form Components:**
```typescript
isEditMode: boolean = false
useAI: boolean = false
loading: boolean = false
error: string = ''

// Data structure mirrors backend entity
complaint: ComplaintBackend = { /* ... */ }
```

---

## Role-Based Access Control

### Permissions Matrix

| Feature | CLIENT | AGENT | ADMIN |
|---------|--------|-------|-------|
| View Complaints | Own only | All | All |
| Create Complaint | ✓ | ✓ | ✓ |
| Edit Complaint | Own | ✓ | ✓ |
| Delete Complaint | ✗ | ✗ | ✓ |
| Close Complaint | ✗ | ✓ | ✓ |
| Create Response | ✗ | ✓ | ✓ |
| Edit Response | ✗ | ✓ | ✓ |
| Delete Response | ✗ | ✓ | ✓ |
| View Stats | ✗ | ✓ | ✓ |
| Submit Feedback | ✓ | ✓ | ✓ |
| View Chatbot | ✓ | ✓ | ✓ |

### Implementation Pattern
```typescript
get isAdmin(): boolean {
  return this.auth.currentUser()?.roles?.includes('ROLE_ADMIN') ?? false;
}

@if (isAdmin) {
  <!-- Admin-only content -->
}
```

---

## HTTP Interceptor Integration

All services use Angular's `HttpClient` with automatic token injection via interceptor:

```typescript
private readonly http = inject(HttpClient);
private readonly baseUrl = `${environment.apiBaseUrl}/complaints`;

// Interceptor automatically adds:
// Authorization: Bearer <forsa_access_token>
```

---

## Error Handling Strategy

### Service Level
```typescript
add(complaint: ComplaintBackend): Observable<ComplaintBackend> {
  return this.http.post<ComplaintBackend>(
    `${this.baseUrl}/add-complaint`,
    complaint
    // HttpErrorResponse automatically propagated
  );
}
```

### Component Level
```typescript
save(): void {
  this.complaintService.add(this.complaint).subscribe({
    next: () => this.router.navigate(['/dashboard/feedback']),
    error: () => {
      this.error = 'Error submitting complaint';
      this.loading = false;
    }
  });
}
```

### UI Display
```html
@if (error) {
  <div class="form-error">{{ error }}</div>
}
```

---

## Routing Configuration

**Route Hierarchy:**
```
/dashboard/feedback (main list)
  ├── /complaint/add (create)
  ├── /complaint/:id (edit)
  ├── /feedback (create)
  ├── /feedback/:id (edit)
  ├── /responses (list)
  ├── /response/add (create)
  ├── /response/:id (edit)
  ├── /chatbot (chat interface)
  └── /stats (ADMIN only)
```

**Lazy Loading:**
- All components loaded on-demand (except main feedback-list)
- Routes use `loadComponent` pattern
- Admin routes use `canMatch: [adminGuard]`

---

## Styling & Design System

### CSS Variables (Forsa Design System)
```css
--color-primary         /* Primary action color */
--color-secondary       /* Secondary/muted color */
--color-background      /* Page background */
--color-card            /* Card background */
--color-foreground      /* Text color */
--color-muted-foreground /* Muted text */
--color-border          /* Border color */
--color-input-border    /* Form input border */

/* Status colors */
--color-destructive     /* Red/error */
--color-emerald-*       /* Green shades */
--color-amber-*         /* Yellow shades */
--color-blue-*          /* Blue shades */
```

### Component Styling Classes
- `.page-toolbar` - Header section with title and actions
- `.page-head` - Title and description
- `.form-group` - Form field container
- `.forsa-label` - Form label styling
- `.forsa-input` - Input/textarea styling
- `.card-pad-lg` - Card padding (24px)
- `.text-muted` - Muted text color
- `.form-error` - Error message styling
- `.form-actions` - Button group at form bottom

---

## Testing Considerations

### Mock Data
```typescript
// In feedback-stats.component.ts
mockSummary = {
  totalComplaints: 50,
  openComplaints: 15,
  resolvedComplaints: 30,
  // ...
};

mockCategoryStats = {
  TECHNICAL: 18,
  FINANCE: 14,
  // ...
};
```

### Test Scenarios
1. **Authentication**: Verify role-based visibility
2. **API Integration**: Test service calls and error handling
3. **Form Validation**: Check min/max length constraints
4. **Navigation**: Verify routing after actions
5. **Statistics**: Confirm calculations and display
6. **Accessibility**: Star rating, icon labels, semantic HTML

---

## Performance Optimizations

1. **Track Function in @for**
   ```html
   @for (item of items; track item.id) { ... }
   ```

2. **OnPush Change Detection** (can be added)
   ```typescript
   changeDetection: ChangeDetectionStrategy.OnPush
   ```

3. **Lazy Loading Routes**
   - Components load only when needed
   - Stats component restricted to ADMIN

4. **Observable Unsubscription**
   - Services use RxJS operators
   - Components clean up in ngOnDestroy (if needed)

---

## Future Enhancements

1. **Chart Library Integration** (Charts.js, NgxCharts)
   - Replace progress bars with actual charts
   - Pie charts for category distribution
   - Line graphs for trend analysis

2. **Export Functionality**
   - PDF/Excel export of statistics
   - CSV download of complaint lists

3. **Advanced Filtering**
   - Date range picker
   - Status multi-select
   - Priority filters

4. **Real-time Updates**
   - WebSocket integration for live stats
   - Push notifications for new complaints

5. **Bulk Operations**
   - Multi-select complaints
   - Batch status updates
   - Bulk delete with confirmation

6. **Custom Reports**
   - Date range selection
   - Report template builder
   - Scheduled report delivery

---

## Deployment Checklist

- [ ] Verify all imports in app.routes.ts
- [ ] Check environment.apiBaseUrl configuration
- [ ] Test authentication token injection
- [ ] Verify admin guard implementation
- [ ] Test all form validations
- [ ] Confirm error handling displays
- [ ] Check responsive design on mobile
- [ ] Verify accessibility features
- [ ] Test role-based visibility
- [ ] Confirm statistical calculations
- [ ] Load test with large datasets
- [ ] Cross-browser testing

---

## Support & Documentation

For questions or updates:
1. Review backend API documentation
2. Check Forsa UI component library
3. Refer to Angular 18 official docs
4. Validate data structures match backend models

---

**Version:** 1.0  
**Last Updated:** 2026-04-18  
**Author:** AI Assistant - GitHub Copilot
