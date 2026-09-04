package org.example.demo4.report;

import jakarta.validation.Valid;
import org.example.demo4.common.R;
import org.example.demo4.report.dto.ReportRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public R<Void> report(@Valid @RequestBody ReportRequest req) {
        reportService.report(req.getTargetType(), req.getTargetId(), req.getReason());
        return R.ok();
    }
}
