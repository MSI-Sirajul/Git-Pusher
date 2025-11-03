package com.sirajul.gitpuaher.models;

public class User {
    private String username;
    private String token;
    private String email;
    private String name;
    private String avatarUrl;
    private int publicRepos;
    private int followers;
    private int following;
    private boolean isLoggedIn;

    public User() {}

    public User(String username, String token) {
        this.username = username;
        this.token = token;
        this.isLoggedIn = true;
    }

    public User(String username, String token, String email, String name) {
        this.username = username;
        this.token = token;
        this.email = email;
        this.name = name;
        this.isLoggedIn = true;
    }

    // Getters and Setters
    public String getUsername() { 
        return username; 
    }
    
    public void setUsername(String username) { 
        this.username = username; 
    }
    
    public String getToken() { 
        return token; 
    }
    
    public void setToken(String token) { 
        this.token = token; 
    }
    
    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }
    
    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }
    
    public String getAvatarUrl() { 
        return avatarUrl; 
    }
    
    public void setAvatarUrl(String avatarUrl) { 
        this.avatarUrl = avatarUrl; 
    }
    
    public int getPublicRepos() { 
        return publicRepos; 
    }
    
    public void setPublicRepos(int publicRepos) { 
        this.publicRepos = publicRepos; 
    }
    
    public int getFollowers() { 
        return followers; 
    }
    
    public void setFollowers(int followers) { 
        this.followers = followers; 
    }
    
    public int getFollowing() { 
        return following; 
    }
    
    public void setFollowing(int following) { 
        this.following = following; 
    }
    
    public boolean isLoggedIn() { 
        return isLoggedIn; 
    }
    
    public void setLoggedIn(boolean loggedIn) { 
        isLoggedIn = loggedIn; 
    }

    // Utility methods
    public String getProfileUrl() {
        return "https://github.com/" + username;
    }

    public boolean hasValidCredentials() {
        return username != null && !username.isEmpty() && 
               token != null && !token.isEmpty();
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", publicRepos=" + publicRepos +
                ", followers=" + followers +
                ", following=" + following +
                ", isLoggedIn=" + isLoggedIn +
                '}';
    }
}