package com.thms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatDTO {
    private Long id;
    private String rowName;
    private Integer seatNumber;
    private Integer screenNumber;
    private String seatType;
    private Double priceMultiplier;
}