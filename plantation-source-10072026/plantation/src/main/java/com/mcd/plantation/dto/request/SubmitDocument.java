package com.mcd.plantation.dto.request;

public class SubmitDocument {

    private String name;
    private String document;
    private String contentType;

    public SubmitDocument() {

    }

    public SubmitDocument(String name, String document, String contentType) {
        this.name = name;
        this.document = document;
        this.contentType = contentType;
    }

    public String getName() {
        return name;
    }

    public String getDocument() {
        return document;
    }

    public String getContentType() {
        return contentType;
    }
}
