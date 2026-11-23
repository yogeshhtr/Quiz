package com.yogi.quiz

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private val h = Handler(Looper.getMainLooper())
    private lateinit var root: FrameLayout
    private var tone: ToneGenerator? = null

    // ---- game state
    private enum class Mode { QUICK, DAILY, DUEL }
    private var mode = Mode.QUICK
    private var gameTitle = ""
    private var qs: List<Q> = emptyList()
    private var i = 0
    private val score = IntArray(2)
    private val right = IntArray(2)
    private var streak = 0
    private var bestStreak = 0
    private var used50 = false
    private var usedTime = false
    private var usedSkip = false
    private var answered = false
    private var timeLeft = 20f
    private var timer: ValueAnimator? = null
    private var level: Int? = null
    private var lastPlay: (() -> Unit)? = null

    private val SECONDS = 20f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Store.init(this)
        window.statusBarColor = C.BG
        window.navigationBarColor = C.BG
        root = FrameLayout(this)
        root.setBackgroundColor(C.BG)
        setContentView(root)
        level = Store.int("level", -1).let { if (it < 0) null else it }
        menu()
    }

    override fun onDestroy() {
        timer?.cancel()
        tone?.release()
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (qs.isNotEmpty()) { timer?.cancel(); qs = emptyList(); menu() } else @Suppress("DEPRECATION") super.onBackPressed()
    }

    private fun beep(ok: Boolean) {
        if (!Store.bool("sound", true)) return
        try {
            if (tone == null) tone = ToneGenerator(AudioManager.STREAM_MUSIC, 55)
            tone?.startTone(if (ok) ToneGenerator.TONE_PROP_ACK else ToneGenerator.TONE_PROP_NACK, 180)
        } catch (_: Exception) {}
    }

    private fun page(): LinearLayout {
        root.removeAllViews()
        val sv = ScrollView(this)
        val col = vbox()
        col.setPadding(0, dp(30), 0, dp(26))
        sv.addView(col)
        root.addView(sv)
        return col
    }

    private fun today() = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())

    // ===================================================================== menu

    private fun menu() {
        qs = emptyList()
        val col = page()
        col.addView(label("Yogi Quiz", 34f, C.ACCENT, true).apply { gravity = Gravity.CENTER })
        col.addView(label("How much do you know? 🤔", 15f, C.SUB).apply { gravity = Gravity.CENTER; setPadding(0, 0, 0, dp(10)) })

        // daily challenge
        val done = Store.str("daily_day") == today()
        val d = card(0xFF3B1E6E.toInt())
        val dr = hbox()
        dr.addView(label("📅", 34f).lin(WRAP, WRAP, r = dp(14)))
        val dm = vbox()
        dm.addView(label("Daily challenge", 18f, C.TEXT, true))
        dm.addView(label(if (done) "Done today: ${Store.int("daily_score", 0)} points · come back tomorrow"
        else "10 questions, the same for everyone today · 🔥 ${Store.int("daily_streak", 0)} day streak", 13f, C.SUB))
        dr.addView(dm.lin(0, WRAP, 1f))
        d.addView(dr)
        d.setOnClickListener { if (done) toast("Come back tomorrow for a new challenge") else startDaily() }
        col.addView(d)

        // offline packs
        col.addView(section("PLAY ANYWHERE (OFFLINE)"))
        grid(col, listOf(Triple("🇳🇵", "Nepal", "nepal"), Triple("🧠", "General", "general"), Triple("🔬", "Science", "science"), Triple("🎲", "All offline", ""))) { t ->
            startQuick(t.second, null, t.third)
        }
        col.addView(section("THOUSANDS MORE (INTERNET)"))
        grid(col, Online.CATS.map { Triple(it.first.third, it.first.second, it.second.toString()) }) { t ->
            startQuick(t.second, t.third.toInt(), null)
        }

        col.addView(section("DIFFICULTY"))
        val lv = hbox()
        lv.setPadding(dp(16), 0, dp(16), 0)
        for ((k, n) in listOf(-1 to "Any", 0 to "Easy", 1 to "Medium", 2 to "Hard")) {
            lv.addView(chip(n, (level ?: -1) == k) { level = if (k < 0) null else k; Store.setInt("level", k); menu() })
        }
        col.addView(lv)

        val duel = card()
        duel.addView(label("👥  Two-player duel", 17f, C.TEXT, true))
        duel.addView(label("Pass the phone: player 1 and player 2 take turns. Mixed offline questions.", 13f, C.SUB))
        duel.setOnClickListener { startDuel() }
        col.addView(duel)

        val st = card()
        val games = Store.int("games", 0)
        val ans = Store.int("answered", 0)
        st.addView(label("Your stats", 13f, C.SUB, true))
        st.addView(label("🎮 $games games · ✅ ${if (ans > 0) Store.int("correct", 0) * 100 / ans else 0}% right · 🏆 best ${Store.int("best", 0)} · 🔥 best streak ${Store.int("best_streak", 0)}",
            14f, C.TEXT).lin(t = dp(4)))
        val snd = hbox()
        snd.addView(label("Sound", 14f, C.SUB).lin(0, WRAP, 1f))
        snd.addView(chip("On", Store.bool("sound", true)) { Store.setBool("sound", true); menu() })
        snd.addView(chip("Off", !Store.bool("sound", true)) { Store.setBool("sound", false); menu() })
        st.addView(snd.lin(t = dp(8)))
        col.addView(st)
        col.addView(label("World questions: Open Trivia Database (CC BY-SA 4.0)", 11f, C.SUB).apply { gravity = Gravity.CENTER; setPadding(0, dp(8), 0, 0) })
    }

    private fun section(t: String): View = label(t, 12f, C.SUB, true).apply { setPadding(dp(22), dp(16), dp(22), dp(6)) }

    private fun grid(col: LinearLayout, items: List<Triple<String, String, String>>, go: (Triple<String, String, String>) -> Unit) {
        var row: LinearLayout? = null
        for ((k, t) in items.withIndex()) {
            if (k % 3 == 0) {
                row = hbox()
                row.setPadding(dp(12), 0, dp(12), 0)
                col.addView(row)
            }
            val c = vbox()
            c.gravity = Gravity.CENTER
            c.background = ripple(C.CARD, 18f)
            c.setPadding(0, dp(14), 0, dp(12))
            c.addView(label(t.first, 26f).apply { gravity = Gravity.CENTER })
            c.addView(label(t.second, 13f, C.TEXT, true).apply { gravity = Gravity.CENTER }.lin(t = dp(4)))
            c.setOnClickListener { go(t) }
            row?.addView(c, LinearLayout.LayoutParams(0, WRAP, 1f).apply { setMargins(dp(4), dp(4), dp(4), dp(4)) })
        }
        val rest = items.size % 3
        if (rest != 0) for (k in 0 until 3 - rest) row?.addView(View(this), LinearLayout.LayoutParams(0, 1, 1f).apply { setMargins(dp(4), 0, dp(4), 0) })
    }

    // ===================================================================== starting

    private fun loading(t: String) {
        root.removeAllViews()
        root.addView(label(t, 16f, C.SUB).apply { gravity = Gravity.CENTER }, FrameLayout.LayoutParams(MATCH, MATCH))
    }

    private fun startQuick(name: String, online: Int?, offline: String?) {
        lastPlay = { startQuick(name, online, offline) }
        mode = Mode.QUICK
        gameTitle = name
        if (online == null) {
            begin(Bank.pick(offline?.ifEmpty { null }, level, 10))
            return
        }
        loading("Getting questions…")
        val lv = level
        Thread {
            val list = try { Online.fetch(online, lv) } catch (e: Exception) { null }
            h.post {
                if (list.isNullOrEmpty()) {
                    toast("No internet, so here are offline questions instead")
                    gameTitle = "$name (offline)"
                    begin(Bank.pick(null, lv, 10))
                } else begin(list)
            }
        }.start()
    }

    private fun startDaily() {
        lastPlay = null
        mode = Mode.DAILY
        gameTitle = "Daily challenge"
        begin(Bank.pick(null, null, 10, Random(today().toLong())))
    }

    private fun startDuel() {
        lastPlay = { startDuel() }
        mode = Mode.DUEL
        gameTitle = "Duel"
        begin(Bank.pick(null, level, 12))
    }

    private fun begin(list: List<Q>) {
        qs = list
        i = 0
        score.fill(0)
        right.fill(0)
        streak = 0
        bestStreak = 0
        used50 = false; usedTime = false; usedSkip = false
        question()
    }

    // ===================================================================== a question

    private fun player() = if (mode == Mode.DUEL) i % 2 else 0

    private fun question() {
        if (i >= qs.size) { results(); return }
        answered = false
        val q = qs[i]
        val col = page()
        val top = hbox()
        top.setPadding(dp(8), 0, dp(18), 0)
        top.addView(label("✕", 22f, C.SUB).apply { setPadding(dp(12), dp(4), dp(12), dp(4)); setOnClickListener { timer?.cancel(); qs = emptyList(); menu() } })
        top.addView(label("$gameTitle · ${i + 1}/${qs.size}", 14f, C.SUB, true).lin(0, WRAP, 1f))
        top.addView(label(if (mode == Mode.DUEL) "P1 ${score[0]}  ·  P2 ${score[1]}" else "⭐ ${score[0]}", 16f, C.ACCENT, true))
        col.addView(top)
        if (mode == Mode.DUEL) col.addView(label(if (player() == 0) "🔵 Player 1's turn" else "🟣 Player 2's turn", 16f,
            if (player() == 0) C.BLUE else C.PINK, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(8), 0, 0) })
        // timer bar
        val barBg = FrameLayout(this)
        barBg.background = rounded(C.CARD2, dpf(5f))
        val fill = View(this)
        fill.background = rounded(C.GREEN, dpf(5f))
        barBg.addView(fill, FrameLayout.LayoutParams(MATCH, MATCH))
        col.addView(barBg.lin(MATCH, dp(10), l = dp(18), r = dp(18), t = dp(12)))
        val secs = label("${SECONDS.toInt()}", 13f, C.SUB).apply { gravity = Gravity.END; setPadding(0, dp(2), dp(20), 0) }
        col.addView(secs)
        // question
        val qc = card(C.CARD)
        qc.setPadding(dp(20), dp(22), dp(20), dp(22))
        qc.addView(label(listOf("Easy", "Medium", "Hard")[q.level.coerceIn(0, 2)] + if (streak >= 3) "   🔥 x$streak streak" else "", 12f,
            if (streak >= 3) C.ACCENT else C.SUB, true))
        qc.addView(label(q.text, 21f, C.TEXT, true).lin(t = dp(8)))
        col.addView(qc)
        val buttons = ArrayList<TextView>()
        for ((k, a) in q.answers.withIndex()) {
            val b = label(listOf("A", "B", "C", "D")[k] + "   " + a, 17f, C.TEXT, true)
            b.background = ripple(C.CARD2, 18f)
            b.setPadding(dp(18), dp(16), dp(18), dp(16))
            b.setOnClickListener { answer(q, a, buttons) }
            buttons.add(b)
            col.addView(b.lin(l = dp(14), r = dp(14), t = dp(10)))
        }
        // lifelines
        val life = hbox()
        life.gravity = Gravity.CENTER
        life.setPadding(0, dp(18), 0, 0)
        fun lifeline(t: String, used: Boolean, f: () -> Unit) = life.addView(chip(t, false) { if (!used && !answered) f() }.apply { alpha = if (used) 0.3f else 1f })
        lifeline("½ 50:50", used50) {
            used50 = true
            val wrongs = buttons.filter { !it.text.endsWith("   " + q.right) }.shuffled().take(2)
            for (w in wrongs) { w.alpha = 0.15f; w.isEnabled = false }
            life.getChildAt(0).alpha = 0.3f
        }
        lifeline("⏱ +10 s", usedTime) {
            usedTime = true
            timeLeft = (timeLeft + 10f)
            runTimer(fill, secs, q, buttons, timeLeft)
            life.getChildAt(1).alpha = 0.3f
        }
        lifeline("⏭ Skip", usedSkip) {
            usedSkip = true
            timer?.cancel()
            i++
            question()
        }
        col.addView(life)
        runTimer(fill, secs, q, buttons, SECONDS)
    }

    private fun runTimer(fill: View, secs: TextView, q: Q, buttons: List<TextView>, from: Float) {
        timer?.cancel()
        timeLeft = from
        val total = maxOf(from, SECONDS)
        timer = ValueAnimator.ofFloat(from, 0f).apply {
            duration = (from * 1000).toLong()
            interpolator = LinearInterpolator()
            addUpdateListener {
                timeLeft = it.animatedValue as Float
                fill.scaleX = timeLeft / total
                fill.pivotX = 0f
                fill.background = rounded(if (timeLeft < 5) C.RED else if (timeLeft < 10) C.ACCENT else C.GREEN, dpf(5f))
                secs.text = "${kotlin.math.ceil(timeLeft).toInt()}"
                if (timeLeft <= 0f && !answered) answer(q, null, buttons)
            }
            start()
        }
    }

    private fun answer(q: Q, chosen: String?, buttons: List<TextView>) {
        if (answered) return
        answered = true
        timer?.cancel()
        val ok = chosen == q.right
        val p = player()
        for (b in buttons) {
            val text = b.text.toString().substringAfter("   ")
            when {
                text == q.right -> { b.background = rounded(C.GREEN, dpf(18f)); b.setTextColor(Color.BLACK) }
                text == chosen -> { b.background = rounded(C.RED, dpf(18f)) }
                else -> b.alpha = 0.45f
            }
        }
        if (ok) {
            streak++
            bestStreak = maxOf(bestStreak, streak)
            right[p]++
            val pts = 100 + (timeLeft * 5).toInt() + q.level * 25
            score[p] += if (streak >= 3) pts * 3 / 2 else pts
        } else streak = 0
        beep(ok)
        root.performHapticFeedback(if (ok) HapticFeedbackConstants.KEYBOARD_TAP else HapticFeedbackConstants.LONG_PRESS)
        if (chosen == null) toast("⏰ Time's up!")
        h.postDelayed({ if (qs.isNotEmpty()) { i++; question() } }, if (ok) 1100 else 1900)
    }

    // ===================================================================== results

    private fun results() {
        val total = qs.size
        val col = page()
        Store.setInt("games", Store.int("games", 0) + 1)
        Store.setInt("answered", Store.int("answered", 0) + (if (mode == Mode.DUEL) total else total))
        Store.setInt("correct", Store.int("correct", 0) + right[0] + right[1])
        Store.setInt("best_streak", maxOf(Store.int("best_streak", 0), bestStreak))
        var newBest = false
        if (mode != Mode.DUEL && score[0] > Store.int("best", 0)) { Store.setInt("best", score[0]); newBest = true }
        if (mode == Mode.DAILY) {
            val yesterday = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(System.currentTimeMillis() - 86400000L))
            val s = if (Store.str("daily_day") == yesterday) Store.int("daily_streak", 0) + 1 else 1
            Store.setInt("daily_streak", s)
            Store.setStr("daily_day", today())
            Store.setInt("daily_score", score[0])
        }
        val headline: String
        val sub: String
        if (mode == Mode.DUEL) {
            headline = when {
                score[0] > score[1] -> "🔵 Player 1 wins!"
                score[1] > score[0] -> "🟣 Player 2 wins!"
                else -> "🤝 It's a tie!"
            }
            sub = "Player 1: ${score[0]} points (${right[0]} right)\nPlayer 2: ${score[1]} points (${right[1]} right)"
        } else {
            val pct = right[0] * 100 / total.coerceAtLeast(1)
            headline = when {
                pct == 100 -> "🏆 Perfect!"
                pct >= 80 -> "🌟 Brilliant!"
                pct >= 60 -> "👏 Well done!"
                pct >= 40 -> "🙂 Not bad!"
                else -> "📚 Keep learning!"
            }
            sub = "${right[0]} of $total right · ${score[0]} points" + (if (newBest) "\n🎉 New personal best!" else "") +
                (if (bestStreak >= 3) "\n🔥 Best streak: $bestStreak" else "")
        }
        col.addView(label(headline, 32f, C.ACCENT, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(40), 0, dp(10)) })
        col.addView(label(sub, 17f, C.TEXT).apply { gravity = Gravity.CENTER; setPadding(dp(20), 0, dp(20), dp(24)) })
        lastPlay?.let { go -> col.addView(button("▶  Play again") { go() }.lin(l = dp(30), r = dp(30), b = dp(10))) }
        col.addView(button("📤  Challenge a friend", C.CARD2, C.TEXT) {
            val msg = if (mode == Mode.DUEL) headline else "I scored ${score[0]} points (${right[0]}/$total) in Yogi Quiz · $gameTitle. Can you beat me? 🤔"
            try { startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, msg), "Share")) } catch (_: Exception) {}
        }.lin(l = dp(30), r = dp(30), b = dp(10)))
        col.addView(button("🏠  Menu", C.CARD2, C.TEXT) { qs = emptyList(); menu() }.lin(l = dp(30), r = dp(30)))
        // review
        col.addView(section("ANSWERS"))
        for ((k, q) in qs.withIndex()) {
            val c = card()
            c.addView(label("${k + 1}. ${q.text}", 14f, C.TEXT))
            c.addView(label("✓ ${q.right}", 13f, C.GREEN, true).lin(t = dp(4)))
            col.addView(c)
        }
        qs = emptyList()
        if (right[0] + right[1] >= total * 0.6 || mode == Mode.DUEL) {
            val conf = Confetti(this)
            root.addView(conf, FrameLayout.LayoutParams(MATCH, MATCH))
            conf.go()
        }
    }
}

