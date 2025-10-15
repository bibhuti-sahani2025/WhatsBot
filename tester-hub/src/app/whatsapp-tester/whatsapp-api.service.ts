import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class WhatsappApiService {
  private baseUrl = 'http://localhost:8080/api/whatsapp'; // Adjust if needed

  constructor(private http: HttpClient) {}

  sendTextMessage(phoneOrGroupId: string, message: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/send`, null, {
      params: { phone: phoneOrGroupId, message }
    });
  }

  sendImage(phoneOrGroupId: string, imageUrl: string, caption?: string): Observable<any> {
    const params: any = { phone: phoneOrGroupId, imageUrl };
    if (caption) params.caption = caption;
    return this.http.post(`${this.baseUrl}/send-image`, null, { params });
  }

  sendDocument(phoneOrGroupId: string, documentUrl: string, caption?: string): Observable<any> {
    const params: any = { phone: phoneOrGroupId, documentUrl };
    if (caption) params.caption = caption;
    return this.http.post(`${this.baseUrl}/send-document`, null, { params });
  }

  sendAudio(phoneOrGroupId: string, audioUrl: string, caption?: string): Observable<any> {
    const params: any = { phone: phoneOrGroupId, audioUrl };
    if (caption) params.caption = caption;
    return this.http.post(`${this.baseUrl}/send-audio`, null, { params });
  }

  sendLocation(phoneOrGroupId: string, latitude: string, longitude: string, address?: string): Observable<any> {
    const params: any = { phone: phoneOrGroupId, latitude, longitude };
    if (address) params.address = address;
    return this.http.post(`${this.baseUrl}/send-location`, null, { params });
  }
}
