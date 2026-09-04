package org.example.demo4.report;

import org.example.demo4.common.BusinessException;
import org.example.demo4.common.CurrentUserService;
import org.example.demo4.community.PostRepository;
import org.example.demo4.product.ProductRepository;
import org.example.demo4.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    public static final String TYPE_PRODUCT = "PRODUCT";
    public static final String TYPE_POST = "POST";

    private final ReportRepository reportRepository;
    private final ProductRepository productRepository;
    private final PostRepository postRepository;
    private final CurrentUserService currentUserService;

    public ReportService(ReportRepository reportRepository,
                         ProductRepository productRepository,
                         PostRepository postRepository,
                         CurrentUserService currentUserService) {
        this.reportRepository = reportRepository;
        this.productRepository = productRepository;
        this.postRepository = postRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public void report(String targetType, Long targetId, String reason) {
        User reporter = currentUserService.requireUser();
        if (TYPE_PRODUCT.equals(targetType)) {
            productRepository.findById(targetId)
                    .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        } else if (TYPE_POST.equals(targetType)) {
            postRepository.findById(targetId)
                    .orElseThrow(() -> new BusinessException(404, "帖子不存在"));
        } else {
            throw new BusinessException("非法的举报类型");
        }
        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
                reporter.getId(), targetType, targetId)) {
            throw new BusinessException("你已经举报过该内容");
        }
        Report r = new Report();
        r.setReporterId(reporter.getId());
        r.setTargetType(targetType);
        r.setTargetId(targetId);
        r.setReason(reason);
        reportRepository.save(r);
    }
}
