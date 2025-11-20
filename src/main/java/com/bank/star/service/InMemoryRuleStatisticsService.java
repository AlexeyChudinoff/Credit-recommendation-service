package com.bank.star.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

@Service
public class InMemoryRuleStatisticsService {

  private final Map<UUID, LongAdder> statistics = new ConcurrentHashMap<>();

  public void incrementRuleCount(UUID ruleId) {
    statistics.computeIfAbsent(ruleId, k -> new LongAdder()).increment();
  }

  public long getRuleCount(UUID ruleId) {
    return statistics.getOrDefault(ruleId, new LongAdder()).longValue();
  }

  public Map<UUID, Long> getAllStatistics() {
    Map<UUID, Long> result = new ConcurrentHashMap<>();
    statistics.forEach((ruleId, count) -> result.put(ruleId, count.longValue()));
    return result;
  }

  public void clearStatistics() {
    statistics.clear();
  }
}