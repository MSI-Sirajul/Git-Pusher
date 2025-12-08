package com.gitpusher.pro;

public class RepoRequest {
    private String name;
    private boolean isPrivate; // 'private' is a reserved keyword in Java

    public RepoRequest(String name, boolean isPrivate) {
        this.name = name;
        this.isPrivate = isPrivate;
    }
}