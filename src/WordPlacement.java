import java.io.IOException;
import java.util.*;
import java.io.BufferedReader;
import java.io.PrintWriter;

public class WordPlacement {
  /* CONSTANTS */
  public static final int Unsolved = -1;

  /* DATA STRUCTURES */
  private char[][] board; /* Represents the game board */
  private char[][] optimalBoard; /* Represents the optimal game board */
  private int[][] letterMultipliers; /* Stores letter multipliers for each cell */
  private int[][] wordMultipliers; /* Stores word multipliers for each cell */
  private int totalScore;
  private Set<String> dictionarySet = new HashSet<>();
  private Map<Character, Integer> letterValueMap = new HashMap<>();
  private List<String> placedWordsList = new ArrayList<>(); /* List of words placed on the board */
  private List<String> optimalPlacedWordsList = new ArrayList<>(); /* List of optimal words placed on the board */

  /* HELPERS */
  private String[] initialPuzzleStream; /* used to initialize or reset to default value */
  private ValidateStream validateStream = new ValidateStream();
  private boolean[] exceptionArray = {false, false, false}; /* { Board, Dictionary, LetterValue } */

  /**
   * Read a board in from the given stream of data.
   *
   * @param puzzleStream stream of puzzle
   * @return true if the puzzle is ready to use
   * @throws IOException when there is an issue reading the file
   */
  public boolean loadBoard(BufferedReader puzzleStream) throws IOException {
    if (puzzleStream != null) {
      List<String> lines = new ArrayList<>();
      String line;
      boolean isValidAndReady;

      while ((line = puzzleStream.readLine()) != null) {
        lines.add(line.trim());
      }

      puzzleStream.close();

      isValidAndReady = validateStream.validatePuzzle(lines.toArray(new String[0]));
      /* If stream is valid and ready to use, initialize the board */
      if (isValidAndReady) {
        initialPuzzleStream = validateStream.removeTrailingBlanks(lines.toArray(new String[0]));
        initializeBoard();
        exceptionArray[0] = true;
      }
      return isValidAndReady;
    }
    return false;
  }

  /**
   * Accepts a sequence of lines, with one word per line, as the dictionary of the words that will
   * be allowed on the board.
   *
   * @param wordStream Stream of dictionary words
   * @return true if this dictionary is ready to be used for the puzzle
   * @throws IOException when there is an issue reading the file
   */
  public boolean dictionary(BufferedReader wordStream) throws IOException {
    if (wordStream != null) {
      List<String> lines = new ArrayList<>();
      Set<String> tempDict;
      String line;

      while ((line = wordStream.readLine()) != null) {
        lines.add(line.trim());
      }

      wordStream.close();

      tempDict = validateStream.validateDictionary(lines.toArray(new String[0]));
      if (tempDict != null) {
        dictionarySet = tempDict;
        exceptionArray[1] = true;
        return true;
      } else {
        return false;
      }
    }
    return false;
  }

  /**
   * Accepts a sequence of lines that identify the value of a letter when scoring. A
   *
   * @param valueStream stream of a letter value pair
   * @return true if the given letter values are ready to be used for the puzzle
   * @throws IOException when there is an issue reading the file
   */
  public boolean letterValue(BufferedReader valueStream) throws IOException {
    if (valueStream != null) {
      List<String> lines = new ArrayList<>();
      Map<Character, Integer> tempLetterValue;
      String line;

      while ((line = valueStream.readLine()) != null) {
        lines.add(line.trim());
      }

      valueStream.close();

      tempLetterValue = validateStream.validateLetterValue(lines.toArray(new String[0]));
      if (tempLetterValue != null) {
        letterValueMap = tempLetterValue;
        exceptionArray[2] = true;
        return true;
      } else {
        return false;
      }
    }
    return false;
  }

