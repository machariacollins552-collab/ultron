package com.kai.assistant

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object AiClient {
    private val client = OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS).callTimeout(70, TimeUnit.SECONDS).build()
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    fun reply(provider: String, model: String, apiKey: String, prompt: String): String {
        require(apiKey.isNotBlank()) { "API key is empty." }
        return when (provider) {
            "Gemini" -> gemini(model, apiKey, prompt)
            "OpenAI" -> openAi("https://api.openai.com/v1/chat/completions", model, apiKey, prompt)
            else -> openAi("https://api.groq.com/openai/v1/chat/completions", model, apiKey, prompt)
        }
    }

    private fun openAi(url: String, model: String, key: String, prompt: String): String {
        val body = JSONObject().put("model", model)
            .put("messages", org.json.JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
            .put("temperature", 0.4).toString().toRequestBody(jsonType)
        val request = Request.Builder().url(url).header("Authorization", "Bearer $key")
            .header("Content-Type", "application/json").post(body).build()
        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IllegalStateException(providerError(response.code, text))
            return JSONObject(text).getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").getString("content")
        }
    }

    private fun gemini(model: String, key: String, prompt: String): String {
        val encodedKey = URLEncoder.encode(key, "UTF-8")
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$encodedKey"
        val body = JSONObject().put("contents", org.json.JSONArray().put(
            JSONObject().put("parts", org.json.JSONArray().put(JSONObject().put("text", prompt)))
        )).toString().toRequestBody(jsonType)
        val request = Request.Builder().url(url).header("Content-Type", "application/json").post(body).build()
        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IllegalStateException(providerError(response.code, text))
            return JSONObject(text).getJSONArray("candidates").getJSONObject(0)
                .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
        }
    }

    private fun providerError(code: Int, body: String): String {
        val detail = runCatching {
            val root = JSONObject(body)
            root.optJSONObject("error")?.optString("message") ?: root.optString("message")
        }.getOrNull().orEmpty()
        return when (code) {
            401, 403 -> "Provider rejected the API key (HTTP $code). Check that it is correct and active."
            404 -> "Model or endpoint not found (HTTP 404). Check the model name in Settings."
            429 -> "Provider rate limit or quota reached (HTTP 429). Try later or check your provider quota."
            else -> "Provider request failed (HTTP $code). ${detail.take(240)}"
        }
    }
}
