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

import com.google.common.collect.ImmutableList;
import com.google.common.net.HostAndPort;
import com.pingcap.tikv.util.BackOff;
import com.pingcap.tikv.util.ExponentialBackOff;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TiConfiguration implements Serializable {
  private static final int DEF_TIMEOUT = 10;
  private static final TimeUnit DEF_TIMEOUT_UNIT = TimeUnit.MINUTES;
  private static final int DEF_SCAN_BATCH_SIZE = 100;
  private static final boolean DEF_IGNORE_TRUNCATE = true;
  private static final boolean DEF_TRUNCATE_AS_WARNING = false;
  private static final int DEF_META_RELOAD_PERIOD = 10;
  private static final TimeUnit DEF_META_RELOAD_UNIT = TimeUnit.SECONDS;
  private static final int DEF_RETRY_TIMES = 3;
  private static final Class<? extends BackOff> DEF_BACKOFF_CLASS = ExponentialBackOff.class;
  private static final int DEF_MAX_FRAME_SIZE = 268435456 * 2; // 256 * 2 MB

  private int retryTimes = DEF_RETRY_TIMES;
  private int timeout = DEF_TIMEOUT;
  private TimeUnit timeoutUnit = DEF_TIMEOUT_UNIT;
  private boolean ignoreTruncate = DEF_IGNORE_TRUNCATE;
  private boolean truncateAsWarning = DEF_TRUNCATE_AS_WARNING;
  private TimeUnit metaReloadUnit = DEF_META_RELOAD_UNIT;
  private int metaReloadPeriod = DEF_META_RELOAD_PERIOD;
  private int maxFrameSize = DEF_MAX_FRAME_SIZE;
  private Class<? extends BackOff> backOffClass = DEF_BACKOFF_CLASS;
  private List<HostAndPort> pdAddrs = new ArrayList<>();

  public static TiConfiguration createDefault(String pdAddrsStr) {
    Objects.requireNonNull(pdAddrsStr, "pdAddrsStr is null");
    TiConfiguration conf = new TiConfiguration();
    conf.pdAddrs = strToHostAndPort(pdAddrsStr);
    return conf;
  }

  public int getRetryTimes() {
    return retryTimes;
  }

  public void setRetryTimes(int n) {
    this.retryTimes = n;
  }

  private static List<HostAndPort> strToHostAndPort(String addressStr) {
    Objects.requireNonNull(addressStr);
    String [] addrs = addressStr.split(",");
    ImmutableList.Builder<HostAndPort> addrsBuilder = ImmutableList.builder();
    for (String addr : addrs) {
      addrsBuilder.add(HostAndPort.fromString(addr));
    }
    return addrsBuilder.build();
  }

  public int getTimeout() {
    return timeout;
  }

  public TiConfiguration setTimeout(int timeout) {
    this.timeout = timeout;
    return this;
  }

  public TimeUnit getTimeoutUnit() {
    return timeoutUnit;
  }

  public TimeUnit getMetaReloadPeriodUnit() {
    return metaReloadUnit;
  }

  public TiConfiguration setMetaReloadPeriodUnit(TimeUnit timeUnit) {
    this.metaReloadUnit = timeUnit;
    return this;
  }

  public TiConfiguration setMetaReloadPeriod(int metaReloadPeriod) {
    this.metaReloadPeriod = metaReloadPeriod;
    return this;
  }

  public int getMetaReloadPeriod() {
    return metaReloadPeriod;
  }

  public TiConfiguration setTimeoutUnit(TimeUnit timeoutUnit) {
    this.timeoutUnit = timeoutUnit;
    return this;
  }

  public List<HostAndPort> getPdAddrs() {
    return pdAddrs;
  }

  public int getScanBatchSize() {
    return DEF_SCAN_BATCH_SIZE;
  }

  boolean isIgnoreTruncate() {
    return ignoreTruncate;
  }

  public TiConfiguration setIgnoreTruncate(boolean ignoreTruncate) {
    this.ignoreTruncate = ignoreTruncate;
    return this;
  }

  boolean isTruncateAsWarning() {
    return truncateAsWarning;
  }

  public TiConfiguration setTruncateAsWarning(boolean truncateAsWarning) {
    this.truncateAsWarning = truncateAsWarning;
    return this;
  }

  public int getMaxFrameSize() {
    return maxFrameSize;
  }

  public TiConfiguration setMaxFrameSize(int maxFrameSize) {
    this.maxFrameSize = maxFrameSize;
    return this;
  }

  public void setRpcRetryTimes(int rpcRetryTimes) {
    this.retryTimes = rpcRetryTimes;
  }

  public int getRpcRetryTimes() {
    return retryTimes;
  }

  public Class<? extends BackOff> getBackOffClass() {
    return backOffClass;
  }

  public void setBackOffClass(Class<? extends BackOff> backOffClass) {
    this.backOffClass = backOffClass;
  }
}
