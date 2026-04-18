# Quick Reference Guide - Feedback Module

## 🚀 Quick Start

### View the Module
```
http://localhost:4200/dashboard/feedback
```

### Access Different Sections
1. **Complaints List** → Main dashboard `/dashboard/feedback`
2. **New Complaint** → Click "New Complaint" button
3. **Feedback** → Click "Feedback" tab
4. **Responses** → Click "Responses" tab (ADMIN/AGENT)
5. **Chatbot** → Click "Chatbot" tab
6. **Statistics** → Click "Stats" tab (ADMIN only)

---

## 📋 Component Cheat Sheet

### ComplaintFormComponent
```typescript
// Create mode
http://localhost:4200/dashboard/feedback/complaint/add

// Edit mode
http://localhost:4200/dashboard/feedback/complaint/1

// Toggle AI auto-detection
useAI = true  // Auto-fills category & priority
```

### FeedbackFormComponent
```typescript
// Create mode
http://localhost:4200/dashboard/feedback/feedback

// Edit mode  
http://localhost:4200/dashboard/feedback/feedback/1

// Star rating
setRating(3)  // Sets 3 out of 5 stars
```

### ResponseListComponent
```typescript
// View all responses
http://localhost:4200/dashboard/feedback/responses

// Actions available (ADMIN/AGENT only)
improveWithAI(responseId)
goToEdit(responseId)
delete(responseId)
```

### ResponseFormComponent
```typescript
// Create response
http://localhost:4200/dashboard/feedback/response/add

// Edit response
http://localhost:4200/dashboard/feedback/response/1

// Auto-filled fields
response.responderName = currentUser.username
response.responderRole = currentUser.roles[0]
```

### ChatbotComponent
```typescript
// Chat interface
http://localhost:4200/dashboard/feedback/chatbot

// Send message
sendMessage()  // Triggered by button or Enter key

// Clear history
clearChat()  // Asks for confirmation
```

### FeedbackStatsComponent
```typescript
// Statistics dashboard (ADMIN only)
http://localhost:4200/dashboard/feedback/stats

// Methods
getTotalComplaints(): number
getOpenComplaints(): number  
getResolvedComplaints(): number
getCategoryValue(category: string): number
```

---

## 🎨 UI Component Usage

### Import in Your Component
```typescript
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';
```

### Button Variants
```html
<app-forsa-button variant="default">Primary</app-forsa-button>
<app-forsa-button variant="outline">Outline</app-forsa-button>
<app-forsa-button variant="ghost">Ghost</app-forsa-button>
<app-forsa-button variant="destructive">Delete</app-forsa-button>
<app-forsa-button variant="secondary">Secondary</app-forsa-button>
```

### Button Sizes
```html
<app-forsa-button size="sm">Small</app-forsa-button>
<app-forsa-button size="md">Medium</app-forsa-button>
<app-forsa-button size="lg">Large</app-forsa-button>
<app-forsa-button size="icon">Icon Only</app-forsa-button>
```

### Badge Tones
```html
<app-forsa-badge tone="default">Default</app-forsa-badge>
<app-forsa-badge tone="success">Success</app-forsa-badge>
<app-forsa-badge tone="warning">Warning</app-forsa-badge>
<app-forsa-badge tone="danger">Danger</app-forsa-badge>
<app-forsa-badge tone="info">Info</app-forsa-badge>
<app-forsa-badge tone="muted">Muted</app-forsa-badge>
```

### Icons
```html
<app-forsa-icon name="alert-circle" [size]="24" />
<app-forsa-icon name="check-circle-2" [size]="24" />
<app-forsa-icon name="x-circle" [size]="24" />
<app-forsa-icon name="clock" [size]="24" />
<app-forsa-icon name="trash" [size]="16" />
<app-forsa-icon name="edit-2" [size]="16" />
<app-forsa-icon name="plus" [size]="16" />
<app-forsa-icon name="sparkles" [size]="16" />
<app-forsa-icon name="star" [size]="32" />
<app-forsa-icon name="send" [size]="20" />
<app-forsa-icon name="loader" [size]="32" />
```

