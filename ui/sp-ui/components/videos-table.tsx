// "use client";

// import { useEffect, useState } from "react";
// import {
//   Table,
//   TableBody,
//   TableCaption,
//   TableCell,
//   TableHead,
//   TableHeader,
//   TableRow,
// } from "@/components/ui/table";
// import { Button } from "@/components/ui/button";
// import { getAllVideos, getPlaybackUrl } from "@/lib/api";
// import { Video } from "@/lib/types";
// import { Spinner } from "@/components/ui/spinner";
// import { RefreshCw, ExternalLink, Play } from "lucide-react";

// export function VideosTable() {
//   const [videos, setVideos] = useState<Video[]>([]);
//   const [loading, setLoading] = useState(true);
//   const [error, setError] = useState<string | null>(null);
//   const [loadingPlayback, setLoadingPlayback] = useState<string | null>(null);

//   const fetchVideos = async () => {
//     try {
//       setLoading(true);
//       setError(null);
//       const data = await getAllVideos();
//       setVideos(data);
//     } catch (err) {
//       setError(err instanceof Error ? err.message : "Failed to fetch videos");
//     } finally {
//       setLoading(false);
//     }
//   };

//   useEffect(() => {
//     fetchVideos();
//   }, []);

//   const handlePlayback = async (videoId: string) => {
//     try {
//       setLoadingPlayback(videoId);
//       const { url } = await getPlaybackUrl(videoId);
      
//       // Open player page in new tab with video URL as query parameter
//       const playerUrl = `/player?videoUrl=${encodeURIComponent(url)}`;
//       window.open(playerUrl, "_blank");
//     } catch (err) {
//       alert(
//         `Failed to get playback URL: ${
//           err instanceof Error ? err.message : "Unknown error"
//         }`
//       );
//     } finally {
//       setLoadingPlayback(null);
//     }
//   };

//   const formatDate = (dateString: string) => {
//     return new Date(dateString).toLocaleString();
//   };

//   if (loading) {
//     return (
//       <div className="flex items-center justify-center p-8">
//         <Spinner className="mr-2" />
//         <span>Loading videos...</span>
//       </div>
//     );
//   }

//   if (error) {
//     return (
//       <div className="flex flex-col items-center justify-center p-8 text-red-500">
//         <p>Error: {error}</p>
//         <Button onClick={fetchVideos} variant="outline" className="mt-4">
//           <RefreshCw className="mr-2 h-4 w-4" />
//           Retry
//         </Button>
//       </div>
//     );
//   }

//   return (
//     <div className="space-y-4">
//       <div className="flex items-center justify-between">
//         <h2 className="text-2xl font-bold">Videos</h2>
//         <Button onClick={fetchVideos} variant="outline" size="sm">
//           <RefreshCw className="mr-2 h-4 w-4" />
//           Refresh
//         </Button>
//       </div>

//       <div className="rounded-md border">
//         <Table>
//           <TableCaption>
//             {videos.length === 0
//               ? "No videos found"
//               : `Total ${videos.length} video${videos.length !== 1 ? "s" : ""}`}
//           </TableCaption>
//           <TableHeader>
//             <TableRow>
//               <TableHead>Title</TableHead>
//               <TableHead>Description</TableHead>
//               <TableHead>Original Filename</TableHead>
//               <TableHead>Created At</TableHead>
//               <TableHead className="text-right">Actions</TableHead>
//             </TableRow>
//           </TableHeader>
//           <TableBody>
//             {videos.length === 0 ? (
//               <TableRow>
//                 <TableCell colSpan={5} className="text-center text-muted-foreground">
//                   No videos to display
//                 </TableCell>
//               </TableRow>
//             ) : (
//               videos.map((video) => (
//                 <TableRow key={video.id}>
//                   <TableCell className="font-medium">{video.title}</TableCell>
//                   <TableCell className="max-w-xs truncate">
//                     {video.description}
//                   </TableCell>
//                   <TableCell className="font-mono text-sm">
//                     {video.originalFilename}
//                   </TableCell>
//                   <TableCell className="text-sm">
//                     {formatDate(video.createdAt)}
//                   </TableCell>
//                   <TableCell className="text-right">
//                     <Button
//                       onClick={() => handlePlayback(video.id)}
//                       disabled={loadingPlayback === video.id}
//                       size="sm"
//                       variant="outline"
//                     >
//                       {loadingPlayback === video.id ? (
//                         <>
//                           <Spinner className="mr-2 h-4 w-4" />
//                           Loading...
//                         </>
//                       ) : (
//                         <>
//                           <Play className="mr-2 h-4 w-4" />
//                           Play
//                           <ExternalLink className="ml-2 h-3 w-3" />
//                         </>
//                       )}
//                     </Button>
//                   </TableCell>
//                 </TableRow>
//               ))
//             )}
//           </TableBody>
//         </Table>
//       </div>
//     </div>
//   );
// }

