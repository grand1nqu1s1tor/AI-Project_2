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

    public static void solve(String firstOperand, String secondOperand, String resultOperand) {
        String puzzle = firstOperand + secondOperand + resultOperand;
        List<Character> letters = getUniqueLetters(puzzle);

        if (letters.size() > 10) {
            System.out.println("INVALID EQUATION: More than one letter maps to the same digit");
            return;
        }

        List<Character> startingLetters = Arrays.asList(firstOperand.charAt(0), secondOperand.charAt(0), resultOperand.charAt(0));
        List<String> validPermutations = getValidPermutations(letters, startingLetters);

        for (String perm : validPermutations) {
            if (isSolution(perm, firstOperand, secondOperand, resultOperand, letters)) {
                Map<Character, Character> answer = new HashMap<>();
                for (int i = 0; i < letters.size(); i++) {
                    answer.put(letters.get(i), perm.charAt(i));
                }
                System.out.println(answer);
                break;
            }
        }
    }

    public static boolean isSolution(String perm, String firstOperand, String secondOperand, String resultOperand, List<Character> letters) {
        Map<Character, Character> charMap = new HashMap<>();
        for (int i = 0; i < letters.size(); i++) {
            charMap.put(letters.get(i), perm.charAt(i));
        }

        long firstNumber = toNumericValue(firstOperand, charMap);
        long secondNumber = toNumericValue(secondOperand, charMap);
        long resultNumber = toNumericValue(resultOperand, charMap);

        return firstNumber + secondNumber == resultNumber;
    }

    public static long toNumericValue(String operand, Map<Character, Character> charMap) {
        StringBuilder builder = new StringBuilder();
        for (char c : operand.toCharArray()) {
            builder.append(charMap.get(c));
        }
        return Long.parseLong(builder.toString());
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
}
