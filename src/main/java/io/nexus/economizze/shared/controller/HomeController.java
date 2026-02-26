package io.nexus.economizze.shared.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller de redirecionamento inicial da aplicacao.
 */
@Controller
public class HomeController {

    /**
     * Redireciona a raiz para o dashboard principal.
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }
}
