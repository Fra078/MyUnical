package it.fra.myunical.network.mapper

import it.fra.myunical.network.models.news.SearchResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class NewsMapperTest {

    private val json = Json { ignoreUnknownKeys = true }

    // Trimmed answers of unical.it/api/search/
    private val response = json.decodeFromString<SearchResponse>(
        """
        {"count": 2, "next": "//www.unical.it/api/search/?page=2", "results": [
          {"title": " Calendario prove Biologo ", "heading": "2° sessione 2026", "content_id": "26029",
           "image": null, "content": "<p>Testo</p>", "sites": ["www.unical.it"],
           "urls": ["//www.unical.it/didattica/contents/news/view/26029-calendario/"],
           "tags": [], "categories": ["Avvisi"], "published": "2026-11-06T10:25:00"},
          {"title": "Reddito di merito", "heading": "", "content_id": "26007",
           "image": "/media/medias/2026/Banner.webp", "content": "", "sites": ["desf.unical.it"],
           "urls": ["//desf.unical.it/contents/news/view/26007-reddito/", "//desf.unical.it/magazine/x/"],
           "tags": ["Workshop"], "categories": ["Avvisi", "Didattica"], "published": "2026-11-05T11:44:00"}
        ]}
        """.trimIndent()
    )

    @Test
    fun `a notice has no image and keeps its summary`() {
        val notice = response.results[0].toNotice()
        assertEquals("s26029", notice.id)
        assertEquals("Calendario prove Biologo", notice.title)
        assertEquals("Avvisi", notice.category)
        assertEquals("2° sessione 2026", notice.summary)
        assertEquals("https://www.unical.it/didattica/contents/news/view/26029-calendario/", notice.link)
        assertNull(notice.imageUrl)
        assertEquals(LocalDateTime.of(2026, 11, 6, 10, 25), notice.publishedAt)
    }

    @Test
    fun `department news take the image from the site and prefer the topic as category`() {
        val news = response.results[1].toNotice()
        assertEquals("https://desf.unical.it/media/medias/2026/Banner.webp", news.imageUrl)
        assertEquals("Didattica", news.category)
        assertEquals("https://desf.unical.it/contents/news/view/26007-reddito/", news.link)
        assertEquals(listOf("Workshop"), news.tags)
    }

    @Test
    fun `search ids do not collide with the university feed ids`() {
        // The university feed numbers items by context id ("n25224"), the search by publication id
        assertEquals("s26007", response.results[1].toNotice().id)
    }
}
