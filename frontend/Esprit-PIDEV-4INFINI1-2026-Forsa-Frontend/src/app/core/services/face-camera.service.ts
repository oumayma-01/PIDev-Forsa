import { Injectable } from '@angular/core';

type FaceDetectorLike = {
  detect: (input: ImageBitmapSource) => Promise<Array<{ boundingBox?: DOMRectReadOnly }>>;
};

@Injectable({ providedIn: 'root' })
export class FaceCameraService {
  private stream: MediaStream | null = null;

  async start(video: HTMLVideoElement): Promise<void> {
    if (!navigator.mediaDevices?.getUserMedia) {
      throw new Error('Camera API not available on this browser.');
    }
    this.stream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: 'user', width: { ideal: 640 }, height: { ideal: 480 } },
      audio: false,
    });
    video.srcObject = this.stream;
    await video.play();
  }

  stop(video?: HTMLVideoElement): void {
    this.stream?.getTracks().forEach((track) => track.stop());
    this.stream = null;
    if (video) {
      video.srcObject = null;
    }
  }

  async captureDescriptor(video: HTMLVideoElement): Promise<number[]> {
    const w = video.videoWidth || 640;
    const h = video.videoHeight || 480;
    const canvas = document.createElement('canvas');
    canvas.width = w;
    canvas.height = h;
    const ctx = canvas.getContext('2d');
    if (!ctx) {
      throw new Error('Cannot capture camera frame.');
    }
    ctx.drawImage(video, 0, 0, w, h);
    const detectorCtor = (window as unknown as { FaceDetector?: new (opts?: { fastMode?: boolean; maxDetectedFaces?: number }) => FaceDetectorLike }).FaceDetector;

    let box: DOMRectReadOnly | null = null;
    if (detectorCtor) {
      const detector = new detectorCtor({ fastMode: true, maxDetectedFaces: 1 });
      try {
        const faces = await detector.detect(canvas);
        box = faces?.[0]?.boundingBox ?? null;
      } catch {
        box = null;
      }
    }

    const crop = this.getCropRect(w, h, box);
    const imageData = ctx.getImageData(crop.x, crop.y, crop.w, crop.h);
    return this.toDescriptor(imageData);
  }

  private getCropRect(width: number, height: number, box: DOMRectReadOnly | null): { x: number; y: number; w: number; h: number } {
    if (box) {
      const pad = 0.12;
      const x = Math.max(0, Math.floor(box.x - box.width * pad));
      const y = Math.max(0, Math.floor(box.y - box.height * pad));
      const w = Math.min(width - x, Math.floor(box.width * (1 + pad * 2)));
      const h = Math.min(height - y, Math.floor(box.height * (1 + pad * 2)));
      if (w > 32 && h > 32) {
        return { x, y, w, h };
      }
    }
    const size = Math.floor(Math.min(width, height) * 0.6);
    const x = Math.floor((width - size) / 2);
    const y = Math.floor((height - size) / 2);
    return { x, y, w: size, h: size };
  }

  private toDescriptor(imageData: ImageData): number[] {
    const targetW = 16;
    const targetH = 8;
    const src = imageData.data;
    const descriptor: number[] = [];
    for (let y = 0; y < targetH; y += 1) {
      for (let x = 0; x < targetW; x += 1) {
        const sx = Math.floor((x / targetW) * imageData.width);
        const sy = Math.floor((y / targetH) * imageData.height);
        const idx = (sy * imageData.width + sx) * 4;
        const r = src[idx];
        const g = src[idx + 1];
        const b = src[idx + 2];
        const gray = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
        descriptor.push(gray);
      }
    }
    return this.normalize(descriptor);
  }

  private normalize(v: number[]): number[] {
    const mean = v.reduce((s, x) => s + x, 0) / (v.length || 1);
    const centered = v.map((x) => x - mean);
    const norm = Math.sqrt(centered.reduce((s, x) => s + x * x, 0)) || 1;
    return centered.map((x) => x / norm);
  }
}
