package com.example.project3_jsingh40;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity2 extends AppCompatActivity {

    boolean checkBoard = false;
    String getnextPlayer;
    String message;
    String nextTurn = "p1";
    private static final int cardOneID = R.id.card1;
    private static final int cardTwoID = R.id.card2;
    private static final int cardThreeID = R.id.card3;
    private static final int cardFourID = R.id.card4;
    private static final int cardFiveID = R.id.card5;
    private static final int cardSixID = R.id.card6;
    private static final int cardOneID1 = R.id.doublecard1;
    private static final int cardOneID2 = R.id.doublecard2;
    private static final int cardOneID3 = R.id.doublecard3;
    private static final int cardOneID4 = R.id.doublecard4;
    private static final int cardOneID5 = R.id.doublecard5;
    private static final int cardOneID6 = R.id.doublecard6;

    int[] getCardID = {
            cardOneID, cardOneID1,
            cardTwoID, cardOneID2,
            cardThreeID, cardOneID3,
            cardFourID, cardOneID4,
            cardFiveID, cardOneID5,
            cardSixID, cardOneID6
    };

    int coverCard = R.drawable.ic_launcher_background;

    int[] faces = {
            R.drawable.pic1, R.drawable.pic1,
            R.drawable.pic2, R.drawable.pic2,
            R.drawable.pic3, R.drawable.pic3,
            R.drawable.pic4, R.drawable.pic4,
            R.drawable.pic5, R.drawable.pic5,
            R.drawable.pic6, R.drawable.pic6
    };

    int count = faces.length;
    int length = getCardID.length;
    int FACECOUNT = count / 2;
    ImageButton[] cards = new ImageButton[length];
    int[] getcardLength = new int[length];

    TextView turnPlayer;
    TextView score;
    String gameId;
    String playerId;
    DatabaseReference dref;
    int player1Match = 0;
    int player2Match = 0;
    int player1Miss = 0;
    int player2Miss = 0;
    private static final String FIREBASE_URL = "https://matchinggame-b675a-default-rtdb.firebaseio.com/";
    private static final String KEY_FIRST = "curr1";
    private static final String KEY_SECOND = "curr2";


    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_main2);
        turnPlayer = findViewById(R.id.turnPlayer);
        gameId = getIntent().getStringExtra("getId");
        playerId = getIntent().getStringExtra("playerId");
        score = findViewById(R.id.score);
        getCards();
        FirebaseDatabase fd = FirebaseDatabase.getInstance(FIREBASE_URL);
        dref = fd.getReference("matchinggames").child(gameId);
        if (getPlayer() && faces != null && count > 0) {
            List<Integer> list = new ArrayList<>();
            int i = 0;
            int length = count;
            while (i < length) {
                list.add(faces[i]);
                i++;
            }
            Collections.shuffle(list);
            dref.child("gameboard").setValue(list);
            dref.child("playerturn").setValue("p1");
        }

        valListener();

    }




    private void valListener() {
        ValueEventListener vl = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot datasnapshot) {
                if (datasnapshot == null || !datasnapshot.exists()) {
                    return;
                }
                if (datasnapshot.getChildrenCount() == length) {
                    int i = 0;
                    Iterator<DataSnapshot> x = datasnapshot.getChildren().iterator();
                    while (x.hasNext()) {
                        DataSnapshot c = x.next();
                        getcardLength[i] = c.getValue(Integer.class);
                        i++;
                    }
                    checkBoard = true;
                    printScore();
                    dref.child("gameboard").removeEventListener(this);
                    DatabaseReference temp = dref.child("gameboard");
                    temp.removeEventListener(this);
                    if (turnListener != null) {
                        turnListener();
                    }
                    if (flipListener != null) {
                        flipListener();
                    }
                    if (resultListener != null) {
                        resultListener();
                    }
                } else {
                    turnPlayer.setText("The other player must join.");
                    return;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Failed to read data: " + error.getMessage());
            }
        };
        dref.child("gameboard").addValueEventListener(vl);
    }

    private boolean getPlayer() {
        return "p1".equals(playerId);
    }

    private void getCards() {
        int i;
        i = 0;
        if (getCardID == null || cards == null)
            return;
        int length = getCardID.length;
        if (cards.length != length)
            return;
        while (i < length) {
            final int temp = i;
            ImageButton ib = findViewById(getCardID[i]);
            if (ib == null)
                continue;
            cards[i] = findViewById(getCardID[i]);
            cards[i].setImageResource(coverCard);
            cards[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkBoard == false) {
                        return;
                    }
                    if (dref == null) {
                        return;
                    }
                    DatabaseReference flipsRef = dref.child("cardflips");
                    Task<DataSnapshot> flipsTask = flipsRef.get();
                    flipsTask.addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(DataSnapshot datasnapshot) {
                            if (datasnapshot == null || datasnapshot.hasChild("secondflip")) {
                                return;
                            }

                            playerChecker(temp);
                        }
                    });

                }
            });
            i++;
        }
    }


    private void playerChecker(final int index) {
        if (dref == null) {
            return;
        }
        DatabaseReference getTurn = dref.child("playerturn");
        if (getTurn == null) {
            return;
        }
        Task<DataSnapshot> turn = getTurn.get();
        turn.addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot datasnapshot) {
                long curr = datasnapshot.getChildrenCount();
                if (!datasnapshot.exists()) {
                    return;
                }
                String temp = datasnapshot.getValue(String.class);
                if (temp != null && temp.equals(playerId)) {
                    DatabaseReference getflips = dref.child("cardflips");
                    DatabaseReference firstflipref = getflips.child("firstflip");
                    firstflipref.get()
                            .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(Task<DataSnapshot> t) {
                                    if (t.getException() != null) {
                                        return;
                                    }
                                    DataSnapshot flipSnap = t.getResult();
                                    confirmFlips(flipSnap, index);
                                }
                            });
                }
            }
        });
    }


    //helper method
    private void confirmFlips(DataSnapshot datasnapshot, int i) {
        DatabaseReference flipsRef = dref.child("cardflips");
        if (!datasnapshot.exists()) {
            DatabaseReference firstFlipRef = flipsRef.child("firstflip");
            firstFlipRef.setValue(i);
        } else {
            int temp = datasnapshot.getValue(Integer.class);
            if (temp != i) {
                flipsRef.child("secondflip").setValue(i);
            }

        }
    }


    private final ValueEventListener flipListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot datasnapshot) {
            DataSnapshot firstFlipSnap = datasnapshot.child("firstflip");
            Integer temp1 = firstFlipSnap.getValue(Integer.class);
            DataSnapshot secondFlipSnap = datasnapshot.child("secondflip");
            Integer temp2 = secondFlipSnap.getValue(Integer.class);
            flipHelper(datasnapshot);

            if (temp1 != null) {
                if (temp2 == null) {
                    DatabaseReference flip = dref.child("playerturn");
                    Task<DataSnapshot> flipTask = flip.get();
                    flipTask.addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(DataSnapshot ds) {
                            String flipVal = ds.getValue(String.class);
                            nextTurn = flipVal;

                        }
                    });
                }
            }
            if (processResults(temp1, temp2)) {
                implement(temp1, temp2);
            }
        }

        @Override
        public void onCancelled(DatabaseError error) {
            System.err.println("Failed to read data: " + error.getMessage());
        }
    };

    private boolean processResults(Integer firstFlip, Integer secondFlip) {
        if (playerId == null) {
            return false;
        }
        return "p1".equals(playerId)
                && firstFlip != null
                && secondFlip != null;
    }


    private void flipHelper(DataSnapshot ds) {
        int length = cards.length;
        Integer first = ds.child("firstflip").getValue(Integer.class);
        Integer second = ds.child("secondflip").getValue(Integer.class);
        if (first != null) {
            if (first >= 0) {
                if (first < length) {
                    cards[first].setImageResource(getcardLength[first]);
                }
            }
        }
        if (second!= null) {
            if (second >= 0)
                if (second < length) {
                    cards[second].setImageResource(getcardLength[second]);
                }
        }
    }

    private void flipListener() {
        DatabaseReference flipsList = dref.child("cardflips");
        flipsList.addValueEventListener(flipListener);
    }



    private final ValueEventListener resultListener = new ValueEventListener() {
        @Override
        public void onDataChange( DataSnapshot datasnapshot) {
            int curr1;
            int curr2;
            if (!datasnapshot.exists())
                return;
            DataSnapshot matchds = datasnapshot.child("match");
            Boolean b = matchds.getValue(Boolean.class);
            boolean match = (b != null && b);
            curr1 = getSnapInt(datasnapshot, "curr1", 0);
            curr2 = getSnapInt(datasnapshot, "curr2", 0);
            decideCardTurn(match, curr1, curr2);
            checkScores(datasnapshot);
            printScore(); // print the match and misses
            if ("p1".equals(playerId)) {
                dref.child("cardflips").setValue(null);
                dref.child("output").setValue(null);
                dref.child("playerturn").setValue(datasnapshot.child("next").getValue(String.class));
            }
            checkMessage();

        }
        public void onCancelled(DatabaseError error) {
            System.err.println("Failed to read data: " + error.getMessage());
        }
    };


    private void checkMessage() {
        if (player1Match + player2Match == FACECOUNT) {
            String message;
            //player 1 has won
            if (player1Match > player2Match) {
                if (playerId.equals("p1")) {
                    message = "You won! \n Would you like to play again?";
                } else {
                    message = "You lost.\nWould you like to play again?";
                }
            }
            //player 2 has won
            else if (player2Match > player1Match) {
                if (playerId.equals("player2")) {
                    message = "You won! \n Would you like to play again?";
                } else {
                    message = "You lost.\nWould you like to play again?";
                }
            }
            else {
                //it is a tie
                message = "It’s a tie! \n Would you like to play again?";
            }

            new AlertDialog.Builder(MainActivity2.this)
                    .setTitle("Game Over")
                    .setMessage(message)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                                        recreate();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            finish();
                        }
                    })
                    .show();
        }

    }

    private int getSnapInt(DataSnapshot datasnapshot, String str, int temp) {
        DataSnapshot snap = datasnapshot.child(str);
        Integer integer = snap.getValue(Integer.class);
        if (integer != null) {
            return integer;
        } else {
            return temp;
        }
    }
    private void checkScores(DataSnapshot ds) {
        if (ds == null) {
            return;
        }
        if (!ds.exists()) {
            return;
        }

        if (ds.hasChild("p1match")) {
            player1Match = getSnapInt(ds, "p1match", player1Match);
        }
        if (ds.hasChild("p2match")) {
            player2Match = getSnapInt(ds, "p2match", player2Match);
        }
        if (ds.hasChild("p1miss")) {
            player1Miss  = getSnapInt(ds, "p1miss",  player1Miss);
        }
        if (ds.hasChild("p2miss")) {
            player2Miss  = getSnapInt(ds, "p2miss",  player2Miss);
        }
    }



    private void decideCardTurn(boolean match, int curr1, int curr2) {
        ImageButton card1 = cards[curr1];
        ImageButton card2 = cards[curr2];
        if (card1 == null || card2 == null) {
            return;
        }
        if (match==false) { //mismatch so flip the cards over
            card1.setImageResource(coverCard);
            card2.setImageResource(coverCard);
        } else { // else get rid of them
            card1.setVisibility(View.INVISIBLE);
            card2.setVisibility(View.INVISIBLE);
        }
    }

    private void resultListener() {
        DatabaseReference resultRef = dref.child("output");
        resultRef.addValueEventListener(resultListener);
    }

    private void turnListener() {
        dref.child("playerturn").addValueEventListener(turnListener);
    }


    private void implement(int card1, int card2) {
        int num = 700;
        getResult(card1, card2);
        Map<String, Object> temp = new HashMap<>();
        temp.put("curr1", card1);
        temp.put("curr2", card2);
        temp.put("match", getcardLength[card1] == getcardLength[card2]);
        boolean comp = "p1".equals(nextTurn);
        if (!comp) {
            getnextPlayer = "p1";
        } else {
            getnextPlayer = "player2";
        }
        temp.put("next", getnextPlayer);
        temp.put("p1match", player1Match);
        temp.put("p2match", player2Match);
        temp.put("p1miss", player1Miss);
        temp.put("p2miss", player2Miss);
        handlerHelper(temp, num);


    }

    private void handlerHelper(final Map<String, Object> data, long time) {
        if (dref == null)
            return;
        if (data == null || data.isEmpty())
            return;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                DatabaseReference dbref = dref.child("output");
                dbref.setValue(data);
            }
        }, time);
    }

    //helper method
    private void getResult(int card1, int card2) {
        int length = getcardLength.length;
        if (getcardLength == null
                || card1 < 0 || card2 < 0
                || card1 >= length
                || card2 >= length) {
            return;
        }
        output(card1, card2);
    }

    private void output(int card1, int card2) {
        if (getcardLength == null) {
            return;
        }
        int len = getcardLength.length;
        if (card1 < 0 || card2 < 0 || card1 >= len || card2 >= len) {
            return;
        }
        switch (nextTurn) {
            case "p1":
                if (getcardLength[card1] != getcardLength[card2]) {
                    player1Miss = player1Miss + 1;
                } else {
                    player1Match = player1Match + 1;
                }
                break;
            case "player2":
                if (getcardLength[card1] != getcardLength[card2]) {
                    player2Miss = player2Miss + 1;
                } else {
                    player2Match = player2Match + 1;
                }
                break;
            default:
                break;
        }
    }



    private final ValueEventListener turnListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot datasnapshot) {
            if (datasnapshot == null || !datasnapshot.exists()) {
                turnPlayer.setText("");
                return;
            }
            String turn = datasnapshot.getValue(String.class);
            if (turn == null) {
                turnPlayer.setText("");
                return;
            }
            boolean temp = playerId.equals(datasnapshot.getValue(String.class));
            if (!temp) {
                turnPlayer.setText("It is the other player's turn. Please wait for them to finish.");
            } else {
                turnPlayer.setText("It is your turn. Choose two cards to flip over.");
            }
        }

        @Override
        public void onCancelled(DatabaseError error) {
            System.err.println("Failed to read data: " + error.getMessage());
        }
    };

    private boolean playerOne() {
        if ("p1".equals(playerId)) {
            return true;
        }
        else {
            return false;
        }
    }

    private void printScore() {
        int x;
        int y;
        boolean temp =  playerOne();
        if (temp) {
            x = player1Match;
            y = player1Miss;
        } else {
            x = player2Match;
            y= player2Miss;
        }
        score.setText("Num of Matches: " + x + "   Num of Misses: " + y);
    }

}