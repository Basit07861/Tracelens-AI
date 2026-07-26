package com.tracelens.investigation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tracelens.exception.CaseNotFoundException;
import com.tracelens.exception.InvalidRequestException;
import com.tracelens.investigation.dto.CaseResponse;
import com.tracelens.investigation.dto.CreateCaseRequest;
import com.tracelens.investigation.entity.CasePriority;
import com.tracelens.investigation.entity.CaseStatus;
import com.tracelens.investigation.entity.InvestigationCase;
import com.tracelens.investigation.repository.InvestigationCaseRepository;
import com.tracelens.user.entity.User;
import com.tracelens.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class InvestigationCaseServiceTest {

    @Mock
    private InvestigationCaseRepository caseRepository;

    @Mock
    private UserRepository userRepository;

    private InvestigationCaseService caseService;

    @BeforeEach
    void setUp() {

        caseService = new InvestigationCaseService(
                caseRepository,
                userRepository
        );
    }

    @Test
    void createCaseNormalizesInputAndUsesDefaultPriority() {

        User owner = createOwner();

        CreateCaseRequest request =
                new CreateCaseRequest(
                        "  Suspicious   Invoice   Investigation  ",
                        "  Investigate possible invoice manipulation.  ",
                        null
                );

        when(
                userRepository.findByEmailIgnoreCase(
                        "owner@example.com"
                )
        ).thenReturn(Optional.of(owner));

        when(
                caseRepository.existsByCaseNumber(anyString())
        ).thenReturn(false);

        when(
                caseRepository.save(
                        any(InvestigationCase.class)
                )
        ).thenAnswer(invocation -> {

            InvestigationCase investigationCase =
                    invocation.getArgument(0);

            investigationCase.setId(51L);
            investigationCase.beforeInsert();

            return investigationCase;
        });

        CaseResponse response = caseService.createCase(
                request,
                "  OWNER@EXAMPLE.COM  "
        );

        assertEquals(51L, response.id());

        assertEquals(
                "Suspicious Invoice Investigation",
                response.title()
        );

        assertEquals(
                "Investigate possible invoice manipulation.",
                response.description()
        );

        assertEquals(
                CaseStatus.OPEN,
                response.status()
        );

        assertEquals(
                CasePriority.MEDIUM,
                response.priority()
        );

        assertEquals(
                "owner@example.com",
                response.ownerEmail()
        );

        assertEquals(
                "Test Investigator",
                response.ownerName()
        );

        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());

        assertTrue(
                response.caseNumber().matches(
                        "TL-\\d{8}-[A-Z2-9]{8}"
                )
        );

        ArgumentCaptor<InvestigationCase> caseCaptor =
                ArgumentCaptor.forClass(
                        InvestigationCase.class
                );

        verify(caseRepository).save(
                caseCaptor.capture()
        );

        InvestigationCase savedCase =
                caseCaptor.getValue();

        assertEquals(owner, savedCase.getOwner());

        assertEquals(
                CasePriority.MEDIUM,
                savedCase.getPriority()
        );

        verify(userRepository)
                .findByEmailIgnoreCase(
                        "owner@example.com"
                );
    }

    @Test
    void getCaseReturnsSafeNotFoundForUnownedCase() {

        when(
                caseRepository
                        .findByIdAndOwnerEmailIgnoreCase(
                                91L,
                                "owner@example.com"
                        )
        ).thenReturn(Optional.empty());

        CaseNotFoundException exception =
                assertThrows(
                        CaseNotFoundException.class,
                        () -> caseService.getCase(
                                91L,
                                "  OWNER@EXAMPLE.COM "
                        )
                );

        assertEquals(
                "Investigation case was not found",
                exception.getMessage()
        );

        verify(caseRepository)
                .findByIdAndOwnerEmailIgnoreCase(
                        91L,
                        "owner@example.com"
                );
    }

    @Test
    void getCasesRejectsNegativePageNumber() {

        InvalidRequestException exception =
                assertThrows(
                        InvalidRequestException.class,
                        () -> caseService.getCases(
                                "owner@example.com",
                                null,
                                null,
                                null,
                                -1,
                                10,
                                "updatedAt",
                                "desc"
                        )
                );

        assertEquals(
                "Page number cannot be negative",
                exception.getMessage()
        );

        verifyNoInteractions(
                caseRepository,
                userRepository
        );
    }

    @Test
    void getCasesRejectsUnsupportedSortField() {

        InvalidRequestException exception =
                assertThrows(
                        InvalidRequestException.class,
                        () -> caseService.getCases(
                                "owner@example.com",
                                null,
                                null,
                                null,
                                0,
                                10,
                                "passwordHash",
                                "desc"
                        )
                );

        assertEquals(
                "Unsupported case sorting field",
                exception.getMessage()
        );

        verifyNoInteractions(
                caseRepository,
                userRepository
        );
    }

    @Test
    void getCasesRejectsInvalidSortDirection() {

        InvalidRequestException exception =
                assertThrows(
                        InvalidRequestException.class,
                        () -> caseService.getCases(
                                "owner@example.com",
                                null,
                                null,
                                null,
                                0,
                                10,
                                "updatedAt",
                                "sideways"
                        )
                );

        assertEquals(
                "Sort direction must be 'asc' or 'desc'",
                exception.getMessage()
        );

        verifyNoInteractions(
                caseRepository,
                userRepository
        );
    }

    private User createOwner() {

        User owner = new User();

        owner.setId(7L);
        owner.setFullName("Test Investigator");
        owner.setEmail("owner@example.com");
        owner.setPasswordHash("not-used-in-unit-test");
        owner.setActive(true);
        owner.beforeInsert();

        return owner;
    }
}