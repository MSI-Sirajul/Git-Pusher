package com.gitpusher.pro;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface GitHubApiService {
    
    // ১. ইউজারনেম পাওয়ার জন্য
    @GET("user")
    Call<ResponseBody> getUser(@Header("Authorization") String token);

    // ২. নির্দিষ্ট রিপোজিটরি আছে কিনা চেক করার জন্য
    @GET("repos/{owner}/{repo}")
    Call<ResponseBody> getRepo(
            @Header("Authorization") String token,
            @Path("owner") String owner,
            @Path("repo") String repo
    );

    // ৩. নতুন রিপোজিটরি তৈরি করার জন্য
    @POST("user/repos")
    Call<ResponseBody> createRepo(
            @Header("Authorization") String token,
            @Body RepoRequest request
    );
}