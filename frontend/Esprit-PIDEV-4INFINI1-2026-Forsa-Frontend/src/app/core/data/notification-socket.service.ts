import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs.min.js';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { ComplaintNotification } from './complaint-notification.service';
import { AuthService } from '../services/auth.service';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class NotificationSocketService {
  private client: Client | null = null;
  private connectedUserId: number | null = null;
  private notificationSubject = new Subject<ComplaintNotification>();
  private readonly seenNotificationIds = new Set<number>();
  private readonly connectionStateSubject = new BehaviorSubject<boolean>(false);

  constructor(private readonly authService: AuthService) {}

  connect(userIdOverride?: number): void {
    const rawUserId = userIdOverride ?? this.authService.currentUser()?.id;
    const userId = Number(rawUserId);
    if (!Number.isFinite(userId) || userId <= 0) return;
    if (this.client?.active && this.connectedUserId === userId) return;

    if (this.client?.active && this.connectedUserId !== userId) {
      this.disconnect();
    }

    this.client = new Client({
      webSocketFactory: () => new SockJS(`${environment.apiBaseUrl.replace('/api', '')}/ws`, undefined, { withCredentials: false }),
      reconnectDelay: 5000,
    });

    this.client.onConnect = () => {
      this.connectedUserId = userId;
      this.connectionStateSubject.next(true);
      this.client?.subscribe(`/topic/notifications/user-${userId}-x7f9`, (message: IMessage) => {
        try {
          const payload = JSON.parse(message.body) as ComplaintNotification;
          if (payload?.recipient?.id && Number(payload.recipient.id) !== userId) return;
          if (payload?.id && this.seenNotificationIds.has(payload.id)) return;
          if (payload?.id) this.seenNotificationIds.add(payload.id);
          this.notificationSubject.next(payload);
        } catch {
          // Ignore malformed frames and keep the stream alive.
        }
      });
    };
    this.client.onStompError = () => {
      this.connectionStateSubject.next(false);
    };
    this.client.onWebSocketClose = () => {
      this.connectionStateSubject.next(false);
      this.connectedUserId = null;
    };

    this.client.activate();
  }

  notifications$(): Observable<ComplaintNotification> {
    return this.notificationSubject.asObservable();
  }

  connectionState$(): Observable<boolean> {
    return this.connectionStateSubject.asObservable();
  }

  disconnect(): void {
    this.client?.deactivate();
    this.client = null;
    this.connectedUserId = null;
    this.seenNotificationIds.clear();
    this.connectionStateSubject.next(false);
  }
}