/** A short burst of confetti for good results. */
class Confetti(ctx: Context) : View(ctx) {
    private class P(var x: Float, var y: Float, val vx: Float, val vy: Float, val c: Int, val s: Float, var r: Float, val vr: Float)
    private val ps = ArrayList<P>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var t = 0f
    private val colors = listOf(0xFFFFC93C.toInt(), 0xFFFF5C8A.toInt(), 0xFF3DDC97.toInt(), 0xFF4FA3FF.toInt(), 0xFFB57CFF.toInt())

    init { isClickable = false; isFocusable = false }

    fun go() {
        post {
            val d = resources.displayMetrics.density
            repeat(140) {
                ps.add(P(width * Random.nextFloat(), -Random.nextFloat() * height * 0.3f, (Random.nextFloat() - 0.5f) * 3 * d,
                    (2 + Random.nextFloat() * 4) * d, colors.random(), (4 + Random.nextFloat() * 5) * d, Random.nextFloat() * 360, (Random.nextFloat() - 0.5f) * 12))
            }
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 3200
                addUpdateListener {
                    t = it.animatedValue as Float
                    for (p in ps) { p.x += p.vx; p.y += p.vy; p.r += p.vr }
                    invalidate()
                }
                start()
            }
        }
    }

    override fun onDraw(c: Canvas) {
        paint.alpha = ((1f - t) * 255).toInt().coerceIn(0, 255)
        for (p in ps) {
            paint.color = p.c
            paint.alpha = ((1f - t) * 255).toInt().coerceIn(0, 255)
            c.save()
            c.rotate(p.r, p.x, p.y)
            c.drawRect(p.x - p.s, p.y - p.s / 2, p.x + p.s, p.y + p.s / 2, paint)
            c.restore()
        }
    }
}
