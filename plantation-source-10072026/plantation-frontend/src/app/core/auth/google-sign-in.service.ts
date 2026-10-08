import { Injectable } from '@angular/core';

/**
 * Google Identity Services (GIS) loader.
 *
 * The Google script is fetched **only** when the backend reports that Google
 * sign-in is configured (`GET /auth/oauth2/google/config` → `enabled: true`,
 * i.e. the operator set `GOOGLE_CLIENT_ID`). If it is not configured nothing
 * external is ever loaded and the login page shows an explanation instead of a
 * dead button.
 *
 * The credential handed to `onCredential` is a Google ID token: it is posted
 * straight to our backend, never stored, never logged and never sent anywhere
 * else.
 */
@Injectable({ providedIn: 'root' })
export class GoogleSignInService {
  private scriptLoading: Promise<void> | null = null;

  /** Renders the official Google button into `host`. */
  async renderButton(
    host: HTMLElement,
    clientId: string,
    onCredential: (credential: string) => void,
  ): Promise<void> {
    await this.loadScript();
    const google = (globalThis as Record<string, any>)['google'];
    google.accounts.id.initialize({
      client_id: clientId,
      callback: (response: { credential?: string }) => {
        if (response?.credential) onCredential(response.credential);
      },
    });
    host.innerHTML = '';
    google.accounts.id.renderButton(host, {
      theme: 'outline',
      size: 'large',
      text: 'continue_with',
      width: 280,
    });
  }

  private loadScript(): Promise<void> {
    const google = (globalThis as Record<string, any>)['google'];
    if (google?.accounts?.id) return Promise.resolve();
    if (this.scriptLoading) return this.scriptLoading;

    this.scriptLoading = new Promise<void>((resolve, reject) => {
      const script = document.createElement('script');
      script.src = 'https://accounts.google.com/gsi/client';
      script.async = true;
      script.defer = true;
      script.onload = () => resolve();
      script.onerror = () => {
        this.scriptLoading = null;
        reject(new Error('Could not load the Google sign-in script. Check your connection and try again.'));
      };
      document.head.appendChild(script);
    });
    return this.scriptLoading;
  }
}
