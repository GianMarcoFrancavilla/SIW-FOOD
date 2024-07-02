package it.uniroma3.siw.controller;

import javax.validation.Valid; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.controller.validator.IngredientValidator;
import it.uniroma3.siw.model.Ingredient;
import it.uniroma3.siw.repository.IngredientRepository;
import it.uniroma3.siw.repository.IngredientRepository;

@Controller
public class IngredientController {
	
	@Autowired 
	private IngredientRepository ingredientRepository;
	@Autowired
	private IngredientValidator ingredientValidator;

//	@GetMapping(value="/admin/formNewArtist")
//	public String formNewArtist(Model model) {
//		model.addAttribute("artist", new Ingredient());
//		return "admin/formNewArtist.html";
//	}
//	
//	@GetMapping(value="/admin/indexArtist")
//	public String indexArtist() {
//		return "admin/indexArtist.html";
//	}
//	
//	@PostMapping("/admin/artist")
//	public String newArtist(@ModelAttribute("artist") Ingredient Ingredient, Model model) {
//		if (!artistRepository.existsByNameAndSurname(Ingredient.getName(), Ingredient.getSurname())) {
//			this.artistRepository.save(Ingredient); 
//			model.addAttribute("artist", Ingredient);
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
//
	@GetMapping("/chef/ingredients")
	public String getIngredients(Model model) {
		model.addAttribute("ingredients", this.ingredientRepository.findAll());
		return "chef/ingredients.html";
	}
	
	@PostMapping("/deleteIngredient/{id}")
    public String deleteIngredient(@PathVariable Long id, Model model) {
        try {
            ingredientRepository.deleteById(id);
            return "redirect:/chef/manageIngredients";
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("errorMessage", "Impossibile eliminare l'ingrediente. È utilizzato in almeno una ricetta.");
            model.addAttribute("ingredients", this.ingredientRepository.findAll());
            return "chef/manageIngredients";
        }
    }
    
	@GetMapping("/chef/manageIngredients")
	public String manageIngredients(Model model) {
		model.addAttribute("ingredients", this.ingredientRepository.findAll());
		return "chef/manageIngredients.html";
	}
	
	@GetMapping(value="/chef/formNewIngredient")
	public String formNewIngredient(Model model) {
		model.addAttribute("ingredient", new Ingredient());
		return "chef/formNewIngredient.html";
	}

	
	@PostMapping("/chef/ingredient")
	public String newIngredient(@Valid @ModelAttribute("ingredient") Ingredient ingredient, BindingResult bindingResult, Model model) {
		
		this.ingredientValidator.validate(ingredient, bindingResult);
		if (!bindingResult.hasErrors()) {
			this.ingredientRepository.save(ingredient); 
			model.addAttribute("ingredient", ingredient);
			model.addAttribute("ingredients", this.ingredientRepository.findAll());
			return "chef/ingredients.html";
		} else {
			return "chef/formNewIngredient.html"; 
		}
	}
	
}
