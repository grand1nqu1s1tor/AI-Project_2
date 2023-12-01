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

    //TODO Add comments

    public static void main(String[] args) {
        try {
            String[] operands = readPuzzleFromFile("src/cryptarithmetic_input.txt");
            String[] solution = solve(operands[0], operands[1], operands[2]);
            if (solution != null) {
                writeSolutionToFile(solution, "cryptarithmetic_output.txt");
                System.out.println("Solution written to " + "cryptarithmetic_output.txt");
            } else {
                writeSolutionToFile(new String[]{"No solution"}, "cryptarithmetic_output.txt");
                System.out.println("No solution exists.");
            }
        } catch (IOException e) {
            System.out.println("Error reading or writing file: " + e.getMessage());
        }
    }

    private static String[] readPuzzleFromFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String firstOperand = reader.readLine();
            String secondOperand = reader.readLine();
            String resultOperand = reader.readLine();
            return new String[]{firstOperand, secondOperand, resultOperand};
        }
    }

    private static void writeSolutionToFile(String[] solution, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("cryptarithmetic_output.txt"))) {
            for (String line : solution) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    private static String convertToNumericString(String operand, Map<Character, Integer> assignment) {
        StringBuilder numericString = new StringBuilder();
        for (char c : operand.toCharArray()) {
            numericString.append(assignment.get(c));
        }
        return numericString.toString();
    }

    public static List<Character> getUniqueLetters(String puzzle) {
        return puzzle.chars()
                .filter(Character::isAlphabetic)
                .mapToObj(c -> (char) c)
                .collect(Collectors.toSet())
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    private static boolean isSolution(Map<Character, Integer> assignment, String firstOperand, String secondOperand, String resultOperand) {
        // Convert the first and second operands and the result operand to their numeric values
        long firstNumber = toNumericValue(firstOperand, assignment);
        long secondNumber = toNumericValue(secondOperand, assignment);
        long resultNumber = toNumericValue(resultOperand, assignment);

        // Check if the sum of the first two operands equals the result operand
        return firstNumber + secondNumber == resultNumber;
    }

    public static String[] solve(String firstOperand, String secondOperand, String resultOperand) {
        String puzzle = firstOperand + secondOperand + resultOperand;
        List<Character> letters = getUniqueLetters(puzzle);
        Map<Character, Integer> assignment = new HashMap<>();

        if (letters.size() > 10) {
            System.out.println("INVALID EQUATION: More than one letter maps to the same digit");
            return new String[0];
        }

        if (firstOperand == null || secondOperand == null || resultOperand == null ||
                firstOperand.isEmpty() || secondOperand.isEmpty() || resultOperand.isEmpty()) {
            System.out.println("Invalid input: operands cannot be null or empty.");
            return new String[0];
        }

        if (backtrackSearch(letters, assignment, firstOperand, secondOperand, resultOperand)) {
            System.out.println(assignment);
            String solvedFirstOperand = convertToNumericString(firstOperand, assignment);
            String solvedSecondOperand = convertToNumericString(secondOperand, assignment);
            String solvedResultOperand = convertToNumericString(resultOperand, assignment);
            System.out.println(solvedFirstOperand + " + " + solvedSecondOperand + " = " + solvedResultOperand);
            return new String[]{solvedFirstOperand, solvedSecondOperand, solvedResultOperand};
        } else {
            System.out.println("No solution exists.");
        }
        return null;
    }

    private static boolean isConsistent(Character var, Integer value, Map<Character, Integer> assignment, String firstOperand, String secondOperand, String resultOperand, List<Character> letters) {
        // Check if the value is already assigned to another variable
        if (assignment.containsValue(value)) {
            return false;
        }

        // Additional constraint: No leading zero in any of the words
        if (value == 0 && (var == firstOperand.charAt(0) || var == secondOperand.charAt(0) || var == resultOperand.charAt(0))) {
            return false;
        }

        // Place the value tentatively
        assignment.put(var, value);
        try {
            // If it's not the first variable and the size is equal to the number of unique letters, check if the solution works
            if (assignment.size() == letters.size() && !isSolution(assignment, firstOperand, secondOperand, resultOperand)) {
                return false;
            }
            return true; // If it is the first variable or if the partial assignment doesn't violate the constraints, it's consistent.
        } finally {
            assignment.remove(var); // Clean up the temporary assignment
        }
    }


    private static boolean backtrackSearch(List<Character> letters, Map<Character, Integer> assignment, String firstOperand, String secondOperand, String resultOperand) {
        if (assignment.size() == letters.size()) {
            return isSolution(assignment, firstOperand, secondOperand, resultOperand);
        }

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

    private static Character selectUnassignedVariable(List<Character> letters, Map<Character, Integer> assignment, String firstOperand, String secondOperand, String resultOperand) {
        Character mrvVariable = null;
        int minDomainSize = Integer.MAX_VALUE; // Initialize with the maximum possible value

        for (Character letter : letters) {
            if (!assignment.containsKey(letter)) {
                List<Integer> domainValues = getDomainValues(letter, firstOperand, secondOperand, resultOperand, assignment, letters);
                // Use 'letter' in the filter
                domainValues = domainValues.stream()
                        .filter(value -> isConsistent(letter, value, assignment, firstOperand, secondOperand, resultOperand, letters))
                        .collect(Collectors.toList());

                if (domainValues.size() < minDomainSize) {
                    minDomainSize = domainValues.size();
                    mrvVariable = letter;
                }
            }
        }
        return mrvVariable;
    }

    private static long toNumericValue(String operand, Map<Character, Integer> assignment) {
        long value = 0;
        for (char c : operand.toCharArray()) {
            Integer digit = assignment.get(c);
            if (digit == null) {
                return -1; // This means the assignment is not yet complete for this operand
            }
            value = value * 10 + digit;
        }
        return value;
    }

    private static List<Integer> getDomainValues(Character var, String firstOperand, String secondOperand, String resultOperand, Map<Character, Integer> assignment, List<Character> letters) {
        List<Integer> domain = IntStream.rangeClosed(0, 9).boxed().collect(Collectors.toList()); // Assuming domain is 0-9 for all except the first letter of any operand

        // If 'var' is the first letter of any operand, it cannot be 0
        if (var == firstOperand.charAt(0) || var == secondOperand.charAt(0) || var == resultOperand.charAt(0)) {
            domain.remove(Integer.valueOf(0));
        }

        // Sort the domain values by the least constraining value heuristic
        domain.sort((val1, val2) -> {
            int count1 = countLegalValuesAfterAssignment(var, val1, assignment, firstOperand, secondOperand, resultOperand, letters);
            int count2 = countLegalValuesAfterAssignment(var, val2, assignment, firstOperand, secondOperand, resultOperand, letters);
            return Integer.compare(count2, count1);
        });

        return domain;
    }

    private static int countLegalValuesAfterAssignment(Character var, Integer value, Map<Character, Integer> assignment, String firstOperand, String secondOperand, String resultOperand, List<Character> letters) {
        int count = 0;
        assignment.put(var, value); // Temporarily assign the value to 'var'

        // For each unassigned variable, count how many values do not cause a conflict
        for (Character otherVar : letters) {
            if (!assignment.containsKey(otherVar)) {
                for (int i = 0; i <= 9; i++) {
                    if (!assignment.containsValue(i)) {
                        assignment.put(otherVar, i);
                        if (isConsistent(otherVar, i, assignment, firstOperand, secondOperand, resultOperand, letters)) {
                            count++;
                        }
                        assignment.remove(otherVar);
                    }
                }
            }
        }
        assignment.remove(var); // Clean up the temporary assignment
        return count; // Return the number of legal values for the other variables after assigning 'value' to 'var'
    }

}
