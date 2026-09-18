package com.scamshield.risk;

import java.util.*;
import org.springframework.stereotype.Service;

/** Deterministic, explainable score. Category bonuses are applied once per phone. */
@Service
public class RiskScoringService {
  public Result score(List<Signal> signals) {
    int score = Math.min(100, signals.size() * 10);
    boolean impersonation = signals.stream().anyMatch(s -> s.provider() != null && !s.provider().equalsIgnoreCase("Other"));
    if (impersonation) score += 20;
    Set<String> behaviors = new HashSet<>();
    signals.stream().map(Signal::attack).filter(Objects::nonNull).map(String::toUpperCase)
      .filter(a -> Set.of("OTP","PIN","MONEY").contains(a)).forEach(behaviors::add);
    score += behaviors.size() * 25;
    if (signals.stream().map(Signal::reporter).filter(Objects::nonNull).distinct().count() >= 2) score += 20;
    score = Math.min(100, score);
    String level = score >= 80 ? "CRITICAL" : score >= 60 ? "HIGH" : score >= 30 ? "SUSPICIOUS" : "LOW";
    List<String> reasons = new ArrayList<>();
    if (!signals.isEmpty()) reasons.add("Report exists");
    if (signals.size() >= 2) reasons.add("Multiple independent reports");
    if (impersonation) reasons.add("Provider impersonation");
    behaviors.forEach(b -> reasons.add(b + " request"));
    return new Result(score, level, List.copyOf(reasons));
  }
  public record Signal(String provider, String attack, String reporter) {}
  public record Result(int score, String level, List<String> reasons) {}
}
