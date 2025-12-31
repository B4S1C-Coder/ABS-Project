const CHUNK_SIZE = 10 * 1024 * 1024; // 10MB
const file = document.getElementById('fileInput').files[0]; // The 50MB file
const totalParts = Math.ceil(file.size / CHUNK_SIZE);

async function uploadFile() {
    // 1. Get Upload ID from your Backend
    const { uploadId, key } = await initiateUpload(file.name);
    const parts = [];

    // 2. Loop through the file in chunks
    for (let partNumber = 1; partNumber <= totalParts; partNumber++) {
        
        // --- THE MAGIC HAPPENS HERE ---
        const start = (partNumber - 1) * CHUNK_SIZE;
        const end = Math.min(start + CHUNK_SIZE, file.size);
        
        // Create a 'slice' (like a mini-file)
        const chunk = file.slice(start, end); 
        // ------------------------------

        // 3. Get Presigned URL for this specific chunk
        const { url } = await getPresignedUrl(key, uploadId, partNumber);

        // 4. Upload the chunk directly to AWS
        // Note: S3 returns an 'ETag' header which is like a receipt
        const s3Response = await fetch(url, {
            method: 'PUT',
            body: chunk
        });
        
        const eTag = s3Response.headers.get('ETag').replaceAll('"', '');
        parts.push({ partNumber, eTag });
        
        console.log(`Uploaded part ${partNumber}/${totalParts}`);
    }

    // 5. Tell Backend to finish
    await completeUpload(key, uploadId, parts);
}