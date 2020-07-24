package com.google.cloud.healthcare.fdamystudies.service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.google.cloud.WriteChannel;
import com.google.cloud.healthcare.fdamystudies.exception.CloudException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Service
public class CloudStorageService implements FileStorageService {

  private Storage storageService;

  private static final String BUCKET_NAME = "consent-test-pdf";

  private static final String PATH_SEPARATOR = "/";

  @PostConstruct
  private void init() {
    storageService = StorageOptions.getDefaultInstance().getService();
  }

  @Override
  public List<String> listFiles(String underDirectory, boolean recursive) {
    return new ArrayList<>();
  }

  @Override
  public String saveFile(String fileName, String content, String underDirectory) {
    String absoluteFileName = null;
    if (!StringUtils.isBlank(content)) {
      absoluteFileName =
          underDirectory == null ? fileName : underDirectory + PATH_SEPARATOR + fileName;
      BlobInfo blobInfo = BlobInfo.newBuilder(BUCKET_NAME, absoluteFileName).build();
      byte[] bytes = null;

      try (WriteChannel writer = storageService.writer(blobInfo)) {
        bytes = content.getBytes();
        writer.write(ByteBuffer.wrap(bytes, 0, bytes.length));
      } catch (IOException e) {
        throw new CloudException();
      }
    }
    return absoluteFileName;
  }

  @Override
  public void downloadFileTo(String absoluteFileName, OutputStream outputStream) {
    try {
      if (StringUtils.isNotBlank(absoluteFileName)) {
        Blob blob = storageService.get(BlobId.of(BUCKET_NAME, absoluteFileName));
        blob.downloadTo(outputStream);
      }
    } catch (Exception e) {
      throw new CloudException();
    }
  }

  @Override
  public void printMetadata() {
    throw new UnsupportedOperationException();
  }
}
