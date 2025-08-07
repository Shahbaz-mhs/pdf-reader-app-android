package com.pdfreader.freecodedfapp.models;

public class PdfModel {
    private String absolutePath;
    private String createdAt;
    private boolean isDirectory;
    private boolean isStarred;
    private Long lastModified;
    private Long length;
    private String name;
    private int numItems;
    private String pdfUri;
    private String thumbUri;

    public PdfModel() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public Long getLength() {
        return this.length;
    }

    public void setLength(Long l) {
        this.length = l;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(String str) {
        this.createdAt = str;
    }

    public String getAbsolutePath() {
        return this.absolutePath;
    }

    public void setAbsolutePath(String str) {
        this.absolutePath = str;
    }

    public Long getLastModified() {
        return this.lastModified;
    }

    public void setLastModified(Long l) {
        this.lastModified = l;
    }

    public String getPdfUri() {
        return this.pdfUri;
    }

    public void setPdfUri(String uri) {
        this.pdfUri = uri;
    }

    public String getThumbUri() {
        return this.thumbUri;
    }

    public void setThumbUri(String uri) {
        this.thumbUri = uri;
    }

    public boolean isStarred() {
        return this.isStarred;
    }

    public void setStarred(boolean z) {
        this.isStarred = z;
    }

    public boolean isDirectory() {
        return this.isDirectory;
    }

    public void setDirectory(boolean z) {
        this.isDirectory = z;
    }

    public int getNumItems() {
        return this.numItems;
    }

    public void setNumItems(int i) {
        this.numItems = i;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        PdfModel pdfModel = (PdfModel) obj;
        
        // Compare all relevant fields
        if (isDirectory != pdfModel.isDirectory) return false;
        if (isStarred != pdfModel.isStarred) return false;
        if (numItems != pdfModel.numItems) return false;
        
        // For String fields, use equals() and handle null cases
        if (name != null ? !name.equals(pdfModel.name) : pdfModel.name != null) return false;
        
        // For path comparison, this is critical for rename operations
        if (absolutePath != null ? !absolutePath.equals(pdfModel.absolutePath) : pdfModel.absolutePath != null)
            return false;
            
        // Compare other fields
        if (length != null ? !length.equals(pdfModel.length) : pdfModel.length != null) return false;
        if (lastModified != null ? !lastModified.equals(pdfModel.lastModified) : pdfModel.lastModified != null)
            return false;
        if (createdAt != null ? !createdAt.equals(pdfModel.createdAt) : pdfModel.createdAt != null)
            return false;
        if (pdfUri != null ? !pdfUri.equals(pdfModel.pdfUri) : pdfModel.pdfUri != null)
            return false;
        return thumbUri != null ? thumbUri.equals(pdfModel.thumbUri) : pdfModel.thumbUri == null;
    }
    
    @Override
    public int hashCode() {
        int result = absolutePath != null ? absolutePath.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (length != null ? length.hashCode() : 0);
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (pdfUri != null ? pdfUri.hashCode() : 0);
        result = 31 * result + (thumbUri != null ? thumbUri.hashCode() : 0);
        result = 31 * result + (isDirectory ? 1 : 0);
        result = 31 * result + (isStarred ? 1 : 0);
        result = 31 * result + numItems;
        return result;
    }
}
