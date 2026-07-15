package com.tracelens.investigation.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracelens.common.PageResponse;
import com.tracelens.exception.CaseNotFoundException;
import com.tracelens.exception.InvalidRequestException;
import com.tracelens.exception.UserNotFoundException;
import com.tracelens.investigation.dto.CaseResponse;
import com.tracelens.investigation.dto.CreateCaseRequest;
import com.tracelens.investigation.dto.UpdateCaseRequest;
import com.tracelens.investigation.dto.UpdateCaseStatusRequest;
import com.tracelens.investigation.entity.CasePriority;
import com.tracelens.investigation.entity.CaseStatus;
import com.tracelens.investigation.entity.InvestigationCase;
import com.tracelens.investigation.repository.InvestigationCaseRepository;
import com.tracelens.investigation.repository.InvestigationCaseSpecifications;
import com.tracelens.user.entity.User;
import com.tracelens.user.repository.UserRepository;

@Service
public class InvestigationCaseService {

    private static final String CASE_NUMBER_CHARACTERS =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private static final int RANDOM_PART_LENGTH = 8;

    private static final int CASE_NUMBER_GENERATION_ATTEMPTS = 10;

    private static final int MAXIMUM_PAGE_SIZE = 100;

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of(
                    "id",
                    "caseNumber",
                    "title",
                    "status",
                    "priority",
                    "createdAt",
                    "updatedAt"
            );

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter
                    .ofPattern("yyyyMMdd")
                    .withZone(ZoneOffset.UTC);

    private final InvestigationCaseRepository caseRepository;
    private final UserRepository userRepository;

    public InvestigationCaseService(
            InvestigationCaseRepository caseRepository,
            UserRepository userRepository
    ) {
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CaseResponse createCase(
            CreateCaseRequest request,
            String authenticatedEmail
    ) {

        User owner = findUserByEmail(authenticatedEmail);

        InvestigationCase investigationCase =
                new InvestigationCase();

        investigationCase.setCaseNumber(
                generateUniqueCaseNumber()
        );

        investigationCase.setTitle(
                normalizeTitle(request.title())
        );

        investigationCase.setDescription(
                normalizeDescription(request.description())
        );

        investigationCase.setStatus(CaseStatus.OPEN);

        investigationCase.setPriority(
                request.priority() == null
                        ? CasePriority.MEDIUM
                        : request.priority()
        );

        investigationCase.setOwner(owner);

        InvestigationCase savedCase =
                caseRepository.save(investigationCase);

        return mapToResponse(savedCase);
    }

    @Transactional(readOnly = true)
    public CaseResponse getCase(
            Long caseId,
            String authenticatedEmail
    ) {

        InvestigationCase investigationCase =
                findOwnedCase(caseId, authenticatedEmail);

        return mapToResponse(investigationCase);
    }

    @Transactional(readOnly = true)
    public PageResponse<CaseResponse> getCases(
            String authenticatedEmail,
            String keyword,
            CaseStatus status,
            CasePriority priority,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {

        validatePagination(page, size);
        validateSortField(sortBy);

        Sort.Direction direction =
                parseSortDirection(sortDirection);

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(direction, sortBy)
        );

        Specification<InvestigationCase> specification =
                InvestigationCaseSpecifications
                        .ownedByEmail(authenticatedEmail);

        if (keyword != null && !keyword.isBlank()) {
            specification = specification.and(
                    InvestigationCaseSpecifications
                            .keywordContains(keyword)
            );
        }

        if (status != null) {
            specification = specification.and(
                    InvestigationCaseSpecifications
                            .hasStatus(status)
            );
        }

        if (priority != null) {
            specification = specification.and(
                    InvestigationCaseSpecifications
                            .hasPriority(priority)
            );
        }

        Page<CaseResponse> responsePage =
                caseRepository
                        .findAll(specification, pageRequest)
                        .map(this::mapToResponse);

        return PageResponse.from(
                responsePage,
                sortBy,
                direction.name()
                        .toLowerCase(Locale.ROOT)
        );
    }

    @Transactional
    public CaseResponse updateCase(
            Long caseId,
            UpdateCaseRequest request,
            String authenticatedEmail
    ) {

        InvestigationCase investigationCase =
                findOwnedCase(caseId, authenticatedEmail);

        investigationCase.setTitle(
                normalizeTitle(request.title())
        );

        investigationCase.setDescription(
                normalizeDescription(request.description())
        );

        investigationCase.setPriority(request.priority());

        InvestigationCase updatedCase =
                caseRepository.save(investigationCase);

        return mapToResponse(updatedCase);
    }

    @Transactional
    public CaseResponse updateCaseStatus(
            Long caseId,
            UpdateCaseStatusRequest request,
            String authenticatedEmail
    ) {

        InvestigationCase investigationCase =
                findOwnedCase(caseId, authenticatedEmail);

        investigationCase.setStatus(request.status());

        InvestigationCase updatedCase =
                caseRepository.save(investigationCase);

        return mapToResponse(updatedCase);
    }

    @Transactional
    public void deleteCase(
            Long caseId,
            String authenticatedEmail
    ) {

        InvestigationCase investigationCase =
                findOwnedCase(caseId, authenticatedEmail);

        caseRepository.delete(investigationCase);
    }

    private User findUserByEmail(String email) {

        String normalizedEmail = normalizeEmail(email);

        return userRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new UserNotFoundException(
                        "Authenticated user account was not found"
                ));
    }

