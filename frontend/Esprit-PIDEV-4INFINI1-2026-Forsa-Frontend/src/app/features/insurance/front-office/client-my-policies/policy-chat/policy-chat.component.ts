import { Component, Input, OnInit, OnChanges, SimpleChanges, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PolicyChatService, ChatMessage, ChatResponseDTO } from '../../../shared/services/policy-chat.service';

@Component({
  selector: 'app-policy-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './policy-chat.component.html',
  styleUrls: ['./policy-chat.component.scss']
})
export class PolicyChatComponent implements OnInit, OnChanges, AfterViewChecked {
  @Input() policyId!: number;
  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  isOpen = false;
  messages: ChatMessage[] = [];
  userInput = '';
  isLoading = false;

  suggestions = [
    "What does my policy cover?",
    "When is my next payment?",
    "How do I file a claim?"
  ];

  constructor(private chatService: PolicyChatService) {}

  ngOnInit(): void {
    this.isOpen = false; // Start closed, user will click the floating button
    this.loadHistory();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['policyId'] && !changes['policyId'].firstChange) {
      this.messages = [];
      this.loadHistory();
    }
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  toggleChat(): void {
    this.isOpen = !this.isOpen;
    if (this.isOpen && this.messages.length === 0) {
      this.loadHistory();
    }
  }

  loadHistory(): void {
    if (!this.policyId) return;
    
    this.chatService.getHistory(this.policyId).subscribe({
      next: (history: ChatMessage[]) => {
        this.messages = history;
        this.scrollToBottom();
      },
      error: (err: any) => console.error('Failed to load chat history', err)
    });
  }

  sendMessage(): void {
    if (!this.userInput.trim() || this.isLoading) return;

    const messageText = this.userInput.trim();
    this.userInput = '';
    this.isLoading = true;

    // Optimistically add user message
    const userMsg: ChatMessage = {
      policyId: this.policyId,
      role: 'user',
      content: messageText,
      timestamp: new Date().toISOString()
    };
    this.messages.push(userMsg);
    this.scrollToBottom();

    this.chatService.sendMessage(this.policyId, messageText).subscribe({
      next: (res: ChatResponseDTO) => {
        // Add a slight delay for realism
        setTimeout(() => {
          const assistantMsg: ChatMessage = {
            policyId: this.policyId,
            role: 'assistant',
            content: res.reply,
            timestamp: res.timestamp || new Date().toISOString()
          };
          this.messages.push(assistantMsg);
          this.isLoading = false;
          this.scrollToBottom();
        }, 600);
      },
      error: (err: any) => {
        console.error('Chat error', err);
        this.messages.push({
          policyId: this.policyId,
          role: 'assistant',
          content: "I'm having trouble connecting to the AI service. Please check your internet or try again later.",
          timestamp: new Date().toISOString()
        });
        this.isLoading = false;
        this.scrollToBottom();
      }
    });
  }

  selectSuggestion(suggestion: string): void {
    this.userInput = suggestion;
    this.sendMessage();
  }

  private scrollToBottom(): void {
    if (this.scrollContainer) {
      try {
        const el = this.scrollContainer.nativeElement;
        el.scrollTop = el.scrollHeight;
      } catch (err) {}
    }
  }
}
