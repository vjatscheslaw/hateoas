package truly.hateoas.game.logic;

import java.util.HashMap;

import org.springframework.hateoas.*;

public class GameLogic {
	
	public static record GameInstanceState(HashMap<String, GameBoard> games, HashMap<String, GameState> states) {}
	
	public static record GameBoard(boolean[][] obstacles, int prizeCoordinateX, int prizeCoordinateY) {}
	
	public static class GameState extends RepresentationModel {
		int playerCoordinateX;
		int playerCoordinateY; 
		int turns;
	}
	
	public static record GameDTO(String interactionWithPlayer, String goUpURI, String goDownURI, String goLeftURI, String goRightURI) {}

	public static record Menu(String clientId) { }
	
	public static class MenuDTO extends EntityModel<Menu> {

		public MenuDTO(Menu content) {
			super(content);
		}
		
	}
	
	public static class GameDTOResource extends EntityModel<GameDTO> {
		public GameDTOResource(GameDTO content) {
			super(content);
		}
	}
}
