package com.shelfpulse.activation_automation.service;

import com.shelfpulse.activation_automation.dto.template.TemplateDto;
import com.shelfpulse.activation_automation.entity.Menu;
import com.shelfpulse.activation_automation.enums.MenuStatus;
import com.shelfpulse.activation_automation.enums.TemplateType;
import com.shelfpulse.activation_automation.enums.UserType;
import com.shelfpulse.activation_automation.repository.MenuRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TemplateService {

    private static final Logger log = LoggerFactory.getLogger(TemplateService.class);

    private final MenuRepository menuRepository;
    private final GcsService gcsService;

    public TemplateService(MenuRepository menuRepository, GcsService gcsService) {
        this.menuRepository = menuRepository;
        this.gcsService = gcsService;
    }

    @Transactional
    public Menu updateTemplate(Integer templateId, Integer adminId, TemplateDto.UpdateTemplateRequest request,
            List<MultipartFile> templateMenuImages) throws Exception {

        Optional<Menu> existingTemplateOpt = menuRepository.findById(templateId);
        if (existingTemplateOpt.isEmpty()) {
            throw new Exception("Template not found.");
        }

        Menu template = existingTemplateOpt.get();

        TemplateType finalType = request.getType() != null ? request.getType() : template.getTemplateType();
        if (finalType == null) {
            throw new Exception("Template type is required.");
        }

        if (request.getTemplateName() != null) {
            template.setTemplateName(request.getTemplateName());
        }

        template.setTemplateType(finalType);
        template.setStatus(MenuStatus.COMPLETED);

        if (templateMenuImages != null && !templateMenuImages.isEmpty()) {
            List<String> imageUrls = uploadTemplateImages(templateMenuImages, adminId, template.getEatery().getId(),
                    finalType);
            template.setTemplateImgUrls(imageUrls);
        }

        return menuRepository.save(template);
    }

    private List<String> uploadTemplateImages(List<MultipartFile> files, Integer adminId, Integer eateryId,
            TemplateType type) throws Exception {
        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty())
                continue;

            String originalFilename = file.getOriginalFilename();
            String ext = getExtension(originalFilename);
            String path = String.format("%d/template/%s", eateryId, type.getValue());

            try {
                String url = gcsService.uploadAdminRecognizedImage(
                        Long.valueOf(adminId),
                        file.getBytes(),
                        path,
                        originalFilename,
                        ext);
                imageUrls.add(url);
            } catch (Exception e) {
                log.error("Failed to upload template image {}: {}", originalFilename, e.getMessage());
                throw new Exception("Failed to upload template image: " + e.getMessage());
            }
        }

        return imageUrls;
    }

    public Page<Menu> getTemplateMenu(Integer eateryId, TemplateDto.GetTemplateRequest request1,
            Integer adminId, UserType userType) {
        int page = (request1.getPage() != null && request1.getPage() > 0) ? request1.getPage() - 1 : 0;
        int limit = (request1.getLimit() != null && request1.getLimit() > 0) ? request1.getLimit() : 10;

        String sortBy = StringUtils.hasText(request1.getSortBy()) ? request1.getSortBy() : "createdAt";
        String sortOrder = StringUtils.hasText(request1.getSortOrder()) ? request1.getSortOrder() : "desc";

        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        Pageable pageable = PageRequest.of(page, limit, sort);

        Specification<Menu> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("eatery").get("id"), eateryId));

            if (StringUtils.hasText(request1.getSearch())) {
                String pattern = "%" + request1.getSearch().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("templateName")), pattern));
            }

            if (userType != UserType.SUPER_ADMIN) {
                predicates.add(cb.equal(root.get("admin").get("id"), adminId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return menuRepository.findAll(spec, pageable);
    }

    public Page<Menu> getTemplatesByType(TemplateType type, TemplateDto.GetTemplateRequest request1) {
        int page = (request1.getPage() != null && request1.getPage() > 0) ? request1.getPage() - 1 : 0;
        int limit = (request1.getLimit() != null && request1.getLimit() > 0) ? request1.getLimit() : 10;

        String sortBy = StringUtils.hasText(request1.getSortBy()) ? request1.getSortBy() : "createdAt";
        String sortOrder = StringUtils.hasText(request1.getSortOrder()) ? request1.getSortOrder() : "desc";

        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        Pageable pageable = PageRequest.of(page, limit, sort);

        Specification<Menu> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("templateType"), type));

            if (StringUtils.hasText(request1.getSearch())) {
                String pattern = "%" + request1.getSearch().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("templateName")), pattern));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return menuRepository.findAll(spec, pageable);
    }

    private String getExtension(String filename) {
        if (filename == null)
            return "";
        int i = filename.lastIndexOf('.');
        return i > 0 ? filename.substring(i) : "";
    }
}
