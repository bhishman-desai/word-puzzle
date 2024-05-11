import java.util.*;

public class ValidateStream {

  public boolean validatePuzzle(String[] puzzleStream) {
    puzzleStream = removeTrailingBlanks(puzzleStream);

    if (puzzleStream.length == 0) {
      return false;
    }

    /* Getting columns/characters from first line/row */
    int numberOfColumns = puzzleStream[0].length();
    int designatedStartCount = 0;

    for (String line : puzzleStream) {

      if (line.isEmpty()) {
        return false;
      }

      /* Inconsistent number of columns */
      if (line.length() != numberOfColumns) {
        return false;
      }

      for (char character : line.toCharArray()) {
        if (character == '0' || character == 0) {
          return false;
        }
        /* Invalid character */
        if (character != 'D'
            && character != 'T'
            && character != '.'
            && character != '*'
            && !Character.isDigit(character)) {
          return false;
        }

        /* More than one designated start */
        if (character == '*') {
          designatedStartCount++;
          if (designatedStartCount > 1) {
            return false;
          }
        }
      }
    }

    return designatedStartCount == 1;
  }

  public Set<String> validateDictionary(String[] wordStream) {
    wordStream = removeTrailingBlanks(wordStream);
    Set<String> dictionary = new HashSet<>();

    if (wordStream.length == 0) {
      return null;
    }

    for (String line : wordStream) {
      if (line.isEmpty()) {
        return null;
      }

      /* More than one word in a line */
      if (line.contains(" ")) {
        return null;
      }

      dictionary.add(line);
    }

    return dictionary;
  }

  public Map<Character, Integer> validateLetterValue(String[] valueStream) {
    valueStream = removeTrailingBlanks(valueStream);
    Map<Character, Integer> letterValue = new HashMap<>();

    if (valueStream.length == 0) {
      return null;
    }

    for (String line : valueStream) {
      if (line.isEmpty()) {
        return null;
      }

      try {
        String[] parts = line.split("\t", 0);
        if (parts.length == 2) {
          if (parts[0].contains(" ")) {
            return null;
          }
          letterValue.put(parts[0].charAt(0), Integer.valueOf(parts[1]));
        } else {
          return null;
        }
      } catch (Exception exception) {
        return null;
      }
    }

    return letterValue;
  }

  /* Helper Functions */
  public String[] removeTrailingBlanks(String[] arr) {
    int lastIndex = arr.length - 1;

    while (lastIndex >= 0 && arr[lastIndex].trim().isEmpty()) {
      lastIndex--;
    }

    /* Create a new array with non-blank elements up to lastIndex */
    return Arrays.copyOf(arr, lastIndex + 1);
  }
}
