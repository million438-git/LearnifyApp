package com.million.learnify.studentbackend.controller;

import com.million.learnify.studentbackend.dto.StudentPreferencesDto;
import com.million.learnify.studentbackend.dto.SystemConfiguration;
import com.million.learnify.studentbackend.security.FirebasePrincipal;
import com.million.learnify.studentbackend.security.FirebaseTokenVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "learnify.firebase.enabled=false")
@AutoConfigureMockMvc
class SettingsBackendIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FirebaseTokenVerifier tokenVerifier;

    @Test
    void publicFeaturesEndpointIsAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/system/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institutionName").isNotEmpty())
                .andExpect(jsonPath("$.aiServiceEnabled").value(true));
    }

    @Test
    void adminConfigEndpointRejectsAnonymous() throws Exception {
        mockMvc.perform(get("/api/admin/system/config"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminConfigEndpointRejectsStudentRole() throws Exception {
        when(tokenVerifier.verify("student-token"))
                .thenReturn(new FirebasePrincipal("student-1", "student@learnify.org", true, false));

        mockMvc.perform(get("/api/admin/system/config")
                        .header("Authorization", "Bearer student-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminConfigEndpointAllowsAdminRole() throws Exception {
        when(tokenVerifier.verify("admin-token"))
                .thenReturn(new FirebasePrincipal("admin-1", "admin@learnify.org", true, true));

        mockMvc.perform(get("/api/admin/system/config")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passingPercentage").value(75))
                .andExpect(jsonPath("$.version").isNumber());
    }

    @Test
    void adminConfigUpdateRejectsInvalidPassingPercentage() throws Exception {
        when(tokenVerifier.verify("admin-token"))
                .thenReturn(new FirebasePrincipal("admin-1", "admin@learnify.org", true, true));

        SystemConfiguration invalidConfig = new SystemConfiguration();
        invalidConfig.setPassingPercentage(150); // Invalid: > 100%

        mockMvc.perform(put("/api/admin/system/config")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidConfig)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Passing percentage must be between 0 and 100."));
    }

    @Test
    void adminConfigUpdateSucceedsAndIncrementsVersion() throws Exception {
        when(tokenVerifier.verify("admin-token"))
                .thenReturn(new FirebasePrincipal("admin-1", "admin@learnify.org", true, true));

        SystemConfiguration validConfig = new SystemConfiguration();
        validConfig.setInstitutionName("Learnify International School");
        validConfig.setPassingPercentage(80);

        mockMvc.perform(put("/api/admin/system/config")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validConfig)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institutionName").value("Learnify International School"))
                .andExpect(jsonPath("$.passingPercentage").value(80))
                .andExpect(jsonPath("$.modifiedBy").value("admin@learnify.org"));

        // Verify audit log has the update
        mockMvc.perform(get("/api/admin/system/audit-logs")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("UPDATE_SYSTEM_CONFIG"));
    }

    @Test
    void studentPreferencesEndpointRetrievesAndUpdates() throws Exception {
        when(tokenVerifier.verify("student-token"))
                .thenReturn(new FirebasePrincipal("student-1", "student@learnify.org", true, false));

        // GET preferences
        mockMvc.perform(get("/api/student/preferences")
                        .header("Authorization", "Bearer student-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyStudyMinutes").value(30));

        // PUT update
        StudentPreferencesDto update = new StudentPreferencesDto();
        update.setDailyStudyMinutes(45);
        update.setWeeklyLessonGoal(15);
        update.setVersion(1);
        update.setUpdatedAt(System.currentTimeMillis());

        mockMvc.perform(put("/api/student/preferences")
                        .header("Authorization", "Bearer student-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyStudyMinutes").value(45))
                .andExpect(jsonPath("$.weeklyLessonGoal").value(15))
                .andExpect(jsonPath("$.version").value(2));
    }

    @Test
    void studentPreferencesConflictDetection() throws Exception {
        when(tokenVerifier.verify("student-token"))
                .thenReturn(new FirebasePrincipal("student-conflict-user", "student.conflict@learnify.org", true, false));

        // Establish version 2 on server
        StudentPreferencesDto v1 = new StudentPreferencesDto();
        v1.setDailyStudyMinutes(60);
        v1.setVersion(1);
        v1.setUpdatedAt(System.currentTimeMillis() + 1000);

        mockMvc.perform(put("/api/student/preferences")
                        .header("Authorization", "Bearer student-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(v1)))
                .andExpect(status().isOk());

        // Now attempt an update with version 0 and old timestamp (stale client)
        StudentPreferencesDto stale = new StudentPreferencesDto();
        stale.setDailyStudyMinutes(15);
        stale.setVersion(0);
        stale.setUpdatedAt(System.currentTimeMillis() - 50000);

        mockMvc.perform(put("/api/student/preferences")
                        .header("Authorization", "Bearer student-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stale)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }
}
