package truly.hateoas.game.state;

import java.util.HashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.WebApplicationContext;

import truly.hateoas.game.logic.GameLogic.GameBoard;
import truly.hateoas.game.logic.GameLogic.GameInstanceState;
import truly.hateoas.game.logic.GameLogic.GameState;

@Configuration
public class GameConfig {

	@Bean
	@Scope(value = WebApplicationContext.SCOPE_APPLICATION)
	public GameInstanceState gameInstanceState() {
		return new GameInstanceState(new HashMap<String, GameBoard>(), new HashMap<String, GameState>());
	}
	
}