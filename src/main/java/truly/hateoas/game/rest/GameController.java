package truly.hateoas.game.rest;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import truly.hateoas.game.logic.GameLogic.GameInstanceState;
import truly.hateoas.game.logic.GameLogic.Menu;
import truly.hateoas.game.logic.GameLogic.MenuDTO;

@RestController
@RequestMapping("/game")
public class GameController {

	@Autowired
	private GameInstanceState gameInstanceState;
	
	@GetMapping("/menu")
	public MenuDTO getMainMenu() {
		MenuDTO m =  new MenuDTO(new Menu(UUID.randomUUID().toString()));
		m.add(WebMvcLinkBuilder.linkTo(GameController.class).slash("menu").withSelfRel());
		return m;
	}
	
}
