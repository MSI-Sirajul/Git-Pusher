package com.sirajul.gitpuaher.models;

public class FileItem {
    private String name;
    private String path;
    private boolean isDirectory;
    private long size;
    private String extension;

    public FileItem() {}

    public FileItem(String name, String path, boolean isDirectory, long size) {
        this.name = name;
        this.path = path;
        this.isDirectory = isDirectory;
        this.size = size;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    
    public boolean isDirectory() { return isDirectory; }
    public void setDirectory(boolean directory) { isDirectory = directory; }
    
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    
    public String getExtension() { return extension; }
    public void setExtension(String extension) { this.extension = extension; }
}