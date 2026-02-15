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
import { Badge } from "@/components/ui/badge";
import { getAllJobs } from "@/lib/api";
import { VideoProcessingJob } from "@/lib/types";
import { Spinner } from "@/components/ui/spinner";
import { RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";

export function JobsTable() {
  const [jobs, setJobs] = useState<VideoProcessingJob[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchJobs = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getAllJobs();
      setJobs(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to fetch jobs");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchJobs();
  }, []);

  const getStatusBadge = (status: string) => {
    const statusLower = status.toLowerCase();
    
    if (statusLower === "completed" || statusLower === "complete") {
      return <Badge className="bg-green-500">Completed</Badge>;
    } else if (statusLower === "processing" || statusLower === "in_progress") {
      return <Badge className="bg-blue-500">Processing</Badge>;
    } else if (statusLower === "failed" || statusLower === "error") {
      return <Badge className="bg-red-500">Failed</Badge>;
    } else if (statusLower === "pending" || statusLower === "queued") {
      return <Badge className="bg-yellow-500">Pending</Badge>;
    }
    
    return <Badge variant="outline">{status}</Badge>;
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center p-8">
        <Spinner className="mr-2" />
        <span>Loading jobs...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center p-8 text-red-500">
        <p>Error: {error}</p>
        <Button onClick={fetchJobs} variant="outline" className="mt-4">
          <RefreshCw className="mr-2 h-4 w-4" />
          Retry
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold">Processing Jobs</h2>
        <Button onClick={fetchJobs} variant="outline" size="sm">
          <RefreshCw className="mr-2 h-4 w-4" />
          Refresh
        </Button>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableCaption>
            {jobs.length === 0
              ? "No processing jobs found"
              : `Total ${jobs.length} job${jobs.length !== 1 ? "s" : ""}`}
          </TableCaption>
          <TableHeader>
            <TableRow>
              <TableHead>Job ID</TableHead>
              <TableHead>Video Title</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Worker ID</TableHead>
              <TableHead>Lease Until</TableHead>
              <TableHead>Created At</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {jobs.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="text-center text-muted-foreground">
                  No jobs to display
                </TableCell>
              </TableRow>
            ) : (
              jobs.map((job) => (
                <TableRow key={job.id}>
                  <TableCell className="font-mono text-sm">
                    {job.id.substring(0, 8)}...
                  </TableCell>
                  <TableCell className="font-medium">{job.video.title}</TableCell>
                  <TableCell>{getStatusBadge(job.status)}</TableCell>
                  <TableCell className="font-mono text-sm">
                    {job.workerId ? `${job.workerId.substring(0, 8)}...` : "—"}
                  </TableCell>
                  <TableCell className="text-sm">
                    {job.leaseUntil ? formatDate(job.leaseUntil) : "—"}
                  </TableCell>
                  <TableCell className="text-sm">
                    {formatDate(job.createdAt)}
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