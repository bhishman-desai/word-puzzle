import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

  public static void main(String[] args) {

    WordPlacement wordPlacement = new WordPlacement();

    /* LOAD BOARD */
    try (BufferedReader bufferedReader =
        new BufferedReader(new FileReader("src/resources/puzzleStream.txt"))) {
      wordPlacement.loadBoard(bufferedReader);
    } catch (IOException ioException) {
      System.out.println(ioException.getMessage());
    }

    /* ADD TO DICTIONARY */
    try (BufferedReader bufferedReader =
        new BufferedReader(new FileReader("src/resources/wordStream.txt"))) {
      wordPlacement.dictionary(bufferedReader);
    } catch (IOException ioException) {
      System.out.println(ioException.getMessage());
    }

    /* ADD LETTER VALUE PAIRS */
    try (BufferedReader bufferedReader =
        new BufferedReader(new FileReader("src/resources/valueStream.txt"))) {
      wordPlacement.letterValue(bufferedReader);
    } catch (IOException ioException) {
      System.out.println(ioException.getMessage());
    }

    /* PLACE WORDS */
    List<String> inputWords = new ArrayList<>();
    inputWords.add("cat");
    inputWords.add("tea");
    inputWords.add("let");
    try {
      System.out.println(wordPlacement.placeWords(inputWords));
    } catch (BoardNotLoadedException
        | DictionaryNotLoadedException
        | LetterValueNotLoadedException
        | WordCantFitException exceptionMessage) {
      System.out.println(exceptionMessage.getMessage());
    }

    /* PRINT */
    try (PrintWriter printWriter = new PrintWriter(new FileWriter("src/resources/output.txt"))) {
      wordPlacement.print(printWriter);
    } catch (IOException | BoardNotLoadedException exceptionMessage) {
      System.out.println(exceptionMessage.getMessage());
    }

    /* SOLVE OPTIMALLY */
    Set<String> inputSet = new HashSet<>();
    inputSet.add("cat");
    inputSet.add("tea");
    inputSet.add("let");
    try {
      System.out.println(wordPlacement.solve(inputSet));
    } catch (BoardNotLoadedException
        | DictionaryNotLoadedException
        | LetterValueNotLoadedException exceptionMessage) {
      System.out.println(exceptionMessage.getMessage());
    }

    /* WORD ORDER */
    System.out.println(wordPlacement.wordOrder());
  }
}
