package com.trabalho.tag.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/escanear")
    public String escanear() {
        return "escanear";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/tag/formulario")
    public String tagFormulario() {
        return "redirect:/tag";
    }
}
