package com.sirajul.gitpuaher.interfaces;

import com.sirajul.gitpuaher.models.Repository;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GitHubApiInterface {
    
    // User endpoints
    @GET("/user")
    Call<ResponseBody> getUserInfo(
        @Header("Authorization") String credentials,
        @Header("User-Agent") String userAgent
    );

    // Repository endpoints
    @POST("/user/repos")
    Call<ResponseBody> createRepository(
        @Header("Authorization") String credentials,
        @Header("User-Agent") String userAgent,
        @Body RepositoryCreateRequest repository
    );

    @GET("/user/repos")
    Call<List<Repository>> getUserRepositories(
        @Header("Authorization") String credentials,
        @Header("User-Agent") String userAgent,
        @Query("per_page") int perPage,
        @Query("page") int page
    );

    @GET("/repos/{owner}/{repo}")
    Call<Repository> getRepository(
        @Header("Authorization") String credentials,
        @Header("User-Agent") String userAgent,
        @Path("owner") String owner,
        @Path("repo") String repo
    );

    @GET("/repos/{owner}/{repo}/contents/{path}")
    Call<ResponseBody> getRepositoryContents(
        @Header("Authorization") String credentials,
        @Header("User-Agent") String userAgent,
        @Path("owner") String owner,
        @Path("repo") String repo,
        @Path("path") String path
    );

    @PUT("/repos/{owner}/{repo}/contents/{path}")
    Call<ResponseBody> uploadFile(
        @Header("Authorization") String credentials,
        @Header("User-Agent") String userAgent,
        @Path("owner") String owner,
        @Path("repo") String repo,
        @Path("path") String path,
        @Body FileUploadRequest fileRequest
    );

    @DELETE("/repos/{owner}/{repo}")
    Call<ResponseBody> deleteRepository(
        @Header("Authorization") String credentials,
        @Header("User-Agent") String userAgent,
        @Path("owner") String owner,
        @Path("repo") String repo
    );

    // Check if repository exists - Using GET instead of HEAD
    @GET("/repos/{owner}/{repo}")
    Call<Void> checkRepositoryExists(
        @Header("Authorization") String credentials,
        @Header("User-Agent") String userAgent,
        @Path("owner") String owner,
        @Path("repo") String repo
    );

    // Branch operations
    @GET("/repos/{owner}/{repo}/branches")
    Call<ResponseBody> getBranches(
        @Header("Authorization") String credentials,
        @Header("User-Agent") String userAgent,
        @Path("owner") String owner,
        @Path("repo") String repo
    );

    // Rate limit check
    @GET("/rate_limit")
    Call<ResponseBody> getRateLimit(
        @Header("Authorization") String credentials,
        @Header("User-Agent") String userAgent
    );

    // Request classes for API calls
    class RepositoryCreateRequest {
        public String name;
        public String description;
        public String homepage;
        public boolean isPrivate;
        public boolean auto_init;
        public String gitignore_template;
        public String license_template;

        public RepositoryCreateRequest(String name, String description, boolean isPrivate) {
            this.name = name;
            this.description = description;
            this.isPrivate = isPrivate;
            this.auto_init = false;
        }
    }

    class FileUploadRequest {
        public String message;
        public String content;
        public String branch;
        public String sha; // Required for updates

        public FileUploadRequest(String message, String content) {
            this.message = message;
            this.content = content;
            this.branch = "main";
        }

        public FileUploadRequest(String message, String content, String branch) {
            this.message = message;
            this.content = content;
            this.branch = branch;
        }
    }

    class FileUpdateRequest extends FileUploadRequest {
        public String sha;

        public FileUpdateRequest(String message, String content, String sha) {
            super(message, content);
            this.sha = sha;
        }
    }
}