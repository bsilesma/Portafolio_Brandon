package pruebaTechShop.Brandon;

import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@Configuration
public class projectConfig implements WebMvcConfigurer {

    /* localeResolver determina el idioma por defecto de la sesión */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.getDefault()); // Toma el idioma del sistema operativo
        slr.setLocaleAttributeName("session.current.locale");
        slr.setTimeZoneAttributeName("session.current.timezone");
        return slr;
    }

    /* localeChangeInterceptor crea una vía para conocer cómo se determina el cambio de idioma */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang"); // El parámetro en la URL será ?lang=es o ?lang=en
        return lci;
    }

    /* addInterceptors es el responsable de registrar el interceptor en el ambiente de ejecución */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    /* Permite utilizar textos/mensajes guardados en archivos de propiedades externos */
    @Bean("messageSource")
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    /* ==========================================================================
       Lec09 - Introducción a Spring Security
       ========================================================================== */

    /* Vistas simples que no necesitan un controlador propio (login y acceso denegado).
       Nota: "/" NO se registra aquí porque ya lo maneja IndexController. */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/acceso_denegado").setViewName("acceso_denegado");
    }

    /* Agrupación de URLs por tipo de acceso, para una gestión clara y centralizada.
       PUBLIC_URLS: rutas de acceso libre (cualquier persona, autenticada o no). */
    public static final String[] PUBLIC_URLS = {
        "/", "/index", "/css/**", "/js/**", "/webjars/**", "/fav/**",
        "/carrito/**", "/consultas/**", "/registro/**",
        "/login", "/acceso_denegado"
    };

    /* ADMIN_URLS: rutas exclusivas para el rol ADMIN (crear, modificar, eliminar). */
    public static final String[] ADMIN_URLS = {
        "/producto/nuevo", "/producto/guardar", "/producto/modificar/**", "/producto/eliminar/**",
        "/categoria/nuevo", "/categoria/guardar", "/categoria/modificar/**", "/categoria/eliminar/**",
        "/usuario/nuevo", "/usuario/guardar", "/usuario/modificar/**", "/usuario/eliminar/**"
    };

    /* ADMIN_OR_VENDEDOR_URLS: rutas de solo lectura (listados) para ADMIN y VENDEDOR. */
    public static final String[] ADMIN_OR_VENDEDOR_URLS = {
        "/producto/listado", "/categoria/listado", "/usuario/listado"
    };

    /* USUARIO_URLS: rutas para el rol USUARIO (funcionalidades del carrito). */
    public static final String[] USUARIO_URLS = {
        "/facturar/carrito"
    };

    /* securityFilterChain: cadena de filtros que aplica las reglas de acceso en orden. */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(request -> request
                .requestMatchers(PUBLIC_URLS).permitAll()
                .requestMatchers(ADMIN_URLS).hasRole("ADMIN")
                .requestMatchers(ADMIN_OR_VENDEDOR_URLS).hasAnyRole("ADMIN", "VENDEDOR")
                .requestMatchers(USUARIO_URLS).hasRole("USUARIO")
                .anyRequest().authenticated()
        ).formLogin(form -> form // Configuración del formulario de login
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
        ).logout(logout -> logout // Configuración de logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
        ).exceptionHandling(exceptions -> exceptions // Manejo de excepciones
                .accessDeniedPage("/acceso_denegado")
        ).sessionManagement(session -> session // Configuración de sesiones
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
        );
        return http.build();
    }

    /* PasswordEncoder: BCrypt es el estándar de la industria; las contraseñas nunca
       se almacenan en texto plano. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /* UserDetailsService: usuarios en memoria SOLO para la fase de desarrollo.
       Este método será reemplazado por una solución en base de datos la siguiente semana. */
    @Bean
    public UserDetailsService users(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.builder()
                .username("juan")
                .password(passwordEncoder.encode("123"))
                .roles("ADMIN")
                .build();

        UserDetails sales = User.builder()
                .username("rebeca")
                .password(passwordEncoder.encode("456"))
                .roles("VENDEDOR")
                .build();

        UserDetails user = User.builder()
                .username("pedro")
                .password(passwordEncoder.encode("789"))
                .roles("USUARIO")
                .build();

        return new InMemoryUserDetailsManager(admin, sales, user);
    }
}
