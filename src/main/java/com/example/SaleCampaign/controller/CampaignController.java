package com.example.SaleCampaign.controller;

import com.example.SaleCampaign.dto.CampaignRequestDTO;
import com.example.SaleCampaign.entity.CampaignModel;
import com.example.SaleCampaign.service.CampaignService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    @Autowired
    private CampaignService campaignService;

    @PostMapping
    public ResponseEntity<CampaignModel> createCampaign(@Valid @RequestBody CampaignRequestDTO campaignRequestDTO) {
        return ResponseEntity.ok(campaignService.createCampaign(campaignRequestDTO));
    }

}
