package com.logstream.client;

import com.logstream.grpc.IngestResponse;
import com.logstream.grpc.LogIngestionServiceGrpc;
import com.logstream.grpc.LogMessage;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.time.Instant;

public class SampleGrpcClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        try {
            LogIngestionServiceGrpc.LogIngestionServiceBlockingStub stub =
                    LogIngestionServiceGrpc.newBlockingStub(channel);

            LogMessage log = LogMessage.newBuilder()
                    .setTimestamp(Instant.now().toString())
                    .setLevel("ERROR")
                    .setService("payment-service")
                    .setMessage("Database connection timeout")
                    .setResponseTimeMs(1500)
                    .setHost("localhost")
                    .setTraceId("demo-trace-001")
                    .build();

            IngestResponse response = stub.sendLog(log);

            System.out.println("Success: " + response.getSuccess());
            System.out.println("Message: " + response.getMessage());
            System.out.println("Log ID: " + response.getLogId());
        } finally {
            channel.shutdown();
        }
    }
}
