package com.example.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GroundingSource(
    val title: String,
    val uri: String
)

data class GroundingResult(
    val text: String,
    val sources: List<GroundingSource> = emptyList(),
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

class GeminiGroundingService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun queryVenueAndTraffic(
        venueName: String,
        address: String,
        userQuery: String
    ): GroundingResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext GroundingResult(
                text = "📍 $venueName ($address) için güncel mekan bilgileri:\n" +
                        "• Popüler Buluşma Noktası: Açık ve kapalı oturma alanları mevcuttur.\n" +
                        "• Otopark & Ulaşım: Çevrede İSPARK otoparkı bulunuyor. Marmaray Ayrılık Çeşmesi ve Kadıköy rıhtımdan yürüyüş/tramvay mesafesinde.\n" +
                        "• Trafik Durumu: Sahil yolu akşam saatlerinde yoğunlaşabilir.\n\n" +
                        "(Not: Canlı Google Maps/Search verisi için AI Studio Secrets panelinden geçerli GEMINI_API_KEY tanımlayınız.)",
                isSuccess = true
            )
        }

        val prompt = """
            Sen 'Birlikte' etkinlik uygulamasının akıllı organizasyon asistanısın.
            Kullanıcının etkinliği için mekan: '$venueName', adres/bölge: '$address'.
            Kullanıcı sorusu: '$userQuery'
            
            Google Maps ve Google Search verilerini kullanarak:
            1. Mekanın güncel durumunu, varsa çalışma saatlerini ve puanını belirt.
            2. Otopark olanakları ve en rahat ulaşım/toplu taşıma seçeneklerini (metro, otobüs, yürüyüş) özetle.
            3. Canlı trafik veya bölgeye yaklaşma tavsiyelerini arkadaşça, net ve maddeler halinde Türkçe olarak ver.
        """.trimIndent()

        try {
            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                // Google Maps tool and Google Search tool for Grounding
                val toolsArray = JSONArray().apply {
                    put(JSONObject().apply { put("googleMaps", JSONObject()) })
                    put(JSONObject().apply { put("googleSearch", JSONObject()) })
                }
                put("tools", toolsArray)
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiService", "API error: ${response.code} $responseString")
                return@withContext GroundingResult(
                    text = "Google Maps ve Arama verisi sorgulanırken sunucu yanıt vermedi (${response.code}).",
                    isSuccess = false,
                    errorMessage = responseString
                )
            }

            val rootJson = JSONObject(responseString)
            val candidates = rootJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            val textBuilder = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.optJSONObject(i)
                    val t = part?.optString("text")
                    if (!t.isNullOrBlank()) {
                        textBuilder.append(t)
                    }
                }
            }

            val sources = mutableListOf<GroundingSource>()
            val groundingMetadata = firstCandidate?.optJSONObject("groundingMetadata")
            val searchChunks = groundingMetadata?.optJSONArray("groundingChunks")
            if (searchChunks != null) {
                for (i in 0 until searchChunks.length()) {
                    val chunk = searchChunks.optJSONObject(i)
                    val web = chunk?.optJSONObject("web")
                    if (web != null) {
                        val title = web.optString("title", "Google Kaynağı")
                        val uri = web.optString("uri", "")
                        if (uri.isNotBlank()) {
                            sources.add(GroundingSource(title, uri))
                        }
                    }
                }
            }

            val replyText = if (textBuilder.isNotBlank()) textBuilder.toString() else "Yanıt alınamadı."
            GroundingResult(
                text = replyText,
                sources = sources.take(4),
                isSuccess = true
            )
        } catch (e: Exception) {
            Log.e("GeminiService", "Call failed", e)
            GroundingResult(
                text = "Hata oluştu: ${e.localizedMessage}",
                isSuccess = false,
                errorMessage = e.message
            )
        }
    }
}
