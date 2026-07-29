package com.ronext.optimizer.adapter.in.http;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import java.util.ArrayList;
import java.util.List;

/** Legacy-only mapping of the characterized storage behavior to GCS. */
final class GcpLegacyStorage implements LegacyStorage {
    private final Storage storage;

    GcpLegacyStorage(Storage storage) {
        this.storage = storage;
    }

    @Override
    public byte[] readResult(String bucket, String objectKey) {
        var blob = storage.get(BlobId.of(bucket, objectKey));
        return blob == null ? null : blob.getContent();
    }

    @Override
    public void writeCandidate(String bucket, String objectKey, byte[] content) {
        storage.create(jsonBlob(bucket, objectKey), content);
    }

    @Override
    public List<CandidateObjectReference> listCandidateObjects(String bucket, String prefix) {
        List<CandidateObjectReference> references = new ArrayList<>();
        for (var blob : storage.list(bucket, Storage.BlobListOption.prefix(prefix)).iterateAll()) {
            references.add(new GcpCandidateObjectReference(blob.getBlobId()));
        }
        return references;
    }

    @Override
    public byte[] readCandidate(String bucket, CandidateObjectReference reference) {
        if (!(reference instanceof GcpCandidateObjectReference gcpReference)
                || !bucket.equals(gcpReference.blobId().getBucket())) {
            throw new IllegalArgumentException("Candidate object reference does not belong to the requested GCS bucket");
        }
        return storage.readAllBytes(gcpReference.blobId());
    }

    @Override
    public void writeResult(String bucket, String objectKey, byte[] content) {
        storage.create(jsonBlob(bucket, objectKey), content);
    }

    private static BlobInfo jsonBlob(String bucket, String objectKey) {
        return BlobInfo.newBuilder(bucket, objectKey)
                .setContentType("application/json")
                .build();
    }

    private record GcpCandidateObjectReference(BlobId blobId)
            implements CandidateObjectReference {
        @Override
        public String objectKey() {
            return blobId.getName();
        }
    }
}
