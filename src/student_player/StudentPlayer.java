package student_player;

import java.util.ArrayList;    

import bohnenspiel.BohnenspielBoardState;
import bohnenspiel.BohnenspielMove;
import bohnenspiel.BohnenspielPlayer;
import bohnenspiel.BohnenspielMove.MoveType;
import student_player.mytools.MyTools;

/** A Hus player submitted by a student. */
public class StudentPlayer extends BohnenspielPlayer {

    public static int[] bestScore(BohnenspielBoardState stateToCheck){

        int score;
        int[] best= {-1, 0}; // [0] = bestScore, [1] = bestMove

        ArrayList<BohnenspielMove> moves = stateToCheck.getLegalMoves();

          for (int i=0; i<moves.size(); i++){

            BohnenspielBoardState cloned_board_state = (BohnenspielBoardState) stateToCheck.clone();
            cloned_board_state.move(moves.get(i));
            score=cloned_board_state.getScore(stateToCheck.getTurnPlayer());
            if (score>best[0]){

                best[0] = score;
                best[1] = i;

            }
        }

        if (best[0] == 0) best[0] = 1;
        return best;
    }


    public static int greedyOp(BohnenspielBoardState stateToCheck){

        double ratio_myScore_opBestScore = 0;
        int bestMove = 0;
        int score;

        int[] opBestScore={1,0};

        ArrayList<BohnenspielMove> movesToCheck = stateToCheck.getLegalMoves();
        for (int i=0; i<movesToCheck.size(); i++){

            BohnenspielBoardState stateCopy = (BohnenspielBoardState) stateToCheck.clone();
            BohnenspielMove thisMove = movesToCheck.get(i);
            stateCopy.move(thisMove);

            opBestScore = bestScore(stateCopy);

            score=stateCopy.getScore(stateCopy.getTurnPlayer());
            if (ratio_myScore_opBestScore < score/opBestScore[0]){
                bestMove = i;
                ratio_myScore_opBestScore = score/opBestScore[0];
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
     * The ``board_state`` object contains theo current state of the game,
     * which your agent can use to make decisions. See the class
bohnenspiel.RandomPlayer
     * for another example agent. */
    public BohnenspielMove chooseMove(BohnenspielBoardState board_state)
    {

        ArrayList<BohnenspielMove> moves = board_state.getLegalMoves();
        int lol = greedyOp(board_state);
        System.out.println(lol);
        BohnenspielMove move = moves.get(lol);

        // But since this is a placeholder algorithm, we won't act on that information.
        return move;
    }
}