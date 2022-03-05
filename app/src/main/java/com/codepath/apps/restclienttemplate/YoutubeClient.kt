package com.codepath.apps.restclienttemplate

import com.google.api.client.auth.oauth2.BearerToken
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.auth.oauth2.TokenResponse
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.PlaylistListResponse
import com.google.api.services.youtube.model.VideoListResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class YoutubeClient(accessToken: String){
    private val youtube: YouTube
    init {
        val tokenResponse = TokenResponse()
        tokenResponse.accessToken = accessToken
        val credential: Credential = createCredentialWithAccessTokenOnly(tokenResponse)
        youtube = YouTube.Builder(NetHttpTransport(), JacksonFactory(), credential).build()
        
    }
    private fun createCredentialWithAccessTokenOnly(tokenResponse: TokenResponse): Credential {
        return Credential(BearerToken.authorizationHeaderAccessMethod()).setFromTokenResponse(
            tokenResponse
        )
    }

    fun getLikedVideos(handler: YoutubeResponseHandler<VideoListResponse>) {
        val request: YouTube.Videos.List = youtube.videos().list("snippet,contentDetails,statistics")
        val coroutineScope = MainScope()
        coroutineScope.launch {
            val defer = async(Dispatchers.IO) {
                request.setMyRating("like").execute()
            }
            handler.onResponse(defer.await())
        }
    }

    fun getUserPlaylists(handler: YoutubeResponseHandler<PlaylistListResponse>) {
        val request = youtube.playlists().list("snippet,contentDetails")
        val coroutineScope = MainScope()
        coroutineScope.launch {
            val defer = async(Dispatchers.IO) {
                request.setMaxResults(10L).setMine(true).execute()
            }
            handler.onResponse(defer.await())
        }
    }




}
