package truly.hateoas.game.state;

import java.util.HashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.WebApplicationContext;

import truly.hateoas.game.logic.GameLogic.GameServerState;
import truly.hateoas.game.logic.GameLogic.GameState;

@Configuration
public class GameConfig {

	@Bean
	@Scope(value = WebApplicationContext.SCOPE_APPLICATION)
	public GameServerState gameServerState() {
		return new GameServerState(new HashMap<String, GameState>());
	}
	
}