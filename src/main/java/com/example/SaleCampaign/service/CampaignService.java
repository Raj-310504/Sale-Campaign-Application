package com.example.SaleCampaign.service;

import com.example.SaleCampaign.dto.CampaignDiscountRequestDTO;
import com.example.SaleCampaign.dto.CampaignRequestDTO;
import com.example.SaleCampaign.entity.CampaignProduct;
import com.example.SaleCampaign.entity.CampaignModel;
import com.example.SaleCampaign.entity.ProductModel;
import com.example.SaleCampaign.exception.ResourceNotFoundException;
import com.example.SaleCampaign.exception.ServiceException;
import com.example.SaleCampaign.repository.CampaignRepository;
import com.example.SaleCampaign.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CampaignService {

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private ProductRepository productRepository;

    public CampaignModel createCampaign(CampaignRequestDTO campaignRequestDTO) {
        try {
            CampaignModel campaignModel = new CampaignModel();
            campaignModel.setTitle(campaignRequestDTO.getTitle());
            campaignModel.setStartDate(campaignRequestDTO.getStartDate());
            campaignModel.setEndDate(campaignRequestDTO.getEndDate());

            List<CampaignProduct> campaignProducts = new ArrayList<>();
            for (CampaignDiscountRequestDTO discountDTO : campaignRequestDTO.getCampaignDiscount()) {
                ProductModel product = productRepository.findById(discountDTO.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException(
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
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            throw new ServiceException("Failed to create campaign due to database error.", ex);
        } catch (Exception ex) {
            throw new ServiceException("Unexpected error while creating campaign.", ex);
        }
    }
}