  /**
   * Prints the result to output stream
   *
   * @param outstream file where output will be printed
   * @throws BoardNotLoadedException When the board is not loaded properly in the system
   */
  public void print(PrintWriter outstream) throws BoardNotLoadedException {
    if (!exceptionArray[0]) {
      throw new BoardNotLoadedException();
    }
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board[i].length; j++) {
        char currentCell = board[i][j];
        outstream.print(currentCell);
      }
      outstream.println();
    }
    outstream.flush();
    outstream.close();
  }

  /**
   * place each of the words in the “words” parameter into the puzzle, inserted in the order of the
   * list, so that each word generates the maximum number of points from the given board as the word
   * is placed
   *
   * @param words list of words
   * @return score of the placed word
   * @throws BoardNotLoadedException When the board is not loaded in the system
   * @throws DictionaryNotLoadedException when the dictionary is not loaded in the system
   * @throws LetterValueNotLoadedException when the letter value pair is not loaded in the system
   * @throws WordCantFitException when the first word can't fit on the given board
   */
  public int placeWords(List<String> words)
      throws BoardNotLoadedException,
          DictionaryNotLoadedException,
          LetterValueNotLoadedException,
          WordCantFitException {
    if (!exceptionArray[0]) {
      throw new BoardNotLoadedException();
    }

    if (!exceptionArray[1]) {
      throw new DictionaryNotLoadedException();
    }

    if (!exceptionArray[2]) {
      throw new LetterValueNotLoadedException();
    }

    if (words == null || words.isEmpty()) {
      return Unsolved;
    }

    PlaceWord placeWord =
        new PlaceWord(
            board,
            letterMultipliers,
            wordMultipliers,
            dictionarySet,
            letterValueMap,
            placedWordsList);

    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board[i].length; j++) {
        if (board[i].length < words.get(0).length() && board.length < words.get(0).length()) {
          throw new WordCantFitException();
        }
      }
    }

    int isScoreReady = placeWord.placeWords(words);

    if (isScoreReady == Unsolved) {
      initializeBoard();
    }

    return isScoreReady;
  }

  /* Methods for the bonus parts of the assignment */

  public int solve(Set<String> words)
      throws BoardNotLoadedException, DictionaryNotLoadedException, LetterValueNotLoadedException {

    if (!exceptionArray[0]) {
      throw new BoardNotLoadedException();
    }

    if (!exceptionArray[1]) {
      throw new DictionaryNotLoadedException();
    }

    if (!exceptionArray[2]) {
      throw new LetterValueNotLoadedException();
    }

    if (words == null || words.isEmpty()) {
      return Unsolved;
    }

    List<List<String>> possibleCombinations = generateCombinations(words);

    int isOptimalScoreReady = -1;
    PlaceWord placeWord;

    for (List<String> currentCombination : possibleCombinations) {
      initializeBoard();
      placeWord =
          new PlaceWord(
              board,
              letterMultipliers,
              wordMultipliers,
              dictionarySet,
              letterValueMap,
              placedWordsList);
      int currentCombinationScore = placeWord.placeWords(currentCombination);
      if (currentCombinationScore >= isOptimalScoreReady) {
        isOptimalScoreReady = currentCombinationScore;
        optimalBoard = board;
        optimalPlacedWordsList = currentCombination;
      }
    }

    if (isOptimalScoreReady == Unsolved) {
      optimalPlacedWordsList = new ArrayList<>();
      initializeBoard();
    }
    board = optimalBoard;
    return isOptimalScoreReady;
  }

  public List<String> wordOrder() {
    return optimalPlacedWordsList;
  }

  /* HELPER FUNCTIONS */

  /** Initializes the board, letterMultipliers and wordMultipliers from input stream */
  private void initializeBoard() {
    String[] puzzleStream = initialPuzzleStream;
    int numOfRows = puzzleStream.length;
    int numOfColumns = puzzleStream[0].length();

    board = new char[numOfRows][numOfColumns];
    letterMultipliers = new int[numOfRows][numOfColumns];
    wordMultipliers = new int[numOfRows][numOfColumns];
    placedWordsList = new ArrayList<>();

    for (int i = 0; i < numOfRows; i++) {
      String currentRow = puzzleStream[i];
      for (int j = 0; j < numOfColumns; j++) {
        char currentCell = currentRow.charAt(j);

        board[i][j] = currentCell;

        if (Character.isDigit(currentCell)) {
          letterMultipliers[i][j] = Integer.parseInt(String.valueOf(currentCell));
          wordMultipliers[i][j] = 1;
        } else if (currentCell == 'D') {
          letterMultipliers[i][j] = 1;
          wordMultipliers[i][j] = 2;
        } else if (currentCell == 'T') {
          letterMultipliers[i][j] = 1;
          wordMultipliers[i][j] = 3;
        } else if (currentCell == '*') {
          letterMultipliers[i][j] = 1;
          wordMultipliers[i][j] = 2;
        } else {
          letterMultipliers[i][j] = 1;
          wordMultipliers[i][j] = 1;
        }
      }
    }
  }

  /**
   * Method to generate combinations
   *
   * @param words the words
   * @return list of list of combinations
   */
  private List<List<String>> generateCombinations(Set<String> words) {
    List<List<String>> result = new ArrayList<>();
    List<String> wordList = new ArrayList<>(words);
    int n = wordList.size();

    generateCombinationsHelper(result, wordList, 0, n - 1);

    return result;
  }

  /**
   * Recursive helper function to generate combinations using backtracking
   *
   * @param result the result
   * @param words the words
   * @param left helps keep track of which elements have already been included in the combination
   * @param right helps limit the range of elements that can be swapped during each step of the
   *     combination
   */
  private void generateCombinationsHelper(
      List<List<String>> result, List<String> words, int left, int right) {
    /* If we have reached the end of the word list, we have a complete combinations */
    if (left == right) {
      result.add(new ArrayList<>(words));
    } else {
      for (int i = left; i <= right; i++) {
        swap(words, left, i);
        /* Recursively generate permutations for the remaining elements */
        generateCombinationsHelper(result, words, left + 1, right);
        /* Backtrack by swapping the elements back to their original positions */
        swap(words, left, i);
      }
    }
  }

  /**
   * Utility function to swap elements at two positions in the list
   *
   * @param words the words
   * @param i i'th position
   * @param j j'th position
   */
  private void swap(List<String> words, int i, int j) {
    String temp = words.get(i);
    words.set(i, words.get(j));
    words.set(j, temp);
  }
}
