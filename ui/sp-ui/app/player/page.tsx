"use client";

import { useSearchParams } from "next/navigation";
import { Suspense } from "react";
import { Player } from "@/components/player"; // Adjust import path as needed
import { Spinner } from "@/components/ui/spinner";

function PlayerContent() {
  const searchParams = useSearchParams();
  const videoUrl = searchParams.get("videoUrl");

  if (!videoUrl) {
    return (
      <div className="flex h-screen items-center justify-center">
        <p className="text-red-500">No video URL provided</p>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-4">
      <h1 className="mb-4 text-2xl font-bold">Video Player</h1>
      <Player videoUrl={videoUrl} />
    </div>
  );
}

export default function PlayerPage() {
  return (
    <Suspense
      fallback={
        <div className="flex h-screen items-center justify-center">
          <Spinner className="mr-2" />
          <span>Loading player...</span>
        </div>
      }
    >
      <PlayerContent />
    </Suspense>
  );
}