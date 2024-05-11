import java.util.List;

public class NewWord {

  private String name;
  private boolean isHorizontal;
  private List<Integer> startIndex;
  private List<Integer> endIndex;
  private int score;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isHorizontal() {
    return isHorizontal;
  }

  public void setHorizontal(boolean horizontal) {
    isHorizontal = horizontal;
  }

  public List<Integer> getStartIndex() {
    return startIndex;
  }

  public void setStartIndex(List<Integer> startIndex) {
    this.startIndex = startIndex;
  }

  public List<Integer> getEndIndex() {
    return endIndex;
  }

  public void setEndIndex(List<Integer> endIndex) {
    this.endIndex = endIndex;
  }

  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

  public NewWord(
      String name,
      boolean isHorizontal,
      List<Integer> startIndex,
      List<Integer> endIndex,
      int score) {
    this.name = name;
    this.isHorizontal = isHorizontal;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
    this.score = score;
  }

  @Override
  public String toString() {
    return "NewWord {"
        + "name='"
        + name
        + '\''
        + ", isHorizontal="
        + isHorizontal
        + ", startIndex="
        + startIndex
        + ", endIndex="
        + endIndex
        + ", score="
        + score
        + '}';
  }
}
