package com.axiel7.moelist.model;

import com.google.gson.annotations.SerializedName;

public abstract class AccessToken2 {
    @SerializedName("token_type")
    private String tokenType;
    @SerializedName("expires_in")
    private Integer expiresIn;
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("refresh_token")
    private String refreshToken;

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
