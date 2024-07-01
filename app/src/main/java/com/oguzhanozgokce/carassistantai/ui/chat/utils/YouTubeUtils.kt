package com.oguzhanozgokce.carassistantai.ui.chat.utils

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import com.oguzhanozgokce.carassistantai.common.Constant.YouTube_API_KEY
import com.oguzhanozgokce.carassistantai.ui.chat.ChatBotFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object YouTubeUtils {

    fun searchAndOpenYouTube(fragment: ChatBotFragment, query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val videoId = getFirstVideoId(query)
            if (videoId != null) {
                withContext(Dispatchers.Main) {
                    openYouTubeWithVideoId(fragment, videoId)
                }
            }
        }
    }

    private fun getFirstVideoId(query: String): String? {
        val youtubeHelper = YouTubeHelper(YouTube_API_KEY)
        val results = youtubeHelper.searchVideos(query)
        return results?.firstOrNull()?.id?.videoId
    }

    private fun openYouTubeWithVideoId(fragment: Fragment, videoId: String) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.youtube.com/watch?v=$videoId")
        ).apply {
            setPackage("com.google.android.youtube")
        }
        if (intent.resolveActivity(fragment.requireActivity().packageManager) != null) {
            fragment.startActivity(intent)
        } else {
            val webIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId"))
            fragment.startActivity(webIntent)
        }
    }


}
