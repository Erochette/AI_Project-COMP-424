package student_player;

import java.util.ArrayList;              
import java.util.Random;           

import bohnenspiel.BohnenspielBoardState;
import bohnenspiel.BohnenspielMove;
import bohnenspiel.BohnenspielPlayer;
import bohnenspiel.BohnenspielMove.MoveType; 
import student_player.mytools.MyTools;  

/** A Hus player submitted by a student. */
public class StudentPlayer extends BohnenspielPlayer {       

    //To check if enough turns left to go
    public static boolean isThereEnoughTurn(BohnenspielBoardState stateToCheck, int numberOfTurns){

        //So far, there is
        boolean enough = true; 

        //Check if this is a valid turn
        ArrayList<BohnenspielMove> movesToCheck = stateToCheck.getLegalMoves();
        if (movesToCheck.size() > 0){
            if (numberOfTurns > 0){
                //Go to the next turns
                for (int c=0; c<movesToCheck.size(); c++){
                    BohnenspielBoardState stateCopy = (BohnenspielBoardState) stateToCheck.clone();
                    ArrayList<BohnenspielMove> thoseMoves = stateCopy.getLegalMoves();
                    BohnenspielMove thisMove = thoseMoves.get(c);
                    stateCopy.move(thisMove);
                    enough = isThereEnoughTurn(stateCopy, --numberOfTurns);
                    if (!enough) break;
                }
            }

        }else
            enough = false;

        return enough;
    }

    //Return average possible score after next opponent turn
    public static double[] compareMoves(BohnenspielBoardState stateToCheck){

        double[] overallScore = {0,0};  //[0] = myScore, [1] = oppScore

        //Go through all possible moves
        ArrayList<BohnenspielMove> movesToCheck = stateToCheck.getLegalMoves();
        int sizeOfMoves = movesToCheck.size();
        for (int i=0; i<sizeOfMoves; i++){

            //Make opponent's move
            BohnenspielBoardState stateCopy = (BohnenspielBoardState) stateToCheck.clone();
            ArrayList<BohnenspielMove> oppMoves = stateCopy.getLegalMoves();
            BohnenspielMove thisMove = oppMoves.get(i);
            stateCopy.move(thisMove);

            //Add this move's score to the others
            overallScore[0] += stateCopy.getScore(stateCopy.getTurnPlayer());
            overallScore[1] += stateCopy.getScore((stateCopy.getTurnPlayer()+1)%2);
        }

        //Compute average score made this turn for each player
        overallScore[0] = overallScore[0]/sizeOfMoves;
        overallScore[1] = overallScore[1]/sizeOfMoves;

        return overallScore;
    }


   public static double[] moveChain(BohnenspielBoardState stateToCheck, int howFarAhead){

        double[] overallAverage = {0,0};  //[0] = myScore, [1] = oppScore

        if (howFarAhead > 0){      

            //Check for all opponent's move
            ArrayList<BohnenspielMove> movesToCheck = stateToCheck.getLegalMoves();
            int overallMoveSize = movesToCheck.size();
            for (int a=0; a<overallMoveSize; a++){

                //Execute opponent's move
                BohnenspielBoardState stateCopyOp = (BohnenspielBoardState) stateToCheck.clone();
                ArrayList<BohnenspielMove> opMoves = stateCopyOp.getLegalMoves();
                BohnenspielMove thisMove = opMoves.get(a);
                stateCopyOp.move(thisMove);

                //Check for all my moves
                ArrayList<BohnenspielMove> myMoves = stateCopyOp.getLegalMoves();
                int thisMovesSize = myMoves.size();
                double[] thisMoveAverage = {0,0}; 
                for (int h=0; h<thisMovesSize; h++){

                    //Executing my move
                    BohnenspielBoardState myStateCopy = (BohnenspielBoardState) stateCopyOp.clone();
                    myMoves = myStateCopy.getLegalMoves();
                    BohnenspielMove myMoveToCheck = myMoves.get(h);
                    myStateCopy.move(myMoveToCheck);

                    //Adding next branch's nodes average to add it to this node
                    thisMoveAverage = moveChain(myStateCopy, --howFarAhead);
                    overallAverage[0] += thisMoveAverage[0];
                    overallAverage[1] += thisMoveAverage[1];

                }

                //Taking the average
                overallAverage[0] = overallAverage[0]/thisMovesSize;
                overallAverage[1] = overallAverage[1]/thisMovesSize;

            }

            return overallAverage;

         }else{

            //Return the average of the last node in branch
            return compareMoves(stateToCheck);
        }
   }