// components/videos-table.tsx
"use client";

import { useEffect, useState } from "react";
import {
  Table,
  TableBody,
  TableCaption,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { getAllVideos, getPlaybackUrl } from "@/lib/api";
import { Video } from "@/lib/types";
import { Spinner } from "@/components/ui/spinner";
import { RefreshCw, ExternalLink, Play } from "lucide-react";

export function VideosTable() {
  const [videos, setVideos] = useState<Video[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [loadingPlayback, setLoadingPlayback] = useState<string | null>(null);

  const fetchVideos = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getAllVideos();
      setVideos(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to fetch videos");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVideos();
  }, []);

  const handlePlayback = async (videoId: string, s3Key: string) => {
    try {
      setLoadingPlayback(videoId);
      
      // Remove file extension from s3Key
      const keyWithoutExtension = s3Key.replace(/\.[^.]+$/, '');
      
      const { url } = await getPlaybackUrl(keyWithoutExtension);
      
      // Open player page in new tab with video URL as query parameter
      const playerUrl = `/player?videoUrl=${encodeURIComponent(url)}`;
      window.open(playerUrl, "_blank");
    } catch (err) {
      alert(
        `Failed to get playback URL: ${
          err instanceof Error ? err.message : "Unknown error"
        }`
      );
    } finally {
      setLoadingPlayback(null);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center p-8">
        <Spinner className="mr-2" />
        <span>Loading videos...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center p-8 text-red-500">
        <p>Error: {error}</p>
        <Button onClick={fetchVideos} variant="outline" className="mt-4">
          <RefreshCw className="mr-2 h-4 w-4" />
          Retry
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold">Videos</h2>
        <Button onClick={fetchVideos} variant="outline" size="sm">
          <RefreshCw className="mr-2 h-4 w-4" />
          Refresh
        </Button>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableCaption>
            {videos.length === 0
              ? "No videos found"
              : `Total ${videos.length} video${videos.length !== 1 ? "s" : ""}`}
          </TableCaption>
          <TableHeader>
            <TableRow>
              <TableHead>Title</TableHead>
              <TableHead>Description</TableHead>
              <TableHead>Original Filename</TableHead>
              <TableHead>Created At</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {videos.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="text-center text-muted-foreground">
                  No videos to display
                </TableCell>
              </TableRow>
            ) : (
              videos.map((video) => (
                <TableRow key={video.id}>
                  <TableCell className="font-medium">{video.title}</TableCell>
                  <TableCell className="max-w-xs truncate">
                    {video.description}
                  </TableCell>
                  <TableCell className="font-mono text-sm">
                    {video.originalFilename}
                  </TableCell>
                  <TableCell className="text-sm">
                    {formatDate(video.createdAt)}
                  </TableCell>
                  <TableCell className="text-right">
                    <Button
                      onClick={() => handlePlayback(video.id, video.s3Key)}
                      disabled={loadingPlayback === video.id}
                      size="sm"
                      variant="outline"
                    >
                      {loadingPlayback === video.id ? (
                        <>
                          <Spinner className="mr-2 h-4 w-4" />
                          Loading...
                        </>
                      ) : (
                        <>
                          <Play className="mr-2 h-4 w-4" />
                          Play
                          <ExternalLink className="ml-2 h-3 w-3" />
                        </>
                      )}
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}