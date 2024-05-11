import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceWord {
  /* CONSTANTS */
  private static final int UNSOLVED = -1;
  private static final String smallLettersRegex = "[a-z]";

  /* DATA STRUCTURES */
  private char[][] board;
  private int[][] letterMultipliers;
  private int[][] wordMultipliers;
  private Set<String> dictionarySet;
  private Map<Character, Integer> letterValueMap;
  private List<String> placedWordsList;
  private List<NewWord> placedWordsMetadata = new ArrayList<>();
  private List<String> validAugmentedWords = new ArrayList<>();

  public PlaceWord(
      char[][] board,
      int[][] letterMultipliers,
      int[][] wordMultipliers,
      Set<String> dictionarySet,
      Map<Character, Integer> letterValueMap,
      List<String> placedWordsList) {
    this.board = board;
    this.letterMultipliers = letterMultipliers;
    this.wordMultipliers = wordMultipliers;
    this.dictionarySet = dictionarySet;
    this.letterValueMap = letterValueMap;
    this.placedWordsList = placedWordsList;
  }

  /**
   * The driver code which iterates through the list of words and calculates total score
   *
   * @param words list of words to be placed on board
   * @return the total score
   */
  public int placeWords(List<String> words) {
    int totalScore = 0;

    for (String word : words) {
      if (!isWordInDictionary(word)) {
        return UNSOLVED;
      }

      int wordScore = placeWord(word);

      /* If word score cannot be computed */
      if (wordScore == UNSOLVED) {
        return UNSOLVED;
      }
      totalScore += wordScore;
      placedWordsList.add(word);
    }

    int augmentedScore = calculateAugmentedScore();

    return totalScore + augmentedScore;
  }

  /**
   * This method handles the actual placing of the word on the board
   *
   * @param word the word to be placed
   * @return score of the word after getting placed
   */
  private int placeWord(String word) {
    int bestScore;
    int[] horizontalScoreArray = {0, 0, -1}; /* {row, column, score} */
    int[] verticalScoreArray = {0, 0, -1}; /* {row, column, score} */

    lines:
    for (int currentRow = 0; currentRow < board.length; currentRow++) {
      characters:
      for (int currentColumn = 0; currentColumn < board[currentRow].length; currentColumn++) {

        /* Adding first word */
        if (board[currentRow][currentColumn] == '*') {

          int[] horizontalScoreArrayForFirst =
              getBestPlacementForFirstWordOnBoard(currentRow, currentColumn, word, true);
          int[] verticalScoreArrayForFirst =
              getBestPlacementForFirstWordOnBoard(currentRow, currentColumn, word, false);

          if (horizontalScoreArrayForFirst[1] >= verticalScoreArrayForFirst[1]) {
            placeHorizontal(currentRow, horizontalScoreArrayForFirst[0], word, board);
            placedWordsMetadata.add(
                new NewWord(
                    word,
                    true,
                    List.of(currentRow, currentColumn),
                    List.of(currentRow, currentColumn + (word.length() - 1)),
                    horizontalScoreArrayForFirst[1] * 2));
            return horizontalScoreArrayForFirst[1] * 2;
          } else {
            placeVertical(verticalScoreArrayForFirst[0], currentColumn, word, board);
            placedWordsMetadata.add(
                new NewWord(
                    word,
                    false,
                    List.of(currentRow, currentColumn),
                    List.of(currentRow + (word.length() - 1), currentColumn),
                    verticalScoreArrayForFirst[1] * 2));
            return verticalScoreArrayForFirst[1] * 2;
          }
        }

        /* Adding a word when there is at least one word on the board */
        if (String.valueOf(board[currentRow][currentColumn]).matches(smallLettersRegex)
            && commonLetterExists(word, currentRow, currentColumn)
            && !placedWordsList.contains(word)) {

          /* Calculating the best placement for a word both horizontally and vertically */
          int[] tempHorizontalScoreArray =
              getBestHorizontalPlacementOnBoard(currentRow, currentColumn, word);
          int[] tempVerticalScoreArray =
              getBestVerticalPlacementOnBoard(currentRow, currentColumn, word);

          if (horizontalScoreArray[2] <= tempHorizontalScoreArray[2]) {
            horizontalScoreArray = tempHorizontalScoreArray;
          }

          if (verticalScoreArray[2] <= tempVerticalScoreArray[2]) {
            verticalScoreArray = tempVerticalScoreArray;
          }
        }
      }
    }

    /* If we did not find any best placements, check for cross-placement */
    if (horizontalScoreArray[2] == -1 && verticalScoreArray[2] == -1) {
      return checkCrossWord(word);
    }

    /* When we find the placement */
    if (horizontalScoreArray[2] >= verticalScoreArray[2]) {
      /* If the horizontal placement was best of two, we place the word and add to the list */
      bestScore = horizontalScoreArray[2];
      placeHorizontal(horizontalScoreArray[0], horizontalScoreArray[1], word, board);
      placedWordsMetadata.add(
          new NewWord(
              word,
              true,
              List.of(horizontalScoreArray[0], horizontalScoreArray[1]),
              List.of(horizontalScoreArray[0], horizontalScoreArray[1] + (word.length() - 1)),
              horizontalScoreArray[2]));
    } else {
      /* If the vertical placement was best of two, we place the word and add to the list */
      bestScore = verticalScoreArray[2];
      placeVertical(verticalScoreArray[0], verticalScoreArray[1], word, board);
      placedWordsMetadata.add(
          new NewWord(
              word,
              false,
              List.of(verticalScoreArray[0], verticalScoreArray[1]),
              List.of(verticalScoreArray[0] + (word.length() - 1), verticalScoreArray[1]),
              verticalScoreArray[2]));
    }

    return bestScore;
  }

  /**
   * This function is used to find best placement for first word considering that it should touch
   * the designated start spot
   *
   * @param row the row
   * @param column the columns
   * @param word the word to be placed
   * @param isHorizontal true for horizontal placement
   * @return row, column and score of the best placement
   */
  private int[] getBestPlacementForFirstWordOnBoard(
      int row, int column, String word, boolean isHorizontal) {
    /* This method checks for both horizontal and vertical placement */
    int maxScore = 0;
    int startIndex = (isHorizontal ? column : row) - (word.length() - 1);
    int endIndex = isHorizontal ? column : row;
    int searchIndex = 0;

    if (startIndex < 0) {
      startIndex += word.length() - 1;
      endIndex += word.length();
    }

    /* Boundary check */
    while (startIndex <= column
        && (isHorizontal ? endIndex <= board[row].length : endIndex <= board.length)) {

      /* Calculating the best scores for horizontal and vertical positions */
      int currentScore =
          isHorizontal
              ? calculateScoreOnBoard(row, startIndex, word, true)
              : calculateScoreOnBoard(startIndex, column, word, false);

      if (maxScore <= currentScore) {
        maxScore = currentScore;
        searchIndex = startIndex;
      }

      startIndex += 1;
      endIndex += 1;
    }

    return new int[] {searchIndex, maxScore};
  }

  /**
   * Get the best placement for horizontal placement of the word
   *
   * @param row the row
   * @param column the column
   * @param word the word to be placed
   * @return row, column and score for the best placement on board
   */
  private int[] getBestHorizontalPlacementOnBoard(int row, int column, String word) {
    int[] horizontalScoreArray = {0, 0, -1};

    /* Overwriting Check */
    if ((column > 1 && !String.valueOf(board[row][column - 1]).matches(smallLettersRegex))
        && (column < board[row].length - 1
            && !String.valueOf(board[row][column + 1]).matches(smallLettersRegex))) {

      /* Boundary Check */
      if (canWordFitHorizontally(word, column - word.indexOf(board[row][column]))) {

        /* Getting Maximum Score */
        int currentScore =
            calculateScoreOnBoard(row, column - word.indexOf(board[row][column]), word, true);

        if (horizontalScoreArray[2] <= currentScore) {
          horizontalScoreArray[0] = row;
          horizontalScoreArray[1] = column - word.indexOf(board[row][column]);
          horizontalScoreArray[2] = currentScore;
        }
      }
    }

    /* If we got a score then checking if the word placement is valid and fulfills the constraints */
    if (horizontalScoreArray[2] > 0 && isWordPlacementInvalid(word, horizontalScoreArray, true)) {
      return new int[] {0, 0, -1};
    }

    return horizontalScoreArray;
  }

  /**
   * Get the best placement for vertical placement of the word
   *
   * @param row the row
   * @param column the column
   * @param word the word to be placed
   * @return row, column and score for the best placement on board
   */
  private int[] getBestVerticalPlacementOnBoard(int row, int column, String word) {
    int[] verticalScoreArray = {0, 0, -1};

    /* Overwriting Check */
    if ((row > 1 && !String.valueOf(board[row - 1][column]).matches(smallLettersRegex))
        && (row < board.length - 1
            && !String.valueOf(board[row + 1][column]).matches(smallLettersRegex))) {

      /* Boundaries Check */
      if (canWordFitVertically(word, row - word.indexOf(board[row][column]))) {

        /* Getting Maximum Score */
        int currentScore =
            calculateScoreOnBoard(row - word.indexOf(board[row][column]), column, word, false);

        if (verticalScoreArray[2] <= currentScore) {
          verticalScoreArray[0] = row - word.indexOf(board[row][column]);
          verticalScoreArray[1] = column;
          verticalScoreArray[2] = currentScore;
        }
      }
    }

    /* If we got a score then checking if the word placement is valid and fulfills the constraints */
    if (verticalScoreArray[2] > 0 && isWordPlacementInvalid(word, verticalScoreArray, false)) {
      return new int[] {0, 0, -1};
    }

    return verticalScoreArray;
  }

  /**
   * Method to calculate the score a word would generate when placed on the board
   *
   * @param row the row the word is placed in
   * @param column the column the word is placed in
   * @param word the word to be placed
   * @param isHorizontal is the direction of placement horizontal?
   * @return the score after placement
   */
  private int calculateScoreOnBoard(int row, int column, String word, boolean isHorizontal) {
    int score = 0;
    int wordMultiplier = 1; /* Initialize word multiplier */

    for (int i = 0; i < word.length(); i++) {
      char letter = word.charAt(i);
      int letterMultiplier =
          isHorizontal
              ? letterMultipliers[row][column + i]
              : letterMultipliers[row + i][column]; /* Get letter multiplier for the cell */

      score += letterValueMap.get(letter) * letterMultiplier;

      /* Check for word multipliers (D and T) */
      char cell = isHorizontal ? board[row][column + i] : board[row + i][column];
      if (cell == 'D') {
        wordMultiplier *= 2;
      } else if (cell == 'T') {
        wordMultiplier *= 3;
      }
    }

    /* Apply the word multiplier */
    score *= wordMultiplier;

    return score;
  }

  /**
   * Placing the word horizontally on the board
   *
   * @param row row on board
   * @param column column on board
   * @param word word to be placed
   */
  private void placeHorizontal(int row, int column, String word, char[][] board) {
    for (int j = column, k = 0; k < word.length(); j++, k++) {
      board[row][j] = word.charAt(k);
    }
  }

  /**
   * Placing the word vertically on the board
   *
   * @param row row on board
   * @param column column on board
   * @param word word to be placed
   */
  private void placeVertical(int row, int column, String word, char[][] board) {
    for (int i = row, k = 0; k < word.length(); i++, k++) {
      board[i][column] = word.charAt(k);
    }
  }

  /**
   * Checks if the word placement on board is valid or not
   *
   * @param word the word to be placed
   * @param scoreArray row, column and score of the word
   * @param isHorizontal true for horizontal placement check
   * @return true if placement is invalid
   */
  private boolean isWordPlacementInvalid(String word, int[] scoreArray, boolean isHorizontal) {
    char[][] cloneBoard = cloneBoard();

    if (isHorizontal) {
      placeHorizontal(scoreArray[0], scoreArray[1], word, cloneBoard);
    } else {
      placeVertical(scoreArray[0], scoreArray[1], word, cloneBoard);
    }

    int cloneRows = cloneBoard.length;
    int cloneColumns = cloneBoard[0].length;

    for (int i = 0; i < cloneRows; i++) {
      /* Check words formed from rows */
      String rowString = new String(cloneBoard[i]);
      Pattern pattern = Pattern.compile("[a-z]+");
      Matcher matcher = pattern.matcher(rowString);

      while (matcher.find()) {
        String formedWord = matcher.group();
        if (formedWord.length() >= 2 && !dictionarySet.contains(formedWord)) {
          return true;
        }
        validAugmentedWords.add(
            formedWord); /* Adding the valid-formed words alongside so that we can calculate score for augmented words */
      }

      /* Check words formed from columns */
      char[] column = new char[cloneColumns];
      for (int j = 0; j < cloneColumns; j++) {
        column[j] = cloneBoard[j][i];
      }
      String colString = new String(column);
      pattern = Pattern.compile("[a-z]+");
      matcher = pattern.matcher(colString);

      while (matcher.find()) {
        String formedWord = matcher.group();
        if (formedWord.length() >= 2 && !dictionarySet.contains(formedWord)) {
          return true;
        }
        validAugmentedWords.add(
            formedWord); /* Adding the valid-formed words alongside so that we can calculate score for augmented words */
      }
    }

    return false;
  }

  /**
   * Checks if word can fit horizontally on board
   *
   * @param word the word
   * @param column the column
   * @return true if word can fit else false
   */
  private boolean canWordFitHorizontally(String word, int column) {
    return column > 0 && column + (word.length() - 1) < board[column].length;
  }

  /**
   * Checks if word can fit vertically on board
   *
   * @param word the word
   * @param row the column
   * @return true if word can fit else false
   */
  private boolean canWordFitVertically(String word, int row) {
    return row > 0 && row + (word.length() - 1) < board.length;
  }

  /**
   * This is to check if the word can be placed cross to the existing words on the board
   *
   * @param word the word to be placed
   * @return score of the word if placed else -1
   */
  private int checkCrossWord(String word) {
    int row = 0;
    int column = 0;
    boolean isValidPlacement;

    wordsWhichArePlaced:
    for (String placedWordsOnBoard : placedWordsList) {

      wordToBePlaced:
      for (int i = 0; i < word.toCharArray().length; i++) {

        String pre = prefix(word.charAt(i), placedWordsOnBoard); /* Adding character at the start */
        String post = postfix(word.charAt(i), placedWordsOnBoard); /* Adding character at the end */

        /* Getting the placed word and their metadata */
        NewWord newWord = placedWordsMetadata.get(placedWordsList.indexOf(placedWordsOnBoard));

        /* If the word exists in the dictionary */
        if (dictionarySet.contains(pre)) {
          if (newWord.isHorizontal()) {
            /* If the placed word is horizontally placed, placing the current word cross to it in vertical direction */
            row = newWord.getStartIndex().get(0) - i;
            column = newWord.getStartIndex().get(1) - 1;
          } else {
            row = newWord.getStartIndex().get(0) + 1;
            column = newWord.getStartIndex().get(1) - i;
          }
        }
        if (dictionarySet.contains(post)) {
          if (newWord.isHorizontal()) {
            row = newWord.getEndIndex().get(0) - i;
            column = newWord.getEndIndex().get(1) + 1;
          } else {
            row = newWord.getEndIndex().get(0) + 1;
            column = newWord.getEndIndex().get(1) - i;
          }
        }

        if (row > 0 && column > 0) {
          /* Checking if the cross-placement is within the boundaries and not overwriting the existing words on board */
          isValidPlacement =
              newWord.isHorizontal()
                  ? isCrossWithinVerticalLimits(word, row, column)
                  : isCrossWithinHorizontalLimits(word, row, column);

          if (isValidPlacement) {
            /* Getting score of the cross placed word, placing it on the board and later adding it to the placedWordsMetadata array */
            int score = calculateScoreOnBoard(row, column, word, !newWord.isHorizontal());
            if (isWordPlacementInvalid(
                word, new int[] {row, column, score}, !newWord.isHorizontal())) {
              return UNSOLVED;
            }
            if (newWord.isHorizontal()) {
              placeVertical(row, column, word, board);
              placedWordsMetadata.add(
                  new NewWord(
                      word,
                      false,
                      List.of(row, column),
                      List.of(row + (word.length() - 1), column),
                      score));
            } else {
              placeHorizontal(row, column, word, board);
              placedWordsMetadata.add(
                  new NewWord(
                      word,
                      true,
                      List.of(row, column),
                      List.of(row, column + (word.length() - 1)),
                      score));
            }
            return score;
          }
        }
      }
    }

    /* If the cross-placement was not successful */
    return UNSOLVED;
  }

  /* Cloning the original board. Useful when we need to check if the word placement is valid or not and fulfills all the constraint */
  private char[][] cloneBoard() {
    char[][] clonedBoard = new char[board.length][board[0].length];
    for (int i = 0; i < clonedBoard.length; i++) {
      System.arraycopy(board[i], 0, clonedBoard[i], 0, clonedBoard[i].length);
    }

    return clonedBoard;
  }

  /**
   * Check if the word is in the dictionary or not
   *
   * @param word the word ot be searched
   * @return true if word is found
   */
  private boolean isWordInDictionary(String word) {
    return dictionarySet.contains(word);
  }

  /**
   * Checks for a common letter in the list of words which are placed
   *
   * @param word the word
   * @param row the row
   * @param column the column
   * @return true if a common letter is found
   */
  private boolean commonLetterExists(String word, int row, int column) {
    return word.contains(String.valueOf(board[row][column]));
  }

  /**
   * Adding prefix character to a word
   *
   * @param startCharacter start character to be prefixed
   * @param word the word
   * @return prefixed word
   */
  private String prefix(char startCharacter, String word) {
    return startCharacter + word;
  }

  /**
   * Adding postfix character to a word
   *
   * @param endCharacter end character to be postfix
   * @param word the word
   * @return postfix word
   */
  private String postfix(char endCharacter, String word) {
    return word + endCharacter;
  }

  /**
   * Within the boundaries and empty cell validation
   *
   * @param word word to be placed
   * @param row row to be checked on board
   * @param column column to be checked on board
   * @return true if the word pass validation
   */
  private boolean isCrossWithinHorizontalLimits(String word, int row, int column) {
    /* Checking if within the limit */
    if (board[row].length >= (column + word.length())) {
      /* Checking if the cells are empty */
      for (int j = column, k = 0; k < word.length(); j++, k++) {
        if (String.valueOf(board[row][j]).matches(smallLettersRegex)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Within the boundaries and empty cell validation
   *
   * @param word word to be placed
   * @param row row to be checked on board
   * @param column column to be checked on board
   * @return true if the word pass validation
   */
  private boolean isCrossWithinVerticalLimits(String word, int row, int column) {
    /* Checking if within the limit */
    if (board.length >= (row + word.length())) {
      /* Checking if the cells are empty */
      for (int j = row, k = 0; k < word.length(); j++, k++) {
        if (String.valueOf(board[j][column]).matches(smallLettersRegex)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /* Calculating the score for augmented words */
  private int calculateAugmentedScore() {
    int score = 0;
    for (String word : validAugmentedWords) {
      if (word.length() > 1 && !placedWordsList.contains(word)) {
        for (char character : word.toCharArray()) {
          score += letterValueMap.get(character);
        }
      }
    }

    return score;
  }
}
