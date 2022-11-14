package org.spring.project.application.server.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ResourceProperties {

    @Value("${resource.image.libraryMainImage}")
    private String libraryMainImage;
    @Value("${resource.image.libraryLogo}")
    private String libraryLogo;
    @Value("${resource.image.newsPreviewImage}")
    private String newsPreviewImage;
    @Value("${resource.image.updateImage}")
    private String updateImage;

    @Value("${resource.style.gamePageStyle}")
    private String gamePageStyle;
    @Value("${resource.style.gamesByCategoryPageStyle}")
    private String gamesByCategoryPageStyle;
    @Value("${resource.style.profilePageStyle}")
    private String profilePageStyle;
    @Value("${resource.style.storePageStyle}")
    private String storePageStyle;
    @Value("${resource.style.successBuyingPageStyle}")
    private String successBuyingPageStyle;

    @Value("${resource.image.format}")
    private String imageFormat;
}