    //Return the best next move availabe if I want to play this move
    //It assumes the opponent will have greedy behavior
    public static int nextBestMove(BohnenspielBoardState stateToCheck){

        int oppBestScore = -1;  int myBestScore = -1;
        int oppBestMove = 0;    int myBestMove = 0;
        BohnenspielBoardState forMyMove = (BohnenspielBoardState) stateToCheck.clone();
        int myCurrentScore = stateToCheck.getScore((stateToCheck.getTurnPlayer()+1)%2);

        //Go through all possible opponent's moves
        ArrayList<BohnenspielMove> movesToCheck = stateToCheck.getLegalMoves();
        for (int i=0; i<movesToCheck.size(); i++){

            //Make opponent's move
            BohnenspielBoardState stateCopy = (BohnenspielBoardState) stateToCheck.clone();
            BohnenspielMove thisMove = movesToCheck.get(i);
            stateCopy.move(thisMove);

            //Compare which move would be better for the opponent
            if (oppBestScore < stateCopy.getScore((stateCopy.getTurnPlayer()+1)%2)){
                oppBestScore = stateCopy.getScore((stateCopy.getTurnPlayer()+1)%2);
                oppBestMove = i;
            }
        }

        //Play opponent's best move
        movesToCheck = forMyMove.getLegalMoves();
        BohnenspielMove oppMove = movesToCheck.get(oppBestMove);
        forMyMove.move(oppMove);

        //Go through all of my possible moves
        movesToCheck = forMyMove.getLegalMoves();
        for (int a=0; a<movesToCheck.size(); a++){

            //Make my move
            BohnenspielBoardState stateCopy = (BohnenspielBoardState) forMyMove.clone();
            BohnenspielMove thisMove = movesToCheck.get(a);
            stateCopy.move(thisMove);

            //Compare which move would be better
            if (myBestScore < stateCopy.getScore((stateCopy.getTurnPlayer()+1)%2)){
                myBestScore = stateCopy.getScore((stateCopy.getTurnPlayer()+1)%2);
            }
        }

        return (myBestScore-myCurrentScore);

    }

   //Decide which move to play next, by looking ahead for consequences.
   public static int lookAhead(BohnenspielBoardState stateToCheck, int howFarAhead){

        int thisMoveScore;  
        double averageFuturScore[]; 
        int bestMove = 0;
        double bestScore = 0;   
        boolean enoughTurns = true; 

        //Check if there is a next turn after this one
        enoughTurns = isThereEnoughTurn(stateToCheck, 2);

        //Check if there is enough turn to use moveChain
        boolean enoughChain = true;
        enoughChain = isThereEnoughTurn(stateToCheck, 4);

        //Record my current score, and my opponent's current score
        int myCurrentScore = stateToCheck.getScore(stateToCheck.getTurnPlayer());
        int oppCurrentScore = stateToCheck.getScore((stateToCheck.getTurnPlayer()+1)%2);

        //Check for all possible moves this turn
        ArrayList<BohnenspielMove> possibleMoves = stateToCheck.getLegalMoves();
        for (int i=0; i<possibleMoves.size(); i++){ 

            //Do one of my moves, and compare if it does better 
            BohnenspielBoardState stateCopy = (BohnenspielBoardState) stateToCheck.clone();
            ArrayList<BohnenspielMove> movesToCheck = stateCopy.getLegalMoves();
            BohnenspielMove thisMove = movesToCheck.get(i);
            stateCopy.move(thisMove);
 
            //Take this move's improvement in score
            thisMoveScore = stateCopy.getScore((stateCopy.getTurnPlayer()+1)%2) - myCurrentScore;

            //Take next best possible move's score after this one
            int bestNextMove = 0;
            if (enoughTurns) bestNextMove = nextBestMove(stateCopy);

            //Take difference in average ...
            double averageDifference = 0;        
            if (enoughChain){ 
                averageFuturScore = moveChain(stateCopy, 1);
                averageDifference = (averageFuturScore[1] - oppCurrentScore) / (averageFuturScore[0] - myCurrentScore);
            }

            //Compare which move would be better
            if (bestNextMove+thisMoveScore-averageDifference > bestScore){
                bestScore = bestNextMove+thisMoveScore-averageDifference;
                bestMove = i;
            }
        }

        return bestMove;
   }

    /** You must modify this constructor to return your student number.
     * This is important, because this is what the code that runs the
     * competition uses to associate you with your agent.
     * The constructor should do nothing else. */
    public StudentPlayer() { super("260587675"); }

    /** This is the primary method that you need to implement.
     * The ``board_state`` object contains the current state of the game,
     * which your agent can use to make decisions. See the class
     * bohnenspiel.RandomPlayer
     * for another example agent. */
    public BohnenspielMove chooseMove(BohnenspielBoardState board_state)
    {     

        BohnenspielMove move;
        Random rand = new Random();       
        final int howFarAhead = 0;
        ArrayList<BohnenspielMove> moves = board_state.getLegalMoves();

        if (board_state.getTurnNumber() == 0){
            move = moves.get(rand.nextInt(moves.size()));
        }else{
            int moveToPlay = lookAhead(board_state, howFarAhead);
            move = moves.get(moveToPlay);
        }

        return move;
    }
}
