package com.shelfpulse.activation_automation.service;

import com.shelfpulse.activation_automation.dto.eatery.EateryDto;
import com.shelfpulse.activation_automation.entity.Admin;
import com.shelfpulse.activation_automation.entity.Eatery;
import com.shelfpulse.activation_automation.enums.MenuStatus;
import com.shelfpulse.activation_automation.enums.UserType;
import com.shelfpulse.activation_automation.repository.AdminRepository;
import com.shelfpulse.activation_automation.repository.EateryRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EateryService {

    private static final Logger log = LoggerFactory.getLogger(EateryService.class);

    private final EateryRepository eateryRepository;
    private final AdminRepository adminRepository;
    private final GcsService gcsService;

    public EateryService(EateryRepository eateryRepository, AdminRepository adminRepository, GcsService gcsService) {
        this.eateryRepository = eateryRepository;
        this.adminRepository = adminRepository;
        this.gcsService = gcsService;
    }

    @Transactional
    public Eatery createEatery(EateryDto.CreateRequest request, Integer adminId,
            MultipartFile eateryImage, MultipartFile eateryLogoImage) throws Exception {

        log.debug("createEatery called with adminId: {}", adminId);
        log.debug("eateryImage: present={}, empty={}", eateryImage != null,
                eateryImage != null ? eateryImage.isEmpty() : "N/A");
        log.debug("eateryLogoImage: present={}, empty={}", eateryLogoImage != null,
                eateryLogoImage != null ? eateryLogoImage.isEmpty() : "N/A");

        Optional<Admin> adminOpt = adminRepository.findById(adminId);
        if (adminOpt.isEmpty()) {
            throw new Exception("Admin not found with ID: " + adminId);
        }

        if (eateryRepository.existsBySaamnaId(request.getSaamnaId())) {
            throw new Exception("An eatery with this SAAMNA ID already exists.");
        }

        Eatery eatery = new Eatery();
        BeanUtils.copyProperties(request, eatery);
        eatery.setAdmin(adminOpt.get());
        eatery.setStatus(MenuStatus.COMPLETED);

        eatery = eateryRepository.save(eatery);

        boolean updated = false;
        if (eateryImage != null && !eateryImage.isEmpty()) {
            log.debug("Uploading eateryImage: originalFilename={}, size={}", eateryImage.getOriginalFilename(),
                    eateryImage.getSize());
            String ext = getExtension(eateryImage.getOriginalFilename());
            String path = String.format("%d/eateryPicture", eatery.getId());
            try {
                String url = gcsService.uploadAdminRecognizedImage(Long.valueOf(adminId), eateryImage.getBytes(), path,
                        eateryImage.getOriginalFilename(), ext);
                log.debug("eateryImage uploaded, URL: {}", url);
                eatery.setEateryImgUrl(url);
                updated = true;
            } catch (Exception e) {
                log.error("Failed to upload eatery image: {}", e.getMessage());
                throw new Exception("Failed to upload eatery image: " + e.getMessage());
            }
        } else {
            log.debug("eateryImage is null or empty, skipping upload");
        }

        if (eateryLogoImage != null && !eateryLogoImage.isEmpty()) {
            log.debug("Uploading eateryLogoImage: originalFilename={}, size={}", eateryLogoImage.getOriginalFilename(),
                    eateryLogoImage.getSize());
            String ext = getExtension(eateryLogoImage.getOriginalFilename());
            String path = String.format("%d/eateryLogo", eatery.getId());
            try {
                String url = gcsService.uploadAdminRecognizedImage(Long.valueOf(adminId), eateryLogoImage.getBytes(),
                        path, eateryLogoImage.getOriginalFilename(), ext);
                log.debug("eateryLogoImage uploaded, URL: {}", url);
                eatery.setEateryLogoImgUrl(url);
                updated = true;
            } catch (Exception e) {
                log.error("Failed to upload eatery logo: {}", e.getMessage());
                throw new Exception("Failed to upload eatery logo: " + e.getMessage());
            }
        } else {
            log.debug("eateryLogoImage is null or empty, skipping upload");
        }

        if (updated) {
            eatery = eateryRepository.save(eatery);
        }

        return eatery;
    }

    @Transactional
    public Eatery updateEatery(Integer eateryId, EateryDto.UpdateRequest request, Integer adminId,
            MultipartFile eateryImage, MultipartFile eateryLogoImage) throws Exception {

        Optional<Eatery> eateryOpt = eateryRepository.findById(eateryId);
        if (eateryOpt.isEmpty()) {
            throw new Exception("Eatery not found!");
        }
        Eatery eatery = eateryOpt.get();

        if (request.getName() != null)
            eatery.setName(request.getName());
        if (request.getSaamnaId() != null)
            eatery.setSaamnaId(request.getSaamnaId());
        if (request.getAddress() != null)
            eatery.setAddress(request.getAddress());
        if (request.getState() != null)
            eatery.setState(request.getState());
        if (request.getCity() != null)
            eatery.setCity(request.getCity());
        if (request.getPostalCode() != null)
            eatery.setPostalCode(request.getPostalCode());
        if (request.getPhoneNumber() != null)
            eatery.setPhoneNumber(request.getPhoneNumber());
        if (request.getEmail() != null)
            eatery.setEmail(request.getEmail());
        if (request.getWebsiteLink() != null)
            eatery.setWebsiteLink(request.getWebsiteLink());
        if (request.getSubChannel() != null)
            eatery.setSubChannel(request.getSubChannel());
        if (request.getLongitude() != null)
            eatery.setLongitude(request.getLongitude());
        if (request.getLatitude() != null)
            eatery.setLatitude(request.getLatitude());

        if (eateryImage != null && !eateryImage.isEmpty()) {
            String ext = getExtension(eateryImage.getOriginalFilename());
            String path = String.format("%d/eateryPictures", eatery.getId());
            String url = gcsService.uploadAdminRecognizedImage(Long.valueOf(adminId), eateryImage.getBytes(), path,
                    eateryImage.getOriginalFilename(), ext);
            eatery.setEateryImgUrl(url);
        } else if (Boolean.TRUE.equals(request.getRemoveEateryImage())) {
            eatery.setEateryImgUrl(null);
        }

        if (eateryLogoImage != null && !eateryLogoImage.isEmpty()) {
            String ext = getExtension(eateryLogoImage.getOriginalFilename());
            String path = String.format("%d/logos", eatery.getId());
            String url = gcsService.uploadAdminRecognizedImage(Long.valueOf(adminId), eateryLogoImage.getBytes(), path,
                    eateryLogoImage.getOriginalFilename(), ext);
            eatery.setEateryLogoImgUrl(url);
        } else if (Boolean.TRUE.equals(request.getRemoveEateryLogoImage())) {
            eatery.setEateryLogoImgUrl(null);
        }

        return eateryRepository.save(eatery);
    }

    public Eatery getEatery(Integer eateryId, Integer adminId, UserType userType) throws Exception {
        Optional<Eatery> eateryOpt = eateryRepository.findById(eateryId);
        if (eateryOpt.isEmpty()) {
            return null;
        }
        Eatery eatery = eateryOpt.get();
        if (eatery.getStatus() == MenuStatus.INACTIVE) {
            return null;
        }

        if (userType != UserType.SUPER_ADMIN && !eatery.getAdmin().getId().equals(adminId)) {
            return null;
        }
        return eatery;
    }

    public Page<Eatery> getAllEateries(EateryDto.GetAllRequest request, Integer adminId, UserType userType) {
        log.debug("getAllEateries called - adminId: {}, userType: {}, search: '{}'", adminId, userType,
                request.getSearch());

        int page = (request.getPage() != null && request.getPage() > 0) ? request.getPage() - 1 : 0;
        int limit = (request.getLimit() != null && request.getLimit() > 0) ? request.getLimit() : 10;

        String sortBy = StringUtils.hasText(request.getSortBy()) ? request.getSortBy() : "creationDate";
        String sortOrder = StringUtils.hasText(request.getSortOrder()) ? request.getSortOrder() : "desc";

        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        Pageable pageable = PageRequest.of(page, limit, sort);

        Specification<Eatery> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Status != INACTIVE
            predicates.add(cb.notEqual(root.get("status"), MenuStatus.INACTIVE));

            // User Permissions
            if (userType != UserType.SUPER_ADMIN) {
                predicates.add(cb.equal(root.get("admin").get("id"), adminId));
            }

            // Search - split by spaces and match each term (AND logic)
            if (StringUtils.hasText(request.getSearch())) {
                String[] searchTerms = request.getSearch().trim().split("\\s+");
                for (String term : searchTerms) {
                    if (!term.isEmpty()) {
                        String pattern = "%" + term.toLowerCase() + "%";
                        predicates.add(cb.like(cb.lower(root.get("name")), pattern));
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return eateryRepository.findAll(spec, pageable);
    }

    @Transactional
    public void deleteEateries(List<Integer> eateryIds, Integer adminId, UserType userType) {
        // Fetch all first to check permissions
        List<Eatery> eateries = eateryRepository.findAllById(eateryIds);
        List<Eatery> toSave = new ArrayList<>();

        for (Eatery e : eateries) {
            if (userType != UserType.SUPER_ADMIN && !e.getAdmin().getId().equals(adminId)) {
                continue; // Skip or throw? Node logic just runs query restricted by adminId implicitly.
                // Node: create query with adminId restriction.
            }
            e.setStatus(MenuStatus.INACTIVE);
            toSave.add(e);
        }
        eateryRepository.saveAll(toSave);
    }

    private String getExtension(String filename) {
        if (filename == null)
            return "";
        int i = filename.lastIndexOf('.');
        return i > 0 ? filename.substring(i) : "";
    }
}
