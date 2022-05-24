package com.analysis.visualization.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StockDto {

    private String ticker;

    private Double prise;
}
