package com.info.configdemo.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class BuildInfoController {

//    @Value("${build.id:default}")
//    @Value("${OS:default}")
//    private String buildId;
//    @Value("${build.version:default}")
//   @Value("${PROCESSOR_LEVEL:default}")
//    private String buildVersion;
//
//   @Value("${JAVA_HOME:default}")
//    @Value("${build.name:default}")
//    private String buildName;


    private BuildInfo buildInfo;




    @GetMapping("/build-info")
    public String getBuildInfo(){
       return "Build ID: " + buildInfo.getId() + ", Build Version: " + buildInfo.getVersion() + ", Build Name: " + buildInfo.getName();
    }
}
