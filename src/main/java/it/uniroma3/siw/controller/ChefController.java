package it.uniroma3.siw.controller;

import java.io.IOException; 
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import it.uniroma3.siw.model.Chef;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.ChefImage;
import it.uniroma3.siw.model.Recipe;
import it.uniroma3.siw.model.RecipeImage;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.repository.ChefRepository;
import it.uniroma3.siw.repository.CredentialsRepository;
import it.uniroma3.siw.repository.ChefImageRepository;
import it.uniroma3.siw.repository.UserRepository;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.repository.ChefRepository;

@Controller
@Transactional
public class ChefController {
	
	@Autowired 
	private ChefRepository chefRepository;
	@Autowired
	 private CredentialsRepository credentialsRepository;
	@Autowired
	 private UserRepository userRepository;
	@Autowired
	private CredentialsService credentialsService;
	@Autowired
	private ChefImageRepository chefImageRepository;


//	@GetMapping(value="/admin/formNewArtist")
//	public String formNewArtist(Model model) {
//		model.addAttribute("artist", new Chef());
//		return "admin/formNewArtist.html";
//	}
//	
//	@GetMapping(value="/admin/indexArtist")
//	public String indexArtist() {
//		return "admin/indexArtist.html";
//	}
//	
//	@PostMapping("/admin/artist")
//	public String newArtist(@ModelAttribute("artist") Chef chef, Model model) {
//		if (!artistRepository.existsByNameAndSurname(chef.getName(), chef.getSurname())) {
//			this.artistRepository.save(chef); 
//			model.addAttribute("artist", chef);
//			return "artist.html";
//		} else {
//			model.addAttribute("messaggioErrore", "Questo artista esiste già");
//			return "admin/formNewArtist.html"; 
//		}
//	}
//
//	@GetMapping("/artist/{id}")
//	public String getArtist(@PathVariable("id") Long id, Model model) {
//		model.addAttribute("artist", this.artistRepository.findById(id).get());
//		return "artist.html";
//	}
	
	@GetMapping("/chef/updateAccount")
	public String showUpdateProfileForm(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
            User user = credentials.getUser();
             
            Chef chef = this.chefRepository.findByNameAndSurname(user.getName(), user.getSurname());

	        model.addAttribute("chef", chef); // Passa solo l'oggetto Chef al modello
	        return "chef/formUpdateAccount.html"; // Ritorna il template per l'aggiornamento
	    } else {
	        return "redirect:/public/chefs"; // Se non trova l'utente, reindirizza
	    }
	}


	@PostMapping("/chef/updateAcc")
	@Transactional
	public String updateChef(
	    @Valid @ModelAttribute("chef") Chef chef,
	    BindingResult bindingResult,
	    @RequestPart(value = "uploadedImage", required = false) MultipartFile uploadedImage,
	    Model model,
	    @RequestParam("id") Long chefId
	) {
	    if (bindingResult.hasErrors()) {
	        return "chef/formUpdateAccount.html";
	    }

	    try {
	        Chef existingChef = chefRepository.findById(chefId)
	            .orElseThrow(() -> new IllegalArgumentException("Lo chef con l'ID fornito non esiste."));

	        existingChef.setDateOfBirth(chef.getDateOfBirth());

	        if (uploadedImage != null && !uploadedImage.isEmpty()) {
	            byte[] imageData = uploadedImage.getBytes();
	            ChefImage chefImage = new ChefImage(imageData);
	            chefImageRepository.save(chefImage);

	            existingChef.setImage(chefImage);
	        }

	        chefRepository.save(existingChef);

	        return "redirect:/public/chefs";
	    } catch (IOException e) {
	        return "chef/formUpdateAccount.html";
	    }
	}

	
	@GetMapping("/admin/manageChefs")
	public String manageChefs(Model model) {
		model.addAttribute("chefs", this.chefRepository.findAll());
		return "admin/manageChefs.html";
	}
