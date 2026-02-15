"use client";

import { useEffect, useState } from "react";
import type { RefObject } from "react";
import type { MediaPlayerInstance } from "@vidstack/react";

type Props = {
  playerRef: RefObject<MediaPlayerInstance | null>;
};

export function PlayerStats({ playerRef }: Props) {
  const [stats, setStats] = useState({
    resolution: "-",
    bitrate: "-",
    buffer: "-",
    dropped: "-",
  });

  useEffect(() => {
    const player = playerRef.current;
    if (!player) return;

    // Subscribe to player state for buffer and dropped frames
    const unsubscribe = player.subscribe(({ bufferedEnd, currentTime }) => {
      const video = player.el?.querySelector("video") as HTMLVideoElement | null;
      
      let buffer = "-";
      if (bufferedEnd && currentTime) {
        const bufferAmount = bufferedEnd - currentTime;
        buffer = `${bufferAmount.toFixed(2)} sec`;
      }

      let dropped = "-";
      if (video) {
        const playbackQuality = video.getVideoPlaybackQuality?.();
        if (playbackQuality) {
          dropped = String(playbackQuality.droppedVideoFrames);
        }
      }

      setStats(prev => ({
        ...prev,
        buffer,
        dropped,
      }));
    });

    // Listen for quality change events
    const handleQualityChange = (event: any) => {
      const quality = event.detail;
      if (quality) {
        setStats(prev => ({
          ...prev,
          resolution: quality.width && quality.height ? `${quality.width}x${quality.height}` : "-",
          bitrate: quality.bitrate ? `${Math.round(quality.bitrate / 1000)} kbps` : "-",
        }));
      }
    };

    player.addEventListener('quality-change', handleQualityChange);

    return () => {
      unsubscribe();
      player.removeEventListener('quality-change', handleQualityChange);
    };
  }, []);

  return (
    <div className="mt-4 rounded-lg bg-black p-4 text-green-400 font-mono text-sm">
      <div>Resolution: {stats.resolution}</div>
      <div>Bitrate: {stats.bitrate}</div>
      <div>Buffer: {stats.buffer}</div>
      <div>Dropped Frames: {stats.dropped}</div>
    </div>
  );
}