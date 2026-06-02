package pruebaTechShop.Brandon;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index"; // Redirige a templates/index.html
    }

    @GetMapping("/ejemplo2")
    public String ejemplo2() {
        return "ejemplo2"; // Redirige a templates/ejemplo2.html
    }

    @GetMapping("/multimedia")
    public String multimedia() {
        return "multimedia"; // Redirige a templates/multimedia.html
    }

    @GetMapping("/iframes")
    public String iframes() {
        return "iframes"; // Redirige a templates/iframes.html
    }
}