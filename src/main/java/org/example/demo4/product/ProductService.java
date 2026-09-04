package org.example.demo4.product;

import jakarta.persistence.criteria.Predicate;
import org.example.demo4.common.BusinessException;
import org.example.demo4.common.PageResult;
import org.example.demo4.product.dto.ProductRequest;
import org.example.demo4.product.dto.ProductResponse;
import org.example.demo4.user.User;
import org.example.demo4.user.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          ProductImageRepository imageRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.categoryRepository = categoryRepository;
    }

    public PageResult<ProductResponse> list(String keyword, Long categoryId, ProductCondition condition,
                                            BigDecimal minPrice, BigDecimal maxPrice,
                                            String sort, int page, int size) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), ProductStatus.ON_SALE));
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), like),
                        cb.like(root.get("description"), like)));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("categoryId"), categoryId));
            }
            if (condition != null) {
                predicates.add(cb.equal(root.get("condition"), condition));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sortSpec = switch (sort == null ? "" : sort) {
            case "priceAsc" -> Sort.by("price").ascending();
            case "priceDesc" -> Sort.by("price").descending();
            case "hot" -> Sort.by("viewCount").descending();
            default -> Sort.by("createdAt").descending();
        };

        Page<Product> pageResult = productRepository.findAll(spec,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 60), sortSpec));

        List<Product> products = pageResult.getContent();
        Map<Long, String> categoryNames = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        Map<Long, List<String>> imageMap = imageRepository
                .findByProductIdIn(products.stream().map(Product::getId).collect(Collectors.toList())).stream()
                .collect(Collectors.groupingBy(ProductImage::getProductId,
                        Collectors.mapping(ProductImage::getUrl, Collectors.toList())));

        List<ProductResponse> list = products.stream()
                .map(p -> toResponse(p, categoryNames, imageMap.getOrDefault(p.getId(), Collections.emptyList())))
                .collect(Collectors.toList());

        return new PageResult<>(list, pageResult.getTotalElements(), page, size);
    }

    @Transactional
    public ProductResponse detail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);

        String categoryName = categoryRepository.findById(product.getCategoryId())
                .map(Category::getName).orElse("");
        List<String> images = imageRepository.findByProductIdOrderBySortOrderAsc(id).stream()
                .map(ProductImage::getUrl).collect(Collectors.toList());
        return toResponse(product, Map.of(product.getCategoryId(), categoryName), images);
    }

    @Transactional
    public ProductResponse create(ProductRequest req, User seller) {
        if (seller.getStoreId() == null || seller.getRole() != UserRole.MERCHANT) {
            throw new BusinessException("请先开通商户后再发布商品");
        }
        Product product = new Product();
        product.setSellerId(seller.getId());
        product.setSellerName(seller.getNickname() == null ? seller.getUsername() : seller.getNickname());
        product.setCategoryId(req.getCategoryId());
        product.setTitle(req.getTitle());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setOriginalPrice(req.getOriginalPrice());
        product.setCondition(req.getCondition());
        product.setStock(req.getStock() == null || req.getStock() < 1 ? 1 : req.getStock());
        productRepository.save(product);

        List<String> urls = req.getImageUrls() == null ? Collections.emptyList() : req.getImageUrls();
        for (int i = 0; i < urls.size(); i++) {
            ProductImage image = new ProductImage();
            image.setProductId(product.getId());
            image.setUrl(urls.get(i));
            image.setSortOrder(i);
            imageRepository.save(image);
        }

        return detail(product.getId());
    }

    public List<ProductResponse> listBySeller(Long sellerId) {
        return toListResponse(productRepository
                .findBySellerIdAndStatusOrderByCreatedAtDesc(sellerId, ProductStatus.ON_SALE));
    }

    public List<ProductResponse> listMine(Long sellerId) {
        return toListResponse(productRepository.findBySellerIdOrderByCreatedAtDesc(sellerId));
    }

    private List<ProductResponse> toListResponse(List<Product> products) {
        if (products.isEmpty()) {
            return List.of();
        }
        Map<Long, String> categoryNames = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        Map<Long, List<String>> imageMap = imageRepository
                .findByProductIdIn(products.stream().map(Product::getId).collect(Collectors.toList())).stream()
                .collect(Collectors.groupingBy(ProductImage::getProductId,
                        Collectors.mapping(ProductImage::getUrl, Collectors.toList())));
        return products.stream()
                .map(p -> toResponse(p, categoryNames, imageMap.getOrDefault(p.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse updateStatus(Long productId, ProductStatus status, Long sellerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        if (!product.getSellerId().equals(sellerId)) {
            throw new BusinessException(403, "无权操作该商品");
        }
        product.setStatus(status);
        productRepository.save(product);
        String categoryName = categoryRepository.findById(product.getCategoryId())
                .map(Category::getName).orElse("");
        List<String> images = imageRepository.findByProductIdOrderBySortOrderAsc(productId).stream()
                .map(ProductImage::getUrl).collect(Collectors.toList());
        return toResponse(product, Map.of(product.getCategoryId(), categoryName), images);
    }

    private ProductResponse toResponse(Product p, Map<Long, String> categoryNames, List<String> images) {
        ProductResponse r = new ProductResponse();
        r.setId(p.getId());
        r.setTitle(p.getTitle());
        r.setDescription(p.getDescription());
        r.setPrice(p.getPrice());
        r.setOriginalPrice(p.getOriginalPrice());
        r.setCondition(p.getCondition());
        r.setStatus(p.getStatus());
        r.setStock(p.getStock());
        r.setViewCount(p.getViewCount());
        r.setCategoryId(p.getCategoryId());
        r.setCategoryName(categoryNames.getOrDefault(p.getCategoryId(), ""));
        r.setSellerId(p.getSellerId());
        r.setSellerName(p.getSellerName());
        r.setImages(images);
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}
