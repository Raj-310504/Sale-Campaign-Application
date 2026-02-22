package com.example.SaleCampaign.service;

import com.example.SaleCampaign.dto.CampaignDiscountRequestDTO;
import com.example.SaleCampaign.dto.CampaignRequestDTO;
import com.example.SaleCampaign.entity.CampaignProduct;
import com.example.SaleCampaign.entity.CampaignModel;
import com.example.SaleCampaign.entity.ProductModel;
import com.example.SaleCampaign.repository.CampaignRepository;
import com.example.SaleCampaign.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class CampaignService {

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private ProductRepository productRepository;

    public CampaignModel createCampaign(CampaignRequestDTO campaignRequestDTO) {
        CampaignModel campaignModel = new CampaignModel();
        campaignModel.setTitle(campaignRequestDTO.getTitle());
        campaignModel.setStartDate(campaignRequestDTO.getStartDate());
        campaignModel.setEndDate(campaignRequestDTO.getEndDate());

        List<CampaignProduct> campaignProducts = new ArrayList<>();
        for (CampaignDiscountRequestDTO discountDTO : campaignRequestDTO.getCampaignDiscount()) {
            ProductModel product = productRepository.findById(discountDTO.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product not found: " + discountDTO.getProductId()
                    ));

            CampaignProduct campaignProduct = new CampaignProduct();
            campaignProduct.setCampaign(campaignModel);
            campaignProduct.setProduct(product);
            campaignProduct.setDiscount(discountDTO.getDiscount());
            campaignProducts.add(campaignProduct);
        }
        campaignModel.setCampaignProducts(campaignProducts);

        return campaignRepository.save(campaignModel);
    }
}
