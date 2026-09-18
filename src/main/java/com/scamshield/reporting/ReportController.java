package com.scamshield.reporting;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
  private final ReportService reportService;

  public ReportController(ReportService reportService) {
    this.reportService = reportService;
  }

  public record Request(
      @NotBlank String phoneNumber,
      @NotBlank String claimedProvider,
      @NotBlank String attackType,
      String description,
      String reporterKey) {}

  @PostMapping
  public Map<String, Object> create(
      @Valid @RequestBody Request request,
      @RequestHeader(value = "X-Reporter-Key", required = false) String headerReporterKey) {
    String reporterKey = headerReporterKey == null ? request.reporterKey() : headerReporterKey;
    return reportService.create(
        request.phoneNumber(),
        request.claimedProvider(),
        request.attackType(),
        request.description(),
        reporterKey);
  }

  @GetMapping
  public List<?> recent() {
    return reportService.recent();
  }
}
