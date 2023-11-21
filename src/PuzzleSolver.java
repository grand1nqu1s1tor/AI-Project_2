import java.util.*;
import java.util.stream.Collectors;

public class PuzzleSolver {

    private static final int FIRST_OPERAND_LENGTH = 4;
    private static final int SECOND_OPERAND_LENGTH = 4;
    private static final int RESULT_OPERAND_LENGTH = 5;

    public static void main(String[] args) {
        // The fixed structure for the puzzle is defined here.
        //Invoke a function that reads the operands from the file
        String firstOperand = "SEND";
        String secondOperand = "MORE";
        String resultOperand = "MONEY";
        solve(firstOperand, secondOperand, resultOperand);
    }
    private static String convertToNumericString(String operand, Map<Character, Integer> assignment) {
        StringBuilder numericString = new StringBuilder();
        for (char c : operand.toCharArray()) {
            numericString.append(assignment.get(c));
        }
        return numericString.toString();
    }
    public static void solve(String firstOperand, String secondOperand, String resultOperand) {
        String puzzle = firstOperand + secondOperand + resultOperand;
        List<Character> letters = getUniqueLetters(puzzle);
        Map<Character, Integer> assignment = new HashMap<>();

        if (letters.size() > 10) {
            System.out.println("INVALID EQUATION: More than one letter maps to the same digit");
            return;
        }

        if (backtrackSearch(letters, assignment, firstOperand, secondOperand, resultOperand)) {
            System.out.println(assignment);
            String solvedFirstOperand = convertToNumericString(firstOperand, assignment);
            String solvedSecondOperand = convertToNumericString(secondOperand, assignment);
            String solvedResultOperand = convertToNumericString(resultOperand, assignment);
            System.out.println(solvedFirstOperand + " + " + solvedSecondOperand + " = " + solvedResultOperand);
        } else {
            System.out.println("No solution exists.");
        }
    }

    private static boolean isConsistent(Character var, Integer value, Map<Character, Integer> assignment, String firstOperand, String secondOperand, String resultOperand) {
        // Check if the value is already assigned to another variable
        if (assignment.containsValue(value)) {
            return false;
        }

        // Additional constraint: No leading zero in any of the words
        // If 'var' is the first letter of any operand and 'value' is 0, then it's inconsistent
        if (value == 0 && (var == firstOperand.charAt(0) || var == secondOperand.charAt(0) || var == resultOperand.charAt(0))) {
            return false;
        }

        // You can add more constraints specific to your cryptarithmetic puzzle here
        // ...

        return true; // The assignment is consistent
    }

    private static boolean backtrackSearch(List<Character> letters, Map<Character, Integer> assignment, String firstOperand, String secondOperand, String resultOperand) {
        if (assignment.size() == letters.size()) {
            return isSolution(assignment, firstOperand, secondOperand, resultOperand);
        }

        Character var = selectUnassignedVariable(letters, assignment, firstOperand, secondOperand, resultOperand);
        List<Integer> domain = getDomainValues(var, firstOperand, secondOperand, resultOperand);

        for (Integer value : domain) {
            if (isConsistent(var, value, assignment, firstOperand, secondOperand, resultOperand)) {
                assignment.put(var, value);
                if (backtrackSearch(letters, assignment, firstOperand, secondOperand, resultOperand)) {
                    return true; // Found a solution
                }
                assignment.remove(var); // Backtrack
            }
        }

        return false; // No solution for this path
    }

    private static Character selectUnassignedVariable(List<Character> letters, Map<Character, Integer> assignment, String firstOperand, String secondOperand, String resultOperand) {
        // Implement MRV and Degree heuristics here.
        // For simplicity, let's just choose the next unassigned variable for now.
        for (Character letter : letters) {
            if (!assignment.containsKey(letter)) {
                return letter;
            }
        }
        return null; // Should not reach here
    }


    private static boolean isSolution(Map<Character, Integer> assignment, String firstOperand, String secondOperand, String resultOperand) {
        // Convert the first and second operands and the result operand to their numeric values
        long firstNumber = toNumericValue(firstOperand, assignment);
        long secondNumber = toNumericValue(secondOperand, assignment);
        long resultNumber = toNumericValue(resultOperand, assignment);

        // Check if the sum of the first two operands equals the result operand
        return firstNumber + secondNumber == resultNumber;
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


    public static List<Character> getUniqueLetters(String puzzle) {
        return puzzle.chars()
                .filter(Character::isAlphabetic)
                .mapToObj(c -> (char) c)
                .collect(Collectors.toSet())
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public static List<String> getValidPermutations(List<Character> letters, List<Character> startingLetters) {
        List<String> validPermutations = new ArrayList<>();
        generatePermutations("", new HashSet<>(), validPermutations, letters.size(), startingLetters, letters);
        return validPermutations;
    }

    public static void generatePermutations(String current, Set<Character> used, List<String> result, int size,
                                            List<Character> startingLetters, List<Character> letters) {
        if (current.length() == size) {
            if (isValidPermutation(current, startingLetters, letters)) {
                result.add(current);
            }
            return;
        }

        for (char c = '0'; c <= '9'; c++) {
            if (!used.contains(c)) {
                used.add(c);
                generatePermutations(current + c, used, result, size, startingLetters, letters);
                used.remove(c);
            }
        }
    }

    public static boolean isValidPermutation(String perm, List<Character> startingLetters, List<Character> letters) {
        for (Character startingLetter : startingLetters) {
            int index = letters.indexOf(startingLetter);
            if (perm.charAt(index) == '0') {
                return false;
            }
        }
        return true;
    }
    private static List<Integer> getDomainValues(Character var, String firstOperand, String secondOperand, String resultOperand) {
        // 'i' (the first letter of resultOperand) has a domain of {1}
        if (var == resultOperand.charAt(0)) {
            return Collections.singletonList(1);
        }
        // 'a' and 'e' (the first letters of firstOperand and secondOperand) have a domain of {1, 2, ..., 9}
        else if (var == firstOperand.charAt(0) || var == secondOperand.charAt(0)) {
            return Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        }
        // All other letters have a domain of {0, 1, 2, ..., 9}
        else {
            return Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        }
    }
}