### Form Controls
```html
<!-- Text Input -->
<input forsaInput type="text" [(ngModel)]="data.field" />

<!-- Text Area -->
<textarea forsaInput [(ngModel)]="data.field"></textarea>

<!-- Select -->
<select forsaInput [(ngModel)]="data.field">
  <option value="opt1">Option 1</option>
  <option value="opt2">Option 2</option>
</select>

<!-- Checkbox -->
<input type="checkbox" [(ngModel)]="data.field" />
```

---

## 🔐 Authentication Checks

### In Component
```typescript
get isAdmin(): boolean {
  return this.auth.currentUser()?.roles?.includes('ROLE_ADMIN') ?? false;
}

get isAgent(): boolean {
  return this.auth.currentUser()?.roles?.includes('ROLE_AGENT') ?? false;
}

get isClient(): boolean {
  return this.auth.currentUser()?.roles?.includes('ROLE_CLIENT') ?? false;
}

get isAdminOrAgent(): boolean {
  return this.isAdmin || this.isAgent;
}
```

### In Template
```html
@if (isAdmin) {
  <!-- Admin only content -->
}

@if (isAdminOrAgent) {
  <!-- Admin or Agent content -->
}

@if (isClient) {
  <!-- Client only content -->
}
```

---

## 📡 Service Method Quick Reference

### ComplaintService
```typescript
complaintService.getAll()                    // Get all complaints
complaintService.getById(id)                 // Get one complaint
complaintService.add(complaint)              // Create new
complaintService.addWithAI(complaint)        // Create with AI
complaintService.update(complaint)           // Update
complaintService.delete(id)                  // Delete
complaintService.close(id)                   // Close complaint
complaintService.getAIResponse(id)           // Get AI suggestion
complaintService.getSummaryReport()          // Get stats
complaintService.getTrendsLastMonths(6)      // Trends
complaintService.getStatsByCategory()        // Category breakdown
complaintService.getStatsByPriority()        // Priority breakdown
complaintService.addResponse(id, msg, role, name)  // Add response
```

### FeedbackService
```typescript
feedbackService.getAll()                     // Get all feedback
feedbackService.getById(id)                  // Get one
feedbackService.add(feedback)                // Create
feedbackService.addWithAI(feedback)          // Create with AI
feedbackService.update(feedback)             // Update
feedbackService.delete(id)                   // Delete
feedbackService.getSummaryReport()           // Get stats
feedbackService.getTrendsReport(6)           // Trends
feedbackService.getAvgRatingByCategory()     // Ratings by category
```

### ResponseService
```typescript
responseService.getAll()                     // Get all responses
responseService.getById(id)                  // Get one
responseService.add(response)                // Create
responseService.update(response)             // Update
responseService.delete(id)                   // Delete
responseService.improveWithAI(id)            // AI improvement
```

### ChatbotService
```typescript
chatbotService.ask(message)                  // Send message
// Returns: { answer: string }
```

---

## 🎯 Common Patterns

### Subscribe to Service
```typescript
this.complaintService.getAll().subscribe({
  next: (data: ComplaintBackend[]) => {
    this.items = data;
    this.loading = false;
  },
  error: () => {
    this.error = 'Error loading data';
    this.loading = false;
  }
});
```

### Navigate After Action
```typescript
this.complaintService.add(complaint).subscribe({
  next: () => {
    this.router.navigate(['/dashboard/feedback']);
  },
  error: () => {
    this.error = 'Error creating complaint';
  }
});
```

### Show/Hide Based on Condition
```html
@if (loading) {
  <div class="spinner">Loading...</div>
}

@if (error) {
  <div class="error">{{ error }}</div>
}

@if (!loading && items.length === 0) {
  <div class="empty">No items found</div>
}
```

### Loop Through Items
```html
@for (item of items; track item.id) {
  <div>{{ item.name }}</div>
}
```

