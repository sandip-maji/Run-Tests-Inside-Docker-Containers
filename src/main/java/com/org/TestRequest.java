package com.org;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class TestRequest {
    private String repoUrl;
    private String mavenCommand;

}
