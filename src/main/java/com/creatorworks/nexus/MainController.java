package com.creatorworks.nexus;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @GetMapping(value = "/")
    public String main(){
        return "main";
    }

    @GetMapping("/main-content")
    public String getMainContent() {
        return "fragment/main_content";
    }
}
