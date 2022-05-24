package com.analysis.visualization.investment.entity;

import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Stock {

    @Id
    @GeneratedValue
    @Column(name = "portfolio_id")
    private UUID portfolio;

    private String ticker;

    private Integer count;

}
