package com.tracelens.investigation.repository;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.tracelens.investigation.entity.CasePriority;
import com.tracelens.investigation.entity.CaseStatus;
import com.tracelens.investigation.entity.InvestigationCase;

public final class InvestigationCaseSpecifications {

    private InvestigationCaseSpecifications() {
    }

    public static Specification<InvestigationCase> ownedByEmail(
            String ownerEmail
    ) {

        String normalizedEmail = ownerEmail
                .trim()
                .toLowerCase(Locale.ROOT);

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        criteriaBuilder.lower(
                                root.join("owner")
                                        .<String>get("email")
                        ),
                        normalizedEmail
                );
    }

    public static Specification<InvestigationCase> keywordContains(
            String keyword
    ) {

        String searchPattern =
                "%"
                + keyword
                        .trim()
                        .toLowerCase(Locale.ROOT)
                + "%";

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.or(

                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.<String>get("caseNumber")
                                ),
                                searchPattern
                        ),

                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.<String>get("title")
                                ),
                                searchPattern
                        ),

                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.<String>get("description")
                                ),
                                searchPattern
                        )
                );
    }

    public static Specification<InvestigationCase> hasStatus(
            CaseStatus status
    ) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("status"),
                        status
                );
    }

    public static Specification<InvestigationCase> hasPriority(
            CasePriority priority
    ) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("priority"),
                        priority
                );
    }
}