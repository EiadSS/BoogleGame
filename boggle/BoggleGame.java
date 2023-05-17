package boggle;

import java.util.*;

/**
 * The BoggleGame class for the first Assignment in CSC207, Fall 2022
 */
public class BoggleGame {

    /**
     * scanner used to interact with the user via console
     */
    public Scanner scanner;
    /**
     * stores game statistics
     */
    private BoggleStats gameStats;

    /**
     * dice used to randomize letter assignments for a small grid
     */
    private final String[] dice_small_grid = //dice specifications, for small and large grids
            {"AAEEGN", "ABBJOO", "ACHOPS", "AFFKPS", "AOOTTW", "CIMOTU", "DEILRX", "DELRVY",
                    "DISTTY", "EEGHNW", "EEINSU", "EHRTVW", "EIOSST", "ELRTTY", "HIMNQU", "HLNNRZ"};
    /**
     * dice used to randomize letter assignments for a big grid
     */
    private final String[] dice_big_grid =
            {"AAAFRS", "AAEEEE", "AAFIRS", "ADENNN", "AEEEEM", "AEEGMU", "AEGMNN", "AFIRSY",
                    "BJKQXZ", "CCNSTW", "CEIILT", "CEILPT", "CEIPST", "DDLNOR", "DDHNOT", "DHHLOR",
                    "DHLNOR", "EIIITT", "EMOTTT", "ENSSSU", "FIPRSY", "GORRVW", "HIPRRY", "NOOTUW", "OOOTTU"};

    /*
     * BoggleGame constructor
     */
    public BoggleGame() {
        this.scanner = new Scanner(System.in);
        this.gameStats = new BoggleStats();
    }

    /*
     * Provide instructions to the user, so they know how to play the game.
     */
    public void giveInstructions() {
        System.out.println("The Boggle board contains a grid of letters that are randomly placed.");
        System.out.println("We're both going to try to find words in this grid by joining the letters.");
        System.out.println("You can form a word by connecting adjoining letters on the grid.");
        System.out.println("Two letters adjoin if they are next to each other horizontally, ");
        System.out.println("vertically, or diagonally. The words you find must be at least 4 letters long, ");
        System.out.println("and you can't use a letter twice in any single word. Your points ");
        System.out.println("will be based on word length: a 4-letter word is worth 1 point, 5-letter");
        System.out.println("words earn 2 points, and so on. After you find as many words as you can,");
        System.out.println("I will find all the remaining words.");
        System.out.println("\nHit return when you're ready...");
    }


    /*
     * Gets information from the user to initialize a new Boggle game.
     * It will loop until the user indicates they are done playing.
     */
    public void playGame() {
        int boardSize;
        while (true) {
            System.out.println("Enter 1 to play on a big (5x5) grid; 2 to play on a small (4x4) one:");
            String choiceGrid = scanner.nextLine();

            //get grid size preference
            if (choiceGrid == "") break; //end game if user inputs nothing
            while (!choiceGrid.equals("1") && !choiceGrid.equals("2")) {
                System.out.println("Please try again.");
                System.out.println("Enter 1 to play on a big (5x5) grid; 2 to play on a small (4x4) one:");
                choiceGrid = scanner.nextLine();
            }

            if (choiceGrid.equals("1")) boardSize = 5;
            else boardSize = 4;

            //get letter choice preference
            System.out.println("Enter 1 to randomly assign letters to the grid; 2 to provide your own.");
            String choiceLetters = scanner.nextLine();

            if (choiceLetters == "") break; //end game if user inputs nothing
            while (!choiceLetters.equals("1") && !choiceLetters.equals("2")) {
                System.out.println("Please try again.");
                System.out.println("Enter 1 to randomly assign letters to the grid; 2 to provide your own.");
                choiceLetters = scanner.nextLine();
            }

            if (choiceLetters.equals("1")) {
                playRound(boardSize, randomizeLetters(boardSize));
            } else {
                System.out.println("Input a list of " + boardSize * boardSize + " letters:");
                choiceLetters = scanner.nextLine();
                while (!(choiceLetters.length() == boardSize * boardSize)) {
                    System.out.println("Sorry, bad input. Please try again.");
                    System.out.println("Input a list of " + boardSize * boardSize + " letters:");
                    choiceLetters = scanner.nextLine();
                }
                playRound(boardSize, choiceLetters.toUpperCase());
            }

            //round is over! So, store the statistics, and end the round.
            this.gameStats.summarizeRound();
            this.gameStats.endRound();

            //Shall we repeat?
            System.out.println("Play again? Type 'Y' or 'N'");
            String choiceRepeat = scanner.nextLine().toUpperCase();

            if (choiceRepeat == "") break; //end game if user inputs nothing
            while (!choiceRepeat.equals("Y") && !choiceRepeat.equals("N")) {
                System.out.println("Please try again.");
                System.out.println("Play again? Type 'Y' or 'N'");
                choiceRepeat = scanner.nextLine().toUpperCase();
            }

            if (choiceRepeat == "" || choiceRepeat.equals("N")) break; //end game if user inputs nothing

        }

        //we are done with the game! So, summarize all the play that has transpired and exit.
        this.gameStats.summarizeGame();
        System.out.println("Thanks for playing!");
    }

