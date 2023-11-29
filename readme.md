
# Cryptarithmetic Puzzle Solver

## Overview
This Cryptarithmetic Puzzle Solver is a Java application that solves alphametic puzzles, where each letter represents a unique digit. The classic example of such a puzzle is "SEND + MORE = MONEY". The solver reads the puzzle from an input text file and writes the solution to an output text file.

## How to Run

### Prerequisites
- Java Development Kit (JDK) installed on your machine.

### Setup
1. Clone the repository to your local machine.
2. Navigate to the directory where the project is located.
3. Ensure that the input text file (e.g., `cryptarithmetic_input.txt`) is placed in the correct directory and formatted properly according to the puzzle you want to solve.

### Running the Program
To run the program, use the following command in the terminal:

```bash
java PuzzleSolver
```

The program will read the input file named `cryptarithmetic_input.txt` from the source directory, attempt to solve the puzzle, and write the solution to `cryptarithmetic_output.txt` in the same directory.

## File Structure
- `PuzzleSolver.java`: The main Java file containing the puzzle solver logic.
- `cryptarithmetic_input.txt`: An example input file containing the puzzle to be solved.
- `cryptarithmetic_output.txt`: The output file where the solution will be written.

## Input and Output File Format

### Input
The input file should contain three rows (lines) of capital letters representing the operands of the puzzle:

```
LLLL
LLLL
LLLLL
```

The first and second rows contain four capital letters, and the third row contains five capital letters with no blank space between letters.

### Output
The output file will be structured as follows, where the `D`s represent digits from 0 to 9:

```
DDDD
DDDD
DDDDD
```

## Contributing
Feel free to fork the repository and submit pull requests. For major changes, please open an issue first to discuss what you would like to change.
