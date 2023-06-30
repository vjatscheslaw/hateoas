package truly.hateoas.game.logic;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.springframework.hateoas.EntityModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

public class GameLogic {

	public enum GameDifficulty {
		EASY("EASY"), NORMAL("NORMAL"), HARD("HARD");

		private String name;

		private GameDifficulty(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}
	}

	public enum GameMove {
		UP("UP"), DOWN("DOWN"), LEFT("LEFT"), RIGHT("RIGHT"), STAY("STAY");

		private String name;

		private GameMove(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}
	}

	public static record GameServerState(HashMap<String, GameState> games) {}

	public static record GameBoard(boolean[][] obstacles, int prizeCoordinateX, int prizeCoordinateY) {}

	@JsonIncludeProperties({"interaction"})
	public static class GameState {
		private GameBoard gameBoard;
		private int playerCoordinateX;
		private int playerCoordinateY;
		private int turns;
		private String interaction;
		private boolean gameFinished;

		public GameState(GameBoard gameBoard, int playerCoordinateX, int playerCoordinateY, GameMove move, int turns) {
			super();
			this.gameBoard = gameBoard;
			this.playerCoordinateX = playerCoordinateX;
			this.playerCoordinateY = playerCoordinateY;
			this.turns = turns;
			this.gameFinished = false;

			boolean hitTheWall = switch (move) {
			case UP -> playerCoordinateY == (gameBoard.obstacles[0].length - 1)
					|| gameBoard.obstacles()[playerCoordinateX][playerCoordinateY + 1];
			case DOWN -> playerCoordinateY == 0 || gameBoard.obstacles()[playerCoordinateX][playerCoordinateY - 1];
			case RIGHT -> playerCoordinateX == (gameBoard.obstacles.length - 1)
					|| gameBoard.obstacles()[playerCoordinateX + 1][playerCoordinateY];
			case LEFT -> playerCoordinateX == 0 || gameBoard.obstacles()[playerCoordinateX - 1][playerCoordinateY];
			default -> false;
			};
			if (hitTheWall) {
				this.interaction = """
						You've touched the wall. Can't go there...
						""";
			} else {
				this.interaction = """
						You walk through anthracite darkness...
						""";
				switch (move) {
				case UP: {
					this.playerCoordinateY++;
					break;
				}
				case DOWN: {
					this.playerCoordinateY--;
					break;
				}
				case RIGHT: {
					this.playerCoordinateX++;
					break;
				}
				case LEFT: {
					this.playerCoordinateX--;
					break;
				}
				case STAY: {
					this.interaction = """
							You stand in an absolutely dark chamber.\s \n \
							There is no light, only watch on your wrist with backlit screen, saying %s\s \n \
							Your task is to find a ladder of a maintenance manhole to climb out of this place.
							""".formatted("X:" + this.playerCoordinateX + " Y:" + this.playerCoordinateY);
				}
				}
				;
				if (this.playerCoordinateX == gameBoard.prizeCoordinateX
						&& this.playerCoordinateY == gameBoard.prizeCoordinateY) {
					this.gameFinished = true;
					this.interaction = """
							Congratulations! You've found the ladder, and climbed up, towards light.\s \n \
							You have completed the game in %s turns on %s difficulty level.\s \n \
							Go back to main menu to play again.
							""".formatted(String.valueOf(turns), switch (this.gameBoard.obstacles.length) {
					case 5:
						yield GameDifficulty.EASY.name;
					case 10:
						yield GameDifficulty.NORMAL.name;
					case 33:
						yield GameDifficulty.HARD.name;
					default:
						yield "cheater";
					});
				}
			}
		}

		public GameBoard getGameBoard() {
			return gameBoard;
		}

		public void setGameBoard(GameBoard gameBoard) {
			this.gameBoard = gameBoard;
		}

		public int getPlayerCoordinateX() {
			return playerCoordinateX;
		}

		public void setPlayerCoordinateX(int playerCoordinateX) {
			this.playerCoordinateX = playerCoordinateX;
		}

		public int getPlayerCoordinateY() {
			return playerCoordinateY;
		}

		public void setPlayerCoordinateY(int playerCoordinateY) {
			this.playerCoordinateY = playerCoordinateY;
		}

		public int getTurns() {
			return turns;
		}

		public void setTurns(int turns) {
			this.turns = turns;
		}

		public String getInteraction() {
			return interaction;
		}

		public boolean isGameFinished() {
			return gameFinished;
		}

