import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PuzzleSolver {

    private static final int FIRST_OPERAND_LENGTH = 4;
    private static final int SECOND_OPERAND_LENGTH = 4;
    private static final int RESULT_OPERAND_LENGTH = 5;

    public static void main(String[] args) {
        // Array of input file names
        String[] inputFileNames = {"input1.txt", "input2.txt"};

        // Loop through each file name
        for (String inputFileName : inputFileNames) {
            try {
                // Read puzzle from the file
                String[] operands = readPuzzleFromFile("src/" + inputFileName);

                // Solve the puzzle
                String[] solution = solve(operands[0], operands[1], operands[2]);

                // Construct the name for the output file
                String outputFileName = "src/output_" + inputFileName;

                // Write the solution or 'No solution' message to the output file
                writeSolutionToFile(solution, outputFileName);
                System.out.println("Solution written to " + outputFileName);

            } catch (IOException e) {
                // Handle exceptions related to file input/output operations
                System.out.println("Error reading or writing file " + inputFileName + ": " + e.getMessage());
            } catch (IllegalArgumentException e) {
                // Handle exceptions related to invalid arguments (e.g., invalid puzzle format or data)
                System.out.println("Invalid argument provided: " + e.getMessage());
            } catch (Exception e) {
                // Catch any other unforeseen exceptions
                System.out.println("An unexpected error occurred: " + e.getMessage());
            }
        }
    }


    private static String[] readPuzzleFromFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            // Read the operands and result from the file and trim them
            String firstOperand = reader.readLine().trim();
            String secondOperand = reader.readLine().trim();
            String resultOperand = reader.readLine().trim();

            // Use constants to check the length of operands
            if (firstOperand.length() != FIRST_OPERAND_LENGTH ||
                    secondOperand.length() != SECOND_OPERAND_LENGTH ||
                    resultOperand.length() != RESULT_OPERAND_LENGTH) {
                throw new IOException("Input file format is incorrect. " +
                        "Expected lengths: " + FIRST_OPERAND_LENGTH + ", " +
                        SECOND_OPERAND_LENGTH + ", " + RESULT_OPERAND_LENGTH + ".");
            }

            // Validate operand characters
            if (!firstOperand.matches("[A-Z]+") || !secondOperand.matches("[A-Z]+") || !resultOperand.matches("[A-Z]+")) {
                throw new IOException("Operands must only contain uppercase letters.");
            }

            // Check that no extra non-null lines are present
            if (reader.readLine() != null) {
                throw new IOException("Input file has more lines than expected.");
            }

            // Return the trimmed and validated operands
            return new String[]{firstOperand, secondOperand, resultOperand};
        }
    }

    private static void writeSolutionToFile(String[] solution, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) { // Use the passed filePath here
            for (String line : solution) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    private static String convertToNumericString(String operand, Map<Character, Integer> assignment) {
        // Convert each character of the operand to its assigned numeric value
        StringBuilder numericString = new StringBuilder();
        for (char c : operand.toCharArray()) {
            numericString.append(assignment.get(c));
        }
        return numericString.toString();
    }

    // Extract unique alphabetic characters from the puzzle and return them as a sorted list
    public static List<Character> getUniqueLetters(String puzzle) {
        return puzzle.chars()
                .filter(Character::isAlphabetic)
                .mapToObj(c -> (char) c)
                .collect(Collectors.toSet())
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Checks if a given assignment of digits to letters solves the puzzle.
     * @param assignment The current letter-to-digit mapping.
     * @param firstOperand The first operand of the puzzle.
     * @param secondOperand The second operand of the puzzle.
     * @param resultOperand The result operand of the puzzle.
     * @return True if the assignment solves the puzzle, otherwise false.
     */
    private static boolean isSolution(Map<Character, Integer> assignment, String firstOperand, String secondOperand, String resultOperand) {
        // Convert the first and second operands and the result operand to their numeric values
        long firstNumber = toNumericValue(firstOperand, assignment);
        long secondNumber = toNumericValue(secondOperand, assignment);
        long resultNumber = toNumericValue(resultOperand, assignment);

        // Check if the sum of the first two operands equals the result operand
        return firstNumber + secondNumber == resultNumber;
    }


    public static String[] solve(String firstOperand, String secondOperand, String resultOperand) {
        // Combine the operands into a single string to process the puzzle
        String puzzle = firstOperand + secondOperand + resultOperand;

        // Extract unique characters (letters) from the puzzle
        List<Character> letters = getUniqueLetters(puzzle);

        // Initialize a map to keep track of the assignments of digits to characters
        Map<Character, Integer> assignment = new HashMap<>();

        // Ensure the result operand is not shorter than any of the input operands
        if (resultOperand.length() < firstOperand.length() || resultOperand.length() < secondOperand.length()) {
            System.out.println("Invalid input: Result cannot be shorter than any operand.");
            return new String[0];
        }

        // Check for invalid puzzles with more than 10 unique letters (since there are only 10 digits)
        if (letters.size() > 10) {
            System.out.println("INVALID EQUATION: More than one letter maps to the same digit");
            return new String[0]; // Return an empty array indicating an invalid puzzle
        }

        // Attempt to solve the puzzle using backtracking search
        if (backtrackSearch(letters, assignment, firstOperand, secondOperand, resultOperand)) {
            // If a solution is found, convert each operand to its numeric equivalent based on the assignment
            String solvedFirstOperand = convertToNumericString(firstOperand, assignment);
            String solvedSecondOperand = convertToNumericString(secondOperand, assignment);
            String solvedResultOperand = convertToNumericString(resultOperand, assignment);

            // Output the solution
            System.out.println(assignment);
            System.out.println(solvedFirstOperand + " + " + solvedSecondOperand + " = " + solvedResultOperand);

            // Return the solution as an array of strings
            return new String[]{solvedFirstOperand, solvedSecondOperand, solvedResultOperand};
        } else {
            // If no solution is found, indicate this outcome
            System.out.println("No solution exists.");
        }

        return null; // Return null to indicate no solution was found
    }

    /**
     * Checks if a proposed assignment of a digit to a letter is consistent with the puzzle's constraints.
     * @param var The character variable to assign a value.
     * @param value The digit value to assign to the variable.
     * @param assignment The current assignment map of characters to digits.
     * @param firstOperand The first operand of the puzzle.
     * @param secondOperand The second operand of the puzzle.
     * @param resultOperand The result operand of the puzzle.
     * @param letters The list of unique characters in the puzzle.
     * @return True if the assignment is consistent, otherwise false.
     */
    private static boolean isConsistent(Character var, Integer value, Map<Character, Integer> assignment, String firstOperand, String secondOperand, String resultOperand, List<Character> letters) {
        // Check if the value is already assigned to another variable
        if (assignment.containsValue(value)) {
            return false;
        }

        // Check for the constraint of no leading zeros in any word
        if (value == 0 && (var == firstOperand.charAt(0) || var == secondOperand.charAt(0) || var == resultOperand.charAt(0))) {
            return false;
        }

        // Temporarily assign the value to the variable
        assignment.put(var, value);
        try {
            // Check if the assignment violates the puzzle's solution constraints
            if (assignment.size() == letters.size() && !isSolution(assignment, firstOperand, secondOperand, resultOperand)) {
                return false;
            }
            return true; // If consistent, return true
        } finally {
            // Remove the temporary assignment
            assignment.remove(var);
        }
    }


    /**
     * Implements the backtracking search algorithm to find a solution to the puzzle.
     * @param letters The list of unique characters in the puzzle.
     * @param assignment The current assignment map of characters to digits.
     * @param firstOperand The first operand of the puzzle.
     * @param secondOperand The second operand of the puzzle.
     * @param resultOperand The result operand of the puzzle.
     * @return True if a solution is found, otherwise false.
     */
    private static boolean backtrackSearch(List<Character> letters, Map<Character, Integer> assignment, String firstOperand, String secondOperand, String resultOperand) {
        if (assignment.size() == letters.size()) {
            return isSolution(assignment, firstOperand, secondOperand, resultOperand);
        }
        //I can put a counter here and check for different puzzles inputs with others.
        Character var = selectUnassignedVariable(letters, assignment, firstOperand, secondOperand, resultOperand);
        if (var == null) {
            return false; // No variables left to assign but the solution isn't found yet
        }

        List<Integer> domain = getDomainValues(var, firstOperand, secondOperand, resultOperand, assignment, letters);
        for (Integer value : domain) {
            if (isConsistent(var, value, assignment, firstOperand, secondOperand, resultOperand, letters)) {
                assignment.put(var, value);
                if (backtrackSearch(letters, assignment, firstOperand, secondOperand, resultOperand)) {
                    return true; // Found a solution
                }
                // If not a solution, backtrack
                assignment.remove(var);
            }
        }

        return false; // No solution for this path
    }

    /**
     * Selects the next unassigned variable using the Minimum Remaining Values (MRV) heuristic.
     * @param letters The list of unique characters in the puzzle.
     * @param assignment The current letter-to-digit mapping.
     * @param firstOperand The first operand of the puzzle.
     * @param secondOperand The second operand of the puzzle.
     * @param resultOperand The result operand of the puzzle.
     * @return The next character to be assigned a value, or null if no such character exists.
     */
    private static Character selectUnassignedVariable(List<Character> letters, Map<Character, Integer> assignment, String firstOperand, String secondOperand, String resultOperand) {
        Character mrvVariable = null;
        int minDomainSize = Integer.MAX_VALUE; // Initialize with the maximum possible value

        for (Character letter : letters) {
            if (!assignment.containsKey(letter)) {
                // Determine domain values for the unassigned variable
                List<Integer> domainValues = getDomainValues(letter, firstOperand, secondOperand, resultOperand, assignment, letters);
                // Filter domain values based on consistency
                domainValues = domainValues.stream()
                        .filter(value -> isConsistent(letter, value, assignment, firstOperand, secondOperand, resultOperand, letters))
                        .collect(Collectors.toList());

                // Choose the variable with the smallest domain size (Minimum Remaining Values heuristic)
                if (domainValues.size() < minDomainSize) {
                    minDomainSize = domainValues.size();
                    mrvVariable = letter;
                }
            }
        }
        return mrvVariable; // Return the variable with the minimum remaining values
    }

    /**
     * Converts an operand string into its numeric value based on the current letter-to-digit mapping.
     * @param operand The operand to be converted.
     * @param assignment The current letter-to-digit mapping.
     * @return The numeric value of the operand, or -1 if the assignment is incomplete.
     */
    private static long toNumericValue(String operand, Map<Character, Integer> assignment) {
        long value = 0;
        for (char c : operand.toCharArray()) {
            Integer digit = assignment.get(c);
            if (digit == null) {
                return -1; // Incomplete assignment for this operand
            }
            value = value * 10 + digit;
        }
        return value; // Return the numeric value of the operand
    }

    /**
     * Determines the domain values for a given variable considering current assignments.
     * @param var The character variable for which domain values are to be determined.
     * @param firstOperand The first operand of the puzzle.
     * @param secondOperand The second operand of the puzzle.
     * @param resultOperand The result operand of the puzzle.
     * @param assignment The current letter-to-digit mapping.
     * @param letters The list of unique characters in the puzzle.
     * @return A list of potential domain values for the given variable.
     */
    private static List<Integer> getDomainValues(Character var, String firstOperand, String secondOperand, String resultOperand, Map<Character, Integer> assignment, List<Character> letters) {
        // Create a list of integers representing the possible domain values (0-9)
        List<Integer> domain = IntStream.rangeClosed(0, 9).boxed().collect(Collectors.toList());

        // Exclude 0 from the domain if 'var' is the first letter of any operand, as it cannot lead with 0
        if (var == firstOperand.charAt(0) || var == secondOperand.charAt(0) || var == resultOperand.charAt(0)) {
            domain.remove(Integer.valueOf(0));
        }

        // Sort the domain values based on the least constraining value heuristic
        domain.sort((val1, val2) -> {
            // Compare the number of legal values for other variables when either val1 or val2 is assigned to 'var'
            int count1 = countLegalValuesAfterAssignment(var, val1, assignment, firstOperand, secondOperand, resultOperand, letters);
            int count2 = countLegalValuesAfterAssignment(var, val2, assignment, firstOperand, secondOperand, resultOperand, letters);
            return Integer.compare(count2, count1);
        });

        return domain; // Return the sorted domain values
    }

    /**
     * Counts the number of legal values for other variables after a given assignment.
     * @param var The character variable being assigned a value.
     * @param value The value being assigned to the character variable.
     * @param assignment The current letter-to-digit mapping.
     * @param firstOperand The first operand of the puzzle.
     * @param secondOperand The second operand of the puzzle.
     * @param resultOperand The result operand of the puzzle.
     * @param letters The list of unique characters in the puzzle.
     * @return The count of legal values for other variables after the assignment.
     */
    private static int countLegalValuesAfterAssignment(Character var, Integer value, Map<Character, Integer> assignment, String firstOperand, String secondOperand, String resultOperand, List<Character> letters) {
        // Initialize a count of legal values for other variables after assigning 'value' to 'var'
        int count = 0;
        assignment.put(var, value); // Temporarily assign the value to 'var'

        // Iterate over each unassigned variable to count how many values do not cause a conflict
        for (Character otherVar : letters) {
            if (!assignment.containsKey(otherVar)) {
                for (int i = 0; i <= 9; i++) {
                    if (!assignment.containsValue(i)) {
                        // Temporarily assign a value to 'otherVar' and check for consistency
                        assignment.put(otherVar, i);
                        if (isConsistent(otherVar, i, assignment, firstOperand, secondOperand, resultOperand, letters)) {
                            count++; // Increment count if the assignment is consistent
                        }
                        // Remove the temporary assignment of 'otherVar'
                        assignment.remove(otherVar);
                    }
                }
            }
        }

        // Remove the temporary assignment of 'var'
        assignment.remove(var);
        return count; // Return the total count of legal values for other variables after assigning 'value' to 'var'
    }

}
