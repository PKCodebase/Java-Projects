package com.mcd.plantation.pojo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CertificatePdf {
    private String name;
    private String occasion;
    private String place;
    private String date;
}
