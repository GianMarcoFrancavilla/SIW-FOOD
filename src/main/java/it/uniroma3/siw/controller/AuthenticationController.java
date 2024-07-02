package it.uniroma3.siw.controller;

import java.util.Locale;
import java.util.Optional;

import javax.validation.Valid;  

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Chef;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.repository.ChefRepository;
import it.uniroma3.siw.repository.CredentialsRepository;
import it.uniroma3.siw.repository.UserRepository;
import it.uniroma3.siw.service.CredentialsService;



@Controller
public class AuthenticationController {
    
    @Autowired
    private CredentialsService credentialsService;
    
    @Autowired
    private CredentialsRepository credentialsRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ChefRepository chefRepository;
    
    @Autowired
    private MessageSource messageSource;
    
    
    @GetMapping(value = "/public/register") 
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("credentials", new Credentials());
        return "public/formRegisterUser";
    }
    
    @GetMapping(value = "/public/login") 
    public String showLoginForm(Model model) {
        return "public/formLogin";
    }

    @GetMapping(value = "/") 
    public String index() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated()) {
            return "public/index.html";
        } else {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
            if (credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
                return "admin/indexAdmin.html";
            } else {
            	return "redirect:/chef/indexChef";
            }
        }
    }

    

    @PostMapping(value = { "/public/register" })
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult userBindingResult, @Valid
                               @ModelAttribute("credentials") Credentials credentials,
                               BindingResult credentialsBindingResult,
                               Model model, Locale locale) {

        // Controlla se l'username esiste già
        Optional<Credentials> existingCredentials = credentialsRepository.findByUsername(credentials.getUsername());
        if (existingCredentials.isPresent()) {
        	credentialsBindingResult.rejectValue("username", "error.username", "Username già esistente");
        }

        // Controlla se l'email esiste già
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            userBindingResult.rejectValue("email", "error.email", "Email già esistente");
        }

        // Se ci sono errori, ritorna al form di registrazione
        if (userBindingResult.hasErrors() || credentialsBindingResult.hasErrors()) {
            model.addAttribute("user", user); // Assicura che l'oggetto user sia aggiunto al model
            model.addAttribute("credentials", credentials); // Assicura che l'oggetto credentials sia aggiunto al model
            return "public/formRegisterUser";
        }

        // Salva l'utente e le credenziali se non ci sono errori
        credentials.setUser(user);
        credentialsService.saveCredentials(credentials);

        // Se il ruolo delle credenziali è default, aggiungi automaticamente un nuovo Chef
        if (credentials.getRole().equals(Credentials.DEFAULT_ROLE)) {
            Chef chef = new Chef();
            chef.setName(user.getName());
            chef.setSurname(user.getSurname());
            chef.setDateOfBirth(null);
            chef.setImage(null);
            // Aggiungi il nuovo chef alla lista degli chef
            chefRepository.save(chef);
        }

        model.addAttribute("user", user);
        return "public/registrationSuccessful";
    }


}
