package truly.hateoas.game.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonFilter;

import truly.hateoas.game.logic.GameLogic;
import truly.hateoas.game.logic.GameLogic.ChooseDifficultyMenu;
import truly.hateoas.game.logic.GameLogic.ChooseDifficultyMenuDTO;
import truly.hateoas.game.logic.GameLogic.GameDifficulty;
import truly.hateoas.game.logic.GameLogic.GameMove;
import truly.hateoas.game.logic.GameLogic.GameServerState;
import truly.hateoas.game.logic.GameLogic.GameStateDTO;
import truly.hateoas.game.logic.GameLogic.MainMenu;
import truly.hateoas.game.logic.GameLogic.MainMenuDTO;

@RestController
@RequestMapping("/game")
public class GameController {

	@Autowired
	private GameServerState gameServerState;

	@GetMapping("/menu")
	public MainMenuDTO getMainMenu(@RequestParam(value = "playerId", required = false) String pId) {
		if (Objects.nonNull(pId))
			gameServerState.games().remove(pId);
		String playerId = UUID.randomUUID().toString();
		MainMenuDTO dto = new MainMenuDTO(new MainMenu(playerId));
		dto.add(
				linkTo(GameController.class).slash("menu").withSelfRel(),
				linkTo(methodOn(GameController.class, dto).chooseDifficulty(playerId)).withRel("start_game")
		);
		return dto;
	}

	@GetMapping("/chooseDifficulty/{playerId}")
	public ChooseDifficultyMenuDTO chooseDifficulty(@PathVariable("playerId") String playerId) {
		ChooseDifficultyMenuDTO dto = new ChooseDifficultyMenuDTO(new ChooseDifficultyMenu(playerId));
		dto.add(linkTo(methodOn(GameController.class, dto).chooseDifficulty(playerId)).withSelfRel(),
				linkTo(methodOn(GameController.class, dto).play(playerId, GameDifficulty.EASY.toString(), GameMove.STAY.toString()))
						.withRel(GameDifficulty.EASY.toString()),
				linkTo(methodOn(GameController.class, dto).play(playerId, GameDifficulty.NORMAL.toString(), GameMove.STAY.toString()))
						.withRel(GameDifficulty.NORMAL.toString()),
				linkTo(methodOn(GameController.class, dto).play(playerId, GameDifficulty.HARD.toString(), GameMove.STAY.toString()))
						.withRel(GameDifficulty.HARD.toString()),
				linkTo(GameController.class).slash("menu?playerId=" + playerId).withRel("back_to_main_menu")

		);
		return dto;
	}

	@GetMapping("/play/{playerId}")
	public GameStateDTO play(@PathVariable("playerId") String playerId, @RequestParam("level") String level, @RequestParam("move") String move) {		
		if (Objects.isNull(gameServerState.games().get(playerId)))
			gameServerState.games().put(playerId, GameLogic.generateNewGame.apply(level)); // <-- we create a new game if not exists yet		
		
		gameServerState.games().put(playerId, GameLogic.nextTurn.apply(gameServerState.games().get(playerId), GameMove.valueOf(move.toUpperCase())));
		
		GameStateDTO dto = new GameStateDTO(gameServerState.games().get(playerId));
		List<Link> links = new ArrayList<>();
		links.add(linkTo(methodOn(GameController.class, dto).play(playerId, level, GameMove.STAY.name())).withRel("stay_and_look_at_your_radar_watch"));
		links.add(linkTo(GameController.class).slash("menu?playerId=" + playerId).withRel("back_to_main_menu"));
		if (!gameServerState.games().get(playerId).isGameFinished()) {
		links.addAll(List.of(		
				linkTo(methodOn(GameController.class, dto).play(playerId, level, GameMove.UP.name())).withRel("go_North"),
				linkTo(methodOn(GameController.class, dto).play(playerId, level, GameMove.DOWN.name())).withRel("go_South"),
				linkTo(methodOn(GameController.class, dto).play(playerId, level, GameMove.LEFT.name())).withRel("go_West"),
				linkTo(methodOn(GameController.class, dto).play(playerId, level, GameMove.RIGHT.name())).withRel("go_East")
						)				
				);
		}
		dto.add(links);
		return dto;
	}
}
