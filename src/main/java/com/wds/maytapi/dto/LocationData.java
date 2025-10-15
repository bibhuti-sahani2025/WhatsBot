package com.wds.maytapi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LocationData {
    private Double latitude;
    private Double longitude;
    private String name;
    private String address;
}
