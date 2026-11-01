package com.million.learnify;

import java.util.Map;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

/**
 * SettingsApiService - Retrofit Interface for Learnify Settings and Governance Endpoints.
 */
public interface SettingsApiService {

    @GET("api/student/preferences")
    Call<ResponseBody> getStudentPreferences();

    @PUT("api/student/preferences")
    Call<ResponseBody> updateStudentPreferences(@Body RequestBody requestBody);

    @GET("api/admin/system/config")
    Call<ResponseBody> getAdminSystemConfig();

    @PUT("api/admin/system/config")
    Call<ResponseBody> updateAdminSystemConfig(@Body RequestBody requestBody);

    @GET("api/system/features")
    Call<Map<String, Object>> getPublicFeatures();
}