		public void setInteraction(String interaction) {
			this.interaction = interaction;
		}
		
		
	}

	public static class MainMenu {
		private String interaction;

		public MainMenu(String playerId) {
			interaction = """
					Welcome to the truly HATEOAS game!\s \n \
					Your unique player ID: %s.\s \n \
					In order to regenerate your ID,\s \n \
					click the self reference.\s \n \
					Click start_game reference in order to play!\
					""".formatted(playerId);
		}

		public String getInteraction() {
			return interaction;
		}

	}

	public static class ChooseDifficultyMenu {
		private String interaction;

		public ChooseDifficultyMenu(String playerId) {
			interaction = """
					Please, choose the difficulty level:\s \n \
					easy(5x5), normal(10x10), hard(33x33).\s \n \
					Or you may go back to main menu by following back_to_main_menu link\
					""".formatted(playerId);
		}

		public String getInteraction() {
			return interaction;
		}
	}

	public static class GameStateDTO extends EntityModel<GameState> {
		public GameStateDTO(GameState content) {
			super(content);
		}
	}

	public static class MainMenuDTO extends EntityModel<MainMenu> {
		public MainMenuDTO(MainMenu content) {
			super(content);
		}
	}

	public static class ChooseDifficultyMenuDTO extends EntityModel<ChooseDifficultyMenu> {
		public ChooseDifficultyMenuDTO(ChooseDifficultyMenu content) {
			super(content);
		}
	}

	public static Function<String, GameState> generateNewGame = (String level) -> placePlayerOntoBoard(
			placePrizeOntoBoard(generateRandomBoard(GameDifficulty.valueOf(level.toUpperCase()))));

	public static BiFunction<GameState, GameMove, GameState> nextTurn = (GameState game, GameMove move) -> 
	new GameState(Optional.ofNullable(game).map(GameState::getGameBoard).get(), // <-- we want an NPE here if the game is null (should never be possible)
					game.getPlayerCoordinateX(),
					game.getPlayerCoordinateY(), 
					Objects.requireNonNull(move), 
					game.getTurns() + 1); // <-- the move should never be null

	private static GameState placePlayerOntoBoard(GameBoard board) {
		int playerCoordinateX = -1;
		int playerCoordinateY = -1;
		while (playerCoordinateX < 0 || playerCoordinateY < 0) {
			int playerCoordinateXCandidate = ThreadLocalRandom.current().nextInt(0, board.obstacles().length);
			int playerCoordinateYCandidate = ThreadLocalRandom.current().nextInt(0, board.obstacles()[0].length);
			if (board.obstacles()[playerCoordinateXCandidate][playerCoordinateYCandidate]
					|| (board.prizeCoordinateX == playerCoordinateXCandidate
							&& board.prizeCoordinateY == playerCoordinateYCandidate))
				continue;
			playerCoordinateX = playerCoordinateXCandidate;
			playerCoordinateY = playerCoordinateYCandidate;
		}
		return new GameState(board, playerCoordinateX, playerCoordinateY, GameMove.STAY, 0);
	}

	private static GameBoard placePrizeOntoBoard(boolean[][] board) {
		int prizeCoordinateX = -1;
		int prizeCoordinateY = -1;
		while (prizeCoordinateX < 0 || prizeCoordinateY < 0) {
			int prizeCoordinateXCandidate = ThreadLocalRandom.current().nextInt(0, board.length);
			int prizeCoordinateYCandidate = ThreadLocalRandom.current().nextInt(0, board[0].length);
			if (board[prizeCoordinateXCandidate][prizeCoordinateYCandidate])
				continue;
			prizeCoordinateX = prizeCoordinateXCandidate;
			prizeCoordinateY = prizeCoordinateYCandidate;
		}
		return new GameBoard(board, prizeCoordinateX, prizeCoordinateY);
	}

	private static boolean[][] generateRandomBoard(GameDifficulty level) {
		return switch (level) {
		case EASY -> generateRandomSquareBoardByDimension(5);
		case NORMAL -> generateRandomSquareBoardByDimension(10);
		case HARD -> generateRandomSquareBoardByDimension(33);
		};
	}

	private static boolean[][] generateRandomSquareBoardByDimension(int dimension) {
		boolean[][] board = new boolean[dimension][dimension];
		for (int i = 0; i < board.length; i++)
			for (int j = 0; j < board[i].length; j++)
				board[i][j] = ThreadLocalRandom.current().nextInt(1, 11) < 3; // TRUE == obstacle
		return board;
	}
}
