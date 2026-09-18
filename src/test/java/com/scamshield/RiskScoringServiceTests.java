package com.scamshield;

import org.junit.jupiter.api.Test;
import java.lang.reflect.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class RiskScoringServiceTests {
  private final Class<?> serviceType;
  private final Class<?> signalType;
  private final Object scorer;
  RiskScoringServiceTests() throws Exception {
    serviceType=Class.forName("com.scamshield.risk.RiskScoringService");
    signalType=Class.forName("com.scamshield.risk.RiskScoringService$Signal");
    scorer=serviceType.getConstructor().newInstance();
  }
  private Object signal(String provider, String attack, String reporter) throws Exception {
    return signalType.getConstructor(String.class,String.class,String.class).newInstance(provider,attack,reporter);
  }
  private Object score(List<Object> signals) throws Exception {
    return serviceType.getMethod("score",List.class).invoke(scorer,signals);
  }
  private int value(Object result,String method) throws Exception { return (int)result.getClass().getMethod(method).invoke(result); }
  private String level(Object result) throws Exception { return (String)result.getClass().getMethod("level").invoke(result); }
  private List<String> reasons(Object result) throws Exception { return (List<String>)result.getClass().getMethod("reasons").invoke(result);
  }

  @Test void repeatedCategoricalSignalsOnlyAddTheirBonusOnce() throws Exception {
    var result = score(List.of(signal("Orange","OTP","a"), signal("Orange","OTP","a")));
    assertEquals(65, value(result,"score")); // two reports, one provider bonus, one OTP bonus
    assertFalse(reasons(result).contains("Multiple independent reports"));
  }

  @Test void independentReportBonusRequiresDistinctReporterKeys() throws Exception {
    var sameReporter = score(List.of(signal("Other","OTHER","a"), signal("Other","OTHER","a")));
    var differentReporters = score(List.of(signal("Other","OTHER","a"), signal("Other","OTHER","b")));
    assertEquals(20, value(sameReporter,"score"));
    assertEquals(40, value(differentReporters,"score"));
    assertTrue(reasons(differentReporters).contains("Multiple independent reports"));
  }

  @Test void levelsFollowDocumentedThresholdsAndScoreCapsAtOneHundred() throws Exception {
    assertEquals("SUSPICIOUS", level(score(new ArrayList<>(Collections.nCopies(3, signal("Other","OTHER","a"))))));
    assertEquals("HIGH", level(score(List.of(signal("Orange","OTHER","a"),signal("Orange","OTHER","b")))));
    var many = new ArrayList<Object>();
    for (int n=0;n<20;n++) many.add(signal("Orange",List.of("OTP","PIN","MONEY","OTHER").get(n%4),"r"+n));
    assertEquals(100, value(score(many),"score"));
    assertEquals("CRITICAL", level(score(many)));
  }
}
