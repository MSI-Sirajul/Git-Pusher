package com.sirajul.gitpuaher.models;

public class Repository {
    private String name;
    private String owner;
    private String description;
    private String createdAt;
    private String defaultBranch;
    private boolean isPrivate;
    private int size;
    private int stars;
    private int forks;

    public Repository() {}

    public Repository(String name, String owner, String description) {
        this.name = name;
        this.owner = owner;
        this.description = description;
        this.defaultBranch = "main";
        this.isPrivate = false;
    }

    public Repository(String name, String owner, String description, boolean isPrivate) {
        this.name = name;
        this.owner = owner;
        this.description = description;
        this.isPrivate = isPrivate;
        this.defaultBranch = "main";
    }

    // Getters and Setters
    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }
    
    public String getOwner() { 
        return owner; 
    }
    
    public void setOwner(String owner) { 
        this.owner = owner; 
    }
    
    public String getDescription() { 
        return description; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }
    
    public String getCreatedAt() { 
        return createdAt; 
    }
    
    public void setCreatedAt(String createdAt) { 
        this.createdAt = createdAt; 
    }
    
    public String getDefaultBranch() { 
        return defaultBranch; 
    }
    
    public void setDefaultBranch(String defaultBranch) { 
        this.defaultBranch = defaultBranch; 
    }
    
    public boolean isPrivate() { 
        return isPrivate; 
    }
    
    public void setPrivate(boolean isPrivate) { 
        this.isPrivate = isPrivate; 
    }
    
    public int getSize() { 
        return size; 
    }
    
    public void setSize(int size) { 
        this.size = size; 
    }
    
    public int getStars() { 
        return stars; 
    }
    
    public void setStars(int stars) { 
        this.stars = stars; 
    }
    
    public int getForks() { 
        return forks; 
    }
    
    public void setForks(int forks) { 
        this.forks = forks; 
    }

    // Utility methods
    public String getFullName() {
        return owner + "/" + name;
    }

    public String getGitUrl() {
        return "https://github.com/" + owner + "/" + name + ".git";
    }

    public String getWebUrl() {
        return "https://github.com/" + owner + "/" + name;
    }

    @Override
    public String toString() {
        return "Repository{" +
                "name='" + name + '\'' +
                ", owner='" + owner + '\'' +
                ", description='" + description + '\'' +
                ", isPrivate=" + isPrivate +
                ", defaultBranch='" + defaultBranch + '\'' +
                '}';
    }
}