    /*
     * Play a round of Boggle.
     * This initializes the main objects: the board, the dictionary, the map of all
     * words on the board, and the set of words found by the user. These objects are
     * passed by reference from here to many other functions.
     */
    public void playRound(int size, String letters) {
        //step 1. initialize the grid
        BoggleGrid grid = new BoggleGrid(size);
        grid.initalizeBoard(letters);
        //step 2. initialize the dictionary of legal words
        Dictionary boggleDict = new Dictionary("wordlist.txt"); //you may have to change the path to the wordlist, depending on where you place it.
        //step 3. find all legal words on the board, given the dictionary and grid arrangement.
        Map<String, ArrayList<Position>> allWords = new HashMap<String, ArrayList<Position>>();
        findAllWords(allWords, boggleDict, grid);
        //step 4. allow the user to try to find some words on the grid
        humanMove(grid, allWords);
        //step 5. allow the computer to identify remaining words
        computerMove(allWords);
    }

    /*
     * This method should return a String of letters (length 16 or 25 depending on the size of the grid).
     * There will be one letter per grid position, and they will be organized left to right,
     * top to bottom. A strategy to make this string of letters is as follows:
     * -- Assign a one of the dice to each grid position (i.e. dice_big_grid or dice_small_grid)
     * -- "Shuffle" the positions of the dice to randomize the grid positions they are assigned to
     * -- Randomly select one of the letters on the given die at each grid position to determine
     *    the letter at the given position
     *
     * @return String a String of random letters (length 16 or 25 depending on the size of the grid)
     */
    private String randomizeLetters(int size) {
        String word = "";
        int max = 5;
        int min = 0;
        if (size == 16) {
            List<String> theWord = Arrays.asList(dice_small_grid);
            Collections.shuffle(theWord);
            for (String s : theWord) {
                int b = (int) (Math.random() * (max - min + 1) + min);
                word += s.charAt(b);
            }
        } else {
            List<String> theWord = Arrays.asList(dice_big_grid);
            Collections.shuffle(theWord);
            for (String s : theWord) {
                int b = (int) (Math.random() * (max - min + 1) + min);
                word += s.charAt(b);
            }

        }
        return word;
    }

