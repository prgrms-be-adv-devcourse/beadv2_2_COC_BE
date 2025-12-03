package com.coc.modi.product.presentation.dto;

public record ImageResponseDto(
        String imageUrl
) {

    public static ImageResponseDto from(String url) {
        return new ImageResponseDto(url);
    }
}
