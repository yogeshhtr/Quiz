package com.yogi.quiz

import android.util.Base64
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Questions from the Open Trivia Database (opentdb.com, CC BY-SA 4.0). Needs internet. */
object Online {
    /** (our id, name, emoji, opentdb category or 0 = any) */
    val CATS = listOf(
        Triple("any", "Mixed", "🎲") to 0,
        Triple("gk", "General", "🧠") to 9,
        Triple("sci", "Science", "🔬") to 17,
        Triple("hist", "History", "🏛") to 23,
        Triple("geo", "Geography", "🌍") to 22,
        Triple("sport", "Sports", "⚽") to 21,
        Triple("film", "Movies", "🎬") to 11,
        Triple("music", "Music", "🎵") to 12,
        Triple("comp", "Computers", "💻") to 18,
        Triple("math", "Maths", "➗") to 19,
        Triple("animal", "Animals", "🐾") to 27,
        Triple("myth", "Mythology", "⚡") to 20,
        Triple("books", "Books", "📚") to 10,
        Triple("games", "Video games", "🎮") to 15
    )

    private fun b64(s: String) = String(Base64.decode(s, Base64.DEFAULT), Charsets.UTF_8)

    /** [level] null = any. Throws when offline. */
    fun fetch(category: Int, level: Int?, n: Int = 10): List<Q> {
        var url = "https://opentdb.com/api.php?amount=$n&type=multiple&encode=base64"
        if (category > 0) url += "&category=$category"
        if (level != null) url += "&difficulty=" + listOf("easy", "medium", "hard")[level]
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 10000
        c.readTimeout = 15000
        c.setRequestProperty("User-Agent", "YogiQuiz")
        val body = try { c.inputStream.bufferedReader().use { it.readText() } } finally { c.disconnect() }
        val o = JSONObject(body)
        val code = o.optInt("response_code")
        if (code == 1 && level != null) return fetch(category, null, n)   // not enough at this level: any level
        if (code == 5) throw IllegalStateException("Too many requests, wait a few seconds")
        val a = o.optJSONArray("results") ?: return emptyList()
        return (0 until a.length()).map { i ->
            val r = a.getJSONObject(i)
            val wrong = r.getJSONArray("incorrect_answers").let { w -> (0 until w.length()).map { b64(w.getString(it)) } }
            Q(b64(r.getString("category")), when (b64(r.getString("difficulty"))) { "easy" -> 0; "medium" -> 1; else -> 2 },
                b64(r.getString("question")), b64(r.getString("correct_answer")), wrong, "Open Trivia DB")
        }
    }
}
