import { Injectable, signal } from '@angular/core';

export interface GlobalNotification {
  id: string;
  message: string;
  type: 'complaint' | 'gift' | 'insurance-warning' | 'insurance-critical';
  actionLabel?: string;
  actionRoute?: string;
}

@Injectable({
  providedIn: 'root'
})
export class GlobalNotificationService {
  notifications = signal<GlobalNotification[]>([]);

  addNotification(notif: GlobalNotification) {
    this.notifications.update(list => {
      // Avoid exact message duplicates
      if (list.some(n => n.message === notif.message)) return list;
      return [notif, ...list];
    });
  }

  removeNotification(id: string) {
    this.notifications.update(list => list.filter(n => n.id !== id));
  }

  clearAll() {
    this.notifications.set([]);
  }
}
