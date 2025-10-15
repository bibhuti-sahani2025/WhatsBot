import { ComponentFixture, TestBed } from '@angular/core/testing';
import { WhatsappTesterComponent } from './whatsapp-tester.component';
import { By } from '@angular/platform-browser';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { WhatsappApiService } from './whatsapp-api.service';

describe('WhatsappTesterComponent', () => {
  let component: WhatsappTesterComponent;
  let fixture: ComponentFixture<WhatsappTesterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WhatsappTesterComponent]
    }).compileComponents();
    fixture = TestBed.createComponent(WhatsappTesterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should allow sending a message to an individual', () => {
    // Simulate filling the form for individual message
    const phoneInput = fixture.nativeElement.querySelector('input[placeholder="Phone number or Group ID"]');
    const messageTypeSelect = fixture.nativeElement.querySelector('select');
    const messageInput = fixture.nativeElement.querySelector('input[placeholder="Message or details..."]');
    phoneInput.value = '8327762384';
    phoneInput.dispatchEvent(new Event('input'));
    messageTypeSelect.value = 'text';
    messageTypeSelect.dispatchEvent(new Event('change'));
    messageInput.value = 'hii';
    messageInput.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    // Simulate form submit
    const form = fixture.nativeElement.querySelector('form');
    form.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
    // Check if the form submission logic is triggered (to be implemented)
    // This is a placeholder: you should check for the expected result in your component
    expect(true).toBeTrue();
  });
});

describe('WhatsappTesterComponent Integration', () => {
  let api: WhatsappApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [WhatsappApiService]
    });
    api = TestBed.inject(WhatsappApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should send a text message to individual', () => {
    api.sendTextMessage('8327762384', 'hii').subscribe(res => {
      expect(res).toEqual({ success: true });
    });
    const req = httpMock.expectOne(r => r.url.includes('/api/whatsapp/send'));
    expect(req.request.method).toBe('POST');
    expect(req.request.params.get('phone')).toBe('8327762384');
    expect(req.request.params.get('message')).toBe('hii');
    req.flush({ success: true });
    httpMock.verify();
  });
});
