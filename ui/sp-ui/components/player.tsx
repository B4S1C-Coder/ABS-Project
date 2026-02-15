"use client";

import '@vidstack/react/player/styles/default/theme.css';
import '@vidstack/react/player/styles/default/layouts/video.css';

import { MediaPlayer, MediaPlayerInstance, MediaProvider } from '@vidstack/react';
import { defaultLayoutIcons, DefaultVideoLayout } from '@vidstack/react/player/layouts/default';
import { PlayerStats } from './player-stats';
import { useRef, useEffect } from 'react';
import { Example } from './example';

export function Player({ videoUrl = "https://files.vidstack.io/sprite-fight/hls/stream.m3u8" }: { videoUrl?: string }) {
  
  const playerRef = useRef<MediaPlayerInstance>(null);

  useEffect(() => {
    if (playerRef.current) {
      Object.defineProperty(playerRef.current, '__REACT_DEVTOOLS_GLOBAL_HOOK__', {
        value: undefined,
        writable: false,
      });
    }
  }, []);

  return (
    <>
      <h1 className='text-4xl'>Test Video</h1>
      <Example className='max-w-4xl mx-auto'>
        <div className='max-w-4xl mx-auto'>
          <MediaPlayer title="Test Video" src={videoUrl} ref={playerRef} playsInline>
            <MediaProvider />
            <DefaultVideoLayout thumbnails="https://files.vidstack.io/sprite-fight/thumbnails.vtt" icons={defaultLayoutIcons} />
          </MediaPlayer>

          <PlayerStats playerRef={playerRef} />
        </div>
      </Example>
    </>
  );
}