import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { WhatsappTesterComponent } from './whatsapp-tester/whatsapp-tester.component';

@Component({
  selector: 'app-root',
  imports: [WhatsappTesterComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('tester-hub');
}
  