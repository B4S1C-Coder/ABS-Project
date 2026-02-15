"use client";
import React, { useState } from "react";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Separator } from "./ui/separator";
import { Spinner } from "./ui/spinner";
import { completeMultipartUpload, initiateMultipartUpload, signPart, uploadPart } from "@/lib/api";
import { MultipartPart } from "@/lib/types";
import { Progress } from "./ui/progress";

const PART_SIZE = 5 * 1024 * 1024; // 5 MB Chunks

export function FileUpload() {
  const [file, setFile] = useState<File | null>(null);
  const [title, setTitle] = useState<string>("Untitled Video");
  const [desc, setDesc] = useState<string>("Video uploaded via UI.");
  const [loading, setLoading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [currentPart, setCurrentPart] = useState(0);
  const [totalParts, setTotalParts] = useState(0);
  const [open, setOpen] = useState(false);

  const onFile = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFile(e.target.files?.[0] ?? null);
    setUploadProgress(0);
    setCurrentPart(0);
    setTotalParts(0);
  };

  const uploadFileMultipart = async () => {
    if (!file) return;

    setLoading(true);
    setUploadProgress(0);

    try {
      console.log("Initiating multipart upload ...");
      const { uploadId, key } = await initiateMultipartUpload({
        filename: file.name,
        title,
        description: desc
      });

      console.log("Upload ID:", uploadId);
      console.log("S3 Key:", key);

      const fileBuffer = await file.arrayBuffer();
      const parts: MultipartPart[] = [];
      let partNumber = 1;
      const totalPartsCount = Math.ceil(fileBuffer.byteLength / PART_SIZE);
      setTotalParts(totalPartsCount);

      for (let offset = 0; offset < fileBuffer.byteLength; offset += PART_SIZE) {
        const chunk = fileBuffer.slice(offset, offset + PART_SIZE);

        console.log(`Signing part ${partNumber}...`);
        const signedResp = await signPart({
          key,
          uploadId,
          partNumber
        });

        console.log(`Uploading part ${partNumber} (${chunk.byteLength} bytes)...`);
        const eTag = await uploadPart(signedResp, chunk);
        if (!eTag) {
          throw new Error(`No ETag received for part ${partNumber} or Upload failed for it.`);
        }

        parts.push({
          partNumber,
          eTag: eTag.replace(/"/g, "")
        });

        setCurrentPart(partNumber);
        setUploadProgress(Math.round((partNumber / totalPartsCount) * 100));
        partNumber++;
      }

      console.log("Completing Multipart Upload...");
      await completeMultipartUpload({
        filename: file.name,
        uploadId,
        key,
        parts
      });

      console.log("Upload finished.");

      // Reset and close
      setFile(null);
      setTitle("Untitled Video");
      setDesc("Video uploaded via UI.");
      setUploadProgress(0);
      setCurrentPart(0);
      setTotalParts(0);
      setOpen(false);

      // Refresh video list
    } catch (error) {
      console.error("Upload failed:", error);
      alert(`Upload failed: ${error instanceof Error ? error.message : 'Unknown error'}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Dialog>
        <DialogTrigger asChild>
          <Button>Upload Video</Button>
        </DialogTrigger>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Upload a Video</DialogTitle>
            <DialogDescription>
              Provide the raw video file that would be processed into streams for HLS.
            </DialogDescription>
          </DialogHeader>
          <Label>
            Raw Video File
          </Label>
          <Input type="file" accept="video/*" onChange={onFile} disabled={loading} />
          {file && (
            <p className="text-sm text-muted-foreground">
              Selected: {file.name} ({(file.size / (1024 * 1024)).toFixed(2)} MB)
            </p>
          )}
          <Separator />
          <Label>Title</Label>
          <Input
            type="text"
            placeholder="Title of the Video"
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
              setTitle(e.target.value);
            }}
            disabled={loading}
          />
          <Label>Description</Label>
          <Input
            type="text"
            placeholder="Description of the Video"
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
              setDesc(e.target.value);
            }}
            disabled={loading}
          />
          {loading && (
          <div className="space-y-2">
            <Progress value={uploadProgress} />
            <p className="text-sm text-center text-muted-foreground">
              Uploading part {currentPart} of {totalParts} ({uploadProgress}%)
            </p>
          </div>
          )}
          <div className="flex items-center justify-center">
            {loading ? (
              <span className="flex items-center gap-2">
                <Spinner /> Uploading
              </span>
            ) : (
              <Button disabled={(!file || loading)} onClick={uploadFileMultipart}>Upload</Button>
            )}
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
}