package gg.rebelsrising.aom.voobly.stats.core.scraper

import gg.rebelsrising.aom.voobly.stats.core.config.VooblyConfig
import org.jsoup.Connection
import org.jsoup.Jsoup

class Session(private val config: VooblyConfig) {

    companion object {

        const val CONTENT_OFFSET = 19 // Fixed post request content length, added to user/pass length.
        const val USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:91.0) Gecko/20100101 Firefox/91.0"
        const val LOGIN_URL = "https://www.voobly.com/login"
        const val LOGIN_AUTH_URL = "https://www.voobly.com/login/auth"

    }

    private val getHeaderMap = mutableMapOf(
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
        "Accept-Encoding" to "gzip, deflate",
        "Accept-Language" to "de-CH",
        "Alt-Used" to "www.voobly.com",
        "Connection" to "keep-alive",
        "DNT" to "1",
        "Host" to "www.voobly.com",
        "Sec-Fetch-Dest" to "document",
        "Sec-Fetch-Mode" to "navigate",
        "Sec-Fetch-Site" to "same-origin",
        "Sec-Fetch-User" to "?1",
        "TE" to "trailers",
        "Upgrade-Insecure-Requests" to "1"
    )

    private val postHeaderMap = mutableMapOf(
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
        "Accept-Encoding" to "gzip, deflate, br",
        "Accept-Language" to "de-CH",
        "Alt-Used" to "www.voobly.com",
        "Connection" to "keep-alive",
        "Content-Length" to (CONTENT_OFFSET + config.user.length + config.pass.length).toString(),
        "Content-Type" to "application/x-www-form-urlencoded",
        "DNT" to "1",
        "Host" to "www.voobly.com",
        "Origin" to "https://www.voobly.com",
        "Referer" to "https://www.voobly.com/login",
        "Sec-Fetch-Dest" to "document",
        "Sec-Fetch-Mode" to "navigate",
        "Sec-Fetch-Site" to "same-origin",
        "Sec-Fetch-User" to "?1",
        "Upgrade-Insecure-Requests" to "1"
    )

    var sessionCookies: MutableMap<String, String> = mutableMapOf()

    fun isLoggedIn(): Boolean {
        TODO("Implement this.")
    }

    fun getRequest(url: String, cookies: MutableMap<String, String> = sessionCookies): Connection.Response {
        return Jsoup.connect(url)
            .headers(getHeaderMap)
            .userAgent(USER_AGENT)
            .cookies(cookies)
            .method(Connection.Method.GET)
            .execute()
    }

    fun postRequest(
        url: String,
        data: MutableMap<String, String> = mutableMapOf(),
        cookies: MutableMap<String, String> = sessionCookies
    ): Connection.Response {
        return Jsoup.connect(url)
            .headers(postHeaderMap)
            .userAgent(USER_AGENT)
            .data(data)
            .cookies(cookies)
            .method(Connection.Method.POST)
            .execute()
    }

    fun login(): Session {
        // Perform get request to login page (to not seem suspicious).
        val get = getRequest(LOGIN_URL)

        val data = mutableMapOf(
            "username" to config.user,
            "password" to config.pass,
        )

        // Login.
        val post = postRequest(LOGIN_AUTH_URL, data, get.cookies())

        sessionCookies = post.cookies()
        sessionCookies.remove("vbly_session2")
        sessionCookies.remove("vbly_session3")
        // Do NOT set vbly_GMT_bias or timestamps will be wrong.

        return this
    }

}
