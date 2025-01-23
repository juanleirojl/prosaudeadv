package com.prosaude.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.prosaude.controllers.requests.SimulacaoRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

    @GetMapping
    public String home(Model model) {
        model.addAttribute("simulacao", new SimulacaoRequest());
        return "simulacao/form";
    }
}