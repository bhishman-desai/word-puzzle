# Scrabble Game

## Project Overview
This project is a collection of classes that perform a process to build a Scrabble game based upon state space exploration. The process includes the following steps:

1. **Have a list of words that are to be added to the puzzle**: The game starts with a list of words that are to be added to the puzzle.

2. **Load the board in the system**: In this step, we load the board into the system, assign points to letters, and add words to the dictionary.

3. **Try to add the words to the board which satisfies certain conditions**: We try to add the words to the board which satisfies the following conditions:
    a. If the word is in the dictionary.
    b. If the word falls within the boundaries of the board.
    c. If the word is not overwriting any existing word on the board.
    d. If after placing a word, it’s augmenting a word which is not in the dictionary.
    e. If the placement of the word gives maximum score.

The goal is to satisfy all puzzle constraints and fit the word to gain maximum points.

## Further Reading 
If you want to learn more about the project including test cases, files and external data, data structures and their relation to each other, assumptions and choices made, key algorithms and design elements, and limitations of the project you can refer this [report](Bhishman_Desai__B00945177.pdf).

## Contributing
Contributions to enhance the game's functionality or address any issues are welcome. Feel free to use the provided source code as a reference for creating similar applications for your institution.

## License
This project is licensed under the [MIT License](LICENSE).
