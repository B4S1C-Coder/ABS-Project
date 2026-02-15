// // import { ComponentExample } from "@/components/component-example";
// // import { Player } from "@/components/player";

// import { FileUpload } from "@/components/file-upload";

// export default function Page() {
//   return (
//     <>
//       {/* <Player videoUrl="http://localhost:8080/video/69878fe3-69fb-491d-9e8f-606b46c050d1-robo/master.m3u8" /> */}
//       <FileUpload />
//     </>
//   );
// }

// app/page.tsx or app/dashboard/page.tsx
import { JobsTable } from "@/components/jobs-table";
import { VideosTable } from "@/components/videos-table";
import { FileUpload } from "@/components/file-upload";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";

export default function DashboardPage() {
  return (
    <div className="container mx-auto p-6 space-y-8">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">Video Processing Dashboard</h1>
        <FileUpload />
      </div>

      <Tabs defaultValue="videos" className="w-full">
        <TabsList className="grid w-full grid-cols-2 max-w-md">
          <TabsTrigger value="videos">Videos</TabsTrigger>
          <TabsTrigger value="jobs">Processing Jobs</TabsTrigger>
        </TabsList>
        <TabsContent value="videos" className="mt-6">
          <VideosTable />
        </TabsContent>
        <TabsContent value="jobs" className="mt-6">
          <JobsTable />
        </TabsContent>
      </Tabs>
    </div>
  );
}