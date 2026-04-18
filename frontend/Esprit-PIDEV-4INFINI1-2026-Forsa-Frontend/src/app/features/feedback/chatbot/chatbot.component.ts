import { Component, OnInit, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatbotService } from '../../../core/data/chatbot.service';
import { ChatMessage } from '../../../core/models/forsa.models';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ForsaButtonComponent,
    ForsaCardComponent,
    ForsaIconComponent,
    ForsaInputDirective,
  ],
  templateUrl: './chatbot.component.html',
  styleUrl: './chatbot.component.css',
})
export class ChatbotComponent implements OnInit, AfterViewChecked {
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;

  messages: ChatMessage[] = [];
  userMessage = '';
  loading = false;
  error = '';
  shouldScroll = false;

  constructor(private chatbotService: ChatbotService) {}

  ngOnInit(): void {
    // Optional: Add initial greeting
    this.messages.push({
      role: 'bot',
      content: 'Hello! How can I help you today?',
      timestamp: new Date(),
    });
  }

  ngAfterViewChecked(): void {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  sendMessage(): void {
    if (!this.userMessage.trim()) {
      return;
    }

    // Add user message to chat
    const message: ChatMessage = {
      role: 'user',
      content: this.userMessage,
      timestamp: new Date(),
    };
    this.messages.push(message);

    // Clear input and set loading
    const userInput = this.userMessage;
    this.userMessage = '';
    this.loading = true;
    this.error = '';
    this.shouldScroll = true;

    // Send to chatbot
    this.chatbotService.ask(userInput).subscribe({
      next: (response) => {
        this.messages.push({
          role: 'bot',
          content: response.answer,
          timestamp: new Date(),
        });
        this.loading = false;
        this.shouldScroll = true;
      },
      error: () => {
        this.error = 'Error communicating with chatbot';
        this.loading = false;
      },
    });
  }

  scrollToBottom(): void {
    try {
      this.messagesContainer.nativeElement.scrollTop =
        this.messagesContainer.nativeElement.scrollHeight;
    } catch (err) {
      // Scroll error
    }
  }

  clearChat(): void {
    if (confirm('Clear all messages?')) {
      this.messages = [
        {
          role: 'bot',
          content: 'Hello! How can I help you today?',
          timestamp: new Date(),
        },
      ];
      this.error = '';
    }
  }
}
