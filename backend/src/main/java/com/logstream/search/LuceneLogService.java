package com.logstream.search;

import com.logstream.model.LogRecord;
import com.logstream.model.SearchResponse;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.store.Directory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LuceneLogService {

    private final IndexWriter indexWriter;
    private final Directory directory;
    private final Analyzer analyzer;

    public LuceneLogService(IndexWriter indexWriter, Directory directory, Analyzer analyzer) {
        this.indexWriter = indexWriter;
        this.directory = directory;
        this.analyzer = analyzer;
    }

    public String index(LogRecord log) throws IOException {
        String id = (log.id() == null || log.id().isBlank())
                ? UUID.randomUUID().toString()
                : log.id();

        Document document = new Document();

        document.add(new StringField("id", id, Field.Store.YES));
        document.add(new StringField("level", safe(log.level()), Field.Store.YES));
        document.add(new StringField("service", safe(log.service()), Field.Store.YES));
        document.add(new TextField("message", safe(log.message()), Field.Store.YES));
        document.add(new StringField("timestamp", safe(log.timestamp()), Field.Store.YES));
        document.add(new LongPoint("timestampEpoch", parseTimestamp(log.timestamp())));
        document.add(new StoredField("responseTimeMs", log.responseTimeMs()));
        document.add(new LongPoint("responseTimeMsPoint", log.responseTimeMs()));
        document.add(new StringField("host", safe(log.host()), Field.Store.YES));
        document.add(new StringField("traceId", safe(log.traceId()), Field.Store.YES));

        indexWriter.updateDocument(new Term("id", id), document);
        indexWriter.commit();

        return id;
    }

    public SearchResponse search(
            String text,
            String level,
            String service,
            Long minResponseTime,
            Long maxResponseTime,
            int page,
            int pageSize) throws Exception {

        if (page < 0) page = 0;
        if (pageSize < 1 || pageSize > 100) pageSize = 20;

        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        if (text != null && !text.isBlank()) {
            QueryParser parser = new QueryParser("message", analyzer);
            builder.add(parser.parse(QueryParserBase.escape(text)), BooleanClause.Occur.MUST);
        } else {
            builder.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
        }

        if (level != null && !level.isBlank()) {
            builder.add(new TermQuery(new Term("level", level.toUpperCase())),
                    BooleanClause.Occur.FILTER);
        }

        if (service != null && !service.isBlank()) {
            builder.add(new TermQuery(new Term("service", service)),
                    BooleanClause.Occur.FILTER);
        }

        if (minResponseTime != null || maxResponseTime != null) {
            long min = minResponseTime == null ? Long.MIN_VALUE : minResponseTime;
            long max = maxResponseTime == null ? Long.MAX_VALUE : maxResponseTime;
            builder.add(LongPoint.newRangeQuery("responseTimeMsPoint", min, max),
                    BooleanClause.Occur.FILTER);
        }

        Query query = builder.build();

        try (DirectoryReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            int limit = Math.min((page + 1) * pageSize, 10000);
            TopDocs topDocs = searcher.search(query, limit);

            int totalHits = Math.toIntExact(Math.min(topDocs.totalHits.value, Integer.MAX_VALUE));
            int from = page * pageSize;

            List<LogRecord> results = new ArrayList<>();
            if (from < topDocs.scoreDocs.length) {
                int to = Math.min(from + pageSize, topDocs.scoreDocs.length);

                for (int i = from; i < to; i++) {
                    Document doc = searcher.doc(topDocs.scoreDocs[i].doc);
                    results.add(toRecord(doc));
                }
            }

            return new SearchResponse(results, totalHits, page, pageSize);
        }
    }

    private LogRecord toRecord(Document doc) {
        return new LogRecord(
                doc.get("id"),
                doc.get("timestamp"),
                doc.get("level"),
                doc.get("service"),
                doc.get("message"),
                Long.parseLong(doc.getField("responseTimeMs").numericValue().toString()),
                doc.get("host"),
                doc.get("traceId")
        );
    }

    private long parseTimestamp(String timestamp) {
        try {
            return Instant.parse(timestamp).toEpochMilli();
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
