package com.analysis.visualization.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class PortfolioDto {

    private UUID id;

    private List<StockDto> ticker;
}
