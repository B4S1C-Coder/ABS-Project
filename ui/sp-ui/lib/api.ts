import {
  CompleteMultipartUploadRequest,
  InitiateMultipartUploadRequest,
  InitiateMultipartUploadResponse,
  IntermediateMultipartUploadRequest,
  IntermediateMultipartUploadResponse,
  ManifestUrlResponse,
  Video,
  VideoProcessingJob 
} from "@/lib/types";

export type ApiError = {
  message: string
  status?: number
  [k: string]: any
}

const getBaseUrl = () => {
  // if (typeof window === "undefined") {
  //   return process.env.API_URL ?? "http://localhost:8080";
  // }

  // return process.env.NEXT_PUBLIC_API_URL ?? "";
  return "http://localhost:8080";
};

async function fetchWithTimeout(input: RequestInfo, init: RequestInit = {}, timeout = 10000) {
  const controller = new AbortController();
  const id = setTimeout(() => controller.abort(), timeout);

  try {
    const res = await fetch(input, { ...init, signal: controller.signal });
    return res;
  } finally {
    clearTimeout(id);
  }
}

async function handleResponse(res: Response) {
  const text = await res.text().catch(() => "");
  let body: any = text;

  try {
    body = text ? JSON.parse(text) : {};
  } catch(_) {}

  if (!res.ok) {
    const err: ApiError = {
      message: body?.message ?? res.statusText ?? "Request Failed",
      status: res.status,
      ...body,
    };

    throw err;
  }

  return body;
}

async function request<T = any>(path: string, opts: RequestInit = {}) {
  const base = getBaseUrl();
  const url = base ? new URL(path, base).toString() : path;
  const res = await fetchWithTimeout(url, opts);
  return handleResponse(res) as Promise<T>;
}

const postOpts = (payload: any) => {
  return {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  };
};

export async function initiateMultipartUpload(
  payload: InitiateMultipartUploadRequest
) {
  return request<InitiateMultipartUploadResponse>("/upload/initiate", postOpts(payload));
}

export async function signPart(
  payload: IntermediateMultipartUploadRequest
) {
  return request<IntermediateMultipartUploadResponse>("/upload/sign-part", postOpts(payload));
}

export async function uploadPart(
  payload: IntermediateMultipartUploadResponse, buffer: any
) {
  const response = await fetch(payload.uploadUrl, {
    method: "PUT",
    body: buffer
  });

  if (!response.ok) {
    throw new Error(`Failed to upload part`);
  }

  const eTag = response.headers.get("etag");
  if (!eTag) {
    throw new Error(`No ETag received for part`);
  }

  return eTag;
}

export async function completeMultipartUpload(
  payload: CompleteMultipartUploadRequest
) {
  return request("/upload/complete", postOpts(payload));
}

export async function getAllJobs() {
  return request<VideoProcessingJob[]>("/upload/all-jobs", { method: "GET" });
}

export async function getAllVideos() {
  return request<Video[]>("/upload/all-videos", { method: "GET" });
}

export async function getPlaybackUrl(videoId: string) {
  return request<ManifestUrlResponse>(`/play/video/${videoId}`, { method: "GET" });
}