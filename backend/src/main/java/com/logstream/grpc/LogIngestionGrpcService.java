package com.logstream.grpc;

import com.logstream.model.LogRecord;
import com.logstream.search.LuceneLogService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import java.util.UUID;

@GrpcService
public class LogIngestionGrpcService extends LogIngestionServiceGrpc.LogIngestionServiceImplBase {

    private final LuceneLogService logService;

    public LogIngestionGrpcService(LuceneLogService logService) {
        this.logService = logService;
    }

    @Override
    public void sendLog(LogMessage request, StreamObserver<IngestResponse> responseObserver) {
        String id = request.getId().isBlank() ? UUID.randomUUID().toString() : request.getId();

        try {
            logService.index(new LogRecord(
                    id,
                    request.getTimestamp(),
                    request.getLevel().toUpperCase(),
                    request.getService(),
                    request.getMessage(),
                    request.getResponseTimeMs(),
                    request.getHost(),
                    request.getTraceId()
            ));

            responseObserver.onNext(IngestResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Log indexed successfully")
                    .setLogId(id)
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(IngestResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Indexing failed: " + e.getMessage())
                    .setLogId(id)
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public StreamObserver<LogMessage> sendLogs(StreamObserver<IngestResponse> responseObserver) {
        return new StreamObserver<>() {
            private int count = 0;

            @Override
            public void onNext(LogMessage request) {
                String id = request.getId().isBlank() ? UUID.randomUUID().toString() : request.getId();
                try {
                    logService.index(new LogRecord(
                            id,
                            request.getTimestamp(),
                            request.getLevel().toUpperCase(),
                            request.getService(),
                            request.getMessage(),
                            request.getResponseTimeMs(),
                            request.getHost(),
                            request.getTraceId()
                    ));
                    count++;
                } catch (Exception e) {
                    responseObserver.onError(e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(IngestResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage(count + " logs indexed successfully")
                        .setLogId("")
                        .build());
                responseObserver.onCompleted();
            }
        };
    }
}
