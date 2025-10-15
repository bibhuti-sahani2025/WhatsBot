import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule, JsonPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WhatsappApiService } from './whatsapp-api.service';

@Component({
  selector: 'whatsapp-tester',
  standalone: true,
  imports: [CommonModule, FormsModule, JsonPipe],
  templateUrl: './whatsapp-tester.component.html',
  styleUrls: ['./whatsapp-tester.component.scss']
})
export class WhatsappTesterComponent {
  recipientType: 'individual' | 'group' = 'individual';
  phoneOrGroupId = '';
  messageType = 'text';
  messageContent = '';
  mediaUrl = '';
  caption = '';
  latitude = '';
  longitude = '';
  address = '';
  sending = false;
  response: any = null;
  gettingLocation = false;
  locationError = '';
 
  constructor(private api: WhatsappApiService, private http: HttpClient) {}

  // For image upload
  uploading = false;
  uploadError = '';
  // Cloudinary config
  cloudName = 'dejkcs9v6';
  uploadPreset = 'whatsapp_unsigned'; // Change if your preset name is different

  onImageFileChange(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    this.uploadError = '';
    this.uploading = true;
    const formData = new FormData();
    formData.append('file', file);
    formData.append('upload_preset', this.uploadPreset);
    this.http.post<any>(`https://api.cloudinary.com/v1_1/${this.cloudName}/image/upload`, formData).subscribe({
      next: (res) => {
        if (res && res.secure_url) {
          // Apply transformation to the URL to resize image (max 1920x1920, quality 80%)
          // Original URL: https://res.cloudinary.com/cloud/image/upload/v123/file.jpg
          // Transformed: https://res.cloudinary.com/cloud/image/upload/w_1920,h_1920,c_limit,q_80/v123/file.jpg
          const transformedUrl = res.secure_url.replace('/upload/', '/upload/w_1920,h_1920,c_limit,q_80/');
          this.mediaUrl = transformedUrl;
        } else {
          this.uploadError = 'Image upload failed.';
        }
        this.uploading = false;
      },
      error: (err) => {
        this.uploadError = 'Image upload failed.';
        this.uploading = false;
      }
    });
  }

  onAudioFileChange(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    this.uploadError = '';
    this.uploading = true;
    const formData = new FormData();
    formData.append('file', file);
    formData.append('upload_preset', this.uploadPreset);
    // Use video/upload for audio files (Cloudinary handles it)
    this.http.post<any>(`https://api.cloudinary.com/v1_1/${this.cloudName}/video/upload`, formData).subscribe({
      next: (res) => {
        if (res && res.secure_url) {
          this.mediaUrl = res.secure_url;
        } else {
          this.uploadError = 'Audio upload failed.';
        }
        this.uploading = false;
      },
      error: (err) => {
        this.uploadError = 'Audio upload failed.';
        this.uploading = false;
      }
    });
  }

  getCurrentLocation() {
    if (!navigator.geolocation) {
      this.locationError = 'Geolocation is not supported by your browser';
      return;
    }

    this.gettingLocation = true;
    this.locationError = '';

    navigator.geolocation.getCurrentPosition(
      (position) => {
        this.latitude = position.coords.latitude.toString();
        this.longitude = position.coords.longitude.toString();
        this.gettingLocation = false;
      },
      (error) => {
        this.locationError = 'Unable to get location: ' + error.message;
        this.gettingLocation = false;
      }
    );
  }

  onSubmit(e: Event) {
    e.preventDefault();
    this.sending = true;
    this.response = null;
    let recipient = this.phoneOrGroupId;
    if (this.recipientType === 'group' && !recipient.endsWith('@g.us')) {
      recipient = recipient + '@g.us';
    }
    if (this.messageType === 'text') {
      this.api.sendTextMessage(recipient, this.messageContent).subscribe({
        next: (res) => {
          this.response = res;
          this.sending = false;
        },
        error: (err) => {
          this.response = err;
          this.sending = false;
        }
      });
    } else if (this.messageType === 'image') {
      this.api.sendImage(recipient, this.mediaUrl, this.caption).subscribe({
        next: (res) => {
          this.response = res;
          this.sending = false;
        },
        error: (err) => {
          this.response = err;
          this.sending = false;
        }
      });
    } else if (this.messageType === 'document') {
      this.api.sendDocument(recipient, this.mediaUrl, this.caption).subscribe({
        next: (res) => {
          this.response = res;
          this.sending = false;
        },
        error: (err) => {
          this.response = err;
          this.sending = false;
        }
      });
    } else if (this.messageType === 'audio') {
      this.api.sendAudio(recipient, this.mediaUrl, this.caption).subscribe({
        next: (res) => {
          this.response = res;
          this.sending = false;
        },
        error: (err) => {
          this.response = err;
          this.sending = false;
        }
      });
    } else if (this.messageType === 'location') {
      this.api.sendLocation(recipient, this.latitude, this.longitude, this.address).subscribe({
        next: (res) => {
          this.response = res;
          this.sending = false;
        },
        error: (err) => {
          this.response = err;
          this.sending = false;
        }
      });
    }
    // TODO: Add logic for vCard, call
  }
}
