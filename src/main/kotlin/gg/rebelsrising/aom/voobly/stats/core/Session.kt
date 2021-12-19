package gg.rebelsrising.aom.voobly.stats.core

import gg.rebelsrising.aom.voobly.stats.core.config.VooblyConfig
import org.jsoup.Connection
import org.jsoup.Jsoup

class LoginFailureException(msg: String) : Exception(msg)

class Session(config: VooblyConfig) {

    companion object {

        const val CONTENT_OFFSET = 19 // Fixed post request content length, added to user/pass length.
        const val POST_TIMEOUT_MILLIS = 10_000
        const val USER_AGENT =
            "Mozilla/5.0 (iPad; U; CPU OS 3_2_1 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Mobile/7B405"
        const val LOGIN_URL = "https://www.voobly.com/login"
        const val LOGIN_AUTH_URL = "https://www.voobly.com/login/auth"
        const val GMT_OFFSET_COOKIE = "vbly_GMT_bias"

        val PURGE_KEY_LIST = listOf("vbly_session2", "vbly_session3")
        val LOGGED_IN_KEY_LIST = listOf("vbly_username", "vbly_password")

    }

    private var sessionCookies: MutableMap<String, String> = mutableMapOf()

    private val loginData = mutableMapOf(
        "username" to config.user,
        "password" to config.pass,
    )

    private val getHeaderMap = mapOf(
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

    private val postHeaderMap = mapOf(
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

    fun getRequest(
        url: String,
        cookies: Map<String, String> = sessionCookies
    ): Connection.Response {
        return Jsoup.connect(url)
            .headers(getHeaderMap)
            .userAgent(USER_AGENT)
            .cookies(cookies)
            .method(Connection.Method.GET)
            .ignoreContentType(true)
            .execute()
    }

    fun postRequest(
        url: String,
        data: Map<String, String> = mapOf(),
        cookies: Map<String, String> = sessionCookies
    ): Connection.Response {
        return Jsoup.connect(url)
            .headers(postHeaderMap)
            .userAgent(USER_AGENT)
            .data(data)
            .cookies(cookies)
            .method(Connection.Method.POST)
            .timeout(POST_TIMEOUT_MILLIS)
            .ignoreContentType(true)
            .execute()
    }

    fun isLoggedIn(response: Connection.Response): Boolean {
        return response.cookies().keys.containsAll(LOGGED_IN_KEY_LIST)
    }

    fun login() {
        // Perform get request to login page (to not seem too suspicious) before making the actual login request.
        val get = getRequest(LOGIN_URL)

        // Requests may throw a SocketTimeoutException if we fail to connect.
        val post = postRequest(LOGIN_AUTH_URL, loginData, get.cookies())

        // Throw an exception if we failed to log in for whatever reason.
        if (!isLoggedIn(post)) {
            throw LoginFailureException("Failed to login - are your credentials specified correctly in the config?")
        }

        sessionCookies = post.cookies()
        sessionCookies.keys.removeAll(PURGE_KEY_LIST)
        sessionCookies[GMT_OFFSET_COOKIE] = "0" // Disable offset so we can use GMT.
    }

}
