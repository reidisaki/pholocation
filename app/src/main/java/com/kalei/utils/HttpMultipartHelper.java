package com.kalei.utils;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

public class HttpMultipartHelper {

    public static HttpEntity getMultiPartEntity(String fileName, String contentType, InputStream fileStream,
            Map<String, String> additionalFormFields) throws IOException {

        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();

        if (additionalFormFields != null && !additionalFormFields.isEmpty()) {
            for (Entry<String, String> field : additionalFormFields.entrySet()) {
                entityBuilder.addTextBody(field.getKey(), field.getValue());
            }
        }

        entityBuilder.addBinaryBody(fileName, IOUtils.toByteArray(fileStream), ContentType.create(contentType), fileName);

        return entityBuilder.build();
    }
}