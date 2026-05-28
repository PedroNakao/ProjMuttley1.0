package com.trabalho.tag.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.trabalho.tag.service.TagService;

@Controller
@RequestMapping("/tag")
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tag", tagService.listarTodas());
        return "tag/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(@RequestParam String nome,
                        RedirectAttributes redirect) {
        if (nome.isBlank()) {
            redirect.addFlashAttribute("erro", "O nome da tag não pode ser vazio.");
            return "redirect:/tag";
        }
        tagService.salvar(nome);
        redirect.addFlashAttribute("sucesso", "Tag " + nome + " salvo com sucesso.");
        return "redirect:/tag";
    }

    @PostMapping("/editar/{id}")
    public String editar(@PathVariable Long id,
                        @RequestParam String nome,
                        RedirectAttributes redirect) {
        tagService.editar(id, nome);
        redirect.addFlashAttribute("sucesso", "Tag " + nome + " atualizado com sucesso.");
        return "redirect:/tag";
    }

    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id,
                        RedirectAttributes redirect) {
        tagService.excluir(id);
        redirect.addFlashAttribute("sucesso", "Tag excluído com sucesso.");
        return "redirect:/tag";
    }
}
