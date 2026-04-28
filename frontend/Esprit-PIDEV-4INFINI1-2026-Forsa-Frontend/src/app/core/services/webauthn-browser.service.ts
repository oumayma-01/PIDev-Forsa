import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class WebAuthnBrowserService {
  isSupported(): boolean {
    return typeof window !== 'undefined' && !!window.PublicKeyCredential && !!navigator.credentials;
  }

  async createCredential(options: PublicKeyCredentialCreationOptions): Promise<PublicKeyCredential> {
    const cred = await navigator.credentials.create({ publicKey: options });
    if (!cred || !(cred instanceof PublicKeyCredential)) {
      throw new Error('Passkey creation failed.');
    }
    return cred;
  }

  async getCredential(options: PublicKeyCredentialRequestOptions): Promise<PublicKeyCredential> {
    const cred = await navigator.credentials.get({ publicKey: options });
    if (!cred || !(cred instanceof PublicKeyCredential)) {
      throw new Error('Passkey authentication failed.');
    }
    return cred;
  }

  toBuffer(base64url: string): ArrayBuffer {
    const base64 = base64url.replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4);
    const binary = atob(padded);
    const bytes = new Uint8Array(binary.length);
    for (let i = 0; i < binary.length; i += 1) {
      bytes[i] = binary.charCodeAt(i);
    }
    return bytes.buffer;
  }

  fromBuffer(value: ArrayBuffer): string {
    const bytes = new Uint8Array(value);
    let binary = '';
    for (let i = 0; i < bytes.byteLength; i += 1) {
      binary += String.fromCharCode(bytes[i]);
    }
    return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  }

  transportsToCsv(transports: readonly string[] | undefined): string {
    return transports?.join(',') ?? '';
  }
}
