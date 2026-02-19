package com.example.SaleCampaign.repository;

import com.example.SaleCampaign.entity.CampaignModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<CampaignModel, Long> {
    List<CampaignModel> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDateTime now1, LocalDateTime now2
    );
}
