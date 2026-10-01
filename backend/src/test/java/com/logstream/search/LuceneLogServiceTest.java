package com.logstream.search;

import com.logstream.model.LogRecord;
import com.logstream.model.SearchResponse;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LuceneLogServiceTest {

    private LuceneLogService service;
    private IndexWriter writer;

    @BeforeEach
    void setUp() throws Exception {
        ByteBuffersDirectory directory = new ByteBuffersDirectory();
        StandardAnalyzer analyzer = new StandardAnalyzer();
        writer = new IndexWriter(directory, new org.apache.lucene.index.IndexWriterConfig(analyzer));
        service = new LuceneLogService(writer, directory, analyzer);

        service.index(new LogRecord(
                UUID.randomUUID().toString(),
                "2026-09-30T10:00:00Z",
                "ERROR",
                "payment-service",
                "Database connection timeout",
                1500,
                "host-1",
                "trace-1"
        ));

        service.index(new LogRecord(
                UUID.randomUUID().toString(),
                "2026-09-30T10:01:00Z",
                "INFO",
                "user-service",
                "User profile loaded",
                120,
                "host-2",
                "trace-2"
        ));
    }

    @AfterEach
    void tearDown() throws Exception {
        writer.close();
    }

    @Test
    void shouldFindLogByMessage() throws Exception {
        SearchResponse response = service.search("database", null, null, null, null, 0, 20);

        assertEquals(1, response.totalHits());
        assertEquals("ERROR", response.results().get(0).level());
    }

    @Test
    void shouldFilterByLevel() throws Exception {
        SearchResponse response = service.search("", "ERROR", null, null, null, 0, 20);

        assertEquals(1, response.totalHits());
        assertEquals("payment-service", response.results().get(0).service());
    }

    @Test
    void shouldFilterByResponseTime() throws Exception {
        SearchResponse response = service.search("", null, null, 1000L, null, 0, 20);

        assertEquals(1, response.totalHits());
        assertEquals(1500, response.results().get(0).responseTimeMs());
    }
}
