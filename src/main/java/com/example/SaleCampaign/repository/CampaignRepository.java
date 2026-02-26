package com.example.SaleCampaign.repository;

import com.example.SaleCampaign.entity.CampaignModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<CampaignModel, Long> {
    List<CampaignModel> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate now1, LocalDate now2
    );
}
