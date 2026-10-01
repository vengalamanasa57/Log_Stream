package com.logstream.config;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class LuceneConfig {

    @Bean
    public Directory luceneDirectory(
            @Value("${logstream.lucene.path}") String indexPath) throws IOException {

        Path path = Path.of(indexPath);
        Files.createDirectories(path);
        return FSDirectory.open(path);
    }

    @Bean
    public StandardAnalyzer luceneAnalyzer() {
        return new StandardAnalyzer();
    }

    @Bean
    public IndexWriter luceneIndexWriter(
            Directory directory,
            StandardAnalyzer analyzer) throws IOException {

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        return new IndexWriter(directory, config);
    }
}