    private InvestigationCase findOwnedCase(
            Long caseId,
            String authenticatedEmail
    ) {

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        return caseRepository
                .findByIdAndOwnerEmailIgnoreCase(
                        caseId,
                        normalizedEmail
                )
                .orElseThrow(() -> new CaseNotFoundException(
                        "Investigation case was not found"
                ));
    }

    private String generateUniqueCaseNumber() {

        String datePart = DATE_FORMATTER.format(
                Instant.now()
        );

        for (
                int attempt = 0;
                attempt < CASE_NUMBER_GENERATION_ATTEMPTS;
                attempt++
        ) {

            String candidate =
                    "TL-"
                    + datePart
                    + "-"
                    + generateRandomPart();

            if (!caseRepository.existsByCaseNumber(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException(
                "Unable to generate a unique case number"
        );
    }

    private String generateRandomPart() {

        StringBuilder result =
                new StringBuilder(RANDOM_PART_LENGTH);

        for (
                int index = 0;
                index < RANDOM_PART_LENGTH;
                index++
        ) {

            int randomIndex = SECURE_RANDOM.nextInt(
                    CASE_NUMBER_CHARACTERS.length()
            );

            result.append(
                    CASE_NUMBER_CHARACTERS.charAt(randomIndex)
            );
        }

        return result.toString();
    }

    private void validatePagination(
            int page,
            int size
    ) {

        if (page < 0) {
            throw new InvalidRequestException(
                    "Page number cannot be negative"
            );
        }

        if (size < 1 || size > MAXIMUM_PAGE_SIZE) {
            throw new InvalidRequestException(
                    "Page size must be between 1 and "
                    + MAXIMUM_PAGE_SIZE
            );
        }
    }

    private void validateSortField(String sortBy) {

        if (sortBy == null
                || !ALLOWED_SORT_FIELDS.contains(sortBy)) {

            throw new InvalidRequestException(
                    "Unsupported case sorting field"
            );
        }
    }

    private Sort.Direction parseSortDirection(
            String sortDirection
    ) {

        if (sortDirection == null) {
            throw new InvalidRequestException(
                    "Sort direction is required"
            );
        }

        return Sort.Direction
                .fromOptionalString(sortDirection)
                .orElseThrow(() -> new InvalidRequestException(
                        "Sort direction must be 'asc' or 'desc'"
                ));
    }

    private String normalizeEmail(String email) {

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeTitle(String title) {

        return title
                .trim()
                .replaceAll("\\s+", " ");
    }

    private String normalizeDescription(String description) {

        return description.trim();
    }

    private CaseResponse mapToResponse(
            InvestigationCase investigationCase
    ) {

        User owner = investigationCase.getOwner();

        return new CaseResponse(
                investigationCase.getId(),
                investigationCase.getCaseNumber(),
                investigationCase.getTitle(),
                investigationCase.getDescription(),
                investigationCase.getStatus(),
                investigationCase.getPriority(),
                owner.getId(),
                owner.getFullName(),
                owner.getEmail(),
                investigationCase.getCreatedAt(),
                investigationCase.getUpdatedAt()
        );
    }
}