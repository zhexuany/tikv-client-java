/*
 * Copyright 2017 PingCAP, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pingcap.tikv;

import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;

import com.pingcap.tikv.operation.ErrorHandler;
import com.pingcap.tikv.policy.RetryNTimes.Builder;
import com.pingcap.tikv.policy.RetryPolicy;
import io.grpc.MethodDescriptor;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;
import java.util.function.Supplier;
import org.apache.log4j.Logger;

public abstract class AbstractGRPCClient<
        BlockingStubT extends AbstractStub<BlockingStubT>, StubT extends AbstractStub<StubT>>
    implements AutoCloseable {
  final Logger logger = Logger.getLogger(this.getClass());
  protected TiSession session;
  protected TiConfiguration conf;

  protected AbstractGRPCClient(TiSession session) {
    this.session = session;
    this.conf = session.getConf();
  }

  public TiSession getSession() {
    return session;
  }
  
  public TiConfiguration getConf() {
    return conf;
  }

  // TODO: Seems a little bit messy for lambda part
  protected <ReqT, RespT> RespT callWithRetry(MethodDescriptor<ReqT, RespT> method,
                                              Supplier<ReqT> requestFactory,
                                              ErrorHandler<RespT> handler) {
    logger.debug(String.format("Calling %s...", method.getFullMethodName()));
    RetryPolicy.Builder<RespT> builder = new Builder<>(conf.getRetryTimes(), conf.getBackOffClass());
    RespT resp =
        builder.create(handler)
            .callWithRetry(
                () -> {
                  BlockingStubT stub = getBlockingStub();
                  return ClientCalls.blockingUnaryCall(
                      stub.getChannel(), method, stub.getCallOptions(), requestFactory.get());
                },
                method.getFullMethodName());
    logger.debug(String.format("leaving %s...", method.getFullMethodName()));
    return resp;
  }

  protected <ReqT, RespT> void callAsyncWithRetry(
      MethodDescriptor<ReqT, RespT> method,
      Supplier<ReqT> requestFactory,
      StreamObserver<RespT> responseObserver,
      ErrorHandler<RespT> handler) {
    logger.debug(String.format("Calling %s...", method.getFullMethodName()));

    RetryPolicy.Builder<RespT> builder = new Builder<>(conf.getRetryTimes(), conf.getBackOffClass());
    builder.create(handler)
        .callWithRetry(
            () -> {
              StubT stub = getAsyncStub();
              ClientCalls.asyncUnaryCall(
                  stub.getChannel().newCall(method, stub.getCallOptions()),
                  requestFactory.get(),
                  responseObserver);
              return null;
            },
            method.getFullMethodName());
    logger.debug(String.format("leaving %s...", method.getFullMethodName()));
  }

  <ReqT, RespT> StreamObserver<ReqT> callBidiStreamingWithRetry(
      MethodDescriptor<ReqT, RespT> method,
      StreamObserver<RespT> responseObserver,
      ErrorHandler<StreamObserver<ReqT>> handler) {
    logger.debug(String.format("Calling %s...", method.getFullMethodName()));

    RetryPolicy.Builder<StreamObserver<ReqT>> builder = new Builder<>(conf.getRetryTimes(), conf.getBackOffClass());
    StreamObserver<ReqT> observer =
        builder.create(handler)
            .callWithRetry(
                () -> {
                  StubT stub = getAsyncStub();
                  return asyncBidiStreamingCall(
                      stub.getChannel().newCall(method, stub.getCallOptions()), responseObserver);
                },
                method.getFullMethodName());
    logger.debug(String.format("leaving %s...", method.getFullMethodName()));
    return observer;
  }

  protected abstract BlockingStubT getBlockingStub();

  protected abstract StubT getAsyncStub();

}
