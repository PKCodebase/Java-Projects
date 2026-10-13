package com.info.configdemo.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class BuildInfoController {

    @Value("${build.id:default}")
    private String buildId;
    @Value("${build.version:default}")
    private String buildVersion;
    @Value("${build.name:default}")
    private String buildName;


//    private BuildInfo buildInfo;




    @GetMapping("/build-info")
    public String getBuildInfo(){
       return "Build ID: " + buildId + ", Build Version: " + buildVersion + ", Build Name: " + buildName;
    }
}
