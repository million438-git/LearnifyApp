package com.million.learnify.studentbackend.controller;

import com.million.learnify.studentbackend.dto.SystemConfiguration;
import com.million.learnify.studentbackend.service.SystemConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * SystemFeaturesController - Public Feature Flag & Operational Status Gateway.
 */
@RestController
@RequestMapping("/api/system")
public class SystemFeaturesController {

    private final SystemConfigService configService;

    public SystemFeaturesController(SystemConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/features")
    public ResponseEntity<Map<String, Object>> getPublicFeatures() {
        SystemConfiguration config = configService.getConfiguration();
        Map<String, Object> features = new HashMap<>();
        features.put("maintenanceMode", config.isMaintenanceMode());
        features.put("maintenanceMessage", config.getMaintenanceMessage());
        features.put("minClientVersion", config.getMinClientVersion());
        features.put("aiServiceEnabled", config.isAiServiceEnabled());
        features.put("labSimulationsEnabled", config.isLabSimulationsEnabled());
        features.put("cameraScannerEnabled", config.isCameraScannerEnabled());
        features.put("chatEnabled", config.isChatEnabled());
        features.put("leaderboardEnabled", config.isLeaderboardEnabled());
        features.put("materialsDownloadEnabled", config.isMaterialsDownloadEnabled());
        features.put("institutionName", config.getInstitutionName());
        features.put("portalTagline", config.getPortalTagline());
        features.put("configVersion", config.getVersion());
        features.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(features);
    }
}