    /*
     * This should be a recursive function that finds all valid words on the boggle board.
     * Every word should be valid (i.e. in the boggleDict) and of length 4 or more.
     * Words that are found should be entered into the allWords HashMap.  This HashMap
     * will be consulted as we play the game.
     *
     * Note that this function will be a recursive function.  You may want to write
     * a wrapper for your recursion. Note that every legal word on the Boggle grid will correspond to
     * a list of grid positions on the board, and that the Position class can be used to represent these
     * positions. The strategy you will likely want to use when you write your recursion is as follows:
     * -- At every Position on the grid:
     * ---- add the Position of that point to a list of stored positions
     * ---- if your list of stored positions is >= 4, add the corresponding word to the allWords Map
     * ---- recursively search for valid, adjacent grid Positions to add to your list of stored positions.
     * ---- Note that a valid Position to add to your list will be one that is either horizontal, diagonal, or
     *      vertically touching the current Position
     * ---- Note also that a valid Position to add to your list will be one that, in conjunction with those
     *      Positions that precede it, form a legal PREFIX to a word in the Dictionary (this is important!)
     * ---- Use the "isPrefix" method in the Dictionary class to help you out here!!
     * ---- Positions that already exist in your list of stored positions will also be invalid.
     * ---- You'll be finished when you have checked EVERY possible list of Positions on the board, to see
     *      if they can be used to form a valid word in the dictionary.
     * ---- Food for thought: If there are N Positions on the grid, how many possible lists of positions
     *      might we need to evaluate?
     *
     * @param allWords A mutable list of all legal words that can be found, given the boggleGrid grid letters
     * @param boggleDict A dictionary of legal words
     * @param boggleGrid A boggle grid, with a letter at each position on the grid
     */
    private void findAllWords(Map<String, ArrayList<Position>> allWords,
                              Dictionary boggleDict, BoggleGrid boggleGrid) {
        for (int i = 0; i < boggleGrid.numRows(); i++) {
            for (int j = 0; j < boggleGrid.numCols(); j++) {
                 findword(allWords, new Position(i, j), boggleGrid, boggleDict,
                         ""+boggleGrid.getCharAt(i, j),new ArrayList<Position>());

            }
        }
    }

    /*
    * finds all possible words with character starting at positions pos
    *
     */
    private void findword(Map<String, ArrayList<Position>> allwords, Position pos, BoggleGrid boogleGrid,
                                                          Dictionary boogleDict, String word,
                                                          ArrayList<Position> positions) {
        positions.add(pos);
        if(pos.getCol()+1 <boogleGrid.numCols()&&posEqual(positions, new Position(pos.getRow(), pos.getCol()+1))){
            if(boogleDict.isPrefix(word+boogleGrid.getCharAt(pos.getRow(), pos.getCol()+1))){
                word += boogleGrid.getCharAt(pos.getRow(), pos.getCol()+1);
                findword(allwords, new Position(pos.getRow(), pos.getCol()+1),
                        boogleGrid, boogleDict, word + boogleGrid.getCharAt(pos.getRow(),
                                pos.getCol()+1), copyPos(positions));
            }
        } if(pos.getRow()+1 <boogleGrid.numRows()&&posEqual(positions, new Position(pos.getRow()+1, pos.getCol()))){
            if (boogleDict.isPrefix(word + boogleGrid.getCharAt(pos.getRow() + 1, pos.getCol()))) {
                findword(allwords, new Position(pos.getRow() + 1, pos.getCol()),
                        boogleGrid, boogleDict, word + boogleGrid.getCharAt(pos.getRow() + 1,
                                pos.getCol()), copyPos(positions));

            }
        }
        if (pos.getRow() - 1 >= 0&&posEqual(positions, new Position(pos.getRow()-1, pos.getCol()))){
            if (boogleDict.isPrefix(word + boogleGrid.getCharAt(pos.getRow() - 1, pos.getCol()))) {
                findword(allwords, new Position(pos.getRow() - 1, pos.getCol()),
                        boogleGrid, boogleDict, word + boogleGrid.getCharAt(
                                pos.getRow() - 1, pos.getCol()), copyPos(positions));
            }
        }

        if(pos.getCol() -1 >= 0&&posEqual(positions, new Position(pos.getRow(), pos.getCol()-1))){
            if(boogleDict.isPrefix(word+boogleGrid.getCharAt(pos.getRow(), pos.getCol()-1))) {
                findword(allwords, new Position(pos.getRow(), pos.getCol()-1),
                        boogleGrid, boogleDict,
                        word+boogleGrid.getCharAt(pos.getRow(), pos.getCol() - 1), copyPos(positions));
            }

        } if(pos.getRow()-1 >= 0&&pos.getCol() -1 >= 0&&posEqual(positions, new Position(pos.getRow()-1, pos.getCol()-1))){
            if(boogleDict.isPrefix(word+boogleGrid.getCharAt(pos.getRow()-1, pos.getCol()-1))) {
                findword(allwords, new Position(pos.getRow()-1, pos.getCol()-1),
                        boogleGrid, boogleDict, word+boogleGrid.getCharAt(pos.getRow()-1,
                                pos.getCol() - 1), copyPos(positions));
            }

        } if (pos.getRow()-1 >= 0&&pos.getCol()+1 <boogleGrid.numCols()&&posEqual(positions, new Position(
                pos.getRow()-1, pos.getCol()+1))){
            if(boogleDict.isPrefix(word+boogleGrid.getCharAt(pos.getRow()-1, pos.getCol()+1))) {
                findword(allwords, new Position(pos.getRow()-1, pos.getCol()+1),
                        boogleGrid, boogleDict, word+boogleGrid.getCharAt(pos.getRow()-1,
                                pos.getCol() + 1), copyPos(positions));
            }

        } if(pos.getRow() + 1 < boogleGrid.numRows() && pos.getCol() -1>=0&&posEqual(positions, new Position(
                pos.getRow()+1, pos.getCol()-1))){
            if(boogleDict.isPrefix(word+boogleGrid.getCharAt(pos.getRow()+1, pos.getCol()-1))) {
                findword(allwords, new Position(pos.getRow()+1, pos.getCol()-1),
                        boogleGrid, boogleDict, word+boogleGrid.getCharAt(
                                pos.getRow()+1, pos.getCol() - 1), copyPos(positions));
            }

        }if (pos.getRow() + 1 < boogleGrid.numRows() && pos.getCol() +1 < boogleGrid.numCols()&&posEqual(positions,
                new Position(pos.getRow()+1, pos.getCol()+1))){
            if(boogleDict.isPrefix(word+boogleGrid.getCharAt(pos.getRow()+1, pos.getCol()+1))) {
                findword(allwords, new Position(pos.getRow()+1, pos.getCol()+1),
                        boogleGrid, boogleDict, word + boogleGrid.getCharAt(pos.getRow()+1,
                                pos.getCol() + 1), copyPos(positions));
            }
        }
        if (word.length() > 3&&boogleDict.containsWord(word)){
            allwords.put(word, positions);
        }
    }
    public boolean posEqual(ArrayList<Position> positions, Position position){
        for(Position p: positions){
            if (p.getRow() == position.getRow()&&p.getCol() == position.getCol()){
                return false;
            }
        }
        return true;
    }
    private ArrayList<Position> copyPos(ArrayList<Position> positions){
        return new ArrayList<Position>(positions);
    }


