const fs = require("fs");
const path = require("path");

const BASE_URL = "http://localhost:20001/internal/uploads";
const FILE_PATH = "../res/robo.mp4";
const PART_SIZE = 5 * 1024 * 1024; // 5 MB

// -----------------------------
// Helpers
// -----------------------------
async function postJson(url, body) {
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body)
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`${res.status} ${res.statusText}: ${text}`);
  }

  return res.status === 204 ? null : res.json();
}

// -----------------------------
// 1. Initiate multipart upload
// -----------------------------
async function initiateMultipartUpload(filename) {
  return postJson(`${BASE_URL}/initiate`, {
    filename,
    title: "My Upload",
    description: "Uploaded via Node.js client"
  });
}

// -----------------------------
// 2. Get presigned URL for part
// -----------------------------
async function signPart(key, uploadId, partNumber) {
  const res = await postJson(`${BASE_URL}/sign-part`, {
    key,
    uploadId,
    partNumber
  });
  return res.uploadUrl;
}

// -----------------------------
// 3. Upload a single part to S3
// -----------------------------
async function uploadPart(uploadUrl, buffer) {
  const res = await fetch(uploadUrl, {
    method: "PUT",
    body: buffer
  });

  if (!res.ok) {
    throw new Error(`Failed to upload part: ${res.statusText}`);
  }

  return res.headers.get("etag");
}

// -----------------------------
// 4. Complete multipart upload
// -----------------------------
async function completeMultipartUpload(filename, key, uploadId, parts) {
  await postJson(`${BASE_URL}/complete`, {
    filename,
    key,
    uploadId,
    parts
  });
}

// -----------------------------
// Main flow
// -----------------------------
async function uploadFileMultipart() {
  const filename = path.basename(FILE_PATH);
  const fileBuffer = fs.readFileSync(FILE_PATH);

  console.log("Initiating multipart upload...");
  const { uploadId, key } = await initiateMultipartUpload(filename);

  console.log("Upload ID:", uploadId);
  console.log("S3 Key:", key);

  const parts = [];
  let partNumber = 1;

  for (let offset = 0; offset < fileBuffer.length; offset += PART_SIZE) {
    const chunk = fileBuffer.slice(offset, offset + PART_SIZE);

    console.log(`Signing part ${partNumber}...`);
    const uploadUrl = await signPart(key, uploadId, partNumber);

    console.log(uploadUrl);

    console.log(`Uploading part ${partNumber} (${chunk.length} bytes)...`);
    const eTag = await uploadPart(uploadUrl, chunk);

    parts.push({
      partNumber,
      eTag
    });

    partNumber++;
  }

  console.log("Completing multipart upload...");
  await completeMultipartUpload(filename, key, uploadId, parts);

  console.log("✅ Upload complete!");
}

// Run
uploadFileMultipart().catch(err => {
  console.error("❌ Upload failed:", err);
  process.exit(1);
});