//
	@GetMapping("/public/chefs")
	public String getChefs(Model model) {
	    List<Chef> chefs = (List<Chef>) chefRepository.findAll();

	    for (Chef chef : chefs) {
	        ChefImage chefImage = chef.getImage();
	        if (chefImage != null && chefImage.getImageData() != null) {
	            String base64Image = chefImage.getImageBase64(); // Ottenere Base64 direttamente dall'oggetto ChefImage
	            model.addAttribute("base64Image_" + chef.getId(), base64Image);
	        } else {
	            System.out.println("Nessuna immagine trovata per l'chef con ID: " + chef.getId());
	        }
	    }
	    

	    model.addAttribute("chefs", chefs);
	    return "public/chefs";
	}

	@Transactional
	@PostMapping("/admin/deleteChef/{id}")
	public String deleteChef(@PathVariable Long id, Model model) {
	    // Trova lo chef da eliminare
	    	    Chef chef = chefRepository.findById(id).orElse(null);
	            // Trova l'utente associato allo chef
	            User user = userRepository.findByNameAndSurname(chef.getName(), chef.getSurname()).orElse(null);
	           
	            if (user != null) {
	                // Trova le credenziali associate all'utente
	                Credentials credentials = credentialsRepository.findByUser(user).orElse(null);

	                // Elimina le credenziali, se presenti
	                if (credentials != null) {
	                	
	                    credentialsRepository.delete(credentials);
	                    
	                }
	            }
	            // Infine, elimina lo chef
	            chefRepository.delete(chef);
	            
	            return "redirect:/admin/manageChefs";

	}


    
    @GetMapping("/public/chef/{id}")
    public String getChefAndRecipes(@PathVariable("id") Long id, Model model) {
        Chef chef = chefRepository.findById(id).orElse(null);

        if (chef != null) {
            // Aggiungi lo chef al modello
            model.addAttribute("chef", chef);

            // Recupera le ricette dello chef
            List<Recipe> recipes = chef.getInventorOf();
            
    	    Map<Long, String> recipeFirstImages = new HashMap<>();

    	    for (Recipe recipe : recipes) {
    	        if (!recipe.getRecipeImages().isEmpty()) {
    	            RecipeImage firstImage = recipe.getRecipeImages().get(0);
    	            String base64Image = Base64Utils.encodeToString(firstImage.getImageData());
    	            recipeFirstImages.put(recipe.getId(), base64Image);
    	        }
    	    }

    	    model.addAttribute("recipes", recipes);
    	    model.addAttribute("recipeFirstImages", recipeFirstImages);
            
            
            
            

            // Recupera l'immagine dello chef e convertila in Base64
            ChefImage chefImage = chef.getImage();
            if (chefImage != null) {
                String base64Image = Base64.getEncoder().encodeToString(chefImage.getImageData());
                model.addAttribute("base64Image", base64Image); // Aggiungi al modello
            } else {
                System.out.println("L'immagine dello chef non è stata trovata"); // Debug
            }
        } else {
            System.out.println("Chef non trovato con ID: " + id); // Debug
        }

        return "public/chef"; // Nome del template HTML
    }

    @GetMapping("/chef/indexChef")
    public String chefIndex(Model model) {
        // Ottieni l'autenticazione corrente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
                User user = credentials.getUser();
                    model.addAttribute("nome", user.getName());
                    model.addAttribute("cognome", user.getSurname());
                
            
        }

        return "chef/indexChef.html"; // Ritorna la pagina dell'index degli chef
    }


	
    @GetMapping("/public/filterChefs")
    public String filteredChefsBySurname(@RequestParam(name = "surname", required = false) String surname, Model model) {
        List<Chef> filteredChefs;

        // Utilizziamo il metodo findBySurname del repository per cercare gli chef per cognome
        if (surname != null && !surname.isEmpty()) {
            filteredChefs = chefRepository.findBySurname(surname);
        } else {
            // Se il parametro cognome è vuoto o non specificato, restituisci tutti gli chef
            filteredChefs = (List<Chef>) chefRepository.findAll();
        }

        for (Chef chef : filteredChefs) {
            ChefImage chefImage = chef.getImage();
            if (chefImage != null) {
                String base64Image = Base64.getEncoder().encodeToString(chefImage.getImageData());
                model.addAttribute("base64Image_" + chef.getId(), base64Image); // Aggiungi al modello con un nome unico per ogni chef
            } else {
                System.out.println("L'immagine dello chef non è stata trovata per chef ID: " + chef.getId()); // Debug
            }
        }

        model.addAttribute("chefs", filteredChefs);
        return "public/chefs"; // Assumi che ci sia una pagina chiamata chef-list.html per visualizzare la lista degli chef
    }

}