    /* 
     * Gets words from the user.  As words are input, check to see that they are valid.
     * If yes, add the word to the player's word list (in boggleStats) and increment
     * the player's score (in boggleStats).
     * End the turn once the user hits return (with no word).
     *
     * @param board The boggle board
     * @param allWords A mutable list of all legal words that can be found, given the boggleGrid grid letters
     */
    private void humanMove(BoggleGrid board, Map<String,ArrayList<Position>> allWords){
        System.out.println("It's your turn to find some words!");
        while(true) {
            //You write code here!
            //step 1. Print the board for the user, so they can scan it for words
            System.out.println(board.toString());
            //step 2. Get a input (a word) from the user via the console
            //step 3. Check to see if it is valid (note validity checks should be case-insensitive)
            //step 4. If it's valid, update the player's word list and score (stored in boggleStats)
            //step 5. Repeat step 1 - 4
            //step 6. End when the player hits return (with no word choice).
            String word = scanner.nextLine();
            if(allWords.containsKey(word)){
                this.gameStats.addWord(word, BoggleStats.Player.Human);
            }
            else if(word.equals("")){
                break;
            }

        }
    }


    /* 
     * Gets words from the computer.  The computer should find words that are
     * both valid and not in the player's word list.  For each word that the computer
     * finds, update the computer's word list and increment the
     * computer's score (stored in boggleStats).
     *
     * @param allWords A mutable list of all legal words that can be found, given the boggleGrid grid letters
     */
    private void computerMove(Map<String,ArrayList<Position>> all_words){
        for(String s: all_words.keySet()){
            if(!gameStats.getPlayerWords().contains(s)){
                gameStats.addWord(s, BoggleStats.Player.Computer);
            }
        }
    }

}
