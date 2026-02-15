export type Video = {
  id: string;
  title: string;
  description: string;
  originalFilename: string;
  s3Key: string;
  createdAt: string;
  updatedAt: string;
};

export type VideoProcessingJob = {
  id: string;
  video: Video;
  status: string;
  workerId: string | null;
  leaseUntil: string | null;
  createdAt: string;
  updatedAt: string;
};

export type InitiateMultipartUploadResponse = {
  uploadId: string;
  key: string;
};

export type InitiateMultipartUploadRequest = {
  filename: string;
  title: string;
  description: string;
};

export type IntermediateMultipartUploadRequest = {
  key: string;
  uploadId: string;
  partNumber: number;
};

export type IntermediateMultipartUploadResponse = {
  uploadUrl: string;
};

export type MultipartPart = {
  partNumber: number;
  eTag: string;
};

export type CompleteMultipartUploadRequest = {
  filename: string;
  uploadId: string;
  key: string;
  parts: MultipartPart[];
};

export type ManifestUrlResponse = {
  url: string;
};