package gg.rebelsrising.aom.voobly.stats.core.parser

import org.jsoup.nodes.Document

fun interface Parser<T> {

    fun parse(doc: Document): T

}
