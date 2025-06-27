package com.creatorworks.nexus.editor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping(value = {"/editor", "/editor/{path:[^\\.]*}", "/editor/**/{path:[^\\.]*}"})
    public String forward() {
        return "forward:/editor/index.html";
    }
} 