---

## 📊 Data Models

### Complaint
```typescript
{
  id?: number;
  subject: string;           // min 5, max 200
  description: string;       // min 10, max 1000
  category?: Category;       // TECHNICAL|FINANCE|SUPPORT|FRAUD|ACCOUNT|CREDIT|OTHER
  status?: ComplaintStatus;  // OPEN|IN_PROGRESS|RESOLVED|CLOSED|REJECTED
  priority?: Priority;       // LOW|MEDIUM|HIGH|CRITICAL
  createdAt?: string;
}
```

### Feedback
```typescript
{
  id?: number;
  rating: number;           // 1-5
  comment?: string;         // max 500
  satisfactionLevel?: SatisfactionLevel;  // VERY_SATISFIED|SATISFIED|NEUTRAL|DISSATISFIED|VERY_DISSATISFIED
  isAnonymous: boolean;
  createdAt?: string;
}
```

### Response
```typescript
{
  id?: number;
  message: string;          // min 10, max 1000
  responderRole: string;    // max 50
  responderName: string;    // min 2, max 100
  responseStatus?: ResponseStatus;  // PENDING|PROCESSED|SENT|FAILED
  responseDate?: string;
}
```

### ChatMessage
```typescript
{
  role: 'user' | 'bot';
  content: string;
  timestamp?: Date;
}
```

---

## 🎨 Status Colors

### Complaint Status
- **OPEN** → Blue (#3b82f6) - alert-circle
- **IN_PROGRESS** → Amber (#f59e0b) - clock
- **RESOLVED** → Green (#10b981) - check-circle-2
- **CLOSED** → Red (#ef4444) - x-circle
- **REJECTED** → Gray (#6b7280) - ban

### Priority Colors
- **CRITICAL** → Red (#ef4444) - danger
- **HIGH** → Red (#ef4444) - danger
- **MEDIUM** → Amber (#f59e0b) - warning
- **LOW** → Blue (#3b82f6) - info

### Response Status
- **PENDING** → Amber (#f59e0b)
- **PROCESSED** → Blue (#60a5fa)
- **SENT** → Green (#10b981)
- **FAILED** → Red (#ef4444)

---

## 🔗 Route Parameters

### URL with ID
```
/dashboard/feedback/complaint/:id        → complaint-form.component
/dashboard/feedback/feedback/:id         → feedback-form.component
/dashboard/feedback/response/:id         → response-form.component
```

### Get ID in Component
```typescript
this.route.snapshot.paramMap.get('id')  // Returns string or null
```

---

## ⌨️ Keyboard Shortcuts

### In Forms
- `Enter` - Submit (if button focused)
- `Escape` - Cancel (if implemented)

### In Chat
- `Enter` - Send message
- `Shift+Enter` - New line in textarea

---

## 🐛 Troubleshooting

### Buttons Not Responding
✅ Check if `(clicked)` event is bound
✅ Verify handler function exists
✅ Check for `[disabled]="true"`

### Data Not Loading
✅ Check network tab for API calls
✅ Verify token in localStorage
✅ Check console for errors
✅ Confirm backend URL is correct

### Routing Issues
✅ Check app.routes.ts for correct paths
✅ Verify router outlet exists in layout
✅ Confirm component is exported with standalone: true
✅ Check lazy loading syntax

### Form Not Binding
✅ Verify `[(ngModel)]="field"` syntax
✅ Check field exists on component property
✅ Confirm FormsModule is imported
✅ Verify input has `forsaInput` directive

---

## 📚 Additional Resources

1. **Full Documentation** → `IMPLEMENTATION_GUIDE.md`
2. **Statistics Methods** → `STATISTIQUES_EXPLICATION.md`
3. **File Listing** → `FILES_SUMMARY.md`
4. **Angular Docs** → https://angular.io/
5. **RxJS Docs** → https://rxjs.dev/

---

**Quick Reference v1.0**  
Updated: 2026-04-18
